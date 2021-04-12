package com.raylei.entity;

import com.raylei.annotation.Autowired;
import com.raylei.annotation.Component;
import com.raylei.annotation.Qualifier;
import com.raylei.annotation.Value;
import lombok.Data;

@Data
@Component("stu")
public class Student {
    @Value("2021100101")
    private Integer sid;
    @Value("张三")
    private String name;
    @Value("男")
    private String sex;
    @Autowired
    @Qualifier("cls")
    private Class clazz;
}
