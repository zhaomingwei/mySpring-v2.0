package com.zw.springframework.annotation;

import java.lang.annotation.*;

/**
 * Created by ZhaoWei on 2018/7/1/0001.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller {

    String value() default "";

}
