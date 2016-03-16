package com.slyak.spring.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * .
 * <p>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 16/3/15.
 */
@Configuration
@EnableJpaRepositories(repositoryBaseClass = GenericJpaRepositoryImpl.class, repositoryFactoryBeanClass = GenericJpaRepositoryFactoryBean.class)
public class JpaConfig {

    @Autowired(required = false)
    private PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer;

    @Bean
    public PropertySourcesPlaceholderConfigurer cfg() {
        if (propertySourcesPlaceholderConfigurer == null) {
            propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        }
        propertySourcesPlaceholderConfigurer.setIgnoreResourceNotFound(true);
        return propertySourcesPlaceholderConfigurer;
    }
}
