package com.zw.springframework.context;

public abstract class AbstractApplicationContext {

    //提供给子类重写
    protected void onRefresh(){};

    protected abstract void refreshBeanFactory();

}
