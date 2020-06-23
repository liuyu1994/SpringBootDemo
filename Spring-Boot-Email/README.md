## 1.概述
在项目的维护过程中，我们通常会在应用中加入短信或者邮件预警功能，比如当应用出现异常宕机时应该及时地将预警信息发送给运维或者开发人员，本文将介绍如何在Spring Boot中发送邮件。在Spring Boot中发送邮件使用的是Spring提供的`org.springframework.mail.javamail.JavaMailSender`，其提供了许多简单易用的方法，可发送简单的邮件、HTML格式的邮件、带附件的邮件，并且可以创建邮件模板。

## 2.引入依赖
在Spring Boot中发送邮件，需要用到spring-boot-starter-mail，引入spring-boot-starter-mail：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```


## 3.邮件配置

在application.yml中进行简单的配置（以163邮件为例）：
```yml
server:
  port: 80

spring:
  mail:
    host: smtp.163.com
    username: 你的账号
    password: 你的密码
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
```

spring.mail.username(邮箱账号)，spring.mail.password(授权码，非密码)

## 4.发送简单的邮件
编写EmailController，注入JavaMailSender:

