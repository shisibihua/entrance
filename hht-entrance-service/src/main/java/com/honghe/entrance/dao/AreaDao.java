package com.honghe.entrance.dao;

import com.honghe.entrance.entity.DMArea;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 导入地点
 * @author caoqian
 *
 */
public interface AreaDao {
    /**
     * 根据地点名称查询地点信息
     * @param areaName  地点名称
     * @author caoqian
     * @date 2018/11/22
     * @return
     */
    Map<String,String> getAreaByName(@Param("areaName") String areaName);
    /**
     * 查询学校以下的地点
     * @author caoqian
     * @date 2018/11/26
     * @return
     */
    List<Map<String,String>> getAllAreas();

    /**
     * 根据地点id查询该地点下的所有地点ids
     * @return
     */
    List<Map<String,String>> getAllAreaByParentId(@Param("parentId") String parentAreaId);

    /**
     * 是否存在根级用户与地点关系
     * @return
     */
    int exitsRootUserRArea();

    /**
     * 获取学校信息
     * @return
     */
    DMArea getSchoolInfo();

    /**
     * 获取最大的地点id
     * @return
     */
    String getMaxAreaId();

    /**
     * 获取最大的地点code
     * @return
     */
    String getMaxAreaCode();

    /**
     * 根据地点id查询地点信息
     * @param areaId
     * @return
     */
    Map<String,String> getAreaById(@Param("areaId") String areaId);

    /**
     * 查询学校以下的所有地点id列表
     * @return
     */
    List<Map<String,String>> getAllAreaIds();

    /**
     * 保存地点信息
     * @param list
     * @return
     */
    boolean addAreas(@Param("list") List<DMArea> list);

    /**
     * 保存地点与超级管理员的关系
     * @return
     */
    boolean addUser2Area();

    /**
     * 增加地点与管理员的关系
     * @return
     */
    boolean addUser2AreaAdmin();

    /**
     * 保存地点信息
     * @param orgName
     * @param code
     * @return
     */
    boolean addArea(@Param("orgName") String orgName,@Param("code") String code);

    /**
     * 删除地点
     * @return
     */
    boolean delArea();

    /**
     * 删除地点与用户关系
     * @return
     */
    boolean delUser2Area();
}
