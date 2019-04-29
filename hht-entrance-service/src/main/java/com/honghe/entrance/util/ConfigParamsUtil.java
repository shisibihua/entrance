package com.honghe.entrance.util;

import com.honghe.entrance.common.pojo.model.Result;
import org.apache.commons.lang.StringUtils;
import java.io.File;

/**
 * <p>Description:</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 北京鸿合智能系统股份有限公司</p>
 *
 * @author hthwx
 * @date 2019/2/23
 */

public class ConfigParamsUtil {
    /**
     * 将imagePath转换成url
     * @param imagePath 图片本地全路径
     * @author wuxiao
     * @date 2018/08/08
     * @return
     */
    public static String convertImagePath2Url(String imagePath,String userServiceIp){
        if(StringUtils.isEmpty(userServiceIp))
        {
            userServiceIp = "localhost";
        }
        String tempUrl = imagePath.replaceAll("\\\\","/");
        return "http://"+ userServiceIp + ":7003" + tempUrl.substring(tempUrl.indexOf("//")+1, tempUrl.length());
    }

    /**
     * 获取程序根目录
     * @return
     */
    public static final String getWebRoot() {
        try {
            return (new File("")).getCanonicalPath() + File.separator;
        } catch (Exception var1) {
            return "";
        }
    }
    /**
     * 将图片进行保存，并进行异常判定
     * @param img_name 文件名
     * @author wuxiao
     * @date 2018/08/06
     */
    public static Result saveFileException(String img_name){
        String filePath = getWebRoot() + "upload_dir" + File.separator + img_name;//文件全路径
        Result result = new Result(Result.Code.Success.value());
        try {
            if (StringUtils.isNotBlank(img_name)) {
                File file = new File(filePath);
                if (!file.exists()) {
                    // throw new CommandException(Response.PARAM_ERROR,"图片"+img_name+"不存在，请重新上传");
                    result.setCode(Result.Code.ParamError.value());
                    result.setOther("图片" + img_name + "不存在，请重新上传");
                } else if (FileStorageUtil.checkFileSize(file, 10, "M")) {
                    // throw new CommandException(Response.PARAM_ERROR, "图片太大");
                    result.setCode(Result.Code.ParamError.value());
                    result.setOther("图片太大");
                } else if (null == FileStorageUtil.getImageType(file)) {
                    result.setCode(Result.Code.ParamError.value());
                    result.setOther("图片" + img_name + "不存在，请重新上传");
                } else {
                    String newFilePath = FileStorageUtil.saveFile(filePath);
                    if ("".equals(newFilePath)) {
                        result.setCode(Result.Code.ParamError.value());
                        result.setOther("参数错误");
                    } else {
                        result.setResult(newFilePath);
                    }
                }
            }
        }
        catch (Exception e)
        {
            result.setCode(Result.Code.UnKnowError.value());
            result.setOther("内部错误");
        }
        return result;
    }
    /**
     * Integer 类型验证
     * @param key
     * @param defaultValue
     * @return
     */
    public static Integer getIntegerValue(Integer key, Integer defaultValue){
        if(null != key){
            return key;
        }else{
            return defaultValue;
        }
    }

}
