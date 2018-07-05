package com.zw.springframework.webmvc.servlet;

import com.zw.springframework.annotation.Controller;
import com.zw.springframework.annotation.RequestMapping;
import com.zw.springframework.context.ClassPathXmlApplicationContext;
import com.zw.springframework.webmvc.HandlerMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DispatcherServlet extends HttpServlet {
    
    private String DEFAULT_CONFIG_LOCATION = "contextConfigLocation";

    private List<HandlerMapping> handlerMappings = new ArrayList<HandlerMapping>();
    

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //相当于把IOC容器初始化了
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(config.getInitParameter(DEFAULT_CONFIG_LOCATION));

        initStrategies(ctx);
        
    }

    private void initStrategies(ClassPathXmlApplicationContext context) {
        //有九种策略
        // 针对于每个用户请求，都会经过一些处理的策略之后，最终才能有结果输出
        // 每种策略可以自定义干预，但是最终的结果都是一致
        // ModelAndView

        initMultipartResolver(context);
        initLocaleResolver(context);
        initThemeResolver(context);
        initHandlerMappings(context);
        initHandlerAdapters(context);
        initHandlerExceptionResolvers(context);
        initRequestToViewNameTranslator(context);
        initViewResolvers(context);
        initFlashMapManager(context);
    }


    //此方法是把Controller中配置的RequestMapping和Method对应
    private void initHandlerMappings(ClassPathXmlApplicationContext context) {
        //处理请求的url
        //通常我们理解是一个map容器维护请求的url和对应的方法
        //map.put(url, Method);
        String[] beanNames = context.getBeanDefinitionNames();

        for(String beanName : beanNames){
            Object obj = context.getBean(beanName);
            Class<?> clazz = obj.getClass();
            //如果不是Controller注解则跳过
            if(!clazz.isAnnotationPresent(Controller.class)){
                continue;
            }
            String baseUrl = "";

            //获取RequestMapping配置的url
            if(clazz.isAnnotationPresent(RequestMapping.class)){
                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                baseUrl = requestMapping.value();
            }
            Method[] methods = clazz.getMethods();
            for(Method method:methods){
                if(!method.isAnnotationPresent(RequestMapping.class)){
                    continue;
                }
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                String regex = ("/" + baseUrl + requestMapping.value().replaceAll("\\*", ".*").replaceAll("/+", "/"));
                Pattern pattern = Pattern.compile(regex);
                handlerMappings.add(new HandlerMapping(obj, method, pattern));
            }
        }
    }

    private void initHandlerAdapters(ClassPathXmlApplicationContext context) {










    }

    private void initViewResolvers(ClassPathXmlApplicationContext context) {}




    private void initFlashMapManager(ClassPathXmlApplicationContext context) {}

    private void initRequestToViewNameTranslator(ClassPathXmlApplicationContext context) {}

    private void initHandlerExceptionResolvers(ClassPathXmlApplicationContext context) {}

    private void initThemeResolver(ClassPathXmlApplicationContext context) {}

    private void initLocaleResolver(ClassPathXmlApplicationContext context) {}

    private void initMultipartResolver(ClassPathXmlApplicationContext context) {}


}
