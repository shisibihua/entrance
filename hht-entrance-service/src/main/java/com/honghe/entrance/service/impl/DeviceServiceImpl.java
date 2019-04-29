package com.honghe.entrance.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.honghe.entrance.common.pojo.model.Result;
import com.honghe.entrance.service.DeviceService;
import jodd.io.FileUtil;
import jodd.util.StringUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@Configuration
public class DeviceServiceImpl implements DeviceService {
    private Logger logger=Logger.getLogger(DeviceServiceImpl.class);
    // 换行符
    private final static String NEWLINE = "\r\n";
    private final static String BOUNDARY_PREFIX = "--";
    // 定义数据分隔线
    private final static String BOUNDARY = "========7d4a6d158c9";
    @Value("${entrance.control.url}")
    private String DEVICE_CONTROL_URL;
    @Value("${entrance.control.port}")
    private String DEVICE_CONTROL_PORT;
    @Value("${entrance.module.deviceControlIp}")
    private String deviceControllerIp;
    @Override
    public Result uploadFile(String fileName) throws IOException {
        if(StringUtil.isEmpty(fileName)) {
            return new Result(Result.Code.ParamError.value(),"参数错误：文件名不能为空");
        }
        DataInputStream in=null;
        OutputStream out=null;
        String resultValue="";
        try {
            // 服务器的域名
            URL url = new URL("http://"+deviceControllerIp+":"+DEVICE_CONTROL_PORT+DEVICE_CONTROL_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 设置为POST情
            conn.setRequestMethod("POST");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            // 设置请求头参数
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            conn.connect();
            out = new DataOutputStream(conn.getOutputStream());

            // 上传文件
            String filePath=System.getProperty("user.dir")+File.separator+"upload_dir/"+fileName;
            File file=new File(filePath);
            if(!file.exists()){
                return new Result(Result.Code.NoSuchMethod.value(),"文件上传失败");
            }
            StringBuilder sb = new StringBuilder();
            sb.append(BOUNDARY_PREFIX);
            sb.append(BOUNDARY);
            sb.append(NEWLINE);
            // 文件参数
            sb.append("Content-Disposition: form-data;name=\"file\";filename=" + fileName + NEWLINE);
            sb.append("Content-Type:application/octet-stream");
            // 参数头设置完以后需要两个换行，然后才是参数内容
            sb.append(NEWLINE);
            sb.append(NEWLINE);

            // 将参数头的数据写入到输出流中
            out.write(sb.toString().getBytes());

            // 数据输入流,用于读取文件数据
            in = new DataInputStream(new FileInputStream(file));
            byte[] bufferOut = new byte[1024];
            int bytes = 0;
            // 每次读1KB数据,并且将文件数据写入到输出流中
            while ((bytes = in.read(bufferOut)) != -1) {
                out.write(bufferOut, 0, bytes);
            }
            // 最后添加换行
            out.write(NEWLINE.getBytes());

            // 定义最后数据分隔线，即--加上BOUNDARY再加上--。
            byte[] end_data = (NEWLINE + BOUNDARY_PREFIX + BOUNDARY + BOUNDARY_PREFIX + NEWLINE).getBytes();
            out.write(end_data);
            out.flush();
//          定义BufferedReader输入流来读取URL的响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            String result="";
            while ((line = reader.readLine()) != null) {
                result+=line;
            }
            if(!"".equals(result)){
                JSONObject importResult=JSONObject.parseObject(result);
                if(0==Integer.parseInt(String.valueOf(importResult.get("code"))) &&
                        Boolean.parseBoolean(String.valueOf(importResult.get("result")))){
                    FileUtil.delete(filePath);
                    resultValue = "success";
                }else{
                    String path=importResult.get("other").toString();
                    JSONObject pathJson=JSONObject.parseObject(path);
                    resultValue = pathJson.containsKey("msg")?pathJson.get("msg").toString():"";
                }
            }
        } catch (Exception e) {
            logger.error("发送POST请求出现异常！" + e);
        }finally {
            if(null!=in) {
                in.close();
            }
            if(null!=out) {
                out.close();
            }
        }
        Result result=new Result(Result.Code.Success.value());
        result.setResult(resultValue);
        return result;
    }
}
