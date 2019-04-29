package com.honghe.entrance.service;

import com.honghe.entrance.common.pojo.model.Result;

/**
 * <p>Description:</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 北京鸿合智能系统股份有限公司</p>
 *
 * @author hthwx
 * @date 2019/2/22
 */
public interface RoleService {
    /**
     * 获取角色列表
     * @param userId 用户id
     * @param token
     * @return
     */
     Result roleSearch(String userId, String token);

    /**
     * 更新角色的权限
     * @param roleId
     * @param authorityIds
     * @return
     */
     Result updatePermission(String roleId,String authorityIds);

    /**
     * 多用户分配多角色
     * @param userIds
     * @param roleIds
     * @Update caoqian 2018/11/26
     * @return
     */
     Result usersRoleAllot(String userIds,String roleIds);
}
