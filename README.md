# spring-data-jpa-extra
## use spring data jpa more comfortable
I love spring-data-jpa, she let my hands free, crud methods are boring! However she is not perfect on dynamic native query and her return type must be an entity, although she provide us a specification solution, but i think it's heavy and not easy to use.

<b>spring-data-jpa-extra comes to solve three problem:</b>

- dynamic native query support like mybatis
- return type can be anything
- no code, just sql

## Example
- first extends GenericJpaRepository insteadof JpaRepository

<pre><code>
	public interface SampleRepository extends GenericJpaRepository<Sample, Long> {
		@TemplateQuery
		Page<Sample> findByContent(String content, Pageable pageable);
		@TemplateQuery
		SampleDTO findSampleDTO(Long id);
		}</code>
</pre>

- second create a file named Sample.xml in your classpath:/sqls/ (you can change this path by setting placeholder <font color="#008B8B">spring.jpa.template-location</font>)

<pre>
    &lt;?xml version="1.0" encoding="utf-8" ?&gt;;
    &lt;sqls xmlns="http://www.slyak.com/schema/templatequery" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.slyak.com/schema/templatequery http://www.slyak.com/schema/templatequery.xsd"&gt;
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

## How to use?

you can use it by using source code or adding a maven dependency (later, I'll put it to maven central repository)

<pre>
    &lt;dependency&gt;
        &lt;groupId&gt;com.slyak&lt;/groupId&gt;
        &lt;artifactId&gt;spring-data-jpa-extra&lt;/artifactId&gt;
        &lt;version&gt;1.0.0-SNAPSHOT&lt;/version&gt;
    &lt;/dependency&gt;
</pre>

## 2 Miniute Tutorial

### Template Query

### Template Query Object

### Entity Assemblers

### More Useful Methods (eg: mget togglestatus fakedelete)

<pre><code>
    //batch get items and put the result into a map
    Map<ID, T> mget(Collection<ID> ids);
    
    //get items one by one for cache
    Map<ID, T> mgetOneByOne(Collection<ID> ids);
    
    //get items one by one for cache
    List<T> findAllOneByOne(Collection<ID> ids);
    
    //toggle entity status if it has a Status property
    void toggleStatus(ID id);
    
    //set entity status to Status.DELETED if it has a Status property
    void fakeDelete(ID... id);
</code></pre>
