package com.honghe.entrance.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowj on 2017/4/13.
 */
public enum UserTypeEnum {

    /**
     * 用户身份 教师职务
     */
    SUPER_USERTYPE_ADMIN("0"),//超级管理员(引导项添加的管理员)身份
    USERTYPE_TEACHER("17"),//老师身份
    USERTYPE_MANAGER("3"),//普通管理员
    USERTYPE_STUDENT("18"),//学生身份
    USERTYPE_PARENT("19"),//家长身份
    DUTYTYPE_HEADMASTER("7"),//学校领导
    DUTYTYPE_GRADEMASTER("15"),//年级组长
    DUTYTYPE_CLASSMASTER("16"),//班主任
    USERTYPE_MANAGER_NAME("管理员"),
    USERTYPE_TEACHER_NAME("普通用户");
    private String type;
    private static final Map<String, UserTypeEnum> map = new HashMap<>();

    UserTypeEnum(String type) {
        this.type = type;
    }
    static {
        for (UserTypeEnum userType : UserTypeEnum.values()) {
            map.put(userType.getType(), userType);
        }
    }

    public static UserTypeEnum getUsertype(String type) {
        return map.get(type);
    }

    public String getType() {
        return type;
    }


}