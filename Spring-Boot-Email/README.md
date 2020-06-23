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
```java
@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private JavaMailSender jms;
    
    @Value("${spring.mail.username}")
    private String from;
    
    @RequestMapping("sendSimpleEmail")
    public String sendSimpleEmail() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo("888888@qq.com"); // 接收地址
            message.setSubject("一封简单的邮件"); // 标题
            message.setText("使用Spring Boot发送简单邮件。"); // 内容
            jms.send(message);
            return "发送成功";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
```

启动项目访问 http://localhost/email/sendSimpleEmail ，提示发送成功。

## 5.发送HTML格式的邮件
改造EmailController，SimpleMailMessage替换为MimeMessage：
```java
@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private JavaMailSender jms;
    
    @Value("${spring.mail.username}")
    private String from;
    
    @RequestMapping("sendHtmlEmail")
    public String sendHtmlEmail() {
        MimeMessage message = null;
        try {
            message = jms.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from); 
            helper.setTo("888888@qq.com"); // 接收地址
            helper.setSubject("一封HTML格式的邮件"); // 标题
            // 带HTML格式的内容
            StringBuffer sb = new StringBuffer("<p style='color:#6db33f'>使用Spring Boot发送HTML格式邮件。</p>");
            helper.setText(sb.toString(), true);
            jms.send(message);
            return "发送成功";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
```

`helper.setText(sb.toString(), true)`;中的`true`表示发送HTML格式邮件。启动项目，访问 http://localhost/email/sendHtmlEmail ，提示发送成功，可看到文本已经加上了颜色#6db33f。


## 6.发送带附件的邮件

发送带附件的邮件和普通邮件相比，其实就只是多了个传入附件的过程。不过使用的仍是MimeMessage：

```java
@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private JavaMailSender jms;
    
    @Value("${spring.mail.username}")
    private String from;
	
    @RequestMapping("sendAttachmentsMail")
    public String sendAttachmentsMail() {
        MimeMessage message = null;
        try {
            message = jms.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from); 
            helper.setTo("888888@qq.com"); // 接收地址
            helper.setSubject("一封带附件的邮件"); // 标题
            helper.setText("详情参见附件内容！"); // 内容
            // 传入附件
            FileSystemResource file = new FileSystemResource(new File("src/main/resources/static/file/项目文档.docx"));
            helper.addAttachment("项目文档.docx", file);
            jms.send(message);
            return "发送成功";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
```

启动项目访问 http://localhost/email/sendAttachmentsMail ，提示发送成功。


## 6.发送带静态资源的邮件

发送带静态资源的邮件其实就是在发送HTML邮件的基础上嵌入静态资源（比如图片），嵌入静态资源的过程和传入附件类似，唯一的区别在于需要标识资源的cid：

```java
@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private JavaMailSender jms;
    
    @Value("${spring.mail.username}")
    private String from;
	
    @RequestMapping("sendInlineMail")
    public String sendInlineMail() {
        MimeMessage message = null;
        try {
            message = jms.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from); 
            helper.setTo("888888@qq.com"); // 接收地址
            helper.setSubject("一封带静态资源的邮件"); // 标题
            helper.setText("<html><body>博客图：<img src='cid:img'/></body></html>", true); // 内容
            // 传入附件
            FileSystemResource file = new FileSystemResource(new File("src/main/resources/static/img/sunshine.png"));
            helper.addInline("img", file); 
            jms.send(message);
            return "发送成功";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
```


`helper.addInline("img", file);`中的img和图片标签里cid后的名称相对应。启动项目访问 http://localhost/email/sendInlineMail ，提示发送成功。

## 7.使用模板发送邮件
在发送验证码等情况下可以创建一个邮件的模板，唯一的变量为验证码。这个例子中使用的模板解析引擎为Thymeleaf，所以首先引入Thymeleaf依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

在template目录下创建一个emailTemplate.html模板：
```html
<!DOCTYPE html>
<html lang="zh" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8" />
    <title>模板</title>
</head>
<body>
    您好，您的验证码为<span th:text="${code}"></span>，请在两分钟内使用完成操作。
</body>
</html>
```


发送模板邮件，本质上还是发送HTML邮件，只不过多了绑定变量的过程，详细如下所示：

```java
@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private JavaMailSender jms;
    
    @Value("${spring.mail.username}")
    private String from;
    
    @Autowired
    private TemplateEngine templateEngine;
	
    @RequestMapping("sendTemplateEmail")
    public String sendTemplateEmail(String code) {
        MimeMessage message = null;
        try {
            message = jms.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from); 
            helper.setTo("888888@qq.com"); // 接收地址
            helper.setSubject("邮件摸板测试"); // 标题
            // 处理邮件模板
            Context context = new Context();
            context.setVariable("code", code);
            String template = templateEngine.process("emailTemplate", context);
            helper.setText(template, true);
            jms.send(message);
            return "发送成功";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
```
其中code对应模板里的${code}变量。启动项目，访问http://localhost/email/sendTemplateEmail?code=EOS9，页面提示发送成功。




