package com.honghe.entrance.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.File;
import java.util.Properties;

/**
 * 读取服务静态资源
 * @author caoqian
 * @date 20190301
 */
@CrossOrigin
@Configuration
public class FileServerConfig  extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /**
         * 系统属性
         * windows与linux环境下访问静态资源路径不同，此处做了判断，分别进行处理
         */
        Properties props=System.getProperties();
        String sysName=props.getProperty("os.name").toLowerCase();
        if(sysName.contains("windows")) {
            registry.addResourceHandler("/resources/**").addResourceLocations("file:" + getResourcesPath() + "resources/");
            registry.addResourceHandler("/image/**").addResourceLocations("file:" + getResourcesPath() + "image/");
        }else if(sysName.contains("linux")){
            registry.addResourceHandler("/resources/**").addResourceLocations("file:/resources/");
            registry.addResourceHandler("/image/**").addResourceLocations("file:/image/");
        }else{
            registry.addResourceHandler("/resources/**").addResourceLocations("file:/resources/");
            registry.addResourceHandler("/image/**").addResourceLocations("file:/image/");
        }
        super.addResourceHandlers(registry);
    }
    private String getResourcesPath(){
        String path=System.getProperty("user.dir")+ File.separator;
//        System.out.println(">>>>>>>>>>>>读取文件地址："+path);
        return path;
    }
}
