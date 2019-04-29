package com.honghe.entrance.util;

import org.apache.commons.io.FileUtils;

import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by lichong on 2017/3/21.
 */
public class FileStorageUtil {

    public static final String TYPE_JPG = "jpg";
    public static final String TYPE_GIF = "gif";
    public static final String TYPE_PNG = "png";
    public static final String TYPE_BMP = "bmp";
    public static final String TYPE_UNKNOWN = "unknown";

    public final String getWebRoot() {
        String path = this.getClass().getResource("/").getPath();
        int n = path.indexOf("WEB-INF/classes/");
        if (n == -1) {
            return "";
        } else {
            path = path.substring(0, n);
            path = path.replaceAll("%20", " ");
            return path;
        }
    }

    /**
     * 根据目录结构下的文件名存储到对应的位置
     *
     * @param filePath 文件全路径
     * @author wuxiao
     * @date 2018/08/03
     * @return
     */
    public static String saveFile(String filePath){
        String result = "";//返回值
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String formatDate = format.format(new Date());
        String storage_path = ConfigParamsUtil.getWebRoot() + File.separator+"image"+File.separator+formatDate;//储存路径
        File storage_dir = new File(storage_path);
        if(!storage_dir.exists() && !storage_dir.isDirectory()){
            storage_dir.mkdirs();
        }
        // 获得文件路径和文件名
        String fileName = "";
        // 执行文件上传
        if (!filePath.equals("")) {
            fileName = generateFileName(filePath);
            String newPath = storage_path + File.separator +fileName;//生成后的名字
            File file = new File(storage_path, fileName);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    FileInputStream in = new FileInputStream(filePath);
                    FileOutputStream out = new FileOutputStream(file);
                    byte[] cache = new byte[1024];
                    int length = 0;
                    while ((length = in.read(cache)) != -1) {
                        out.write(cache, 0, length);
                    }
                    out.flush();
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                    result = newPath;
                    FileUtils.forceDelete(new File(filePath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 根据路径名获取文件名
     *
     * @param fileName
     * @author wuxiao
     * @date 2018/08/03
     * @return
     */
    public static String getRealFileName(String fileName){
        int start = fileName.lastIndexOf("\\");
        String value ="";
        if(start>0){
            value = fileName.substring(start+1);
        }else{
            value = fileName;
        }

        return value;
    }

    /**
     *
     * @param fileName
     * @author wuxiao
     * @date 2018/08/03
     * @return
     */
    public static String generateFileName(String fileName) {
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String formatDate = format.format(new Date());
        int random = new Random().nextInt(10000);
        int position = fileName.lastIndexOf(".");
        String extension = fileName.substring(position);
        return formatDate + random + extension;
    }

    public static String FormetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 验证文件是否图片格式
     * @param srcFilePath
     * @return
     */
    public static String getImageType(File srcFilePath) {
        FileInputStream fis = null;
        //读取文件的前几个字节来判断图片格式
        byte[] b = new byte[4];
        try {
            fis = new FileInputStream(srcFilePath);
            fis.read(b, 0, b.length);
            String type = bytesToHexString(b).toUpperCase();
            if (type.contains("FFD8FF")) {
                return TYPE_JPG;
            } else if (type.contains("89504E47")) {
                return TYPE_PNG;
            } else if (type.contains("47494638")) {
                return TYPE_GIF;
            } else if (type.contains("424D")) {
                return TYPE_BMP;
            }else{
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * byte数组转换成16进制字符串
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


    /**
     * 获取图片宽度
     * @param file  图片文件
     * @return 宽度
     */
    public static int getImgWidth(File file) {
        InputStream is = null;
        BufferedImage src = null;
        int ret = -1;
        try {
            is = new FileInputStream(file);
            src = javax.imageio.ImageIO.read(is);
            ret = src.getWidth(null); // 得到源图宽
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }


    /**
     * 获取图片高度
     * @param file  图片文件
     * @return 高度
     */
    public static int getImgHeight(File file) {
        InputStream is = null;
        BufferedImage src = null;
        int ret = -1;
        try {
            is = new FileInputStream(file);
            src = javax.imageio.ImageIO.read(is);
            ret = src.getHeight(null); // 得到源图高
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 判断文件大小
     *
     * @param file
     *            文件
     * @param size
     *            限制大小
     * @param unit
     *            限制单位（B,K,M,G）
     * @return
     */
    public static boolean checkFileSize(File file, int size, String unit) {
        long len = file.length();
        double fileSize = 0;
        if ("B".equals(unit.toUpperCase())) {
            fileSize = (double) len;
        } else if ("K".equals(unit.toUpperCase())) {
            fileSize = (double) len / 1024;
        } else if ("M".equals(unit.toUpperCase())) {
            fileSize = (double) len / 1048576;
        } else if ("G".equals(unit.toUpperCase())) {
            fileSize = (double) len / 1073741824;
        }
        if (fileSize > size) {
            return true;
        }
        return false;
    }
}
