package com.honghe.entrance.dao;

import com.honghe.entrance.common.pojo.model.Result;
import com.honghe.entrance.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用户
 * @author caoqian
 * @date 20190221
 */
public interface UserDao {
    /**
     * 查询用户是否存在
     * @param params
     * @return
     */
    int isExits(@Param("userInfo") Map<String,Object> params);

    /**
     * 添加用户
     * @param user
     * @return
     */
    int add(@Param("userInfo")User user);

    /**
     * 保存用户与根节点组织机构关系
     * @param userId
     * @return
     */
    int addCampus2User(@Param("userId") int userId);

    /**
     * 根据用户名查询用户信息
     * @param name 用户名
     * @return
     */
    List<Map<String,String>> findUserInfo(@Param("name") String name);

    /**
     * 同步本地用户到爱学班班
     * @return
     * @author caoqian
     */
    boolean synUserData(@Param("userId") int userId,@Param("userRealName") String userRealName);

    /**
     * 根据用户id查询用户信息
     * @param userId
     * @return
     */
    Map<String,String> findUserById(@Param("userId") int userId);

    /**
     * 根据用户名查询用户信息
     * @param loginName
     * @return
     */
    Map<String,Object> searchUserByName(@Param("userName") String loginName);

    /**
     * 更新用户信息
     * @param map
     * @return
     */
    boolean updateUser(@Param("userInfo") Map<String, Object> map);

    /**
     * 删除用户
     * @param userIds
     * @return
     */
    boolean deleteUsers(@Param("userIds") String[] userIds);

    /**
     * 删除用户与角色关系
     * @param userIds
     * @return
     */
    boolean deleteUser2Role(@Param("userIds") String[] userIds);

    /**
     * 修改用户密码
     * @param userId
     * @param salt
     * @param newPwd
     * @return
     */
    boolean updateUserPassword(@Param("userId") int userId,@Param("salt") String salt,@Param("newPwd") String newPwd);

    /**
     * 修改登录次数
     * @param userId
     * @return
     */
    boolean updateLoginCount(@Param("userId") int userId);

    /**
     * 根据查询条件查询用户数量
     * @param userId
     * @param searchKey
     * @return
     */
    int searchUserCount(@Param("userId") String userId,@Param("searchKey") String searchKey);

    /**
     * 分页查询用户信息
     * @param userId
     * @param searchKey
     * @param start
     * @param pageSize
     * @param flag true:分页查询；false：不分页查询
     * @return
     */
    List<Map<String,String>> userPageList(@Param("userId") String userId,@Param("searchKey") String searchKey,
                                          @Param("start") int start,@Param("pageSize") int pageSize,@Param("flag") boolean flag);

    /**
     * 重置密码
     * @param salt     盐值
     * @param userPwd  密码
     * @param userId   用户id
     * @return
     */
    boolean resetPwd(@Param("salt") String salt,@Param("userPwd") String userPwd,@Param("userId")String userId);
}
