package com.honghe.entrance.entity;

import java.io.Serializable;

/**
 * 地点
 * @author  caoqian
 * @date 2018/11/26
 */
public class DMArea implements Serializable {
    private int areaId;

    private String areaUuid;

    private String areaName;

    private String areaParentId;

    private String areaParentName;

    private Integer provinceId;

    private Integer cityId;

    private Integer countyId;

    private String areaParentPath;

    private String areaCode;

    private Integer areaLevel;

    private Integer areaType;

    private String areaRemark;

    private int scheduleId;

    private String errorMsg;

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getAreaParentId() {
        return areaParentId;
    }

    public void setAreaParentId(String areaParentId) {
        this.areaParentId = areaParentId;
    }

    public Integer getAreaLevel() {
        return areaLevel;
    }

    public String getAreaRemark() {
        return areaRemark;
    }

    public void setAreaRemark(String areaRemark) {
        this.areaRemark = areaRemark;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    private static final long serialVersionUID = 1L;

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public String getAreaUuid() {
        return areaUuid;
    }

    public void setAreaUuid(String areaUuid) {
        this.areaUuid = areaUuid == null ? null : areaUuid.trim();
    }

    public String getAreaParentPath() {
        return areaParentPath;
    }

    public void setAreaParentPath(String areaParentPath) {
        this.areaParentPath = areaParentPath;
    }

    public void setAreaLevel(Integer areaLevel) {
        this.areaLevel = areaLevel;
    }

    public Integer getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Integer provinceId) {
        this.provinceId = provinceId;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public Integer getCountyId() {
        return countyId;
    }

    public void setCountyId(Integer countyId) {
        this.countyId = countyId;
    }

    public Integer getAreaType() {
        return areaType;
    }

    public void setAreaType(Integer areaType) {
        this.areaType = areaType;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getAreaParentName() {
        return areaParentName;
    }

    public void setAreaParentName(String areaParentName) {
        this.areaParentName = areaParentName;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}