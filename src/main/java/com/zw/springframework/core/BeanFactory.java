package com.zw.springframework.core;

public interface BeanFactory {

    //根据名称获取对象
    Object getBean(String baneName);

}
