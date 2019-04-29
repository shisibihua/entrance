package com.honghe.entrance.controller;

import com.alibaba.fastjson.JSONArray;
import com.honghe.entrance.common.pojo.model.Result;
import com.honghe.entrance.common.util.DateUtil;
import com.honghe.entrance.common.util.ParamUtil;
import com.honghe.entrance.hitecloud.DataSynchronizationService;
import com.honghe.entrance.service.*;
import com.honghe.entrance.servicemanager.ServiceAnnotation;
import com.honghe.entrance.servicemanager.ServiceManager;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * 入口服务控制层
 * @author caoqian
 * @date 20190221
 */
@CrossOrigin
@RestController("entranceCommand")
@ServiceAnnotation(ServiceName = "entrance")
public class EntranceController {
    @Autowired
    private EntranceConfigService entranceConfigService;
    @Autowired
    private UserService userService;
    @Autowired
    private AreaService areaService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DataSynchronizationService dataSynchronizationService;
    @Autowired
    private SysModuleService sysModuleService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private RoleService roleService;

    /**
     * 统一分发接口
     * @param request
     * @return
     * @author caoqian
     */
    @RequestMapping(value = "service/cloud/httpCommandService")
    public Result execute(HttpServletRequest request){
        Map requestMap = ParamUtil.getParams(request);
        return ServiceManager.getInstance().execute(requestMap);
    }

    /**
     * 查询验证配置
     * author caoqian
     */
    public Result searchEntranceConfig(){
        return entranceConfigService.getEntranceConfig();
    }

    /**
     * 导入用户信息
     * @param fileName 文件名
     * author caoqian
     */
    public Result initUsers(String fileName){
        return userService.importUsers(fileName);
    }

    /**
     * 导入地点信息
     * @param fileName 文件名
     * author caoqian
     */
    public Result initAreas(String fileName){
        return areaService.importAreas(fileName);
    }

    /**
     * 导入设备信息(调用集控设备导入接口)
     * @param fileName 文件名
     * author caoqian
     */
    public Result initDevices(String fileName) throws IOException{
        return deviceService.uploadFile(fileName);
    }

    /**
     * 登录接口
     * @param loginName 用户名
     * @param userPwd   密码
     * @param userCode 手机验证码
     * @author caoqian
     */
    public Result userCheck(String loginName, String userPwd ,String userCode){
        return userService.userCheck(loginName,userPwd,userCode);
    }
    /**
     * 验证用户是否是首次登录
     * true：首次登录，需要手机验证码验证;false:已绑定到爱学
     * @param loginName   用户名
     * @author caoqian
     * @return
     */
    public Result checkUserIsFirstLogin(String loginName){
        return userService.checkUserIsFirstLoginByName(loginName);
    }
    /**
     * 根据用户名验证是否修改过密码  true:已重置过密码；false:未重置密码
     * @param loginName 用户名
     * @return
     */
    public Result checkUserIsUpdatePwd(String loginName){
        return userService.checkUserIsUpdatePwd(loginName);
    }

    /**
     * 根据手机号获取爱学班班验证码
     * @param mobile  手机号
     * @author caoqian
     * @return
     */
    public Result getMobileCode(String mobile){
        Result result=new Result(Result.Code.Success.value());
        result.setResult(dataSynchronizationService.sendMobileCode(mobile));
        return result;
    }
    /**
     * 根据用户id查询用户信息
     * @param userId 用户id
     * @return
     */
    public Result userSearch(String userId) {
        return userService.userSearch(userId);
    }

    /**
     * 修改用户信息
     * @param userId               用户id
     * @param userRealName         用户真实名称
     * @param userName             用户名
     * @param userType             用户类型
     * @param userMobile           手机号
     * @param userRemark           备注
     * @author caoqian
     * @return
     */
    public Result userUpdate(String userId, String userRealName, String userName,String userType, String userMobile,
                             String userRemark) {
        return userService.userUpdate(userId,userRealName,userName,userType,userMobile,userRemark);
    }

    /**
     * 添加用户信息
     *
     * @param userName     用户名
     * @param userRealName 真实姓名
     * @param userMobile   手机号
     * @param userType     用户类型:老师;管理员
     * @param remark       备注
     * @return 用户Id
     */
    public Result userAdd(String userName,String userRealName, String userMobile,String userType,String remark){
        Map<String,Object> resultMap=userService.userAdd(userName,userRealName, userMobile, userType,remark);
        Result result=new Result(Result.Code.Success.value());
        if(resultMap.containsKey("addUserId")){
            result.setResult(Long.parseLong(String.valueOf(resultMap.get("addUserId"))));
        }else{
            result.setResult(0);
        }
        return result;
    }

    /**
     * 删除用户,支持批量用户
     * @param userId 用户id,多个","连接，如1,2,3
     * @return boolean 是否成功
     * @author caoqian
     */
    public Result userDelete(String userId){
        return userService.userDelete(userId);
    }

    /**
     * 修改密码
     *
     * @param userId
     * @param oldPassword
     * @param newPassword
     * @return
     */
    public Result userChangePassword(String userId, String oldPassword, String newPassword){
        return userService.userChangePassword(userId, oldPassword, newPassword);
    }

    /**
     * 分页查询用户列表
     * @param pageSize 页大小
     * @param pageCount 第几页
     * @param userType  用户类型
     * @param searchKey 查询关键字
     * @param userId 用户id
     * @author caoqian
     */
    public Result userList(String pageSize, String pageCount , String userType,String searchKey,String userId){
        return  userService.userList(Integer.parseInt(pageSize), Integer.parseInt(pageCount) , userType, searchKey,userId);
    }

    /**
     * 导入用户信息
     * @param fileName 文件名
     * @return 
     * @author caoqian
     */
    public Result importUsers(String fileName){
        return userService.importUsers(fileName);
    }

    /**
     * 导出用户信息
     * @param userId  用户id
     * @param userType  用户类型
     * @param searchKey 关键字
     * @return
     * @author caoqian
     */
    public Result exportUsers(String userId,String userType,String searchKey){
        return userService.exportUsers(userId,userType,searchKey);
    }

    /**
     * 根据userToken获取用户Id（单点登录使用）
     *
     * @param userToken 用户令牌
     * @return
     * @author caoqian
     */
    public Result userInfoByToken(String userToken){
        return userService.searchUserInfoByToken(userToken);
    }

    /**
     * 入口服务心跳接口
     * @return
     * @author caoqian
     */
    public Result keep_alive() {
        return userService.keepAlive();
    }

    /**
     * 添加爱学用户时，根据手机号拉取爱学班班用户信息
     * @param userMobile  手机号
     * @author caoqian
     * @return
     */
    public Result getUserByMobile(String userMobile){
        if(StringUtils.isEmpty(userMobile)){
            return new Result(Result.Code.ParamError.value(),"根据手机号拉取用户失败");
        }
        Result result=new Result(Result.Code.Success.value());
        result.setResult(userService.getHiteCloudUserByMobile(userMobile));
        return result;
    }

    /**
     * 重置密码
     * @param userId        用户id
     * @author caoqian
     * @return
     */
    public Result resetPwd(String userId){
        return userService.resetPwd(userId);
    }

    /**
     * 更新角色的权限
     *
     * @param roleId
     * @param authorityIds
     * @return
     * @update by hwx20190223
     */
    public Result updatePermission(String roleId,String authorityIds){
        return roleService.updatePermission(roleId,authorityIds);
    }

    /**
     * 获取所有带标记的模块权限
     * @param roleId 角色Id
     * @param ip  浏览器ip
     * @return com.honghe.entrance.entity.sysModule.Permissions
     * @Create Wangzy 2018/8/3 10:53
     * @Update Wangzy 2018/8/3 10:53
     */
    public Result getMarkedPermissions(String roleId,String ip){
        return sysModuleService.getMarkedModules(roleId,ip);
    }

    /**
     * 获取用户有权限的模块信息
     * @param userId 用户id
     * @param searchKey 查询key
     * @param status 模块状态0 禁用 1启用 不填所有
     * @param ip
     * @return java.util.List<com.honghe.entrance.entity.sysModule.UserSysPosition>
     * @Create Wangzy 2018/8/4 11:30
     * @Update caoqian 2018/12/4
     */
    public Result sysGetAllEnableModule(String userId, String searchKey, String status, String ip){
        return sysModuleService.sysGetEnableModule(userId, searchKey, status,ip);
    }

    /**
     * 删除模块
     * @param userId 用户id
     * @param moduleIds 模块id以，分割
     * @return
     */
    public Result deleteSysModules(String userId, String moduleIds){
        return sysModuleService.deleteSysModules(userId, moduleIds);
    }

    /**
     * 添加模块及用户与模块对应关系
     * @param moduleStr 模块信息字符串
     * @return
     */
    public Result sysAddModulePositionInfo(String moduleStr){
        return sysModuleService.addUserSysModules(moduleStr);
    }

    /**
     * 获取新增的模块id
     * @author caoqian
     * @return
     */
    public Result getMaxModuleId(){
        return sysModuleService.getMaxModuleId();
    }
    /**
     * 移动模块时修改布局
     * @param moduleStr         用户id
     * @author caiqian
     * @date 20190108
     * @return
     */
    public Result updateModulePosition(String moduleStr){
        return sysModuleService.updateModulePosition(moduleStr);
    }

    /**
     * 更新模块状态
     * @param userSysModuleIds 模块id
     * @param status 模块状态
     * @return
     * @author caoqian
     * @date 20190227
     */
    public Result updateModuleStatus(String userSysModuleIds, String status){
        return sysModuleService.updateModuleStatus(userSysModuleIds, status);
    }
    /**
     * 更新模块及用户与模块对应关系 单个json(临时）
     ** @param moduleStr         用户id
     * @return
     * @author caoqian
     * @date 20190227
     */
    public Result sysSetModulePositionInfoSolo(String moduleStr){
        return sysModuleService.updateUserSysSoloModules(moduleStr);
    }

    /**
     * 主题设置
     *
     * @param user_id
     * @param themeTitle
     * @param themeEnTitle
     * @param themeLogo
     * @param themeLoginBackGround
     * @param themeBackGround
     * @return
     * @author caoqian
     */
    public Result themeSet(String user_id, String themeTitle, String themeEnTitle, String themeLogo,
                           String themeLoginBackGround, String themeBackGround){
        return sysModuleService.themeSet(user_id, themeTitle, themeEnTitle, themeLogo, themeLoginBackGround, themeBackGround);
    }
    /**
     * 恢复入口背景、logo初始化设置
     * @return
     * @author caoqian
     * @date 20190121
     */
    public Result resetTheme(){
        return sysModuleService.resetTheme();
    }

    /**
     * 获取当前主题数据
     * @ip ip地址
     * @update caoqian
     * @date 20190228
     */
    public Result getCurrentThem(String ip){
        return sysModuleService.getCurrentTheme(ip);
    }

    /**
     * 添加根节点组织机构
     * @param orgName  学校名称
     * @param orgName  学校code码
     * @return
     * @author caoqian
     */
    public Result addRootOrg(String orgName,String code){
        return organizationService.saveRootOrg(orgName,code);
    }

    /**
     * 查询地区
     * @param parentId  父级id
     * @param level     级别，1:省;2:市;3：区/县；
     * @return
     * @author caoqian
     */
    public Result searchArea(String parentId, String level){
        JSONArray area = dataSynchronizationService.searchArea(Integer.parseInt(parentId),Integer.parseInt(level));
        Result result=new Result(Result.Code.Success.value());
        result.setResult(area);
        return result;
    }

    /**
     * 查询学校列表
     * @param areaId        地区id
     * @param schoolName    学校名，关键字，非必需
     * @return
     */
    public Result searchSchools(String areaId, String schoolName){
        JSONArray area = dataSynchronizationService.searchSchools(Integer.parseInt(areaId),schoolName);
        Result result=new Result(Result.Code.Success.value());
        result.setResult(area);
        return result;
    }

    /**
     * 删除根目录组织机构
     * @return
     * @author caoqian
     */
    public Result deleteRootOrg(){
        return organizationService.deleteRootOrg();
    }

    public Result clearRedis(String token){
        return userService.clearRedis(token);
    }
}
