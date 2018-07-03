package com.zw.springframework.context;

import com.zw.springframework.beans.BeanDefinition;
import com.zw.springframework.context.support.BeanDefinitionReader;
import com.zw.springframework.core.BeanFactory;
import com.zw.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassPathXmlApplicationContext extends AbstractApplicationContext implements BeanFactory {

    //加载的资源文件位置地址数组
    private String[] configLocations;

    //存储BeanDefinition
    private BeanDefinitionReader reader;

    //存储所有被代理过的对象
    private Map beanDefinitionMap = new ConcurrentHashMap();

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

    public Object getBean(String baneName) {
        return null;
    }
}
