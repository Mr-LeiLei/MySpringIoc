package com.raylei.annotation;

import lombok.AllArgsConstructor;
import lombok.Data;

// 用于包装注解类的id和class
@Data
@AllArgsConstructor
public class BeanDefinition {
    private String beanName;
    private Class beanClass;
}
