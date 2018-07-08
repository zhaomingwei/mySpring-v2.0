package com.zw.springframework.beans;

import com.zw.springframework.aop.AopConfig;
import com.zw.springframework.aop.AopProxy;

public class BeanWrapper {

    AopProxy aopProxy = new AopProxy();

    //包装原始对象后的对象
    private Object wrapperInstance;

    //保存原始对象
    private Object originalInstance;

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public Object getOriginalInstance() {
        return originalInstance;
    }

    public BeanWrapper(Object instance) {
        //这里可以用动态代理来包装，目前还没包装
        this.wrapperInstance = aopProxy.getProxy(instance);
        this.originalInstance = instance;
    }

    public void setAopConfig(AopConfig config){
        aopProxy.setAopConfig(config);
    }
}
