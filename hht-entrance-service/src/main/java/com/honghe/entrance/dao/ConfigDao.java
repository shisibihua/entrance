package com.honghe.entrance.dao;

import java.util.Map;

public interface ConfigDao {
    /**
     * 查询验证配置
     * @return
     */
    Map<String, String> getEntranceConfig();
}
