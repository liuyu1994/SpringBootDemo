## Spring Boot中使用MyBatis

### 1.Druid数据源
Druid是一个关系型数据库连接池，是阿里巴巴的一个开源项目，地址： https://github.com/alibaba/druid。Druid  不但提供连接池的功能，还提供监控功能，可以实时查看数据库连接池和SQL查询的工作情况。
#### 配置Druid依赖
Druid为Spring Boot项目提供了对应的starter：
```xml
<dependency>
   <groupId>com.alibaba</groupId>
   <artifactId>druid-spring-boot-starter</artifactId>
   <version>1.1.6</version>
</dependency>
```

#### Druid数据源配置（MySQL）
上面通过查看mybatis starter的隐性依赖发现，Spring Boot的数据源配置的默认类型是org.apache.tomcat.jdbc.pool.Datasource，为了使用Druid连接池，需要在application.yml下配置：
```xml
server:
  context-path: /web

spring:
  datasource:
    druid:
      # 数据库访问配置, 使用druid数据源
      url: jdbc:mysql://localhost:3306/test?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false
      username: root
      password: root
      type: com.alibaba.druid.pool.DruidDataSource
      driver-class-name: com.mysql.jdbc.Driver
      # 初始化大小，最小，最大
      initialSize: 5
      minIdle: 5
      maxActive: 20
      # 配置获取连接等待超时的时间
      maxWait: 60000
      # 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
      timeBetweenEvictionRunsMillis: 60000
      # 配置一个连接在池中最小生存的时间，单位是毫秒
      minEvictableIdleTimeMillis: 300000
      #是否在连接放回连接池后检测其可用性
      testOnReturn: false
      #是否在获得连接后检测其可用性
      testOnBorrow: false
      #是否在连接空闲一段时间后检测其可用性
      testWhileIdle: true
      # 打开PSCache，并且指定每个连接上PSCache的大小
      poolPreparedStatements: true
      maxPoolPreparedStatementPerConnectionSize: 20
      validationQuery: SELECT 1 FROM DUAL

logging:  
  level:
    com.springboot.mapper: debug   #配置sql输出
mybatis:   #配置包扫描，针对xml方式
  mapper-locations: classpath:mappers/*.xml
  type-aliases-package: com.springboot.bean
```

#### Druid数据源配置（Oracle）
```xml
server:
  context-path: /web

spring:
  datasource:
    druid:
      # 数据库访问配置, 使用druid数据源
      type: com.alibaba.druid.pool.DruidDataSource
      driver-class-name: oracle.jdbc.driver.OracleDriver
      url: jdbc:oracle:thin:@localhost:1521:ORCL
      username: scott
      password: 123456
      # 连接池配置
      initial-size: 5
      min-idle: 5
      max-active: 20
      # 连接等待超时时间
      max-wait: 30000
      # 配置检测可以关闭的空闲连接间隔时间
      time-between-eviction-runs-millis: 60000
      # 配置连接在池中的最小生存时间
      min-evictable-idle-time-millis: 300000
      validation-query: select '1' from dual
```

上述配置不但配置了Druid作为连接池，而且还开启了Druid的监控功能。 其他配置可参考官方wiki——https://github.com/alibaba/druid/tree/master/druid-spring-boot-starter

此时，运行项目，访问 http://localhost:8080/web/druid

关于Druid的更多说明，可查看官方 wiki: https://github.com/alibaba/druid/wiki/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98


### 2.使用MyBatis

使用的库表：
```sql
create table students
(
    SNO VARCHAR(10) not null,
    SNAME VARCHAR(9) null,
    SEX CHAR null
);

INSERT INTO students (SNO, SNAME, SEX) VALUES ('1', 'ZHANGSAN', 'M');
INSERT INTO students (SNO, SNAME, SEX) VALUES ('2', 'LISI', 'M');
INSERT INTO students (SNO, SNAME, SEX) VALUES ('3', 'XIAOMING', 'F');
```

创建对应实体：
```java
public class Student implements Serializable{
	
	private static final long serialVersionUID = -339516038496531943L;
	private String sno;
	private String name;
	private String sex;
	//......
}
```
StudentMapper的实现可以基于xml也可以基于注解。

#### 使用注解方式
```java
@Component
@Mapper  //指定mapper，针对于注解用法
public interface StudentMapper {
	@Insert("insert into students(sno,sname,ssex) values(#{sno},#{name},#{sex})")
	int add(Student student);
	
	@Update("update students set sname=#{name},ssex=#{sex} where sno=#{sno}")
    int update(Student student);
	
	@Delete("delete from students where sno=#{sno}")
    int deleteBysno(String sno);
	
	@Select("select * from students where sno=#{sno}")
	@Results(id = "student",value= {
		 @Result(property = "sno", column = "sno", javaType = String.class),
         @Result(property = "name", column = "sname", javaType = String.class),
         @Result(property = "sex", column = "ssex", javaType = String.class)
	})
    Student queryStudentBySno(String sno);
}
```
简单的语句只需要使用@Insert、@Update、@Delete、@Select这4个注解即可，动态SQL语句需要使用@InsertProvider、@UpdateProvider、@DeleteProvider、@SelectProvider等注解。具体可参考MyBatis官方文档：http://www.mybatis.org/mybatis-3/zh/java-api.html。


#### 使用xml方式
使用xml方式需要在application.yml中进行一些额外的配置：

```xml
mybatis:
  mapper-locations: classpath:mappers/*.xml   #xml实现扫描路径
  type-aliases-package: com.springboot.bean   #扫描实体对象路径
```

接口：
```java
@Component
@Mapper
public interface StudentMapper {
	List<Student> queryStudentByXml();
}
```

在resources目录下，创建名为`mappers`的目录，创建`student.xml`
```xml
<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.springboot.mapper.StudentMapper">
    <select id="queryStudentByXml" resultType="com.springboot.bean.Student">
        select * from students
    </select>
</mapper>
```



测试:
```java
@RestController
public class TestController {

	@Autowired
	private StudentService studentService;
	
	@RequestMapping( value = "/querystudent", method = RequestMethod.GET)
	public Student queryStudentBySno(String sno) {
		return this.studentService.queryStudentBySno("1");
	}

        //xml方式
	@RequestMapping( value = "/querystudentByXml", method = RequestMethod.GET)
	public List<Student> queryStudentByxml(String sno) {
		return this.studentService.queryStudentByXml();
	}
}
```