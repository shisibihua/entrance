package com.honghe.entrance.service;


import com.honghe.entrance.common.pojo.model.Result;

/**
 * 导入地点业务逻辑
 * @author caoqian
 * @date 20190222
 */
public interface AreaService {
    /**
     * 导入地点
     * @param fileName
     * @return
     */
    Result importAreas(String fileName);
}
