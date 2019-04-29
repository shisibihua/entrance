package com.honghe.entrance.service;

import com.honghe.entrance.common.pojo.model.Result;

import java.util.Map;

/**
 * 组织机构业务逻辑
 * @author caoqian
 * @date 20190225
 */
public interface OrganizationService {
    /**
     * 添加组织机构
     * @param orgName  机构名称
     * @param code     机构的code编码
     * @return
     */
    Result saveRootOrg(String orgName,String code);

    /**
     * 删除组织机构
     * @return
     */
    Result deleteRootOrg();

    /**
     * 查询学校
     * @return
     */
    Map<String,String> searchRootOrg();

    /**
     * 是否存在根目录组织机构
     * @return
     */
    boolean exitsRootOrg();
}
