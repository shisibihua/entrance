package com.honghe.entrance.entity;

/**
 * 模块实体
 * @author caoqian
 * @date 20190227
 */
public class Module {
    private int moduleId;
    //模块名称
    private String name;
    //模块访问路径
    private String url;
    //模块背景色
    private String bgColor;
    private String keyValueString;
    //图标
    private String icon;
    //token
    private String token;
    private String enName;
    private String version;
    private String remark;
    //模块状态，0：禁用;1:启用
    private int status;
    private String browser;
    private String browserRoute;
    private int nameFlag;

    public Module(int moduleId, String name, String url, String bgColor, String keyValueString,
                  String icon, String token, String enName, String version, String remark, int status,
                  String browser, String browserRoute, int nameFlag) {
        this.moduleId = moduleId;
        this.name = name;
        this.url = url;
        this.bgColor = bgColor;
        this.keyValueString = keyValueString;
        this.icon = icon;
        this.token = token;
        this.enName = enName;
        this.version = version;
        this.remark = remark;
        this.status = status;
        this.browser = browser;
        this.browserRoute = browserRoute;
        this.nameFlag = nameFlag;
    }

    public Module(String name, String url, String bgColor, String keyValueString, String icon, String token,
                  String enName, String version, String remark, int status, String browser, String browserRoute,
                  int nameFlag) {
        this.name = name;
        this.url = url;
        this.bgColor = bgColor;
        this.keyValueString = keyValueString;
        this.icon = icon;
        this.token = token;
        this.enName = enName;
        this.version = version;
        this.remark = remark;
        this.status = status;
        this.browser = browser;
        this.browserRoute = browserRoute;
        this.nameFlag = nameFlag;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    public String getKeyValueString() {
        return keyValueString;
    }

    public void setKeyValueString(String keyValueString) {
        this.keyValueString = keyValueString;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getBrowserRoute() {
        return browserRoute;
    }

    public void setBrowserRoute(String browserRoute) {
        this.browserRoute = browserRoute;
    }

    public int getNameFlag() {
        return nameFlag;
    }

    public void setNameFlag(int nameFlag) {
        this.nameFlag = nameFlag;
    }
}
