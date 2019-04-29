package com.honghe.entrance.common.util;

import com.honghe.entrance.servicemanager.ServiceAnnotation;
import com.honghe.entrance.servicemanager.ServiceManager;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description 获取bean
 * @Author sunchao
 * @Date: 2017-12-22 9:49
 * @Mender:
 */
@Component
public class SpringUtil {

    private static ApplicationContext applicationContext = null;
    static org.slf4j.Logger logger = LoggerFactory.getLogger(ServiceManager.class);

    public static void setApplicationContext(ApplicationContext applicationContext) {
        if(SpringUtil.applicationContext == null){
            SpringUtil.applicationContext  = applicationContext;
        }
    }

    //获取applicationContext
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    //通过name获取 Bean.
    public static Object getBean(String name){
        return getApplicationContext().getBean(name);

    }

    //通过class获取Bean.
    public static <T> T getBean(Class<T> clazz){
        return getApplicationContext().getBean(clazz);
    }

    //通过name,以及Clazz返回指定的Bean
    public static <T> T getBean(String name,Class<T> clazz){
        return getApplicationContext().getBean(name, clazz);
    }

    public static void scannerBeans(){
        //扫描服务注解
        String[] serviceNames = applicationContext.getBeanNamesForAnnotation(ServiceAnnotation.class);
        for(String string:serviceNames){
            Object service = getBean(string);
            String serviceName = service.getClass().getAnnotation(ServiceAnnotation.class).ServiceName();
            ServiceManager.getInstance().register(serviceName,service);
        }
    }

    /**
     * 获取请求的ip地址
     * 获取用户真实IP地址，不使用request.getRemoteAddr();的原因是有可能用户使用了代理软件方式避免真实IP地址,
     *
     * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，究竟哪个才是真正的用户端的真实IP呢？
     * 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。
     *
     * 如：X-Forwarded-For：192.168.1.110

     , 192.168.1.120

     , 192.168.1.130

     ,
     * 192.168.1.100

     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}