package com.honghe.entrance.service.impl;
import com.honghe.entrance.common.pojo.model.Result;
import com.honghe.entrance.dao.*;
import com.honghe.entrance.service.RoleService;
import jodd.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Description:</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 北京鸿合智能系统股份有限公司</p>
 *
 * @author hthwx
 * @date 2019/2/22
 */
@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private Role2PermissionDao role2PermissionDao;
    @Autowired
    private User2RoleDao user2RoleDao;
    @Autowired
    private UserSysPositionDao userSysPositionDao;
    /**
     * 获取角色列表
     *
     * @param userId 用户id
     * @param token
     * @author: wuxiao
     * @create: 2018-08-02
     * @return Object
     */
    @Override
    public Result roleSearch(String userId, String token) throws IllegalArgumentException{
        Object re_value = null;
        Result result = new Result(Result.Code.Success.value());
        if(userId != null){
            re_value = roleDao.getRoleInfoByUserId(userId);
        } else {
            re_value = roleDao.findAllRole();
        }
        result.setResult(re_value);
        return result;
    }
    /**
     * 更新角色的权限
     *
     * @param roleId
     * @param authorityIds
     * @return
     * update by hwx20190223
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result updatePermission(String roleId, String authorityIds) {
        Result result = new Result(Result.Code.Success.value());
        if (StringUtil.isEmpty(roleId)){
           // return Response.PARAM_ERROR;//必填参数为空
            return new Result(Result.Code.ParamError.value(),"角色不能为空");
        }else {
            //删除原有权限
            if(!role2PermissionDao.delete(roleId)){
                System.out.println("删除原有权限失败或者原来没有权限");
            }
            //添加新权限
            if (authorityIds == null || authorityIds.equals("")){
               // return "1";//成功
                result.setResult("1");
            }else {
                //添加权限信息
                String [] authority = authorityIds.split(",");
                int totalCount = authority.length;
                Boolean allSuccess = true;
                for (int i = 0;i<totalCount;i++){
                    String authorityId = authority[i];
                    if (!authorityId.equals("")){
                        boolean permissionId = role2permissionAdd(roleId,authorityId);
                        if (!permissionId){
                            allSuccess = false;
                        }
                    }
                }
                if (allSuccess){
                    //修改用户与模块的布局
                    boolean updatePosition= false;
                    boolean updatePosition1= false;
                    //开启所有权限
                    updatePosition=userSysPositionDao.enableModuleAuthority(roleId);
                    //关闭权限
                    if(authorityIds != null && !",".equals(authorityIds) && authorityIds.endsWith(",")){
                        authorityIds=authorityIds.substring(0,authorityIds.lastIndexOf(","));
                        updatePosition1 = userSysPositionDao.disableModuleAuthority(roleId,authorityIds.split(","));
                    }
                    if(allSuccess && updatePosition) {
                        //成功
                        result.setResult("1");
                    }else{
                        //失败
                        result.setResult("-2");
                    }
                }else {
                    //添加角色权限失败，或者一部分失败
                    result.setResult("-2");
                }
            }
        }
        return result;
    }

    /**
     * 多用户分配多角色
     *
     * @param userIds
     * @param roleIds
     * @return
     * update by hwx20190223
     */
    @Override
    public Result usersRoleAllot(String userIds, String roleIds) {
        Result result = new Result(Result.Code.Success.value());
        if (StringUtil.isEmpty(userIds)) {
            // throw new CommandException(Response.PARAM_ERROR,"参数错误：用户id不能为空");
            result.setCode(Result.Code.ParamError.value());
            result.setOther("参数错误：用户ids不能为空");
        }
        //添加角色前先删除用户对应的角色
        boolean delU2R=user2RoleDao.deleteByUserId(userIds);
        boolean re_value = true;
        if(delU2R && roleIds != null && !roleIds.equals("")){
            List<String> roleIdList = Arrays.asList(roleIds.split(","));
            StringBuilder values = new StringBuilder();
            String[] userIdArr=userIds.split(",");
            for(String uId:userIdArr) {
                for (String roleId : roleIdList) {
                    String value = " (" + uId + "," + roleId + "),";
                    values.append(value);
                }
            }
            int valueSize = values.length();
            if (valueSize>3){
                int u2rId =user2RoleDao.add(values.substring(0,valueSize-1));
                if (u2rId <= 0){
                    re_value = false;//添加失败
                }
            }

        }
        result.setResult(re_value);
        return result;
    }
    /**
     * 添加角色权限对应关系
     * @param roleId
     * @param permissionId
     * @return
     * @throws IllegalArgumentException
     */
    public boolean role2permissionAdd(String roleId, String permissionId) throws IllegalArgumentException {
        Map<String, Object> pram = new HashMap<>();
        pram.put("roleId", roleId);
        pram.put("permissionId", permissionId);
        boolean re_value = role2PermissionDao.add(pram);
        return re_value;
    }

}
