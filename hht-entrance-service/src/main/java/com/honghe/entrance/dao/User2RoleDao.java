package com.honghe.entrance.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>Description:</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 北京鸿合智能系统股份有限公司</p>
 *
 * @author hthwx
 * @date 2019/2/22
 */
public interface User2RoleDao {
    /**
     * 通过用户id删除关联关系
     * @param userId 用户id
     * @return boolean
     * @Create Wangzy 2018/8/3 14:01
     * @Update Wangzy 2018/8/3 14:01
     */
    boolean deleteByUserId(@Param("userId") String userId);

    /**
     * 添加用户角色关联关系
     * @param sqlValues 角色insert语句的集合
     * @return java.util.Long
     * @Create Wangzy 2018/8/3 14:09
     * @Update Wangzy 2018/8/3 14:09
     */
    int add(@Param("values") String sqlValues);
    /**
     * 添加用户角色关联关系
     * @param userId 用户id
     * @param roleId 角色id
     * @return
     * @author caoqian
     * @date 20181221
     */
    boolean addUser2Role(@Param("userId") String userId,@Param("roleId") String roleId);
}
