package com.raylei.annotation;

import java.lang.annotation.*;

// 标注在类上定义的注解
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
    String value() default "";
}
