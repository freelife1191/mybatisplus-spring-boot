package com.baomidou.springboot.excel.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 스프링 컨테이너에있는 Bean에 대한 정적 액세스를위한 스프링 도구
 * @author lisuo
 *
 */
public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext ctx;

    /**
     * bean 가져 오기
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String id){
        return (T) ctx.getBean(id);
    }

    /**
     * bean 유형별로보기
     * @param clazz
     * @return
     */
    public static <T> T getBean(Class<T> clazz){
        return ctx.getBean(clazz);
    }

    /**
     * 유형 및 ID로 bean가져 오기
     * @param id
     * @param clazz
     * @return
     */
    public static <T> T getBean(String id, Class<T> clazz){
        return ctx.getBean(id, clazz);
    }

    /**
     *
     * SpringUtil이 초기화를 완료했는지 확인하십시오.
     * @param
     * @return boolean
     * @throws
     */
    public static boolean isInited(){
        return null!=ctx;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }
}
