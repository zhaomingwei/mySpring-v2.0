package com.zw.springframework.annotation;

import java.lang.annotation.*;

/**
 * Created by ZhaoWei on 2018/7/1/0001.
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {

    String value() default "";

}
