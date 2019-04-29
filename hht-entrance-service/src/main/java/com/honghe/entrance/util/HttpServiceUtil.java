package com.honghe.entrance.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;


/**
 * Created by lyx on 2016-09-26.
 * <p>
 * 请求服务类的工厂，用于创建对应的服务的连接
 */
public class HttpServiceUtil {

    private static Logger logger= LoggerFactory.getLogger(HttpServiceUtil.class);

    /**
     * 返回值错误解析
     *
     * @param code
     * @return
     */
    private static String parseErrorCode(int code) {
        String msg = "";
        if (code == -1) {
            msg = "参数错误";
        } else if (code == -2) {
            msg = "没有此方法";
        } else {
            msg = "未知错误";
        }
        return msg;
    }

    /**
     * 发送get请求
     *
     * @param url 路径
     * @return
     */
    public static JSONObject httpGetJson(String url) {
        //get请求返回结果
        JSONObject jsonResult =new JSONObject();
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            //发送get请求
            HttpGet request = new HttpGet(url);
            request.addHeader("Accept", "text/json");
            request.addHeader("charset", "UTF-8");
            //连接超时和请求超时时间设置
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(60000)
                    .setConnectionRequestTimeout(60000)
                    .setSocketTimeout(60000).build();
            request.setConfig(requestConfig);
            HttpResponse response = client.execute(request);

            /**请求发送成功，并得到响应**/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /**读取服务器返回过来的json字符串数据**/
//                String strResult = EntityUtils.toString(response.getEntity(),"GBK");
                String strResult = EntityUtils.toString(response.getEntity(),"UTF-8");
                /**把json字符串转换成json对象**/
                if(strResult!=null && !"".equals(strResult)){
                    try {
                        jsonResult = JSONObject.parseObject(strResult);
                    }catch (JSONException e){
                        logger.error("字符串转换json异常,jsonStr="+strResult);
                        jsonResult=new JSONObject();
                    }
                }
            } else {
                url = URLDecoder.decode(url, "UTF-8");
//                logger.error("get请求提交失败:" + url);
            }
        } catch (IOException e) {
            logger.error("get请求提交失败:" + url, e);
            jsonResult=new JSONObject();
        }
        return jsonResult;
    }

    /**
     * 发送get请求
     *
     * @param url 路径
     * @return
     */
    public static JSONObject httpGetOutTimeAndRetry(String url, Object time, int retryCount) {
        //get请求返回结果
        JSONObject jsonResult = null;
        try {
            CloseableHttpClient client = new DefaultHttpClient();
            //发送get请求
            HttpGet request = new HttpGet(url);
//            request.addHeader("Accept", "text/json");
            request.addHeader("charset", "UTF-8");
            //连接超时和请求超时时间设置
            if(time==null){
                time=60000;
            }
            // 请求重试处理
            HttpRequestRetryHandler handler = new HttpRequestRetryHandler() {
                @Override
                public boolean retryRequest(IOException arg0, int retryTimes, HttpContext arg2) {
                    if (retryTimes > retryCount) {
//                        logger.info("接口请求次数retryTimes==========="+(retryTimes-1)+"次，调用结束");
                        return false;
                    }

                    HttpClientContext clientContext = HttpClientContext.adapt(arg2);
                    HttpRequest request = clientContext.getRequest();
                    boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
                    if (idempotent) {
                        // 如果请求被认为是幂等的，那么就重试。即重复执行不影响程序其他效果的
                        return true;
                    }
                    return false;
                }
            };
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout((int)time)
                    .setConnectionRequestTimeout((int)time)
                    .setSocketTimeout((int)time).build();
            request.setConfig(requestConfig);
            client = HttpClients.custom().setRetryHandler(handler).build();
            HttpResponse response = client.execute(request);

            /**请求发送成功，并得到响应**/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /**读取服务器返回过来的json字符串数据**/
                String strResult = EntityUtils.toString(response.getEntity(),"GBK");
                /**把json字符串转换成json对象**/
                jsonResult = JSONObject.parseObject(strResult);
                url = URLDecoder.decode(url, "UTF-8");
            } else {
                logger.info("get请求提交失败:" + url);
            }
        } catch (IOException e) {
            logger.error("get请求提交失败:" + url, e);
        }
        return jsonResult;
    }
    public static JSONObject httpPostOutTimeAndRetry(String url, String param, int retryCount) {
        //post请求返回结果
        CloseableHttpClient httpClient = HttpClients.createDefault();
        JSONObject jsonResult = new JSONObject();
        HttpPost method = new HttpPost(url);
        try {
            // 请求重试处理
            HttpRequestRetryHandler handler = new HttpRequestRetryHandler() {
                @Override
                public boolean retryRequest(IOException arg0, int retryTimes, HttpContext arg2) {
                    if (retryTimes > retryCount) {
                        return false;
                    }
                    HttpClientContext clientContext = HttpClientContext.adapt(arg2);
                    HttpRequest request = clientContext.getRequest();
                    boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
                    if (idempotent) {
                        // 如果请求被认为是幂等的，那么就重试。即重复执行不影响程序其他效果的
                        return true;
                    }
                    return false;
                }
            };
            StringEntity entity = new StringEntity(param,"UTF-8");
            entity.setContentEncoding("UTF-8");
            method.setEntity(entity);
            httpClient = HttpClients.custom().setRetryHandler(handler).build();
            HttpResponse response = httpClient.execute(method);
            url = URLDecoder.decode(url, "UTF-8");
            /**请求发送成功，并得到响应**/
            if (response.getStatusLine().getStatusCode() == 200) {
                String str = "";
                try {
                    /**读取服务器返回过来的json字符串数据**/
                    str = EntityUtils.toString(response.getEntity());
                    /**把json字符串转换成json对象**/
                    jsonResult = JSON.parseObject(str);

                } catch (Exception e) {
                    logger.error("post请求提交失败:" + url, e);
                }
            }
        } catch (IOException e) {
            logger.error("post请求提交失败:" + url, e);
        }
        return jsonResult;
    }
    public static String convertParams(Map<String,Object> params){
        String strParams="";
        StringBuffer stringBuffer=new StringBuffer();
        if(params!=null && !params.isEmpty()){
            Iterator iterator=params.keySet().iterator();
            while (iterator.hasNext()){
                String keyStr=(String)iterator.next();
                stringBuffer.append(keyStr).append("=").append(params.get(keyStr)).append("&");
            }
        }
        strParams=stringBuffer.toString();
        if(strParams.endsWith("&")){
            strParams=strParams.substring(0,strParams.lastIndexOf("&"));
        }
        return strParams;
    }
}
