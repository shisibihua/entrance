package com.honghe.entrance.controller;

import com.honghe.entrance.common.util.PathUtil;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * 下载文件
 * @author caoqian
 * @date 20190301
 */
@CrossOrigin
@WebServlet(name = "downLoadServlet", urlPatterns = {"/service/cloud/httpDownloadService"})
public class FileDownloadController extends HttpServlet{
        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            doPost(request, response);
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            String fileName = request.getParameter("fileName");

            OutputStream outputStream;
            InputStream inputStream;

            try {
                response.setContentType("application/force-download");
                // 设置文件名
                response.addHeader("Content-Disposition", "inline;fileName=" + new String(fileName.getBytes("GBK"), "ISO8859_1"));

                File file = new File(getFilePath(fileName));
                outputStream = response.getOutputStream();
                inputStream = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int i = -1;
                while ((i = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, i);
                }
                outputStream.flush();
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (FileNotFoundException e) {
                System.out.println("download FileNotFoundException!" + e);
            } catch (IOException e) {
                System.out.println("download IOException!" + e);
            }

        }

        private String getFilePath(String pathInfo) {
            return PathUtil.getPath(PathUtil.PathType.UPLOAD) + pathInfo;
        }
}
