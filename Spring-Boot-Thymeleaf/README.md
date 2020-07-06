## Spring Boot中使用thymeleaf

Spring Boot支持FreeMarker、Groovy、Thymeleaf和Mustache四种模板解析引擎，官方推荐使用Thymeleaf。
### 1.spring-boot-starter-thymeleaf

在Spring Boot中使用Thymeleaf只需在pom中加入Thymeleaf的starter即可：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```


在Spring Boot 1.5.9.RELEASE版本中，默认的Thymeleaf版本为2.1.6.RELEASE版本，这里推荐使用3.0以上版本。在pom中将Thymeleaf的版本修改为3.0.2.RELEASE：

```xml
<properties>
    <thymeleaf.version>3.0.2.RELEASE</thymeleaf.version>
    <thymeleaf-layout-dialect.version>2.0.1</thymeleaf-layout-dialect.version>
</properties>
```

在Spring Boot中，默认的html页面地址为src/main/resources/templates，默认的静态资源地址为src/main/resources/static。

### 2.Thymeleaf默认配置
```xml
#开启模板缓存（默认值：true）
spring.thymeleaf.cache=true 
#Check that the template exists before rendering it.
spring.thymeleaf.check-template=true 
#检查模板位置是否正确（默认值:true）
spring.thymeleaf.check-template-location=true
#Content-Type的值（默认值：text/html）
spring.thymeleaf.content-type=text/html
#开启MVC Thymeleaf视图解析（默认值：true）
spring.thymeleaf.enabled=true
#模板编码
spring.thymeleaf.encoding=UTF-8
#要被排除在解析之外的视图名称列表，用逗号分隔
spring.thymeleaf.excluded-view-names=
#要运用于模板之上的模板模式。另见StandardTemplate-ModeHandlers(默认值：HTML5)
spring.thymeleaf.mode=HTML5
#在构建URL时添加到视图名称前的前缀（默认值：classpath:/templates/）
spring.thymeleaf.prefix=classpath:/templates/
#在构建URL时添加到视图名称后的后缀（默认值：.html）
spring.thymeleaf.suffix=.html
#Thymeleaf模板解析器在解析器链中的顺序。默认情况下，它排第一位。顺序从1开始，只有在定义了额外的TemplateResolver Bean时才需要设置这个属性。
spring.thymeleaf.template-resolver-order=
#可解析的视图名称列表，用逗号分隔
spring.thymeleaf.view-names=
```

一般开发中将spring.thymeleaf.cache设置为false，其他保持默认值即可。


### 3.简单示例
编写一个简单的Controller：
```java
@Controller
public class IndexController {
	
    @RequestMapping("/account")
    public String index(Model m) {
        List<Account> list = new ArrayList<Account>();
        list.add(new Account("KangKang", "康康", "e10adc3949ba59abbe56e", "超级管理员", "17777777777"));
        list.add(new Account("Mike", "麦克", "e10adc3949ba59abbe56e", "管理员", "13444444444"));
        list.add(new Account("Jane","简","e10adc3949ba59abbe56e","运维人员","18666666666"));
        list.add(new Account("Maria", "玛利亚", "e10adc3949ba59abbe56e", "清算人员", "19999999999"));
        m.addAttribute("accountList",list);
        return "account";
    }
}
```


编写account.html页面：

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>account</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" th:href="@{/css/style.css}" type="text/css">
</head>
<body>
    <table>
        <tr>
            <th>no</th>
            <th>account</th>
            <th>name</th>
            <th>password</th>
            <th>accountType</th>
            <th>tel</th>
        </tr>
        <tr th:each="list,stat : ${accountList}">
            <td th:text="${stat.count}"></td>
            <td th:text="${list.account}"></td>
            <td th:text="${list.name}"></td>
            <td th:text="${list.password}"></td>
            <td th:text="${list.accountType}"></td>
            <td th:text="${list.tel}"></td>
        </tr>
    </table>
</body>
</html>
```

启动项目，访问 http://localhost:8080/web/account
