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
public interface RoleDao {

    /**
     * 通过userId获取角色信息
     *
     * @param param 用户id
     * @return
     */
    List<Map<String,String>> getRoleInfoByUserId(@Param("userId") String param);

    /**
     * 查询所有角色
     *
     * @return 结果集
     */
    List<Map<String, String>> findAllRole();

    /**
     * 添加新角色信息
     *
     * @param roleId 权限id
     * @param roleName  用户名称
     * @param roleInit 初始化标识符
     * @return long
     */
    int addRole(@Param("roleId") String roleId,@Param("roleName") String roleName,@Param("roleInit") String roleInit);

    /**
     * 查询角色是否存在
     *
     * @param data 所有数据
     * @return boolean
     */
    boolean isRoleExist(@Param("data") Map<String, Object> data);

    /**
     * 删除角色信息
     *
     * @param roleId 角色ID
     * @return boolean
     */
    int delete(@Param("roleId") String roleId);

    /**
     * 修改角色信息
     *
     * @param data
     * @return
     */
    boolean updateRoleByRoleId(@Param("data") Map<String, Object> data);

    /**
     * 查询角色是否分配给用户
     */
    int user2RoleById(@Param("roleId") String roleId);


    /**
     * 删除角色
     * @param userIds
     * @return
     * @author caoqian
     */
    boolean deleteRoles(@Param("userIds")String[] userIds);

    /**
     * 删除用户与模块关系
     * @param userIds
     * @return
     * @author caoqian
     */
    boolean deleteRole2Modules(@Param("userIds")String[] userIds);
}
