## Spring Boot一些基础配置
### 1.定制Banner
Spring Boot项目在启动的时候会有一个默认的启动图案：
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v1.5.9.RELEASE)
```

我们可以把这个图案修改为自己想要的。在`src/main/resources`目录下新建`banner.txt`文件，然后将自己的图案黏贴进去即可。ASCII图案可通过网站 http://www.network-science.de/ascii/  一键生成，比如输入mrbird生成图案后复制到banner.txt，启动项目，IDEA控制台输出如下：
```
  _   _   _   _   _   _  
 / \ / \ / \ / \ / \ / \ 
( m | r | b | i | r | d )
 \_/ \_/ \_/ \_/ \_/ \_/ 
```

banner也可以关闭，在main方法中：
```java
public static void main(String[] args) {
    SpringApplication app = new SpringApplication(DemoApplication.class);
    app.setBannerMode(Mode.OFF);   //关闭banner
    app.run(args);
}
```

### 2.全局默认配置
在src/main/resources目录下，Spring Boot提供了一个名为application.properties的全局配置文件，可对一些默认配置的配置值进行修改。

> 附：[application.properties中可配置所有官方属性](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html)


### 3.默认配置文件获取自定义属性值

Spring Boot允许我们在application.properties下自定义一些属性，比如：

````
liuyu.blog.name=liuyu's blog
liuyu.blog.title=Spring Boot
````
**使用注解@Value映射**

1.映射到实体Bean的方式（需要交spring管理@Component和使用时注入）

定义一个BlogProperties Bean，通过@Value("${属性名}")来加载配置文件中的属性值：

````java
@Component
public class BlogProperties {
	
	@Value("${liuyu.blog.name}")
	private String name;
	
	@Value("${liuyu.blog.title}")
	private String title;
	//get set省略
}
````


编写IndexController，注入该Bean：

````java
@RestController
public class IndexController {
	@Autowired
	private BlogProperties blogProperties;
	
	@RequestMapping("/")
	String index() {
		return testConfigBean.getName()+"，"+testConfigBean.getAge();
	}
}
````
启动项目，访问http://localhost:8080，页面显示如下：
````
liuyu's blog，Spring Boot
````


2.映射到字段的方式（必须是Spring管理的Bean的字段上）

同时也可以不指定一个bean对象，可以直接通过@Value注解将配置文件中的值映射到一个Spring管理的Bean的字段上。这里直接写在controller层中
```java
@RestController
public class IndexController {
	@Value("${liuyu.blog.name}")
	private String name;

	@Value("${liuyu.blog.title}")
	private String title;
	
	@RequestMapping("/")
	String index() {
		return name+"，"+title;
	}
}
```


**使用注解@ConfigurationProperties映射**

1.映射到bean对象的方式

在属性非常多的情况下，也可以定义一个和配置文件对应的Bean：
```java
@ConfigurationProperties(prefix="liuyu.blog")
public class ConfigBean {
    private String name;
    private String title;
    
    //必选实现set方法
    public void setName(String name) {
    		this.name = name;
    	}
    public void setTitle(String title) {
		this.title = title;
	}
}
```

通过注解`@ConfigurationProperties(prefix="liuyu.blog")`通过注解@ConfigurationProperties(prefix=”配置文件中的key的前缀”)可以将配置文件中的配置自动与实体进行映射。

除此之外还需在Spring Boot入口类加上注解@EnableConfigurationProperties({ConfigBean.class})来启用该配置：
```java
@SpringBootApplication
@EnableConfigurationProperties({ConfigBean.class})
public class Application {
	
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

之后便可在IndexController中注入该Bean，并使用了：
```java
@RestController
public class IndexController {
    @Autowired
    private ConfigBean configBean;
    
    @RequestMapping("/")
    String index() {
        return configBean.getName()+"——"+configBean.getTitle();
    }
}
```

2.映射到属性的方式

这里在controller层做示例

```java
@RestController
@ConfigurationProperties(prefix="liuyu.blog")
public class IndexController {
	private String name;
	private String title;
	@RequestMapping("/")
	String index() {
		return name+"，"+title;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
```

> 注意：使用@ConfigurationProperties方式可以进行配置文件与实体字段的自动映射，但需要字段必须提供set方法才可以，而使用@Value注解修饰的字段不需要提供set方法

**属性间的引用**

在application.properties配置文件中，各个属性可以相互引用，如下：
```
liuyu.blog.name=liuyu's blog
liuyu.blog.title=Spring Boot
liuyu.blog.wholeTitle=${liuyu.blog.name}--${liuyu.blog.title}
```
`wholeTitle`引用`liuyu.blog`的**name**和**title**

### 4.自定义配置文件获取自定义属性值

除了可以在application.properties里配置属性，我们还可以自定义一个配置文件。在src/main/resources目录下新建一个test.properties:

```
test.name=KangKang
test.age=25
```

定义一个对应该配置文件的Bean(TestConfigBean)：
```java
@Configuration
@ConfigurationProperties(prefix="test")
@PropertySource("classpath:test.properties")
@Component
public class TestConfigBean {
    private String name;
    private int age;
    // get,set略
}
```

注解`@PropertySource("classpath:test.properties")`指明了使用哪个配置文件。要使用该配置Bean，同样也需要在入口类里使用注解`@EnableConfigurationProperties`({TestConfigBean.class})来启用该配置。


### 通过命令行设置属性值
在运行Spring Boot jar文件时，可以使用命令java -jar xxx.jar --server.port=8081来改变端口的值。这条命令等价于我们手动到application.properties中修改（如果没有这条属性的话就添加）server.port属性的值为8081。

如果不想项目的配置被命令行修改，可以在入口文件的main方法中进行如下设置：
```java
public static void main(String[] args) {
    SpringApplication app = new SpringApplication(Application.class);
    app.setAddCommandLineProperties(false);
    app.run(args);
}
```
注意：使用@ConfigurationProperties方式可以进行配置文件与实体字段的自动映射，但需要字段必须提供set方法才可以，而使用@Value注解修饰的字段不需要提供set方法


### 使用xml配置
虽然Spring Boot并不推荐我们继续使用xml配置，但如果出现不得不使用xml配置的情况，Spring Boot允许我们在入口类里通过注解@ImportResource({"classpath:some-application.xml"})来引入xml配置文件。


### Profile配置
Profile用来针对不同的环境下使用不同的配置文件，多环境配置文件必须以application-{profile}.properties的格式命，其中{profile}为环境标识。比如定义两个配置文件：

* application-dev.properties：开发环境
```
server.port=8080
```

* application-prod.properties：生产环境
```
server.port=8081
```

至于哪个具体的配置文件会被加载，需要在application.properties文件中通过spring.profiles.active属性来设置，其值对应{profile}值。

如：spring.profiles.active=dev就会加载application-dev.properties配置文件内容。可以在运行jar文件的时候使用命令java -jar xxx.jar --spring.profiles.active={profile}切换不同的环境配置。