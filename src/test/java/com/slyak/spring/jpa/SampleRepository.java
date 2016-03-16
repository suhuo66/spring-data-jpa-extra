package com.slyak.spring.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * .
 * <p>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 16/3/15.
 */
public interface SampleRepository extends GenericJpaRepository<Sample, Long> {

    @TemplateQuery
    Page<Sample> findByContent(String content, Pageable pageable);

    @TemplateQuery
    List<Sample> findByTemplateQueryObject(SampleQuery sampleQuery);

    @TemplateQuery
    long countContent(String content);

    @TemplateQuery
    List<SampleDTO> findDtos();
}
