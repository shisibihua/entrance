package com.honghe.entrance.dao;

import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 组织机构
 * @author caoqian
 * @date 20190225
 */
public interface OrganizationDao {
    /**
     * 添加组织机构
     * @param orgName
     * @return
     */
    boolean addCampus(@Param("orgName") String orgName);

    /**
     * 增加地点组
     * @param orgName
     * @return
     */
    boolean addGroup(String orgName);

    /**
     * 删除组织机构
     * @return
     */
    boolean delCampus();

    /**
     * 删除机构组
     * @return
     */
    boolean delGroup();

    /**
     * 查询学校信息
     * @return
     */
    Map<String,String> searchSchool();

    /**
     * 查询根目录组织机构的数量
     * @return
     */
    int searchCampusNum();

    /**
     * 删除组织机构与用户的关系
     * @param userIds
     * @return
     */
    boolean deleteCampus2Users(@Param("userIds") String[] userIds);
}
