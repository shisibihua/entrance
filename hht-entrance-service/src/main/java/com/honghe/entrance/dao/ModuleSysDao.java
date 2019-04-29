package com.honghe.entrance.dao;

import com.honghe.entrance.entity.Module;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 模块设置
 * @author caoqian
 * @date 20190221
 */
public interface ModuleSysDao {
    /**
     * 获取主题设置
     * @return
     */
    Map<String,String> getCurrentTheme();

    /**
     * 修改主题设置logo、背景
     * @param themeLogo
     * @param themeBackGround
     * @return
     */
    boolean updateThemeUrl(@Param("logo") String themeLogo,@Param("backGround") String themeBackGround);
    /**
     * 获取所有权限
     * @return com.honghe.entrance.entity.sysModule.Permissions
     * @author caoqian
     *
     */
//    List<Map<String,String>> getAllModules(@Param("moduleIds") String moduleIds);
    List<Map<String,Object>> getAllModules();
    /**
     * 筛选模块ids，不包括用户管理与运维平台
     * @return
     */
    List<String> selectModuleIds();
    /**
     * 获取公网信息
     * @return
     */
    Map<String,String> getExtraInfo();
    /**
     * 删除对应模块
     * @param moduleIdList
     * @return
     * @author caoqian
     */
//     boolean deleteSysModules(@Param("userSysModuleIds") String userSysModuleIds);
     boolean deleteSysModules(@Param("list") List<String> moduleIdList);
    /**
     * 根据模块名获取模块信息
     *
     * @param name 模块名
     * @Create wuxiao 2018/8/6
     * @Update wuxiao 2018/8/6
     */
     Map<String,Object> getModuleByName(@Param("name") String name);
    /**
     * 插入系统模块
     * name 模块中文名称
     * url 模块链接地址
     * keyValueString 模块参数字符串
     * token 模块token
     * bgColor 模块背景颜色
     * icon 模块图标
     * status 模块状态 int
     * enEnglish 英文名
     * version 版本
     * remark 备注
     * browser 浏览器标识
     * browserRoute 浏览器路径
     * nameFlag 用户名标识 int
     * @return long 新插入的模块id
     * @author caoqian
     * @date 20190225
     */
//     int addSysModules(@Param("paramMap") Map<String,Object> paramMap);
     boolean addSysModules(@Param("paramMap") Module module);

    /**
     * 获取默认模块权限，不包括用户、运维、巡课、设备控制
     * @return
     */
    List<String> selectDefaultModuleIds();

    /**
     * 获取最大的模块id
     * @return
     */
    int getMaxModuleId();

    /**
     * 更新模块状态
     * @param moduleId   模块id
     * @param status     模块状态
     * @return
     * @author caoqian
     * @date 20190227
     */
    boolean updateModuleStatus(@Param("moduleId") String moduleId,@Param("status") String status);

    /**
     * 检查重名模块
     * @param moduleId 模块id
     * @param name     模块名称
     * @return
     * @author caoqian
     * @date 20190227
     */
    int repeatName(@Param("moduleId") String moduleId,@Param("name") String name);

    /**
     * 更新系统模块
     * @param updModuleParams
     * @return
     * @author caoqian
     * @date 20190227
     */
    boolean updateSysModules(@Param("params") Map<String, Object> updModuleParams);

   /**
     * 重置主题设置
     * @return
     */
    boolean resetTheme();

    /**
     * 修改主题设置
     * @param themeParams
     * @return
     * @author caoqian
     * @date 20190301
     */
    boolean themeSet(@Param("params") Map<String, Object> themeParams);
}
