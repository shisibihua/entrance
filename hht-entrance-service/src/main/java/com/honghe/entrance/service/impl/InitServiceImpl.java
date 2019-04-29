package com.honghe.entrance.service.impl;

import com.honghe.entrance.dao.ModuleDao;
import com.honghe.entrance.dao.ModuleSysDao;
import com.honghe.entrance.service.InitService;
import jodd.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Configuration
public class InitServiceImpl implements InitService {
    @Value("${entrance.module.userIp}")
    private String userIp;
    @Value("${entrance.module.resIp}")
    private String resIp;
    @Value("${entrance.module.ywIp}")
    private String ywIp;
    @Value("${entrance.module.deviceControlIp}")
    private String deviceControlIp;
    @Value("${entrance.module.patrolIp}")
    private String patrolIp;
    @Autowired
    private ModuleDao moduleDao;
    @Autowired
    private ModuleSysDao moduleSysDao;
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void initModuleData() {
        initUser();
        initResLive();
        initResCourse();
        initResFamousTeacher();
        initResTeaching();
        initResVideo();
        initResPersonal();
        initYunWei();
        initDeviceControl();
        initPatrol();
        initCloudResource();
        replaceThemeUrl(userIp);
    }
    //用户管理
    private boolean initUser(){
        String userPath="http://"+userIp+":7002/index.html#/?logOut=http://"+userIp+":7001";
        return moduleDao.updateUserModule(userPath);
    }

    /**
     * 资源平台
     * @return
     */
    //直播
    private boolean initResLive(){
        String resLivePath="http://"+resIp+":8402/liveClass.html#/?logOut=http://"+userIp+":7001";
        String resLiveColor="http://"+userIp+":7003/resources/live.png";
        String resLiveIcon="http://"+userIp+":7003/resources/live-logo.png";
        Map<String,String> params=new HashMap<>();
        params.put("path",resLivePath);
        params.put("color",resLiveColor);
        params.put("icon",resLiveIcon);
        return moduleDao.updateResLiveModule(params);
    }
    //课件中心
    private boolean initResCourse(){
        String resLivePath="http://"+resIp+":8402/file.html#/?logOut=http://"+userIp+":7001";
        String resLiveColor="http://"+userIp+":7003/resources/resources.png";
        String resLiveIcon="http://"+userIp+":7003/resources/resources-logo.png";
        Map<String,String> params=new HashMap<>();
        params.put("path",resLivePath);
        params.put("color",resLiveColor);
        params.put("icon",resLiveIcon);
        return moduleDao.updateResCourse(params);
    }
    //名师风采
    private boolean initResFamousTeacher(){
        String resLivePath="http://"+resIp+":8402/teacher.html#/?logOut=http://"+userIp+":7001";
        String resLiveColor="http://"+userIp+":7003/resources/famousteacher.png";
        String resLiveIcon="http://"+userIp+":7003/resources/famousteacher-logo.png";
        Map<String,String> params=new HashMap<>();
        params.put("path",resLivePath);
        params.put("color",resLiveColor);
        params.put("icon",resLiveIcon);
        return moduleDao.updateResFamousTeacher(params);
    }
    //教研活动
    private boolean initResTeaching(){
        String resLivePath="http://"+resIp+":8402/activity.html#/?logOut=http://"+userIp+":7001";
        String resLiveColor="http://"+userIp+":7003/resources/jiaoyanhuodong.png";
        String resLiveIcon="http://"+userIp+":7003/resources/jiaoyanhuodong-logo.png";
        Map<String,String> params=new HashMap<>();
        params.put("path",resLivePath);
        params.put("color",resLiveColor);
        params.put("icon",resLiveIcon);
        return moduleDao.updateResTeaching(params);
    }
    //优课视频
    private boolean initResVideo(){
        String resLivePath="http://"+resIp+":8402/cloud.html#/?logOut=http://"+userIp+":7001";
        String resLiveColor="http://"+userIp+":7003/resources/video.png";
        String resLiveIcon="http://"+userIp+":7003/resources/video-logo.png";
        Map<String,String> params=new HashMap<>();
        params.put("path",resLivePath);
        params.put("color",resLiveColor);
        params.put("icon",resLiveIcon);
        return moduleDao.updateResVideo(params);
    }
    //个人中心
    private boolean initResPersonal(){
        String resLivePath="http://"+resIp+":8402/personal.html#/?logOut=http://"+userIp+":7001";
        Map<String,String> params=new HashMap<>();
        params.put("path",resLivePath);
        return moduleDao.updateResPersonal(params);
    }
    //巡课平台
    private boolean initPatrol(){
        String patrolPath="http://"+patrolIp+":8310/index.html?logOut=http://"+userIp+":7001";
        String patrolColor="http://"+userIp+":7003/resources/xunke.png";
        String patrolIcon="http://"+userIp+":7003/resources/xunke-logo.png";
        Map<String,String> params=new HashMap<>();
        params.put("path",patrolPath);
        params.put("color",patrolColor);
        params.put("icon",patrolIcon);
        return moduleDao.updatePatrol(params);
    }
    //集控
    private boolean initDeviceControl(){
        String deviceControlPath="http://"+deviceControlIp+":8320/index.html?logOut=http://"+userIp+":7001";
        String deviceControlColor="http://"+userIp+":7003/resources/devicecontrol.png";
        String deviceControlIcon="http://"+userIp+":7003/resources/devicecontrol-logo.png";
        Map<String,String> params=new HashMap<>();
        params.put("path",deviceControlPath);
        params.put("color",deviceControlColor);
        params.put("icon",deviceControlIcon);
        return moduleDao.updateDeviceControl(params);
    }
    //运维平台
    private boolean initYunWei(){
        String ywPath="http://"+ywIp+":80/index.html#/?logOut=http://"+userIp+":7001";
        String ywIcon="http://"+userIp+":7003/resources/yunwei-logo.png";
        Map<String,String> params=new HashMap<>();
        params.put("path",ywPath);
        params.put("icon",ywIcon);
        return moduleDao.updateYunWei(params);
    }
    //云上资源
    private boolean initCloudResource(){
        String cloudColor="http://"+userIp+":7003/resources/cloudresource.png";
        String cloudIcon="http://"+userIp+":7003/resources/cloudresource-logo.png";
        Map<String,String> params=new HashMap<>();
        params.put("color",cloudColor);
        params.put("icon",cloudIcon);
        return moduleDao.updateCloudRes(params);
    }
    /**
     * 替换主题设置ip地址
     * @param ip
     */
    private boolean replaceThemeUrl(String ip) {
        Map<String,String> themeInfo=moduleSysDao.getCurrentTheme();
        //匹配ip的正则
        String reg = "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)";
        boolean updTheme=false;
        if(themeInfo!=null && !themeInfo.isEmpty()) {
            String themeLogo="";
            if(!StringUtil.isEmpty(themeInfo.get("themeLogo"))){
                themeLogo=themeInfo.get("themeLogo");
                themeLogo=themeLogo.replaceFirst(reg,ip);
            }
            String themeBackGround="";
            if(!StringUtil.isEmpty(themeInfo.get("themeBackGround"))){
                themeBackGround=themeInfo.get("themeBackGround");
                themeBackGround=themeBackGround.replaceFirst(reg,ip);
            }
            updTheme=moduleSysDao.updateThemeUrl(themeLogo,themeBackGround);
        }
        List<Map<String,Object>> moduleList=moduleDao.getNewAddModule();
        boolean updModule=false;
        if(moduleList!=null && !moduleList.isEmpty()){
            for(Map<String,Object> module:moduleList){
                String color="";
                if(module.get("color")!=null && !"".equals(module.get("color").toString())){
                    color=module.get("color").toString();
                    color=color.replaceAll(reg,ip);
                }
                String icon="";
                if(module.get("icon")!=null && !"".equals(module.get("icon").toString())){
                    icon=module.get("icon").toString();
                    icon=icon.replaceFirst(reg,ip);
                }
                if("".equals(color) && "".equals(icon)){
                    updModule=true;
                }else {
                    updModule = moduleDao.updateNewModule(color, icon,
                            Integer.parseInt(String.valueOf(module.get("moduleId"))));
                }
            }
        }
        return updTheme && updModule;
    }
}
