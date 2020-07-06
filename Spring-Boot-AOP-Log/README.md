## Spring Boot AOP记录用户操作日志

### 1.自定义注解

定义一个方法级别的@Log注解，用于标注需要监控的方法：

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
	String value() default "";
}
```

### 2.保存日志的方法
为了方便，这里直接使用Mybatis 注解方式来操作数据库。定义一个SysLogDao接口，包含一个保存操作日志的抽象方法：

```java
@Component
@Mapper
public interface SysLogDao {
	@Insert("insert into sys_log(id,username,operation,time,method,params,ip,createtime) values(#{id},#{username},#{operation},#{time},#{method},#{params},#{ip},#{createtime})")
	void saveSysLog(SysLog syslog);
}
```

### 3.切面和切点
定义一个LogAspect类，使用@Aspect标注让其成为一个切面，切点为使用@Log注解标注的方法，使用@Around环绕通知：
```java
@Aspect
@Component
public class LogAspect {

	@Autowired
	private SysLogDao sysLogDao;

	@Pointcut("@annotation(com.springboot.annotation.Log)")
	public void pointcut() {
	}

	@Around("pointcut()")
	public void around(ProceedingJoinPoint point) {
		long beginTime = System.currentTimeMillis();
		try {
			// 执行方法
			point.proceed();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		// 执行时长(毫秒)
		long time = System.currentTimeMillis() - beginTime;
		// 保存日志
		saveLog(point, time);
	}

	private void saveLog(ProceedingJoinPoint joinPoint, long time) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		SysLog sysLog = new SysLog();
		Log logAnnotation = method.getAnnotation(Log.class);
		if (logAnnotation != null) {
			// 注解上的描述
			sysLog.setOperation(logAnnotation.value());
		}
		// 请求的方法名
		String className = joinPoint.getTarget().getClass().getName();
		String methodName = signature.getName();
		sysLog.setMethod(className + "." + methodName + "()");
		// 请求的方法参数值
		Object[] args = joinPoint.getArgs();
		// 请求的方法参数名称
		LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
		String[] paramNames = u.getParameterNames(method);
		StringBuffer params = new StringBuffer();
		if (args != null && paramNames != null) {
			for (int i = 0; i < args.length; i++) {
				params.append("  " + paramNames[i] + ": " + args[i]);
			}
			sysLog.setParams(params.toString());
		}
		// 获取request
		HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
		sysLog.setId(UUID.randomUUID().toString().replace("-","").toUpperCase());
		// 设置IP地址
		sysLog.setIp(IPUtils.getIpAddr(request));
		// 模拟一个用户名
		sysLog.setUsername("lyouths");
		sysLog.setTime((int) time);
		Date date = new Date();
		sysLog.setCreatetime(date);
		// 保存系统日志
		sysLogDao.saveSysLog(sysLog);
	}
}
```


### 4.测试
TestController：

```java
@RestController
public class TestController {

	@Log("执行方法一")
	@GetMapping("/one")
	public void methodOne(String name) {
		
	}

	@Log("执行方法二")
	@GetMapping("/two")
	public void methodTwo() throws InterruptedException {
		Thread.sleep(2000);
	}

	@Log("执行方法三")
	@GetMapping("/three")
	public void methodThree(String name, String age) {
		
	}
}
```

启动项目，分别访问：


* http://localhost:8080/web/one?name=KangKang

* http://localhost:8080/web/two

* http://localhost:8080/web/three?name=Mike&age=25