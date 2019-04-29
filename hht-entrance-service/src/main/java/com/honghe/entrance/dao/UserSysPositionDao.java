package com.honghe.entrance.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>Description:</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 北京鸿合智能系统股份有限公司</p>
 *
 * @author hthwx
 * @date 2019/2/23
 */
public interface UserSysPositionDao {
    /**
     * 关闭权限
     * @param roleId
     * add by hwx20190223
     * @return
     */
    boolean disableModuleAuthority(@Param("roleId") String roleId,@Param("authorityIds") String[] authorityIds);

    /**
     * 开启权限
     * @param roleId
     * @return
     * @author caoqian
     * @date 20190302
     */
    boolean enableModuleAuthority(String roleId);

    /**
     * 根据用户id获取用户权限模块
     * @param userId 用户id
     * @param searchKey 查询key
     * @param status 模块状态0 禁用 1启用 不填所有
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.String>>
     * @Update caoqian
     *
     */
    List<Map<String, Object>> sysGetAllEnableModule(@Param("userId") String userId,@Param("searchKey") String searchKey,
                                                    @Param("status") String status);
    /**
     * 删除用户对应模块
     * @param moduleIdList
     * @return
     * @author caoqian
     */
//     boolean deleteSysModules(String userSysModuleIds);
     boolean deleteSysModules(@Param("list") List<String> moduleIdList);
    /**
     * 根据模块id和用户与模块关系id获取模块信息
     *
     * @param userId 用户id
     * @param userSysModuleId 模块id
     * @author caoqian
     * @date 20190227
     */
    Map<String,Object> getUser2ModuleByName(@Param("userId") int userId, @Param("userSysModuleId") int userSysModuleId);
    /**
     * 插入用户模块对应关系
     * @param list
     * @author caoqian
     * @date 2018/11/18
     * @return
     */
    boolean addUser2Position(@Param("list") List<Map<String,Object>> list);
    /**
     * 根据用户id删除模块位置关系
     * @param userId 用户id
     * @return boolean
     * @Create Wangzy 2018/8/7 14:02
     * @Update Wangzy 2018/8/7 14:02
     */
    boolean deleteSysModulesByUserId(@Param("userId") String userId);

    /**
     * 查询默认模块布局,(未登录显示模块/学校新增模块)
     * @return
     */
    List<Map<String, Object>> getDefaultPosition();

    /**
     * 删除用户与模块关联
     * @param userIds
     * @return
     */
    boolean deletePositions(@Param("userIds") String[] userIds);

    /**
     * 更新用户模块对应关系
     * @param params
     * @return
     * @author caoqian
     * @date 20180927
     */
    boolean updateUser2PerModules(@Param("params") Map<String,Object> params);

}
