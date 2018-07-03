package com.zw.springframework.annotation;

import java.lang.annotation.*;

/**
 * Created by ZhaoWei on 2018/7/1/0001.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {

    String value() default "";

}
