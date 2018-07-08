package com.zw.springframework.aop;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

public class AopProxyUtils {

    private static boolean isAopProxy(Object obj){
        return Proxy.isProxyClass(obj.getClass());
    }

    public static Object getTargetObject(Object proxy) throws Exception {
        //如果不是代理对象则直接返回
        if(!isAopProxy(proxy)){
            return proxy;
        }
        return getProxyTargetObject(proxy);
    }

    private static Object getProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        AopProxy aopProxy = (AopProxy) h.get(proxy);
        Field target = aopProxy.getClass().getDeclaredField("target");
        target.setAccessible(true);
        return target.get(aopProxy);
    }

}
