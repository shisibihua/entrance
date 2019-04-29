package com.honghe.entrance.service;

import com.honghe.entrance.common.pojo.model.Result;

import java.io.IOException;

/**
 * 导入设备业务逻辑
 * @author caoqian
 * @date 20190222
 */
public interface DeviceService {
    /**
     * 导入设备
     * @param fileName
     * @return
     */
    Result uploadFile(String fileName) throws IOException;
}
