package com.honghe.entrance.hitecloud.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.honghe.entrance.hitecloud.DataSynchronizationService;
import com.honghe.entrance.service.OrganizationService;
import com.honghe.entrance.util.HttpServiceUtil;
import com.honghe.entrance.util.SecuritySettings;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DataSynchronizationServiceImpl implements DataSynchronizationService {
    private static final int RETRY_COUNT = 3;
    private Logger logger = LoggerFactory.getLogger(DataSynchronizationServiceImpl.class);

    private SecuritySettings securitySettings = new SecuritySettings();

    private static String HITE_CLOUD_URL="http://www.hitecloud.cn/";

    @Autowired
    private OrganizationService organizationService;

    @Override
    public boolean login(String userName, String userPwd) {
        try {
            Map<String,String> paramMap = new HashMap<>();
            paramMap.put("username",userName);
            paramMap.put("password",userPwd);
            String param = securitySettings.initPostParams(paramMap);
            String url = HITE_CLOUD_URL + "api/login/login" ;
            logger.debug("请求爱学班班链接为：" + url);
            JSONObject resultJson= HttpServiceUtil.httpPostOutTimeAndRetry(url,param,RETRY_COUNT);
            logger.debug(resultJson.toString());
            return parseResult(resultJson)!=null;
        } catch (Exception e) {
            logger.error("获取班班学校数据或数据解析失败",e);
            return false;
        }

    }

    @Override
    public boolean checkMobileCode(String mobile, String code) {
        try {
            Map<String,String> paramMap = new HashMap<>();
            paramMap.put("mobile",mobile);
            paramMap.put("code",code);
            String param = securitySettings.initPostParams(paramMap);
            String url = HITE_CLOUD_URL + "api/userstep/checkmobilecode";
            logger.debug("请求爱学班班链接为：" + url);
            JSONObject result= HttpServiceUtil.httpPostOutTimeAndRetry(url,param,RETRY_COUNT);
            logger.debug(result.toString());
            return parseResult(result)!=null;
        } catch (Exception e) {
            logger.error("获取班班学校数据或数据解析失败",e);
            return false;
        }
    }


    @Override
    public int sendMobileCode(String mobile) {
        try {
            Map<String,String> paramMap = new HashMap<>();
            paramMap.put("mobile",mobile);
            String url2 = HITE_CLOUD_URL + "api/userstep/sendmcode";
            String param2 = securitySettings.initPostParams(paramMap);
            logger.debug("请求爱学班班链接为：" + url2);
            JSONObject result2=HttpServiceUtil.httpPostOutTimeAndRetry(url2,param2,RETRY_COUNT);
            logger.debug(result2.toString());
            if(!"".equals(parseResult(result2))){
                //发送失败
                return 2;
            }
            return 0;

        } catch (Exception e) {
            logger.error("获取班班学校数据或数据解析失败",e);
            return 2;
        }
    }


    @Override
    public JSONObject getUserByMobile(String mobile){
        JSONObject jsonObject = new JSONObject();
        try {
            Map<String,String> paramMap = new HashMap<>();
            paramMap.put("mobile",mobile);
            String param = securitySettings.initGetParams(paramMap);
            String url = HITE_CLOUD_URL + "api/user/Userinfobymobile" + param;
            logger.debug("请求爱学班班链接为：" + url);
            JSONObject result=HttpServiceUtil.httpGetOutTimeAndRetry(url,null,RETRY_COUNT);
            logger.debug(result.toString());
            jsonObject =  (JSONObject)parseResult(result);

        } catch (Exception e) {
            logger.error("获取班班学校数据或数据解析失败",e);
        }
        return jsonObject;
    }

    @Override
    public JSONObject registerUser(String userName, String userPwd, String mobile){
        JSONObject jsonObject = new JSONObject();
        try {
            Map<String,String> paramMap = new HashMap<>();
            paramMap.put("username",mobile);
            paramMap.put("userpassword",userPwd);
            paramMap.put("usertype","1");
            paramMap.put("realname",userName);
            Map<String,String> schoolInfo=organizationService.searchRootOrg();
            if(schoolInfo!=null && !schoolInfo.isEmpty()){
                paramMap.put("schoolid",schoolInfo.get("id"));
                paramMap.put("schoolname",schoolInfo.get("name"));
            }
            String param = securitySettings.initPostParams(paramMap);
            String url = HITE_CLOUD_URL + "api/loginapp/reg";
            logger.debug("请求爱学班班链接为：" + url);
            JSONObject result=HttpServiceUtil.httpPostOutTimeAndRetry(url,param,RETRY_COUNT);
            logger.debug(result.toString());
            jsonObject =  (JSONObject)parseResult(result);
        } catch (Exception e) {
            logger.error("获取班班学校数据或数据解析失败",e);
        }
        return jsonObject;
    }
    private static final String SEARCH_AREA_URL="api/group/citys";
    private static final String SEARCH_SCHOOL_URL="api/group/schoolitems";

    /**
     * 查询地区
     * @param parentId  父级id
     * @param level     级别，1:省;2:市;3：区/县；4:学校
     * @return
     */
    @Override
    public JSONArray searchArea(int parentId, int level){
        Map<String,String> params=new HashMap<>();
        params.put("pid",String.valueOf(parentId));
        params.put("level",String.valueOf(level));
        String areaUrl=HITE_CLOUD_URL+SEARCH_AREA_URL+securitySettings.initGetParams(params);
        JSONArray areaResult = new JSONArray();
        JSONObject resultJson=null;
        try {
            resultJson = HttpServiceUtil.httpGetOutTimeAndRetry(areaUrl, null, RETRY_COUNT);
        }catch (Exception e){
            ;
        }
        if (resultJson != null && !resultJson.isEmpty() && Boolean.parseBoolean(resultJson.get("success").toString())) {
            String data = resultJson.get("data").toString();
            if (!StringUtil.isEmpty(data)) {
                JSONArray result = JSONArray.parseArray(data.replace("area_id", "id"));
                for (Object o : result) {
                    JSONObject json = JSONObject.parseObject(String.valueOf(o));
                    json.put("code", json.get("id"));
                    json.put("id", Integer.parseInt(String.valueOf(json.get("id"))));
                    json.put("parentId", parentId);
                    json.put("level", level);
                    areaResult.add(json);
                }
            }
        }
        return areaResult;
    }

    public static final int SCHOOL_LEVEL=4;
    /**
     * 查询学校列表
     * @param areaId        地区id
     * @param schoolName    学校名，关键字，非必需
     * @return
     */
    @Override
    public JSONArray searchSchools(int areaId, String schoolName){
        Map<String,String> params=new HashMap<>();
        params.put("area",String.valueOf(areaId));
        if(!StringUtil.isEmpty(schoolName)) {
            params.put("key", schoolName);
        }
        String areaUrl=HITE_CLOUD_URL+SEARCH_SCHOOL_URL+securitySettings.initGetParams(params);
        JSONObject resultJson=HttpServiceUtil.httpGetOutTimeAndRetry(areaUrl,null,RETRY_COUNT);
        JSONArray areaResult=new JSONArray();
        if(resultJson!=null && !resultJson.isEmpty() && Boolean.parseBoolean(resultJson.get("success").toString())){
            String data=resultJson.get("data").toString();
            if(!StringUtil.isEmpty(data)) {
                JSONArray result = JSONArray.parseArray(data);
                for(Object o:result){
                    JSONObject json= JSONObject.parseObject(String.valueOf(o));
                    JSONObject schoolDataJson=new JSONObject();
                    schoolDataJson.put("code",json.get("school_id"));
                    schoolDataJson.put("id",Integer.parseInt(String.valueOf(json.get("school_id"))));
                    schoolDataJson.put("parendId",areaId);
                    schoolDataJson.put("level",SCHOOL_LEVEL);
                    schoolDataJson.put("name",json.get("s_name").toString());
                    areaResult.add(schoolDataJson);
                }

            }
        }
        return areaResult;
    }
    /**
     * 公共处理返回结果方法
     * @param result 爱学班班返回的结果
     * @return 处理后的结果
     */
    private Object parseResult(JSONObject result){
        if(result != null && !result.isEmpty()){
            if((boolean)result.get("success")){
                return result.get("data");
            }
        }
        return null;
    }
}
