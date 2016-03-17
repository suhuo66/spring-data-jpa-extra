# spring-data-jpa-extra
## use spring data jpa more comfortable
I love spring-data-jpa, she let my hands free, crud methods are boring! However she is not perfect on dynamic native query and her return type must be an entity, although she provide us a specification solution, but i think it's heavy and not easy to use.

<b>spring-data-jpa-extra comes to solve three problem:</b>
1. dynamic native query support like mybatis
2. return type can be anything
3. no code, just sql

## Example
1. first extends GenericJpaRepository insteadof JpaRepository
<pre>
    <code>
        public interface SampleRepository extends GenericJpaRepository<Sample, Long> {
            @TemplateQuery
            Page<Sample> findByContent(String content, Pageable pageable);
            @TemplateQuery
            SampleDTO findSampleDTO(Long id);
        }
    </code>
</pre>

2. second create a file named Sample.xml in your classpath:/sqls/ (you can change this path by setting placeholder <font color="#008B8B">spring.jpa.template-location</font>)
<pre>
    &lt;?xml version="1.0" encoding="utf-8" ?&gt;;
    &lt;sqls xmlns="http://www.slyak.com/schema/templatequery" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.slyak.com/schema/templatequery http://www.slyak.com/schema/templatequery.xsd"&gt
        &lt;sql name="findByContent"&gt;
            &lt;![CDATA[
              SELECT * FROM t_sample WHERE 1=1
              &lt;#if content??&gt;
                AND content LIKE :content
              &lt;/#if&gt;
            ]]&gt;
        &lt;/sql&gt;
        &lt;sql name="findSampleDTO"&gt;
            &lt;![CDATA[
              SELECT id,name as viewName FROM t_sample WHERE id=:id
            ]]&gt;
        &lt;/sql&gt;
    &lt;/sqls&gt;
</pre>

## All features
### template query

### template query object

### object assemblers

### more useful methods (eg: mget togglestatus fakedelete)

## Use it with maven
<pre>
    &lt;dependency&gt;
        &lt;groupId&gt;com.slyak&lt;/groupId&gt;
        &lt;artifactId&gt;spring-data-jpa-extra&lt;/artifactId&gt;
        &lt;version&gt;1.0.0-SNAPSHOT&lt;/version&gt;
    &lt;/dependency&gt;
</pre>
