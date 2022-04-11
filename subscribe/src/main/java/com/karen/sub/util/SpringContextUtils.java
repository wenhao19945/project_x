package com.karen.sub.util;

import java.lang.reflect.Method;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 对Spring容器进行各种上下文操作的工具类
 * 使用方式：继承这个类并且使用@Component注入
 * @author wenhao
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {

    private static ApplicationContext context;

    /**
     * 获取ApplicationContext
     * @param applicationContext
     * @author WenHao
     * @date 2022/1/4 17:30 
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    /**
     * 反射-动态调用bean的方法
     * @param clazz
     * @param methodName
     * @param args
     * @author WenHao
     * @return java.lang.Object
     */
    public static Object callMethod(Class<?> clazz, String methodName, Object... args){
        try{
            //从spring容器中获取bean
            Object o = getBean(clazz);
            if(null == o){
                throw new RuntimeException("--- Not Bean!");
            }
            Method[] methods = o.getClass().getDeclaredMethods();
            Method method = null;
            //查找匹配的方法，保证参数长度与入参长度，所有参数类型与所有入参类型均一致
            for(Method m : methods){
                if(m.getName().equals(methodName)){
                    if(m.getParameterTypes().length != args.length){
                        continue;
                    }
                    for(int i = 0,len = m.getParameterTypes().length; i < len; i++){
                        if(args[i].getClass() != m.getParameterTypes()[i]){
                            continue;
                        }
                    }
                    method = m;
                }
            }
            if(null == method){
                throw new RuntimeException("--- NoSuchMethod Err!");
            }
            //调用方法
            return method.invoke(o,args);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("--- InvokeMethod Err!");
        }
    }

    /**
     * 根据Bean名称获取Bean对象
     * @param name Bean名称
     * @return 对应名称的Bean对象
     */
    public static Object getBean(String name) {
        return context.getBean(name);
    }

    /**
     * 根据Bean的类型获取对应的Bean
     * @param requiredType Bean类型
     * @return 对应类型的Bean对象
     */
    public static <T> T getBean(Class<T> requiredType) {
        return context.getBean(requiredType);
    }

    /**
     * 根据Bean名称获取指定类型的Bean对象
     * @param name         Bean名称
     * @param requiredType Bean类型（可为空）
     * @return 获取对应Bean名称的指定类型Bean对象
     */
    public static <T> T getBean(String name, Class<T> requiredType) {
        return context.getBean(name, requiredType);
    }

    /**
     * 判断是否包含对应名称的Bean对象
     * @param name Bean名称
     * @return 包含：返回true，否则返回false。
     */
    public static boolean containsBean(String name) {
        return context.containsBean(name);
    }

    /**
     * 获取对应Bean名称的类型
     * @param name Bean名称
     * @return 返回对应的Bean类型
     */
    public static Class<?> getType(String name) {
        return context.getType(name);
    }

    /**
     * 获取上下文对象，可进行各种Spring的上下文操作
     * @return Spring上下文对象
     */
    public static ApplicationContext getContext() {
        return context;
    }

}