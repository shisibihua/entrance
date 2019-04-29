package com.honghe.entrance.entity;

/**
 * 用户信息
 * @author caoqian
 * @date 20181206
 */
public class User {
    private int userId;
    //用户名
    private String userName;
    //真实姓名
    private String userRealName;
    //手机号
    private String userMobile;
    //用户来源,0:爱学用户;1:本地用户
    private int userSource;
    //用户类型,17:教师；3：管理员
    private String userType;
    private int userStatus;
    //密码
    private String userPwd;
    //性别
    private int userGender;
    private String userUuid;
    private String userCode;
    private String userSalt;
    private String orgCode;
    //备注
    private String remark;
    //错误信息
    private String errorMsg;

    public User() {
    }

    public User(int userId, String userName, String userRealName, String userMobile, String userType,int userStatus,
                String userPwd, int userGender, int userSource, String userUuid, String userCode,String userSalt,
                String orgCode,String remark) {
        this.userId = userId;
        this.userName = userName;
        this.userRealName = userRealName;
        this.userMobile = userMobile;
        this.userType = userType;
        this.userStatus=userStatus;
        this.userPwd = userPwd;
        this.userGender = userGender;
        this.userSource = userSource;
        this.userUuid = userUuid;
        this.userCode=userCode;
        this.userSalt=userSalt;
        this.orgCode=orgCode;
        this.remark = remark;
    }

    public User(String userName, String userRealName, String userMobile, String userType, int userStatus,
                String userPwd,int userGender, int userSource, String userUuid,String userCode,String userSalt,
                String orgCode, String remark) {
        this.userName = userName;
        this.userRealName = userRealName;
        this.userMobile = userMobile;
        this.userType = userType;
        this.userStatus=userStatus;
        this.userPwd = userPwd;
        this.userGender = userGender;
        this.userSource = userSource;
        this.userUuid = userUuid;
        this.userCode=userCode;
        this.userSalt=userSalt;
        this.orgCode=orgCode;
        this.remark = remark;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserRealName() {
        return userRealName;
    }

    public void setUserRealName(String userRealName) {
        this.userRealName = userRealName;
    }

    public String getUserMobile() {
        return userMobile;
    }

    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public int getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(int userStatus) {
        this.userStatus = userStatus;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public int getUserGender() {
        return userGender;
    }

    public void setUserGender(int userGender) {
        this.userGender = userGender;
    }

    public int getUserSource() {
        return userSource;
    }

    public void setUserSource(int userSource) {
        this.userSource = userSource;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getUserSalt() {
        return userSalt;
    }

    public void setUserSalt(String userSalt) {
        this.userSalt = userSalt;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
