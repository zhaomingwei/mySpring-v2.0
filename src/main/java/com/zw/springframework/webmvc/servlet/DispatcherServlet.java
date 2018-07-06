package com.zw.springframework.webmvc.servlet;

import com.zw.springframework.annotation.Controller;
import com.zw.springframework.annotation.RequestMapping;
import com.zw.springframework.annotation.RequestParam;
import com.zw.springframework.context.ClassPathXmlApplicationContext;
import com.zw.springframework.webmvc.HandlerAdapter;
import com.zw.springframework.webmvc.HandlerMapping;
import com.zw.springframework.webmvc.ModelAndView;
import com.zw.springframework.webmvc.ViewResolvers;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatcherServlet extends HttpServlet {
    
    private final String DEFAULT_CONFIG_LOCATION = "contextConfigLocation";

    private final String TEMPLATE_ROOT = "templateRoot";

    private List<HandlerMapping> handlerMappings = new ArrayList<HandlerMapping>();

    private Map<HandlerMapping, HandlerAdapter> handlerAdapterMap = new HashMap();

    private List<ViewResolvers> viewResolvers = new ArrayList();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        }catch (Exception e){
            resp.getWriter().write("<font size='25' color='blue'>500 Exception</font><br/>Details:<br/>" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]","")
                    .replaceAll("\\s","\r\n") +  "<font color='green'><i>Copyright@GupaoEDU</i></font>");
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        HandlerMapping handler = getHandler(req);
        if(handler == null){
            resp.getWriter().write("<font size='25' color='red'>404 Not Found</font><br/><font color='green'><i>Copyright@GupaoEDU</i></font>");
            return;
        }

        HandlerAdapter ha = getHandlerAdapter(handler);


        //这一步只是调用方法，得到返回值
        ModelAndView mv = ha.handler(req, resp, handler);


        //这一步才是真的输出
        processDispatchResult(resp, mv);

    }

    private void processDispatchResult(HttpServletResponse resp, ModelAndView mv) throws Exception {
        if(null==mv){
            return;
        }
        if(this.viewResolvers.isEmpty()){
            return;
        }
        for(ViewResolvers vr:this.viewResolvers){
            if(!mv.getViewName().equals(vr.getViewName())){
                continue;
            }
            String out = vr.viewResolver(mv);
            if(null!=out){
                resp.getWriter().write(out);
                break;
            }
        }
    }

    private HandlerAdapter getHandlerAdapter(HandlerMapping handler) {
        if(this.handlerAdapterMap.isEmpty()){return  null;}
        return this.handlerAdapterMap.get(handler);
    }

    private HandlerMapping getHandler(HttpServletRequest req) {
        if(this.handlerMappings.isEmpty()){ return  null;}
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        for(HandlerMapping handler : this.handlerMappings){
            Matcher matcher = handler.getPattern().matcher(url);
            if(!matcher.matches()){
                continue;
            }
            return handler;
        }
        return null;
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
                this.handlerMappings.add(new HandlerMapping(obj, method, pattern));
            }
        }
    }

    private void initHandlerAdapters(ClassPathXmlApplicationContext context) {
        for(HandlerMapping handlerMapping:this.handlerMappings){

            //每一个方法有一个参数列表，这里保存的是形参列表
            Map<String, Integer> paramMapping = new HashMap<String, Integer>();

            //只处理命名参数
            Annotation[][] ann = handlerMapping.getMethod().getParameterAnnotations();
            for(int i=0;i<ann.length;i++){
                for(Annotation an : ann[i]){
                    if(an instanceof RequestParam){
                        String paramName = ((RequestParam) an).value().trim();
                        if(!"".equals(paramName)){
                            paramMapping.put(paramName, i);
                        }
                    }
                }
            }

            //处理非命名参数
            //只处理request、response
            Class<?>[] paramTypes = handlerMapping.getMethod().getParameterTypes();
            for(int i = 0; i < paramTypes.length; i++){
                if(paramTypes[i] == HttpServletRequest.class
                        || paramTypes[i] == HttpServletResponse.class){
                    paramMapping.put(paramTypes[i].getName(), i);
                }
            }
            this.handlerAdapterMap.put(handlerMapping, new HandlerAdapter(paramMapping));
        }
    }

    private void initViewResolvers(ClassPathXmlApplicationContext context) {
        String templateRoot = context.getConfig().getProperty(TEMPLATE_ROOT);
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        for(File template:templateRootDir.listFiles()){
            this.viewResolvers.add(new ViewResolvers(template.getName(), template));
        }
    }




    private void initFlashMapManager(ClassPathXmlApplicationContext context) {}

    private void initRequestToViewNameTranslator(ClassPathXmlApplicationContext context) {}

    private void initHandlerExceptionResolvers(ClassPathXmlApplicationContext context) {}

    private void initThemeResolver(ClassPathXmlApplicationContext context) {}

    private void initLocaleResolver(ClassPathXmlApplicationContext context) {}

    private void initMultipartResolver(ClassPathXmlApplicationContext context) {}


}
