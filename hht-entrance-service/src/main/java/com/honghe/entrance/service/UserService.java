package com.honghe.entrance.service;

import com.honghe.entrance.common.pojo.model.Result;

import java.util.Map;

/**
 * 用户业务逻辑
 * @author caoqian
 * @date 20190221
 */
public interface UserService {
    /**
     * 导入用户
     * @param fileName
     * @return
     */
    Result importUsers(String fileName);

    /**
     * 添加用户信息
     */
    Map<String,Object> userAdd(String userName, String userRealName, String userMobile, String userType, String remark);

    /**
     * 保存用户
     * @param data 用户数据
     * @return
     */
    int addUser(Map<String, Object> data);

    /**
     * 用户登录
     * @param userName  用户名
     * @param userPwd    密码
     * @param userCode   授权码
     * @return
     */
    Result userCheck(String userName, String userPwd, String userCode);

    /**
     * 根据手机号查询爱学用户
     * @param mobile  手机号
     * @author caoqian
     * @return
     */
    Map<String,Object> getHiteCloudUserByMobile(String mobile);

    /**
     * 验证用户是否是首次登录
     * true：首次登录，需要手机验证码验证;false:已绑定到爱学
     * @param loginName   用户名
     * @author caoqian
     * @return
     */
    Result checkUserIsFirstLoginByName(String loginName);

    /**
     * 根据用户名验证是否修改过密码
     * true:已重置过密码；false:未重置密码
     * @param loginName 用户名
     * @return
     */
    Result checkUserIsUpdatePwd(String loginName);

    Result userSearch(String userId);

    /**
     * 修改用户信息
     * @param userId
     * @param userRealName
     * @param userName
     * @param userType
     * @param userMobile
     * @param userRemark
     * @return
     */
    Result userUpdate(String userId, String userRealName, String userName,
                      String userType, String userMobile, String userRemark);

    /**
     * 根据用户id删除用户，支持批量删除
     * @param userId 用户id，多个","分割
     * @return
     */
    Result userDelete(String userId);

    /**
     * 修改密码
     * @param userId
     * @param oldPassword
     * @param newPassword
     * @return
     */
    Result userChangePassword(String userId, String oldPassword, String newPassword);

    /**
     * 分页查询用户
     * @param pageSize
     * @param pageCount
     * @param userType
     * @param searchKey
     * @param userId
     * @return
     */
    Result userList(int pageSize, int pageCount, String userType, String searchKey, String userId);

    /**
     * 导出用户信息
     * @param userId
     * @param userType
     * @param searchKey
     * @return
     */
    Result exportUsers(String userId, String userType, String searchKey);

    /**
     * 重置密码
     * @param userId
     * @return
     */
    Result resetPwd(String userId);

    /**
     * 心跳
     * @return
     */
    Result keepAlive();

    /**
     * 根据userToken获取用户Id（单点登录使用）     *
     * @param userToken 用户令牌
     * @return
     * @author caoqian
     */
    Result searchUserInfoByToken(String userToken);

    /**
     * 根据token清空redis
     * @param token
     * @return
     */
    Result clearRedis(String token);
}
