package com.zw.springframework.webmvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;

public class HandlerAdapter {

    private Map<String, Integer> paramMapping;

    public HandlerAdapter(Map<String, Integer> paramMapping){
        this.paramMapping = paramMapping;
    }


    public ModelAndView handler(HttpServletRequest req, HttpServletResponse resp, HandlerMapping handler) throws Exception{
        //把用户请求的参数信息与method中的参数信息进行动态匹配
        //resp传入只是为了将其赋值给方法参数
        //当

        //1、要准备好这个方法的形参列表
        //方法重载：形参的决定因素：参数个数、参数类型、参数顺序、方法名字
        Class<?>[] paramTypes = handler.getMethod().getParameterTypes();

        //2、拿到自定义命名参数所在位置
        //用户通过URL传过来的参数列表
        Map<String, String[]> reqParameterMap = req.getParameterMap();

        //3、构造实参列表
        Object[] paramValues = new Object[paramTypes.length];
        for(Map.Entry<String, String[]> param : reqParameterMap.entrySet()){
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]","").replaceAll("\\s","");
            if(!this.paramMapping.containsKey(param.getKey())){continue;}

            int index = this.paramMapping.get(param.getKey());

            //因为页面上传过来的值都是String类型的，而在方法中定义的类型是千变万化的
            //要针对我们传过来的参数进行类型转换
            paramValues[index] = caseStringValue(value,paramTypes[index]);
        }

        if(this.paramMapping.containsKey(HttpServletRequest.class.getName())) {
            int reqIndex = this.paramMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = req;
        }

        if(this.paramMapping.containsKey(HttpServletResponse.class.getName())) {
            int respIndex = this.paramMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = resp;
        }

        //4、从handler中取出controller、method，然后利用反射机制进行调用

        Object result = handler.getMethod().invoke(handler.getController(),paramValues);

        if(result == null){ return  null; }

        boolean isModelAndView = handler.getMethod().getReturnType() == ModelAndView.class;
        if(isModelAndView){
            return (ModelAndView)result;
        }else{
            return null;
        }
    }

    private Object caseStringValue(String value,Class<?> clazz){
        if(clazz == String.class){
            return value;
        }else if(clazz == Integer.class){
            return  Integer.valueOf(value);
        }else if(clazz == int.class){
            return Integer.valueOf(value).intValue();
        }else {
            return null;
        }
    }
}
