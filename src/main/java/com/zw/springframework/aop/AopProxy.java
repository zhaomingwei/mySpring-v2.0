package com.zw.springframework.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

//默认使用JDK动态代理
public class AopProxy implements InvocationHandler {

    private AopConfig aopConfig;

    private Object target;

    //把原生对象传进来
    public Object getProxy(Object instance){
        this.target = instance;
        Class<?> clazz = instance.getClass();
        return Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), this);
    }

    public void setAopConfig(AopConfig aopConfig) {
        this.aopConfig = aopConfig;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method m = this.target.getClass().getMethod(method.getName(),method.getParameterTypes());

        //在原始方法调用以前要执行增强代码
        //需要通过原生方法去找,通过代理方法去Map中是找不到的
        if(aopConfig.contains(m)){
            AopConfig.Aspect aspect = aopConfig.get(m);
            aspect.getPoints()[0].invoke(aspect.getAspect());
        }

        //反射调用原始方法
        Object obj = method.invoke(this.target, args);
        System.out.println(args);

        //在原始方法调用以后执行增强代码
        if(aopConfig.contains(m)){
            AopConfig.Aspect aspect = aopConfig.get(m);
            aspect.getPoints()[1].invoke(aspect.getAspect());
        }

        //将最原始方法返回值返回
        return obj;
    }
}
