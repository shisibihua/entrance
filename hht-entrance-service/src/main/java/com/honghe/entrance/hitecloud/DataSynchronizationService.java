package com.honghe.entrance.hitecloud;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 对接爱学班班数据同步业务层
 * @author caoqian
 * @date 2018/11/19
 */

public interface DataSynchronizationService {
    /**
     * 手机号、密码登录
     * @Author caoqian
     * @Mender
     * @param userName 手机号
     * @param userPwd 密码
     * @return 成功true 失败false
     */
    boolean login(String userName, String userPwd);

    /**
     * 手机验证码登陆
     * @Author caoqian
     * @Mender
     * @param mobile 手机号
     * @param code 验证码
     * @return 成功true 失败false
     */
    boolean checkMobileCode(String mobile, String code);

    /**
     * 发送手机验证码
     * @Author caoqian
     * @Mender
     * @param mobile 手机号
     * @return 成功 true 失败false
     */
    int sendMobileCode(String mobile);

    /**
     * 通过手机号查询用户信息
     * @Author caoqian
     * @Mender
     * @param mobile 手机号
     * @return 成功 true 失败false
     */
    JSONObject getUserByMobile(String mobile);

    /**
     * 注册用户到爱学班班
     * @param userName    用户名/手机号码
     * @param userPwd     用户密码
     * @param mobile      手机号码
     * @return
     */
    JSONObject registerUser(String userName, String userPwd, String mobile);

    JSONArray searchArea(int parentId, int level);

    JSONArray searchSchools(int areaId, String schoolName);
}
