package com.honghe.entrance.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Description:</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 北京鸿合智能系统股份有限公司</p>
 *
 * @author hthwx
 * @date 2019/2/23
 */
public final class Permissions {

    private String id = "";
    private String name = "";
    private String path = "";
    private String desc = "";
    private String pId = "";
    private List<Permissions> permissionList;
    private String isSelect = "";
    //图标
    private String icon;
    //判断内外网
    private boolean isExtraNet;

    public Permissions() {
        permissionList=new ArrayList<>();
    }

    public Permissions(String id, String name, String path, String desc, String pId,String icon) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.desc = desc;
        this.pId = pId;
        this.icon=icon;
        permissionList = new ArrayList<>();
    }

    public String getIsSelect() {
        return isSelect;
    }

    public void setIsSelect(String isSelect) {
        this.isSelect = isSelect;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public String getPid() {
        return pId;
    }

    public void setPid(String pId) {
        this.pId = pId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean getIsExtraNet() {
        return isExtraNet;
    }

    public void setIsExtraNet(boolean extraNet) {
        isExtraNet = extraNet;
    }

    public List<Permissions> getPermissions() {
        return permissionList;
    }

    public void setPermissions(List<Permissions> permissionList) {
        this.permissionList = permissionList;
    }
}