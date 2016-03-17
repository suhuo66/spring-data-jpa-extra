# spring-data-jpa-extra
## use spring data jpa more comfortable
I love spring-data-jpa, she let my hands free, crud methods are boring! However she is not perfect on dynamic native query and her return type must be an entity, though she pr

ovid us a specification solution, but i think it's heavy.

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

2. second create Sample.xml in your classpath (you can change this path by setting placeholder <font color="#008B8B">spring.jpa.template-location</font>)
<pre>
    <code>
        <?xml version="1.0" encoding="utf-8" ?>
        <sqls xmlns="http://www.slyak.com/schema/templatequery"
               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xsi:schemaLocation="http://www.slyak.com/schema/templatequery http://www.slyak.com/schema/templatequery.xsd">
            <sql name="findByContent">
                <![CDATA[
                  SELECT * FROM t_sample WHERE 1=1
                  <#if content??>
                    AND content LIKE :content
                  </#if>
                ]]>
            </sql>
            <sql name="findSampleDTO">
                <![CDATA[
                  SELECT id,name as viewName FROM t_sample WHERE id=:id
                ]]>
            </sql>
        </sqls>
    </code>
</pre>

## All features
### template query

### template query object

### object assemblers

### more useful methods (eg: mget togglestatus fakedelete)

## Use it with maven
<pre>
    <code>
        <dependency>
            <groupId>com.slyak</groupId>
            <artifactId>spring-data-jpa-extra</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
    </code>
</pre>
