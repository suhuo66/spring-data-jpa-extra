package com.slyak.spring.jpa;

import com.slyak.util.AopTargetUtils;
import org.hibernate.SQLQuery;
import org.hibernate.jpa.internal.QueryImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.AbstractJpaQuery;
import org.springframework.data.jpa.repository.query.JpaParameters;
import org.springframework.data.jpa.repository.query.JpaQueryMethod;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * .
 * <p/>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 2015/8/9.
 */
public class FreemarkerTemplateQuery extends AbstractJpaQuery {

    private boolean useJpaSpec = false;

    /**
     * Creates a new {@link AbstractJpaQuery} from the given {@link JpaQueryMethod}.
     *
     * @param method
     * @param em
     */
    public FreemarkerTemplateQuery(JpaQueryMethod method, EntityManager em) {
        super(method, em);
    }

    @Override
    protected Query doCreateQuery(Object[] values) {
        String nativeQuery = getQuery(values);
        JpaParameters parameters = getQueryMethod().getParameters();
        ParameterAccessor accessor = new ParametersParameterAccessor(parameters, values);
        String sortedQueryString = QueryUtils
                .applySorting(nativeQuery, accessor.getSort(), QueryUtils.detectAlias(nativeQuery));
        Query query = bind(createJpaQuery(sortedQueryString), values);
        if (parameters.hasPageableParameter()) {
            Pageable pageable = (Pageable) (values[parameters.getPageableIndex()]);
            if (pageable != null) {
                query.setFirstResult(pageable.getOffset());
                query.setMaxResults(pageable.getPageSize());
            }
        }
        return query;
    }

    private String getQuery(Object[] values) {
        return getQueryFromTpl(values);
    }

    private String getQueryFromTpl(Object[] values) {
        return ContextHolder.getBean(FreemarkerSqlTemplates.class)
                .process(getEntityName(), getMethodName(), getParams(values));
    }

    private Map<String, Object> getParams(Object[] values) {
        JpaParameters parameters = getQueryMethod().getParameters();
        //gen model
        Map<String, Object> params = new HashMap<String, Object>();
        for (int i = 0; i < parameters.getNumberOfParameters(); i++) {
            Object value = values[i];
            Parameter parameter = parameters.getParameter(i);
            if (value != null && canBindParameter(parameter)) {
                if (!QueryBuilder.isValidValue(value)) {
                    continue;
                }
                Class<?> clz = value.getClass();
                if (clz.isPrimitive() || String.class.isAssignableFrom(clz) || Number.class.isAssignableFrom(clz)
                        || clz.isArray() || Collection.class.isAssignableFrom(clz) || clz.isEnum()) {
                    params.put(parameter.getName(), value);
                } else {
                    params = QueryBuilder.toParams(value);
                }
            }
        }
        return params;
    }

    public QueryImpl createJpaQuery(String queryString) {
        Class<?> objectType = getQueryMethod().getReturnedObjectType();

        //must be hibernate QueryImpl
        QueryImpl query;

        if (useJpaSpec && getQueryMethod().isQueryForEntity()) {
            query = AopTargetUtils.getTarget(getEntityManager().createNativeQuery(queryString, objectType));
        } else {
            query = AopTargetUtils.getTarget(getEntityManager().createNativeQuery(queryString));
            //find generic type
            ClassTypeInformation<?> ctif = ClassTypeInformation.from(objectType);
            TypeInformation<?> actualType = ctif.getActualType();
            Class<?> genericType = actualType.getType();

            if (genericType != null && genericType != Void.class) {
                QueryBuilder.transform(query.getHibernateQuery(), genericType);
            }
        }
        return query;
    }

    private String getEntityName() {
        return getQueryMethod().getEntityInformation().getJavaType().getSimpleName();
    }

    private String getMethodName() {
        return getQueryMethod().getName();
    }

    @Override
    protected TypedQuery<Long> doCreateCountQuery(Object[] values) {
        QueryImpl nativeQuery = AopTargetUtils
                .getTarget(getEntityManager().createNativeQuery(QueryBuilder.toCountQuery(getQuery(values))));
        return bind(nativeQuery, values);
    }

    public QueryImpl bind(QueryImpl query, Object[] values) {
        SQLQuery sqlQuery = (SQLQuery) query.getHibernateQuery();
        Map<String, Object> params = getParams(values);
        if (!CollectionUtils.isEmpty(params)) {
            QueryBuilder.setParams(sqlQuery, params);
        }
        return query;
    }

    protected boolean canBindParameter(Parameter parameter) {
        return parameter.isBindable();
    }
}
