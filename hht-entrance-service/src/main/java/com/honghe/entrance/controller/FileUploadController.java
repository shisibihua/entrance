package com.honghe.entrance.controller;

import com.honghe.entrance.common.util.FileUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;

/**
 * 上传文件
 * @author caoqian
 * @date 20190301
 */
@CrossOrigin
@RestController("fileUpload")
public class FileUploadController{

    /**
     * 实现文件上传
     * @author caoqian
     * */
    @PostMapping(value = "service/cloud/httpUploadService")
    @ResponseBody
    public String fileUpload(@RequestParam("file")MultipartFile file){
        if(file.isEmpty()){
            return "";
        }
        String fileName = file.getOriginalFilename();
        String path = System.getProperty("user.dir")+ File.separator+ "upload_dir" ;
        String newFileName="";
        try {
            //保存文件
            newFileName=FileUtil.uploadFile(file.getBytes(), path, fileName);
        } catch (Exception e) {
            newFileName="";
        }
        return newFileName;
    }

}
