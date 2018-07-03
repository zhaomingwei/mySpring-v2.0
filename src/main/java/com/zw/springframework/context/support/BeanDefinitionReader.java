package com.zw.springframework.context.support;

import com.zw.springframework.beans.BeanDefinition;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BeanDefinitionReader {

    private Properties config = new Properties();

    //在配置文件中，用来获取自动扫描的包名的key
    private String SCAN_PACKAGE = "scanPackage";

    //保存所有的class
    private List<String> registerBeanClasses = new ArrayList();

    public BeanDefinitionReader(String ... locations){
        //spring中是通过XmlBeanDefinitionReader读取
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:", ""));
        //此处应该是循环取出多个配置文件,待后面研究
//        for(String location : locations){
//        }
        try {
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null!=is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    //递归扫描所有相关class保存到一个List中
    private void doScanner(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));

        File classDir = new File(url.getFile());

        for(File file : classDir.listFiles() ){
            if(file.isDirectory()){
                doScanner(packageName + "." + file.getName());
            }else{
                //上面由于递归的方法第一句就是替换.
                //这里为何要点来拼接？
                registerBeanClasses.add(packageName + "." + file.getName().replace(".class", ""));
            }
        }
    }

    //返回Bean List
    public List<String> loadBeanDefinitions(){
        return this.registerBeanClasses;
    }

    //每注册一个bean则返回一个BeanDefinition对象
    public BeanDefinition registerBean(String className){
        if(registerBeanClasses.contains(className)){
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setBeanClassName(className);
            beanDefinition.setFactoryBeanName(lowFirstCase(className.substring(className.lastIndexOf(".") + 1)));
            return beanDefinition;
        }
        return null;
    }

    /**
     * 首字母小写转换
     * @param str
     * @return
     */
    private String lowFirstCase(String str){
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

}
