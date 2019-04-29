package com.honghe.entrance.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.honghe.entrance.common.pojo.model.Result;
import com.honghe.entrance.common.util.DataUtil;
import com.honghe.entrance.common.util.HttpUtils;
import com.honghe.entrance.common.util.ValidatorUtil;
import com.honghe.entrance.dao.ModuleSysDao;
import com.honghe.entrance.dao.Role2PermissionDao;
import com.honghe.entrance.dao.UserDao;
import com.honghe.entrance.dao.UserSysPositionDao;
import com.honghe.entrance.entity.Module;
import com.honghe.entrance.entity.Permissions;
import com.honghe.entrance.service.SysModuleService;
import com.honghe.entrance.util.ConfigParamsUtil;
import jodd.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * <p>Description:</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 北京鸿合智能系统股份有限公司</p>
 *
 * @author hthwx
 * @date 2019/2/23
 */
@Service
@Configuration
public class SysModuleServiceImpl implements SysModuleService {
    @Autowired
    private ModuleSysDao sysModuleDao;
    @Autowired
    private UserSysPositionDao userSysPositionDao;
    @Autowired
    private Role2PermissionDao role2PermissionDao;
    @Autowired
    private UserDao userDao;
    @Value("${entrance.module.userIp}")
    private String userUrl;
    @Value("${entrance.module.deviceControlIp}")
    private String deviceControlIp;

    private Logger logger= LoggerFactory.getLogger(SysModuleServiceImpl.class);

    @Override
    public Result getMarkedModules(String roleId, String ip) {
        Result result = new Result(Result.Code.Success.value());
        //获取所有模块权限
        //获取所有模块id集合
//        List<String> moduleIdsList = sysModuleDao.selectModuleIds();
//        String moduleIds= StringUtils.join(moduleIdsList.toArray(),",");
//        List<Map<String,String>> allPermissionMaps = sysModuleDao.getAllModules(moduleIds);
        List<Map<String,Object>> allPermissionMaps = sysModuleDao.getAllModules();
        Permissions allPermissions = makeMapListToPermissions(allPermissionMaps);
        //获取角色的权限
        List rolePermissions = role2PermissionDao.role2SysModuleSearch(roleId);
        Map<String,Object> moduleResult= getExtraInfo(ip);
        boolean isExtraNet=false;
        //判断内网还是外网访问
        if(!StringUtil.isEmpty(moduleResult.get("isExtraNet").toString())){
            isExtraNet=(boolean)moduleResult.get("isExtraNet");
        }
        allPermissions.setIsExtraNet(isExtraNet);
        isPermissionUsed(allPermissions.getPermissions(),rolePermissions,roleId,isExtraNet);
        resetPermissStatus(allPermissions.getPermissions());
        result.setResult(allPermissions);
        return result;
    }

    @Override
    public Result sysGetEnableModule(String userId, String searchKey, String status, String ip) {
        Result result = new Result(Result.Code.Success.value());
        if (StringUtil.isEmpty(userId) || "undefined".equals(userId)){
            userId="0";
        }
        if(StringUtil.isEmpty(ip) || !ValidatorUtil.isIPAddr(ip)){
            result.setCode(Result.Code.ParamError.value());
            result.setOther("ip地址不能为空或格式错误");

        }
        List<Map<String,Object>> allEnableModules = userSysPositionDao.sysGetAllEnableModule(userId, searchKey, status);
        markedModuleChecked(userId,allEnableModules);
        List<Map> userSysPositionList = mapToEntity(allEnableModules,userId,ip);
        result.setResult(userSysPositionList);
        return result;
    }

    /**
     * 标记模块是否有权限访问
     * @param userId
     * @param userModuleList
     * @author caoqian
     */
    private List<Map<String, Object>> markedModuleChecked(String userId,List<Map<String, Object>> userModuleList) {
        if (userModuleList != null && !userModuleList.isEmpty()) {
            for (Map<String, Object> module : userModuleList) {
                //查询未删除模块
                if("0".equals(String.valueOf(module.get("is_delete")))){
                    String moduleId=String.valueOf(module.get("user_sys_module_id"));
                    if("0".equals(userId) && ("4".equals(moduleId) || "6".equals(moduleId))){
                        module.put("isSelect", "");
                    }else{
                        module.put("isSelect", "m-checked");
                    }
                }else{
                    module.put("isSelect", "");
                }
            }
        }
        return userModuleList;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result deleteSysModules(String userId, String moduleIds) {
        Result deleteResult = new Result(Result.Code.Success.value(),false);
        if(StringUtils.isBlank(userId) || StringUtils.isBlank(moduleIds)){
          //  throw new CommandException(Response.PARAM_ERROR,"参数错误");
            deleteResult.setCode(Result.Code.ParamError.value());
            deleteResult.setOther("参数错误");
        }
        String[] userSysModuleIdArry = moduleIds.split(",");
//        StringBuilder user_sys_ModuleId = new StringBuilder();
        List<String> moduleIdList=new ArrayList<>();
        for (String userSysModuleId : userSysModuleIdArry){
            if(StringUtils.isNotBlank(userSysModuleId)){
                if("1".equals(userSysModuleId) || "2".equals(userSysModuleId)){
                    break;
                }
//                user_sys_ModuleId.append("'").append(userSysModuleId).append("',");
                moduleIdList.add(userSysModuleId);
            }
        }
        if(!"1".equals(userId)){
           // throw new CommandException(Response.INNER_ERROR,"只有管理员有权限删除");
            deleteResult.setCode(Result.Code.UnKnowError.value());
            deleteResult.setOther("只有管理员有权限删除");
        }
        try {
            /*boolean delModules = sysModuleDao.deleteSysModules(user_sys_ModuleId.substring(0,user_sys_ModuleId.length()-1));//删除对应模块
            boolean delUser2Modules = userSysPositionDao.deleteSysModules(user_sys_ModuleId.substring(0,user_sys_ModuleId.length()-1));//删除用户和模块对应关系
            boolean delRole2Permission = role2PermissionDao.deleteBySysModuleId(user_sys_ModuleId.substring(0,user_sys_ModuleId.length()-1));//删除角色和模块关联*/
            if(!moduleIdList.isEmpty()) {
                //删除对应模块
                boolean delModules = sysModuleDao.deleteSysModules(moduleIdList);
                //删除用户和模块对应关系
                boolean delUser2Modules = userSysPositionDao.deleteSysModules(moduleIdList);
                //删除角色和模块关联
                boolean delRole2Permission = role2PermissionDao.deleteBySysModuleId(moduleIdList);
                boolean result = delModules && delUser2Modules && delRole2Permission;
                deleteResult.setResult(result);
            }else{
                deleteResult.setResult(true);
            }
        }catch (Exception e){
           // throw new CommandException(Response.INNER_ERROR,"执行错误");
            deleteResult.setCode(Result.Code.UnKnowError.value());
            deleteResult.setOther("执行错误");
        }
        return deleteResult;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result addUserSysModules(String jsonString) {
        if(StringUtils.isEmpty(jsonString)){
            return new Result(Result.Code.ParamError.value(),"模块信息不能为空");
        }
        Result addResult = new Result(Result.Code.Success.value(),false);
        JSONObject object = JSON.parseObject(jsonString);
        JSONObject info = object.getJSONObject("info");
        JSONObject layout = object.getJSONObject("layout");
        //info
        String userId = info.getString("userId");
        String name = info.getString("title");
        String url = info.getString("href");
        String keyValueString = info.getString("appParams")==null?"":info.getString("appParams");
        String token = info.getString("token");
        int nameFlag = info.getIntValue("nameFlag");
        String bgColor="";
        //自定义背景
        String definedBgColor=info.getString("appBackground");
        if(!StringUtil.isEmpty(definedBgColor)){
            Result saveFileResult = ConfigParamsUtil.saveFileException(definedBgColor);
            if(saveFileResult != null && saveFileResult.getCode() == Result.Code.Success.value())
            {
                try
                {
                    bgColor = ConfigParamsUtil.convertImagePath2Url(saveFileResult.getResult().toString(),userUrl);
                }
                catch (Exception e)
                {
                    saveFileResult.setCode(Result.Code.UnKnowError.value());
                    saveFileResult.setOther("convertImagePath2Url内部错误");
                    return saveFileResult;
                }
            }
            else
            {
                return saveFileResult;
            }
           // bgColor= ConfigParamsUtil.convertImagePath2Url(SysServiceImpl.saveFileException(definedBgColor),userUrl);
        }else{
            //选择的模板背景色
            bgColor = info.getString("bg");
        }
        String icon="";
        //自定义图标
        String definedIcon=info.getString("logo");
        if(!StringUtil.isEmpty(definedIcon)){
           // icon=ConfigParamsUtil.convertImagePath2Url(SysServiceImpl.saveFileException(definedIcon),userUrl);
            Result saveIconResult = ConfigParamsUtil.saveFileException(definedIcon);
            if(saveIconResult != null && saveIconResult.getCode() == Result.Code.Success.value())
            {
                try
                {
                    icon = ConfigParamsUtil.convertImagePath2Url(saveIconResult.getResult().toString(),userUrl);
                }
                catch (Exception e)
                {
                    saveIconResult.setCode(Result.Code.UnKnowError.value());
                    saveIconResult.setOther("saveIcon内部错误");
                }
            }
            else
            {
                return saveIconResult;
            }
        }else {
            String defaultIcon=info.getString("icon");
            if(defaultIcon.indexOf("fakepath")>-1) {
                icon = null;
            }else{
                icon=defaultIcon;
            }
        }
        int status = info.getIntValue("status");
        String enName = info.getString("en_name")==null?"":info.getString("en_name");
        String version = info.getString("version")==null?"1.0.0":info.getString("version");
        String remark = info.getString("remark")==null?"":info.getString("remark");
        //指定浏览器 浏览器路由
        String browser = getStringValue(info.getString("browser"));
        String browserRoute = getStringValue(info.getString("browserRoute")).replaceAll("\\\\","/");
        //layout
        /*String width = layout.getString("w");
        String height = layout.getString("h");*/
        String width="5";
        String height ="4";
        String x = layout.getString("x");
        String y = layout.getString("y");
        int min_h = layout.getIntValue("minH") == 0 ? 2:layout.getIntValue("minH");
        int min_w = layout.getIntValue("minW") == 0 ? 2:layout.getIntValue("minW");
        /* int isDraggable = layout.getIntValue("isDraggable");
        int isResizable = layout.getIntValue("isResizable");*/

        if(!DataUtil.checkStringIsNull(1, name, url, x, y, bgColor, icon)){
           // throw new CommandException(Response.PARAM_ERROR,"参数错误");
            addResult.setCode(Result.Code.ParamError.value());
            addResult.setOther("参数错误");
            return addResult;
        }
//        long user2Position = 0;
        try {
            int moduleId = 0;
            if(layout.containsKey("i") && layout.getString("i").endsWith("x")){//判别标识，如果是“x”说明用户与模块关系表没有建立，需要添加
                moduleId = Integer.parseInt(info.getString("moduleId"));
            }else {
                Map<String,Object> moduleInfo=sysModuleDao.getModuleByName(name);
                if (moduleInfo==null || moduleInfo.isEmpty()) {
                    //判断重复
                    Module module=new Module(name, url, bgColor, keyValueString, icon, token,
                            enName, version, remark, status, null, browserRoute,nameFlag);
                    //添加模块，获取模块id
                    boolean value=sysModuleDao.addSysModules(module);
                    if(value) {
                        moduleId = module.getModuleId();
                    }else{
                        moduleId=0;
                    }
                } else {
                    moduleId = Integer.parseInt(String.valueOf(moduleInfo.get("id")));
                }
            }
            if (moduleId > 0) {
                Map<String,Object> map=userSysPositionDao.getUser2ModuleByName(Integer.parseInt(userId), moduleId);
                if (map==null || map.isEmpty()) {
                    //查询所有用户，添加模块要给所有用户均分配与模块的布局
                  //  List<Map<String,String>> userList=userDao.searchUserList("","",null);
                    List<Map<String,String>> userList = userDao.userPageList("","",0,0,false);
                    if(userList!=null && !userList.isEmpty()){
                        for(Map<String,String> user:userList){
                            int uId=Integer.parseInt(String.valueOf(user.get("userId")));
                            //默认模块权限关闭
                            int isDelete=1;
                            //管理员默认权限开放
                            if(1==uId || 2==uId){
                                isDelete=0;
                            }
                            List<Map<String,Object>> positionSqlList=new ArrayList<>();
                            Map<String,Object> params=new HashMap<>();
                            params.put("userId",uId);
                            params.put("moduleId",Integer.valueOf(String.valueOf(moduleId)));
                            params.put("x",x);
                            params.put("y",y);
                            params.put("w",width);
                            params.put("h",height);
                            params.put("minW",min_w);
                            params.put("minH",min_h);
                            params.put("isDelete",isDelete);
                            params.put("isDraggable",1);
                            params.put("isResizable",0);
                            positionSqlList.add(params);
                            if (!userSysPositionDao.addUser2Position(positionSqlList)) {
//                                logger.info("用户与模块对应关系添加失败");
                                //return false;
                                addResult.setResult(false);
                                return addResult;
                            }
                        }
                        //为用户分配模块权限
                        Map<String, Object> param = new HashMap<>();
                        param.put("roleId", userId);
                        param.put("permissionId", moduleId);
                        boolean re_value = role2PermissionDao.add(param);
                        if(!re_value){
//                            logger.info("给管理员添加权限失败");
                            addResult.setResult(false);
                        }else{
                            addResult.setResult(true);
                        }
                    }else {
                        addResult.setResult(false);
                    }
                } else {
                    addResult.setCode(Result.Code.ParamError.value());
                    addResult.setOther("模块已存在");
                }
            }
        }catch (Exception e){
            logger.error("添加模块异常",e);
            addResult.setCode(Result.Code.UnKnowError.value());
            addResult.setOther("执行错误");
        }
        return addResult;
    }
    @Override
    public Map<String,Object> getExtraInfo(String ip)
    {
        Map<String,String> netIp = sysModuleDao.getExtraInfo();
        String port="7003";
        Map<String,Object> result=new HashMap<>();
        if(netIp!=null && !netIp.isEmpty() && !StringUtils.isEmpty(ip)){
            //内网
            if(netIp.containsKey("intraNetIp") && netIp.get("intraNetIp").contains(ip)){
                //是内外还是外网
                result.put("isExtraNet",false);
                //是否存在该ip
                result.put("exitIp",true);
            }
            //外网
            else if(netIp.containsKey("extraNetIp") && netIp.get("extraNetIp").contains(ip)){
                result.put("isExtraNet",true);
                result.put("exitIp",true);
            }else{
                result.put("isExtraNet",false);
                result.put("exitIp",false);
            }
        }else{
            result.put("isExtraNet",false);
            result.put("exitIp",false);
        }
        result.put("port",port);
        return result;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean addRole2DefaultSysModule(int roleId) {
        List<String> defaultModuleIdsList= sysModuleDao.selectDefaultModuleIds();
        List<Map<String,Object>> list=new ArrayList<>();
        for(String moduleId:defaultModuleIdsList){
            Map<String,Object> map=new HashMap<>();
            map.put("roleId",roleId);
            map.put("moduleId",moduleId);
            list.add(map);
        }
        boolean re_value=false;
        if(!list.isEmpty()) {
            re_value=saveRole2sysModule(roleId,list);
        }
        return re_value;
    }

    @Override
    public Result updateModuleStatus(String moduleId, String status) {
        if(StringUtils.isEmpty(moduleId) || StringUtils.isEmpty(status)){
            return new Result(Result.Code.ParamError.value(),"参数错误");
        }
        Result result = new Result(Result.Code.Success.value());
        result.setResult(sysModuleDao.updateModuleStatus(moduleId,status));
        return result;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result updateUserSysSoloModules(String moduleStr) {
        JSONObject object = JSON.parseObject(moduleStr);
        JSONObject info = object.getJSONObject("info");
        JSONObject layout = object.getJSONObject("layout");
        //info
        String userId = info.getString("userId");
        String name = info.getString("title");
        String url = info.getString("href");
        String keyValueString = info.getString("appParams") == null ? "" : info.getString("appParams");
        String token = info.getString("token");
        int nameFlag = info.getIntValue("nameFlag");
        String bgColor="";
        //自定义背景
        String definedBgColor=info.getString("appBackground");
        if(!StringUtil.isEmpty(definedBgColor)){
            bgColor=ConfigParamsUtil.convertImagePath2Url(
                    (String)ConfigParamsUtil.saveFileException(definedBgColor).getResult(),userUrl);
        }else{
            //选择的模板背景色
            bgColor = info.getString("bg");
        }
        String icon="";
        //自定义图标
        String definedIcon=info.getString("logo");
        if(!StringUtil.isEmpty(definedIcon)){
            icon=ConfigParamsUtil.convertImagePath2Url(
                    (String)ConfigParamsUtil.saveFileException(definedIcon).getResult(),userUrl);
        }else {
            String defaultIcon=info.getString("icon");
            if(defaultIcon.indexOf("fakepath")>-1) {
                icon = null;
            }else{
                icon=defaultIcon;
            }
        }
        String moduleId = info.getString("moduleId");
        String en_name = info.getString("en_name") == null ? "" : info.getString("en_name");
        String version = info.getString("version") == null ? "1.0.0" : info.getString("version");
        String remark = info.getString("remark") == null ? "" : info.getString("remark");
        String status = info.getString("status")==null ? "":info.getString("status");
        //指定浏览器 浏览器路由
        String browser = getStringValue(info.getString("browser"));
        String browserRoute = getStringValue(info.getString("browserRoute")).replaceAll("\\\\","/");

        String x = layout.getString("x");
        String y = layout.getString("y");
        int isDraggable = 1;
        int isResizable = layout.getIntValue("isResizable");

        if (!DataUtil.checkStringIsNull(1, userId, moduleId)) {
            return new Result(Result.Code.ParamError.value(), "参数错误");
        }
        //判别标识，如果是“x”说明用户与模块关系表没有建立，需要添加
        if(layout.containsKey("i") && layout.getString("i").endsWith("x")){
            addUserSysModules(moduleStr);
        }
        Result result=new Result(Result.Code.Success.value());
        try {
            boolean updateSysModule = false;
            if("2".equals(moduleId) || "5".equals(moduleId) || "12".equals(moduleId)){
                //用户、运维、个人中心管理模块不能修改模块系统
                updateSysModule = true;
            }else {
                if(StringUtils.isNotBlank(name)){
                    //验证名字和模块id
                    int countRepeat = sysModuleDao.repeatName(moduleId,name);
                    if(countRepeat>0){
                        return new Result(Result.Code.UnKnowError.value(), "模块名称重复");
                    }
                }
                //更新模块
                updateSysModule = sysModuleDao.updateSysModules(
                        getUpdModuleParams(moduleId, name, url,keyValueString, token, bgColor, icon,
                        status, en_name, version, remark, browser, browserRoute, nameFlag));
            }
            //更新用户与模块对应关系
         /*   boolean updateUser2Module = userSysPositionDao.updateUser2PerModules(userId, moduleId, getStringValue(x),
                    getStringValue(y), null ,isDraggable, isResizable);*/
            boolean updateUser2Module = userSysPositionDao.updateUser2PerModules(
                    getParams(userId,moduleId,x,y,null,isDraggable,isResizable));
            boolean value = updateSysModule && updateUser2Module;
            if(!value) {
                return new Result(Result.Code.UnKnowError.value(), "更新失败");
            }else{
                result.setResult(value);
            }
        } catch (Exception e) {
            return new Result(Result.Code.UnKnowError.value(), "执行错误");
        }
        return result;
    }

    private Map<String,Object> getUpdModuleParams(String moduleId, String name, String url, String keyValueString,
                                      String token, String bgColor, String icon, String status,String enName,
                                      String version, String remark, String browser, String browserRoute,int nameFlag) {
        Map<String,Object> params=new HashMap<>();
        params.put("moduleId",moduleId);
        params.put("name",getStringValue(name));
        params.put("url",getStringValue(url));
        params.put("keyValueString",getStringValue(keyValueString));
        params.put("token",getStringValue(token));
        params.put("bgColor",getStringValue(bgColor));
        params.put("icon",getStringValue(icon));
        params.put("version",version);
        params.put("status",status);
        params.put("enName",enName);
        params.put("remark",remark);
        params.put("browser",browser);
        params.put("browserRoute",browserRoute);
        params.put("nameFlag",nameFlag);
        return params;
    }

    private Map<String,Object> getParams(String userId, String moduleId, String x, String y,
                                         String isDelete, int isDraggable, int isResizable) {
        Map<String,Object> params=new HashMap<>();
        params.put("userId",userId);
        params.put("moduleId",moduleId);
        params.put("x",getStringValue(x));
        params.put("y",getStringValue(y));
        params.put("isDelete",null);
        params.put("isDraggable",isDraggable);
        params.put("isResizable",isResizable);
        return params;
    }


    @Override
    public Result updateModulePosition(String moduleStr) {
        JSONArray jsonArray = JSONArray.parseArray(moduleStr);
        Result result=new Result(Result.Code.Success.value());
        //判断数组的size
        if(jsonArray.size()==0){
            result.setResult(false);
            return result;
        }
        for (Object o : jsonArray) {
            JSONObject moduleInfo = (JSONObject) o;
            JSONObject info = moduleInfo.getJSONObject("info");
            JSONObject layout = moduleInfo.getJSONObject("layout");
            //info
            String userId = info.getString("userId");
            //userId为空或为0时，表示已退出登录，默认布局，不在修改布局
            if(StringUtil.isEmpty(userId) || "0".equals(userId)){
                result.setResult(true);
                return result;
            }
            String moduleId = info.getString("moduleId");
            String x = layout.getString("x");
            String y = layout.getString("y");
            int isDraggable = 1;
            int isResizable = layout.getIntValue("isResizable");
            if (!DataUtil.checkStringIsNull(1, userId, moduleId)) {
                return new Result(Result.Code.ParamError.value(), "参数错误");
            }
            //判别标识，如果是“x”说明用户与模块关系表没有建立，需要添加
            if(layout.containsKey("i") && layout.getString("i").endsWith("x")){
                addUserSysModules(((JSONObject) o).toJSONString());
            }
            try {
                //更新用户与模块对应关系
                boolean updateUser2Module = userSysPositionDao.updateUser2PerModules(
                        getParams(userId,moduleId,x,y,null,isDraggable,isResizable));
                if(!updateUser2Module) {
                    break;
                }else{
                    result.setResult(updateUser2Module);
                }
            } catch (Exception e) {
                result.setCode(Result.Code.UnKnowError.value());
                result.setOther("执行错误");
            }
        }
        return result;
    }

    @Override
    public Result getMaxModuleId() {
        return new Result(Result.Code.Success.value(),sysModuleDao.getMaxModuleId());
    }

    /**
     * 获取当前主题数据
     *
     * @return
     */
    @Override
    public Result getCurrentTheme(String ip) {
        Result result = new Result(Result.Code.Success.value());
        Map<String,Object> moduleResult= getExtraInfo(ip);
        boolean isExtraNet=false;
        //判断内网还是外网访问
        if(!StringUtil.isEmpty(moduleResult.get("isExtraNet").toString())){
            isExtraNet=(boolean)moduleResult.get("isExtraNet");
        }
        Map<String,Object> currentThemeMap=new HashMap<>();
        try {
            currentThemeMap.putAll(getThemeInfo());
        }catch (Exception e){
            currentThemeMap.putAll(null);
        }
        currentThemeMap.put("isExtraNet",isExtraNet);
        result.setResult(currentThemeMap);
        return result;
    }

    private Map<String,String> getThemeInfo() {
        Map<String, String> theme = sysModuleDao.getCurrentTheme();
        if(theme!=null && !theme.isEmpty()) {
            if (!theme.containsKey("themeTitle")) {
                theme.put("themeTitle", "");
            }
            if (!theme.containsKey("themeLogo")) {
                theme.put("themeLogo", "");
            }
            if (!theme.containsKey("themeBackGround")) {
                theme.put("themeBackGround", "");
            }
        }else{
            theme=new HashMap<>();
            theme.put("themeTitle", "");
            theme.put("themeLogo", "");
            theme.put("themeBackGround", "");
        }
        return theme;
    }

    @Override
    public Result resetTheme() {
        Result result = new Result(Result.Code.Success.value());
        try {
            result.setResult(sysModuleDao.resetTheme());
        }catch (Exception e){
            logger.error("重置主题设置异常",e);
            result.setCode(Result.Code.UnKnowError.value());
            result.setResult(false);
        }
        return result;
    }

    @Override
    public Result themeSet(String userId, String themeTitle, String themeEnTitle, String themeLogo,
                           String themeLoginBackGround, String themeBackGround) {
        Result result=new Result(Result.Code.Success.value());
        if(StringUtils.isEmpty(themeTitle) || StringUtils.isEmpty(userId)){
            result.setCode(Result.Code.ParamError.value());
            result.setResult("标题不能为空");
        }else {
            String themeLogoPath = "";
            String themeLoginBackGroundPath = "";
            String themeBackGroundPath = "";
            if (!StringUtils.isEmpty(themeLogo)) {
                themeLogoPath = ConfigParamsUtil.convertImagePath2Url(
                        (String) ConfigParamsUtil.saveFileException(themeLogo).getResult(), userUrl);
            }
            if (!StringUtils.isEmpty(themeLoginBackGround)) {
                themeLoginBackGroundPath = ConfigParamsUtil.convertImagePath2Url(
                        (String) ConfigParamsUtil.saveFileException(themeLoginBackGround).getResult(), userUrl);
            }
            if (!StringUtils.isEmpty(themeBackGround)) {
                themeBackGroundPath = ConfigParamsUtil.convertImagePath2Url(
                        (String) ConfigParamsUtil.saveFileException(themeBackGround).getResult(), userUrl);
            }
            //插入数据库
            boolean value = updateThemeData(ConfigParamsUtil.getIntegerValue(Integer.parseInt(userId), 1), themeTitle,
                    themeEnTitle, themeLogoPath,themeLoginBackGroundPath, themeBackGroundPath, 1, 0,
                    DataUtil.dateToString(new Date()));
            result.setResult(value);
        }
        return result;
    }

    /**
     * 更新主题数据库
     * @param userId
     * @param themeTitle
     * @param themeEnTitle
     * @param themeLogo
     * @param themeLoginBackGround
     * @param themeBackGround
     * @param type
     * @param isDelete
     * @param modifyTime
     * @return
     * @author caoqian
     * @date 20190301
     */
    private boolean updateThemeData(Integer userId, String themeTitle, String themeEnTitle, String themeLogo,
                                    String themeLoginBackGround, String themeBackGround, Integer type,
                                    Integer isDelete, String modifyTime) {
        Map<String,Object> themeParams=new HashMap<>();
        themeParams.put("userId",userId);
        if(!StringUtils.isEmpty(themeTitle)) {
            themeParams.put("themeTitle", themeTitle);
        }
        if(!StringUtils.isEmpty(themeEnTitle)){
            themeParams.put("themeEnTitle",themeEnTitle);
        }
        if(!StringUtils.isEmpty(themeLogo)) {
            themeParams.put("themeLogo", themeLogo);
        }
        if(!StringUtils.isEmpty(themeLoginBackGround)) {
            themeParams.put("themeLoginBackGround", themeLoginBackGround);
        }
        if(!StringUtils.isEmpty(themeBackGround)) {
            themeParams.put("themeBackGround", themeBackGround);
        }
        themeParams.put("type",type);
        themeParams.put("isDelete",isDelete);
        if(!StringUtils.isEmpty(modifyTime)){
            themeParams.put("modifyTime",modifyTime);
        }
        return sysModuleDao.themeSet(themeParams);
    }

    /**
     * mapList转换为permissions实体类
     * @param mapList
     * @return
     * @author caoqian
     * @date 20190228
     */
    private Permissions makeMapListToPermissions(List<Map<String,Object>>mapList){
        Permissions rootPermissions = new Permissions();
        rootPermissions.setId("0");
        if(mapList!=null && !mapList.isEmpty()) {
            for (Map<String, Object> map : mapList) {
                if (map != null && !map.isEmpty()) {
                    String permissionId = String.valueOf(map.get("user_sys_module_id"));
                    String icon = getModuleLogoIcon(permissionId);
                    String modulePath = map.get("user_sys_module_path")==null?"":map.get("user_sys_module_path").toString();
                    String moduleName=map.get("user_sys_module_name")==null?"":map.get("user_sys_module_name").toString();
                    Permissions childPermission = new Permissions(permissionId,moduleName ,modulePath,
                            map.get("remark")==null?"":map.get("remark").toString(), "0", icon);
                    childPermission.setDesc(moduleName);
                    rootPermissions.getPermissions().add(childPermission);
                }
            }
        }
        return rootPermissions;
    }
    /**
     * 为用户分配权限时，模块图标
     * @param moduleId
     * @return
     */
    private String getModuleLogoIcon(String moduleId) {
        String icon="";
        String logoIconPath="http://"+userUrl+":7003/resources/logo-icon/";
        switch (moduleId){
            case "4":
                icon=logoIconPath+"xunke-icon.png";
                break;
            case "6":
                icon=logoIconPath+"devicecontrol-icon.png";
                break;
            case "7":
                icon=logoIconPath+"live-icon.png";
                break;
            case "8":
                icon=logoIconPath+"resources-icon.png";
                break;
            case "9":
                icon=logoIconPath+"famousteacher-icon.png";
                break;
            case "10":
                icon=logoIconPath+"jiaoyanhuodong-icon.png";
                break;
            case "11":
                icon=logoIconPath+"video-icon.png";
                break;
            case "13":
                icon=logoIconPath+"cloudresource-icon.png";
                break;
            default:
                icon=logoIconPath+"default-icon.png";
        }
        return icon;
    }
    /**
     * 根据角色已分配权限，在全部权限中做标记
     * @param allRolePermission 权限数组
     * @param usedPermission 已分配权限列表
     * @param role_id 角色ID
     * @return JSONArray 所有权限数组
     */
    private void isPermissionUsed(List<Permissions> allRolePermission, List usedPermission, String role_id,boolean isExtraNet){
        if (allRolePermission != null){
            int roleNumber = allRolePermission.size();
            for(int i = 0 ; i < roleNumber; i ++){
                Permissions permissions = allRolePermission.get(i);
                permissions.setIsExtraNet(isExtraNet);
                int usedNumber = usedPermission.size();
                for (int j = 0; j < usedNumber; j++) {
                    Map useMap = (Map) usedPermission.get(j);
                    if (String.valueOf(useMap.get("roleId")).equals(role_id) && permissions.getId().
                            equals(String.valueOf(useMap.get("permissionId")))) {
                        permissions.setIsSelect("m-checked");
                    }
                }
                if(permissions.getPermissions()!=null && permissions.getPermissions().size()>0){
                    isPermissionUsed(permissions.getPermissions(),usedPermission,role_id,isExtraNet);
                }
            }
        }
    }
    /**
     * 重新设置节点状态
     * @param allRolePermission 所有的模块权限
     * @return void
     * @Create Wangzy 2018/8/3 10:57
     * @Update Wangzy 2018/8/3 10:57
     */
    private void resetPermissStatus(List<Permissions> allRolePermission){
        if (allRolePermission != null){
            int permissionNumber = allRolePermission.size();
            for(int i = 0; i < permissionNumber;i++){
                Permissions permissions = allRolePermission.get(i);
                if (permissions.getIsSelect().equals("m-checked")){//如果是被选中状态才重新判断状态，否则不做处理
                    Map booleanMap = new HashMap();
                    booleanMap.put("isSelected","m-checked");
                    caculatePermissionStatus(permissions,booleanMap);
                    if (!booleanMap.get("isSelected").equals("m-checked")){
                        permissions.setIsSelect("");
                        if(permissions.getPermissions()!=null && permissions.getPermissions().size()>0){
                            resetPermissStatus(permissions.getPermissions());
                        }
                    }
                }
            }
        }
    }
    /**
     * 当前节点是否应该被选中,返回true是选中，返回false不选中
     * @param permission
     * @param isSelected
     * @return void
     * @Create Wangzy 2018/8/3 10:58
     * @Update Wangzy 2018/8/3 10:58
     */
    private void caculatePermissionStatus(Permissions permission, Map isSelected){
        List<Permissions>childPermissions = permission.getPermissions();
        if (childPermissions != null && childPermissions.size()>0){
            int permissionNumber = childPermissions.size();
            for(int i = 0; i < permissionNumber;i++){
                Permissions childPermission = childPermissions.get(i);
                if (childPermission.getIsSelect().equals("")){
                    isSelected.put("isSelected","");
                }else {
                    caculatePermissionStatus(childPermission,isSelected);
                }
            }
        }
    }


    /**
     * 验证用户访问集控、巡课模块的权限
     * 给用户分配模块权限后，集控、巡课还分别需要验证是否给用户分配了地点和设备
     * @param userId 用户id
     * @return
     * @author caoqian
     * @date 20180102
     */
    private JSONObject checkUserAuthority(String userId) {
        JSONObject userJson = new JSONObject();
        try {
            // String intefaceIp = ConfigUtil.getInstance().getPropertyValue("HDeviceControl_url");
            String intefaceUrl = "http://" + deviceControlIp + ":8302/service/cloud/httpCommandService"
                    + "?cmd=user&cmd_op=getUserInfoById&userId=" + userId;
            JSONObject result = HttpUtils.httpGet(intefaceUrl);
            if (result != null && !result.isEmpty() && "0".equals(result.get("code").toString())) {
                userJson = JSONObject.parseObject(result.get("result").toString());
            }
        }catch (Exception e){
            ;
        }
        return userJson;
    }
    private String getAuthorityMsg(String moduleId,JSONObject userJson){
        String resultMsg="";
        if(userJson!=null && !userJson.isEmpty()) {
            if ("4".equals(moduleId)) {
                if (0 != userJson.getIntValue("deviceNum")) {
                    resultMsg = "true";
                } else {
                    resultMsg = "您没有可巡课的设备，请联系管理员";
                }
            } else if ("6".equals(moduleId)) {
                if (!StringUtil.isEmpty(userJson.getString("areaName"))) {
                    resultMsg = "true";
                } else {
                    resultMsg = "您没有可管理的设备，请联系管理员";
                }
            }
        }else{
            resultMsg="未查询到此用户的模块权限";
        }
        return resultMsg;
    }
    /**
     * map转换为实体类
     * @param mapList map数组
     * @return java.util.List<com.honghe.entrance.entity.sysModule.UserSysPosition>
     * @Create Wangzy 2018/8/4 11:52
     * @Update caoqian
     */
    private List<Map> mapToEntity(List<Map<String, Object>> mapList,String userId,String ip) {
        //授权信息去掉
//        boolean isAuth = getModuleAuthorized();
        List<Map> re_value = new ArrayList<>();
        boolean isExtraNet=false;
        if(!StringUtil.isEmpty(ip)) {
            //判断内网访问还是外网访问,true:外网;false:内网
            Map<String,Object> extraNetMap=getExtraInfo(ip);
            isExtraNet=(boolean)extraNetMap.get("isExtraNet");
        }
        for (Map<String, Object> map : mapList) {
            Map moduleMap = new HashMap();
            //基本信息
            Map infoMap = new HashMap();
            String moduleId = String.valueOf(map.get("user_sys_module_id"));
            infoMap.put("moduleId",Long.valueOf(moduleId));
            infoMap.put("userId",Integer.valueOf((map.get("user_id") == null ||
                    String.valueOf(map.get("user_id")).equals(""))? userId : String.valueOf(map.get("user_id"))));
            infoMap.put("title",map.get("user_sys_module_name") == null ? "" : map.get("user_sys_module_name"));
            infoMap.put("name",map.get("name") == null ? "" : map.get("name"));
            infoMap.put("version",map.get("version") == null ? "" : map.get("version"));
            infoMap.put("remark",map.get("remark") == null ? "" : map.get("remark"));
            infoMap.put("status",Integer.valueOf(String.valueOf(map.get("status"))));
            infoMap.put("nameFlag",Integer.valueOf(map.get("nameFlag").toString()));
            String path=map.get("user_sys_module_path") == null ? "" : map.get("user_sys_module_path").toString();
            String bg=map.get("user_sys_module_color") == null ? "" : map.get("user_sys_module_color").toString();
            String icon=map.get("user_sys_module_icon") == null ? "" : map.get("user_sys_module_icon").toString();
            infoMap.put("href",path);
            infoMap.put("bg",bg);
            infoMap.put("icon",icon);
            //判断内外网
            infoMap.put("isExtraNet",isExtraNet);
            infoMap.put("isDelete",Integer.valueOf((map.get("is_delete") == null ||
                    String.valueOf(map.get("is_delete")).equals(""))?"0":String.valueOf(map.get("is_delete"))));
            infoMap.put("isAvailable",1);
            infoMap.put("appParams",map.get("user_sys_module_keyvalues") == null ? "" : map.get("user_sys_module_keyvalues"));
            infoMap.put("token",map.get("user_sys_module_token") == null ? "" : map.get("user_sys_module_token"));
            infoMap.put("browser",map.get("browser") == null ? "" : map.get("browser"));
            infoMap.put("browserRoute",map.get("browserRoute") == null ? "" : map.get("browserRoute"));
            //超级管理员直接访问
            if("1".equals(userId) || "2".equals(userId)) {
                infoMap.put("isSelect","m-checked");
                infoMap.put("isAuthority","true");
            }else{
                infoMap.put("isSelect", map.get("isSelect") == null ? "" : map.get("isSelect"));
                //验证巡课和集控是否分配了设备和地点，否则不能访问
                if (("4".equals(moduleId) || "6".equals(moduleId))) {
                    if (!StringUtil.isEmpty(infoMap.get("isSelect").toString())) {
                        infoMap.put("isAuthority", getAuthorityMsg(moduleId, checkUserAuthority(userId)));
                    } else {
                        infoMap.put("isAuthority", "");
                    }
                }
            }
            //布局信息
            Map layoutMap = new HashMap();
            layoutMap.put("i",(map.get("user_sys_position_id")==null ||  map.get("user_sys_position_id").toString().equals(""))?
                    String.valueOf(userId+moduleId+"x"):String.valueOf(map.get("user_sys_position_id")));
            layoutMap.put("x",Integer.valueOf((map.get("x") == null ||
                    String.valueOf(map.get("x")).equals(""))?"0":String.valueOf(map.get("x"))));
            layoutMap.put("y",Integer.valueOf((map.get("y") == null ||
                    String.valueOf(map.get("y")).equals(""))?"0":String.valueOf(map.get("y"))));
            layoutMap.put("w",Integer.valueOf((map.get("w") == null ||
                    String.valueOf(map.get("w")).equals(""))?"6":String.valueOf(map.get("w"))));
            layoutMap.put("h",Integer.valueOf((map.get("h") == null ||
                    String.valueOf(map.get("h")).equals(""))?"2":String.valueOf(map.get("h"))));
            layoutMap.put("minH",Integer.valueOf((map.get("min_h") == null ||
                    String.valueOf(map.get("min_h")).equals(""))?"2":String.valueOf(map.get("min_h"))));
            layoutMap.put("minW",Integer.valueOf((map.get("min_w") == null ||
                    String.valueOf(map.get("min_w")).equals(""))?"2":String.valueOf(map.get("min_w"))));
            if("0".equals(userId)){
                layoutMap.put("isDraggable",0);
                layoutMap.put("isResizable",0);
            }else {
                layoutMap.put("isDraggable", Integer.valueOf((map.get("is_draggable") == null ||
                        String.valueOf(map.get("is_draggable")).equals("")) ? "1" : String.valueOf(map.get("is_draggable"))));
                layoutMap.put("isResizable", Integer.valueOf((map.get("is_resizable") == null ||
                        String.valueOf(map.get("is_resizable")).equals("")) ? "1" : String.valueOf(map.get("is_resizable"))));
            }
            moduleMap.put("info",infoMap);
            moduleMap.put("layout",layoutMap);
            re_value.add(moduleMap);
        }
        return re_value;
    }
    private static String getStringValue(String value){
        String result = "";
        if(StringUtils.isNotBlank(value)){
            result = value;
        }
        return result;
    }
    private boolean saveRole2sysModule(int roleId,List<Map<String,Object>> list){
        boolean re_value=false;
        int num = role2PermissionDao.searchRole2SysModuleNum(roleId);
        if (num==0) {
            re_value=role2PermissionDao.batchAdd(list);
        }
        return re_value;
    }
}
