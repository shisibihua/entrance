package com.honghe.entrance;

import com.honghe.entrance.common.util.ProcessId;
import com.honghe.entrance.common.util.SpringUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import javax.servlet.MultipartConfigElement;
import java.util.TimeZone;


/**
 * @author caoqian
 */
@SpringBootApplication(scanBasePackages = "com.honghe.entrance")
@MapperScan("com.honghe.entrance.dao")
@ServletComponentScan
public class EntranceServiceApplication {
    //最大上传单文件大小
    @Value("${spring.http.multipart.maxFileSize}")
    private  String maxFileSize;
    //最大上传多文件大小
    @Value("${spring.http.multipart.maxRequestSize}")
    private  String maxRequestSize;

    public static void main(String[] args) {
        //写入PId 用于shutDown
        ProcessId.setProcessID();
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        ApplicationContext app=SpringApplication.run(EntranceServiceApplication.class, args);
        SpringUtil.setApplicationContext(app);
        SpringUtil.scannerBeans();
        System.out.println("程序启动完成..........");
    }

    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        return corsConfiguration;
    }

    /**
     * 跨域过滤器
     * @return
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildConfig());
        return new CorsFilter(source);
    }

    /**
     * 文件上传配置
     * @return
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //单个文件最大
        factory.setMaxFileSize(maxFileSize); //KB,MB
        /// 设置总上传数据总大小
        factory.setMaxRequestSize(maxRequestSize);
        return factory.createMultipartConfig();
    }
}