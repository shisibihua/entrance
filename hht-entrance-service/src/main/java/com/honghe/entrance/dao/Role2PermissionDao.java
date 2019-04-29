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
 * @date 2019/2/22
 */
public interface Role2PermissionDao {
    /**
     * 删除角色权限关系信息
     *
     * @param roleId 角色ID
     * @return boolean
     */
    boolean delete(@Param("roleId") String roleId);

    /**
     * 根据模块id删除角色
     * @param moduleIdList
     * @return boolean
     * @author caoqian
     * @date 20190227
     */
//    boolean deleteBySysModuleId(@Param("sysModuleIds") String sysModuleIds);
    boolean deleteBySysModuleId(@Param("list") List<String> moduleIdList);

    /**
     * 添加角色权限关系信息
     *
     * @param data 所有数据
     * @return long
     */
    boolean add(@Param("data") Map<String, Object> data);

    /**
     * 角色权限查询
     * @param roleId 角色id
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.String>>
     * @Create Wangzy 2018/8/3 13:33
     * @Update Wangzy 2018/8/3 13:33
     */
    List<Map<String,String>> role2SysModuleSearch(@Param("roleId") String roleId);

    /**
     * 查询数量
     * @param roleId
     * @return
     * @author caoqian
     * @date 20190225
     */
    int searchRole2SysModuleNum(@Param("roleId") int roleId);

    /**
     * 批量添加数据
     * @param list
     * @return
     * @author caoqian
     * @date 20190225
     */
    boolean batchAdd(@Param("list") List<Map<String, Object>> list);

}
