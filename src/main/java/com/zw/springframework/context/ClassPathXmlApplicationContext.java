package com.zw.springframework.context;

import com.zw.springframework.annotation.Autowired;
import com.zw.springframework.annotation.Controller;
import com.zw.springframework.annotation.Service;
import com.zw.springframework.beans.BeanDefinition;
import com.zw.springframework.beans.BeanWrapper;
import com.zw.springframework.context.support.BeanDefinitionReader;
import com.zw.springframework.core.BeanFactory;
import com.zw.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassPathXmlApplicationContext extends AbstractApplicationContext implements BeanFactory {

    //加载的资源文件位置地址数组
    private String[] configLocations;

    //存储BeanDefinition
    private BeanDefinitionReader reader;

    //存储所有被代理过的对象
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap();

    //用来保证注册式单例的容器
    private Map<String, Object> beanCacheMap = new HashMap<String, Object>();

    //用来存储所有被代理过的对象
    private Map<String, BeanWrapper> beanWrapperMap = new ConcurrentHashMap<String, BeanWrapper>();

    //单个文件构造函数，把单个文件放在数组里在调用参数为一个数组的构造函数
    public ClassPathXmlApplicationContext(String location){
        this(new String[]{location});
    }

    public ClassPathXmlApplicationContext(String ... locations){
        setConfigLocations(locations);
        refresh();
    }

    private void refresh() {
        //定位
        this.reader = new BeanDefinitionReader(configLocations);

        //加载
        List<String> beanDefinitionList = this.reader.loadBeanDefinitions();

        //注册
        doRegister(beanDefinitionList);

        //依赖注入（lazy-init = false），要是执行依赖注入
        //在这里自动调用getBean方法
        doAutowired();



    }

    private void doAutowired() {

        for(Map.Entry<String, BeanDefinition> beanDefinitionEntry:this.beanDefinitionMap.entrySet()){
            if(!beanDefinitionEntry.getValue().isLazyInit()){
                String beanName = beanDefinitionEntry.getKey();
                Object obj = getBean(beanName);
            }
        }

        for(Map.Entry<String, BeanWrapper> beanWrapperEntry:beanWrapperMap.entrySet()){
            populateBean(beanWrapperEntry.getKey(), beanWrapperEntry.getValue().getOriginalInstance());
        }

    }

    //开始真正的注入
    private void populateBean(String key, Object originalInstance) {
        //根据传入的对象获取它的class
        Class clazz = originalInstance.getClass();
        //如果这个class没有Controller、Service注解则返回
        if(!clazz.isAnnotationPresent(Controller.class)
                || !clazz.isAnnotationPresent(Service.class)){
            return;
        }
        //获取这个类所有的字段(不管访问权限)
        Field[] fields = clazz.getDeclaredFields();
        if(fields.length==0){
            return;
        }
        //遍历所有字段
        for(Field field:fields){
            //不是Autowired的注解则进行下一个
            if(!field.isAnnotationPresent(Autowired.class)){
                return;
            }
            //获取Autowired注解信息
            Autowired autowired = field.getAnnotation(Autowired.class);
            //获取注解想要注入的类型的名称
            String autowiredName = autowired.value().trim();
            if("".endsWith(autowiredName)){
                //如果注解想要注入的类型的名称为空则获取定义该字段的类型名称
                autowiredName = field.getType().getName();
            }
            //强制设置所有字段可访问,包括private等
            field.setAccessible(true);
            try {
                //开始注入，给当前有注解的字段赋值
                //从beanWrapperMap中拿出key为autowiredName的BeanWrapper包装类
                //赋值给该字段
                field.set(originalInstance, this.beanWrapperMap.get(autowiredName).getWrapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    //真正的将BeanDefinitions注册到beanDefinitionMap中
    private void doRegister(List<String> beanDefinitionList){
        //beanName有三种情况:
        //1、默认用类名首字母小写
        //2、自定义名字
        //3、接口注入
        for(String className : beanDefinitionList){
            Class<?> beanClass = null;
            try {
                beanClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            //如果是接口则不能实例化用实现类来注入
            if(beanClass == null || beanClass.isInterface()){
                continue;
            }

            BeanDefinition beanDefinition = reader.registerBean(className);

            if(null!=beanDefinition){
                this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
            }

            Class<?>[] interfaces = beanClass.getInterfaces();
            if(null!=interfaces && interfaces.length > 0){
                for(Class<?> clazz : interfaces){
                    //如果是多个实现类则会覆盖，需要自定义名称
                    this.beanDefinitionMap.put(clazz.getName(), beanDefinition);
                }
            }
            //容器初始化完毕
        }
    }

    /**
     * 解析Bean定义资源文件的路径，处理多个资源文件字符串数组
     * @param locations
     */
    private void setConfigLocations(String ... locations) {
        if(null != locations){
            Assert.noNullElements(locations, "Config locations must not be null");
            this.configLocations = new String[locations.length];
            for(int i = 0; i < locations.length; i++){
                // 以下是spring源码：
                //this.configLocations[i] = resolvePath(locations[i]).trim();
                //resolvePath为同一个类中将字符串解析为路径的方法
                this.configLocations[i] = locations[i].trim();
            }
        }else{
            this.configLocations = null;
        }
    }


    protected void refreshBeanFactory() {

    }

    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        String className = beanDefinition.getBeanClassName();
        try{
            Object instance = instantionBean(beanDefinition);
            if(null==instance){
                return null;
            }
            BeanWrapper beanWrapper = new BeanWrapper(instance);
            beanWrapperMap.put(className, beanWrapper);
            //返回的这个WrapperInstance是我们通过动态代理后的对象
            return beanWrapperMap.get(className).getWrapperInstance();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private Object instantionBean(BeanDefinition beanDefinition) {
        Object instance = null;
        String className = beanDefinition.getBeanClassName();
        try{
            if(beanCacheMap.containsKey(className)){
                instance = beanCacheMap.get(className);
            }else{
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();
                beanCacheMap.put(className, instance);
            }
            return instance;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
