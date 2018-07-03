package com.zw.springframework.context;

import com.zw.springframework.beans.BeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultListableBeanFactory extends AbstractApplicationContext {

    //beanDefinitionMap用来保存配置信息
    protected Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>();

    protected void onRefresh(){
    }

    protected void refreshBeanFactory() {
    }
}
