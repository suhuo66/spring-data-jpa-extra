# spring-data-jpa-extra
## use spring data jpa more comfortable
I love spring-data-jpa, she set my hands free, crud methods are boring! However she is not perfect on dynamic native query and her return type must be an entity, although she provide us a specification solution, but i think it's heavy and not easy to use.

<b>spring-data-jpa-extra comes to solve three problem:</b>

- dynamic native query support like mybatis
- return type can be anything
- no code, just sql

## Example
- first extends GenericJpaRepository insteadof JpaRepository

```java
	public interface SampleRepository extends GenericJpaRepository<Sample, Long> {
		@TemplateQuery
		Page<Sample> findByContent(String content, Pageable pageable);
		@TemplateQuery
		CustomVO findCustomVO(Long id);
	}
```

- second create a file named Sample.xml in your classpath:/sqls/ (you can change this path by setting placeholder <font color="#008B8B">spring.jpa.template-location</font>)

```xml
    <?xml version="1.0" encoding="utf-8" ?>
    <sqls xmlns="http://www.slyak.com/schema/templatequery" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.slyak.com/schema/templatequery http://www.slyak.com/schema/templatequery.xsd">
        <sql name="findByContent">
            <![CDATA[
              SELECT * FROM t_sample WHERE 1=1
              <#if content??>
                AND content LIKE :content
              </#if>
            ]]>
        </sql>
        <sql name="findCustomVO">
            <![CDATA[
              SELECT id,name as viewName FROM t_sample WHERE id=:id
            ]]>
        </sql>
    </sqls>
```

## How to use?

you can use it by using source code or adding a maven dependency (later, I'll put it to maven central repository)

```xml
    <dependency>
        <groupId>com.slyak</groupId>
        <artifactId>spring-data-jpa-extra</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
```

right now you can add repository below to get snapshots
```xml
    <repositories>
        <repository>
            <id>slyak-public</id>
            <name>slyak public</name>
            <url>http://nexus.slyak.com/content/groups/public/</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
```


## 2 Miniute Tutorial

### Template Query
Methods annotated with @TemplateQuery tells QueryLookupStrategy to look up query by template,this is often used by dynamic query.

### Template Query Object
Object annotated with @TemplateQueryObject tells template process engine render params provided by object properties.

### Entity Assemblers
Entity assemblers can assembler entity with other entities, such as one to many relation or one to one relation.


### More Useful Methods (eg: mget togglestatus fakedelete)

```java
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
```


## TODO List
- More types of template support (now freemarker)
- More JPA comparison support (now hibernate)
- Performance test and do some optimization
- More other useful features
