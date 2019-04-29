package com.honghe.entrance.service;
import com.alibaba.fastjson.JSONObject;
import com.honghe.entrance.common.pojo.model.Result;

import java.util.Map;

/**
 * <p>Description:</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 北京鸿合智能系统股份有限公司</p>
 *
 * @author hthwx
 * @date 2019/2/23
 */
public interface SysModuleService {
    /**
     * 获取所有带标记的模块权限
     * @param roleId 角色Id
     * @param ip 浏览器ip
     * @return com.honghe.entrance.entity.sysModule.Permissions
     * @Create Wangzy 2018/8/3 10:53
     * @Update caoqian
     */
    Result getMarkedModules(String roleId, String ip);

    /**
     * 获取用户有权限的模块信息
     * @param userId 用户id
     * @param searchKey 查询key
     * @param status 模块状态0 禁用 1启用 不填所有
     * @return java.util.List<com.honghe.entrance.entity.sysModule.UserSysPosition>
     * @Create Wangzy 2018/8/4 11:30
     * @Update caoqian
     */
    Result sysGetEnableModule(String userId, String searchKey, String status, String ip);

    /**
     * 删除模块
     * @param userId 用户id
     * @param moduleIds 模块id
     * @return boolean
     * @Create wuxiao 2018/8/4 12:36
     * @Update wuxiao 2018/8/4 12:36
     */
    Result deleteSysModules(String userId, String moduleIds);

    /**
     * 插入系统模块
     * @return long 新插入的模块id
     * @Create wuxiao 2018/8/4
     * @Update wuxiao 2018/8/4
     */
    Result addUserSysModules(String jsonString);
    /**
     * 更新模块状态
     * @param moduleId 模块id
     * @param status 模块状态
     * @return
     * @author caoqian
     * @date 20190227
     */
    Result updateModuleStatus(String moduleId, String status);

    /**
     * 更新单个系统模块
     * @param moduleStr
     * @return long 新插入的模块id
     * @author caoqian
     * @date 20190227
     */
    Result updateUserSysSoloModules(String moduleStr);

    /**
     * 移动模块时修改布局
     ** @param moduleStr         用户id
     * @return
     * @author caoqian
     * @date 20190227
     */
    Result updateModulePosition(String moduleStr);

    /**
     * 获取新增的模块id
     * @return
     */
    Result getMaxModuleId();
    /**
     * 获取公网信息
     * 供SysService调用
     * @return
     */
    Map<String,Object> getExtraInfo(String ip);

    /**
     * 添加用户与默认模块关系
     * @param roleId
     * @return
     * @author caoqian
     * @date 20190225
     */
    boolean addRole2DefaultSysModule(int roleId);

    /**
     * 获取当前主题数据
     * @return
     * @author caoqian
     * @date 20190227
     */
    Result getCurrentTheme(String ip);

    /**
     * 重置主题设置
     * @return
     * @author caoqian
     * @date 20190227
     */
    Result resetTheme();

    /**
     * 设置主题
     * @param userId
     * @param themeTitle
     * @param themeEnTitle
     * @param themeLogo
     * @param themeLoginBackGround
     * @param themeBackGround
     * @return
     * @author caoqian
     * @date 20190301
     */
    Result themeSet(String userId, String themeTitle, String themeEnTitle, String themeLogo,
                    String themeLoginBackGround, String themeBackGround);
}
