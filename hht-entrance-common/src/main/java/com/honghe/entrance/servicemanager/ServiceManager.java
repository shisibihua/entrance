package com.honghe.entrance.servicemanager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.honghe.entrance.common.pojo.model.Result;
import com.honghe.entrance.common.util.HttpUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.ibatis.javassist.*;
import org.apache.ibatis.javassist.bytecode.CodeAttribute;
import org.apache.ibatis.javassist.bytecode.LocalVariableAttribute;
import org.apache.ibatis.javassist.bytecode.MethodInfo;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * 服务池
 *
 * @auther yuk
 * @Time 2018/2/9 9:00
 */
public class ServiceManager {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(ServiceManager.class);
    private Map<String,Object> serviceMap;
    private Map<String,Map<String,Map<String,Class<?>>>> serviceMethodInfoMap;
    private static volatile ServiceManager serviceManager;

    public static ServiceManager getInstance() {
        if (serviceManager == null) {
            synchronized (ServiceManager.class) {
                if (serviceManager == null) {
                    serviceManager = new ServiceManager();
                }
            }
        }
        return serviceManager;
    }

    private ServiceManager() {
        serviceMap = new HashMap<>();
        serviceMethodInfoMap = new HashMap<>();
    }

    public void register(String serviceName,Object service){
        serviceMap.put(serviceName,service);
        loadMethodParamsInfo(serviceName,service.getClass());

    }

    public Result execute(Map<String,Object> params){

        String serviceName = String.valueOf(params.get("cmd"));
        if(serviceMap.containsKey(serviceName)){
            return executeApi(params);
        }
        return null;
    }

    /**
     *
     * @param jsonObject 参数
     * @param url 流地址
     * @return
     */
    public Result executeDevice(JSONObject jsonObject,String url){
        return HttpUtils.httpPostJson(url,jsonObject);
    }

    private Result executeHttp(Map<String,Object> params){
        return  HttpUtils.httpPostJson(params.get("url").toString(),JSONObject.parseObject(JSON.toJSONString(params)));
    }


    /**
     * 发送get请求
     * @param url 请求链接
     * @return
     */
    public Result executeGet(String url){
        Result result = new Result();
        result.setResult(HttpUtils.httpGet(url));
        return result ;
    }
    /**
     * 发送get请求
     * @param url 请求链接
     * @return
     */
    public Result executeGet(String url,Object time,int retryCount){
        Result result = new Result();
        result.setResult(HttpUtils.httpGetOutTimeAndRetry(url,time,retryCount));
        return result ;
    }

    /**
     * 发送post请求
     * @param url 请求链接
     * @return
     */
    public Result executePost(String url,String param){
        Result result = new Result();
        result.setResult(HttpUtils.httpPostJson(url,param));
        return result ;
    }
    /**
     * 发送post请求
     * @param url 请求链接
     * @return
     */
    public Result executePost(String url,String param,int retryCount){
        Result result = new Result();
        result.setResult(HttpUtils.httpPostOutTimeAndRetry(url,param,retryCount));
        return result ;
    }


    private Result executeApi(Map<String,Object> params){
        Result result = new Result();
        String cmd = params.get("cmd").toString().trim();
        String cmdOp = params.get("cmd_op").toString().trim();
        result.setType(cmdOp + "_ACK");
        Object obj = serviceMap.get(cmd);
        if(!serviceMethodInfoMap.containsKey(cmd) || !serviceMethodInfoMap.get(cmd).containsKey(cmdOp)){
            result.setCode(Result.Code.NoSuchMethod.value());
            result.setOther("NoSuchMethod");
            return result;
        }
        Map paramsInstance = getMethodParamsInstance(obj, serviceMethodInfoMap.get(cmd).get(cmdOp), params);
        if(paramsInstance.isEmpty()) {
            result.setCode(Result.Code.ParamError.value());
            result.setOther("ParamError");
            return result;
        } else {
            try {
                Class[] e = (Class[])paramsInstance.get("methodType");
                Object[] methodValueArray = (Object[])paramsInstance.get("methodValue");
                if(e.length != methodValueArray.length) {
                    result.setCode(Result.Code.ParamError.value());
                    result.setOther("ParamError");
                    return result;
                }
                Method method = obj.getClass().getDeclaredMethod(cmdOp, e);
                Result resultObj = (Result) method.invoke(obj, methodValueArray);
                resultObj.setType(cmdOp + "_ACK");
                return resultObj;
            } catch (IllegalArgumentException e1) {
                logger.error("params is " + params, e1);
                result.setCode(Result.Code.UnKnowError.value());
                result.setOther("UnKnowError");
                return result;
            } catch (Exception e2) {
                logger.error("params is " + params, e2);
                result.setCode(Result.Code.UnKnowError.value());
                result.setOther("UnKnowError");
                return result;
            }
        }
    }

    private void loadMethodParamsInfo(String serviceName,Class _class) {
        Map<String,Map<String,Class<?>>> allMethodInfoMap = new HashMap<>();
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));

        try {
            CtClass e = pool.get(_class.getName());
            CtMethod[] ctMethodArray = e.getDeclaredMethods();
            CtMethod[] keySet = ctMethodArray;
            int methodSize = ctMethodArray.length;

            for(int key = 0; key < methodSize; key++) {
                CtMethod valueList = keySet[key];
                boolean pos = !Modifier.isStatic(valueList.getModifiers());
                if(pos) {
                    MethodInfo methodInfo = valueList.getMethodInfo();
                    LinkedHashMap methodInfoMap = new LinkedHashMap();
                    CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
                    LocalVariableAttribute attr = (LocalVariableAttribute)codeAttribute.getAttribute("LocalVariableTable");
                    int offset = getIndex(attr);
                    CtClass[] ctClasses = valueList.getParameterTypes();

                    int i;
                    for(i = 0; i < ctClasses.length; ++i) {
                        String vName = attr.variableName(i + offset);
                        methodInfoMap.put(vName, getMethodType(ctClasses[i]));
                    }
                    allMethodInfoMap.put(valueList.getName(), methodInfoMap);
                }
            }
            serviceMethodInfoMap.put(serviceName,allMethodInfoMap);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private static int getIndex(LocalVariableAttribute attr) {
        int re_value = -1;

        for(int i = 0; i < attr.tableLength(); ++i) {
            if(attr.variableName(i).toString().equals("this")) {
                re_value = i + 1;
                break;
            }
        }

        return re_value;
    }


    private Map<String, Object> getMethodParamsInstance(Object obj, Map<String, Class<?>> methodInfoMap, Map<String, Object> requestParams) {
        HashMap result = new HashMap();
        Object[] objectArray = new Object[methodInfoMap.size()];
        Class[] classArray = new Class[methodInfoMap.size()];
        if(methodInfoMap.size() == 1) {
            String paramName = (String)methodInfoMap.keySet().iterator().next();
            Class methodType = (Class)methodInfoMap.get(paramName);

            try {
                if(!requestParams.containsKey(paramName)){
                    return result;
                }
                classArray[0] = methodType;
                if(Map.class.isAssignableFrom(methodType)) {
                    objectArray[0] = requestParams;
                } else if(methodType.isArray()) {
                    if(methodType.getCanonicalName().startsWith("java.")) {
                        objectArray[0] = ConvertUtils.convert(requestParams.get(paramName), methodType);
                    }
                } else if(methodType.getName().startsWith("java.")) {
                    objectArray[0] = ConvertUtils.convert(requestParams.get(paramName), methodType);
                } else {
                    BeanUtils.populate(obj, requestParams);
                    objectArray[0] = obj;
                }
            } catch (Exception e) {
                return result;
            }

            result.put("methodValue", objectArray);
            result.put("methodType", classArray);
            return result;
        } else {
            String[] keyArray = (String[])methodInfoMap.keySet().toArray(new String[methodInfoMap.size()]);

            for(int i = 0; i < keyArray.length; ++i) {
                String paramName = keyArray[i];
                Class paramType = (Class)methodInfoMap.get(paramName);
                classArray[i] = paramType;

                try {
                    objectArray[i] = ConvertUtils.convert(requestParams.get(paramName), paramType);
                } catch (Exception var12) {
                }
            }

            result.put("methodValue", objectArray);
            result.put("methodType", classArray);
            return result;
        }
    }

    private static Class<?> getMethodType(CtClass ctClasses) throws Exception {
        String type;
        if(ctClasses instanceof CtPrimitiveType) {
            type = ((CtPrimitiveType)ctClasses).getWrapperName();
        } else if(ctClasses.isArray()) {
            type = "[L" + ctClasses.getComponentType().getName() + ";";
        } else {
            type = ctClasses.getName();
        }

        return Class.forName(type, true, Thread.currentThread().getContextClassLoader());

    }
}
