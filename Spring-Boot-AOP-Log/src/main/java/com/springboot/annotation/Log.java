package com.springboot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)  //应用于方法
@Retention(RetentionPolicy.RUNTIME)  //由JVM 加载，包含在类文件中，在运行时可以被获取到
public @interface Log {
	String value() default "";   //注解值为String类型，默认为""
}
