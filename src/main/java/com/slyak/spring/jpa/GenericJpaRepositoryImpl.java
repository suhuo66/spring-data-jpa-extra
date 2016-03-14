package com.slyak.spring.jpa;

import org.hibernate.SQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import javax.persistence.EntityManager;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * .
 * <p>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 2015/8/7
 */
public class GenericJpaRepositoryImpl<T, ID extends Serializable>
        extends SimpleJpaRepository<T, ID> implements GenericJpaRepository<T, ID>, Serializable {

    private EntityManager em;

    private JpaEntityInformation<T, ID> eif;

    private boolean isStatusAble = false;

    private Method statusReadMethod;

    private Method statusWriteMethod;

    public GenericJpaRepositoryImpl(JpaEntityInformation<T, ID> eif, EntityManager em) {
        super(eif, em);
        this.em = em;
        this.eif = eif;
        PropertyDescriptor descriptor = findFieldPropertyDescriptor(eif.getJavaType(), Status.class);
        isStatusAble = descriptor != null;
        if (isStatusAble) {
            statusReadMethod = descriptor.getReadMethod();
            statusWriteMethod = descriptor.getWriteMethod();
        }
    }

    @Override
    public Map<ID, T> mget(Collection<ID> ids) {
        return toMap(findAll(ids));
    }

    @Override
    public Map<ID, T> mgetOneByOne(Collection<ID> ids) {
        return toMap(findAllOneByOne(ids));
    }

    @Override
    public List<T> findAllOneByOne(Collection<ID> ids) {
        List<T> results = new ArrayList<T>();
        for (ID id : ids) {
            results.add(findOne(id));
        }
        return results;
    }

    private Map<ID, T> toMap(List<T> list) {
        Map<ID, T> result = new LinkedHashMap<ID, T>();
        for (T t : list) {
            result.put(eif.getId(t), t);
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C> Page<C> query(String nativeQuery, Object beanOrMap, Class<C> clazz, Pageable pageable) {
        Map<String, Object> params = QueryBuilder.toParams(beanOrMap);
        SQLQuery query = QueryBuilder.toSQLQuery(em, nativeQuery, params);
        QueryBuilder.transform(query, clazz);
        query.setFirstResult(pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        return new PageImpl<C>(query.list(), pageable, count(QueryUtils.createCountQueryFor(nativeQuery, null), params));
    }

    @Override
    public int count(String nativeQuery, Object beanOrMap) {
        SQLQuery query = QueryBuilder.toSQLQuery(em, nativeQuery, beanOrMap);
        Object result = query.uniqueResult();
        return result == null ? 0 : ((Number) result).intValue();
    }

    @Override
    @Transactional
    public void toggleStatus(ID id) {
        if (isStatusAble && id != null) {
            T target = findOne(id);
            if (target != null) {
                Status status = (Status) ReflectionUtils.invokeMethod(statusReadMethod, target);
                if (status == Status.ENABLED || status == Status.DISABLED) {
                    ReflectionUtils.invokeMethod(statusWriteMethod, target, status == Status.DISABLED ? Status.ENABLED : Status.DISABLED);
                    save(target);
                }
            }
        }
    }

    @Override
    @Transactional
    public void fakeDelete(ID... ids) {
        for (ID id : ids) {
            changeStatus(id, Status.DELETED);
        }
    }

    private void changeStatus(ID id, Status status) {
        if (isStatusAble && id != null) {
            T target = findOne(id);
            if (target != null) {
                Status oldStatus = (Status) ReflectionUtils.invokeMethod(statusReadMethod, target);
                if (oldStatus != status) {
                    ReflectionUtils.invokeMethod(statusWriteMethod, target, status);
                    save(target);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C> List<C> list(String nativeQuery, Object beanOrMap, Class<C> clazz, int size) {
        return QueryBuilder.transform(QueryBuilder.toSQLQuery(em, nativeQuery, beanOrMap), clazz).setFetchSize(size).list();
    }

    private PropertyDescriptor findFieldPropertyDescriptor(Class target, Class fieldClass) {
        PropertyDescriptor[] propertyDescriptors = org.springframework.beans.BeanUtils.getPropertyDescriptors(target);
        for (PropertyDescriptor pd : propertyDescriptors) {
            if (pd.getPropertyType() == fieldClass) {
                return pd;
            }
        }
        return null;
    }
}
