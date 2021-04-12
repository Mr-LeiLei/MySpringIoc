package com.raylei.entity;

import com.raylei.annotation.Component;
import com.raylei.annotation.Value;
import lombok.Data;

@Data
@Component("cls")
public class Class {
    @Value("1001")
    private Integer cid;
    @Value("精英班")
    private String className;
}
