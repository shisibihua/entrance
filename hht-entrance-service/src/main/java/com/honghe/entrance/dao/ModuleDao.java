package com.honghe.entrance.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 模块CRUD
 * @author caoqian
 * @@date 20190221
 */
public interface ModuleDao {
    //更新用户管理ip
    boolean updateUserModule(String path);
    /**
     * 更新资源平台访问ip
     * @return
     */
    //直播
    boolean updateResLiveModule(@Param("params") Map<String,String> params);
    //课件中心
    boolean updateResCourse(@Param("params") Map<String,String> params);
    //名师风采
    boolean updateResFamousTeacher(@Param("params") Map<String,String> params);
    //教研活动
    boolean updateResTeaching(@Param("params") Map<String,String> params);
    //优课视频
    boolean updateResVideo(@Param("params") Map<String,String> params);
    //个人中心
    boolean updateResPersonal(@Param("params") Map<String,String> params);
    //运维平台ip
    boolean updateYunWei(@Param("params") Map<String,String> params);
    //集控平台ip
    boolean updateDeviceControl(@Param("params") Map<String,String> params);
    //巡课平台ip
    boolean updatePatrol(@Param("params") Map<String,String> params);
    //云上资源
    boolean updateCloudRes(@Param("params") Map<String,String> params);

    /**
     * 获取新增模块列表
     * @return
     */
    List<Map<String,Object>> getNewAddModule();

    /**
     * 修改新增模块颜色及图标地址
     * @param color
     * @param icon
     * @param moduleId
     * @return
     */
    boolean updateNewModule(@Param("color") String color,@Param("icon") String icon,@Param("moduleId") int moduleId);
}
