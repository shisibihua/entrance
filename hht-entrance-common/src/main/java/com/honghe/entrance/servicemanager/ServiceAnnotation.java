package com.honghe.entrance.servicemanager;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 服务注解
 *
 * @auther yuk
 * @Time 2018/2/9 9:35
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ServiceAnnotation {
    String ServiceName();
}
