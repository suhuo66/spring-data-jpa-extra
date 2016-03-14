package com.slyak.spring.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * .
 * <p>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 2015/8/7
 */
@NoRepositoryBean
public interface GenericJpaRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    Map<ID, T> mget(Collection<ID> ids);

    //for cache
    Map<ID, T> mgetOneByOne(Collection<ID> ids);

    //for cache
    List<T> findAllOneByOne(Collection<ID> ids);

    <C> Page<C> query(String nativeQuery, Object beanOrMap, Class<C> clazz, Pageable pageable);

    <C> List<C> list(String nativeQuery, Object beanOrMap, Class<C> clazz, int size);

    int count(String nativeQuery, Object beanOrMap);

    void toggleStatus(ID id);

    void fakeDelete(ID... id);
}
