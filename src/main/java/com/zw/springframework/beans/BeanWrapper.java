package com.zw.springframework.beans;

public class BeanWrapper {

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
        this.wrapperInstance = instance;
        this.originalInstance = instance;
    }
}
