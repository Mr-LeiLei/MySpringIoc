package com.raylei.test;

import com.raylei.annotation.AnnotationConfigApplicationContext;

public class Test {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ioc = new AnnotationConfigApplicationContext("com.raylei.entity");
        System.out.println(ioc.getBean("stu"));
    }
}
