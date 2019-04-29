package com.honghe.entrance.filter;

import com.alibaba.fastjson.JSON;
import com.honghe.entrance.common.pojo.model.Result;
import com.honghe.entrance.common.redis.RedisUtil;
import com.honghe.entrance.common.util.ParamUtil;
import com.honghe.entrance.servicemanager.ServiceManager;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @Auther: caoqian
 * @Date: 20190322
 * @Description: 用户权限校验
 */

@Component
public class AuthFilter implements Filter {
    static Logger logger = LoggerFactory.getLogger("AuthFilter");
    private static final String OPTIONS = "OPTIONS";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Autowired
    RedisUtil redisUtil;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        Result result = new Result();
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String token=(request.getHeader("token")==null || "undefined".equals(request.getHeader("token")))?"":request.getHeader("token");
        //未登录时放过
        String urlPath = request.getServletPath();
        if (request.getMethod().equals(OPTIONS)) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "OPTIONS,GET,POST,DELETE,PUT");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me, " +
                    "token,TPD-SecretID,TPD-CallBack-Auth");
            filterChain.doFilter(request, response);
        } else {
            //登录时header没有token
            Map<String, Object> paramMap = ParamUtil.getParams(request);
            //未登录状态
            if("".equals(token)){
                if(paramMap.isEmpty()){
                    //访问静态资源及登录、上传文件及下载文件直接放过
                    if(urlPath.contains("/resources/") || urlPath.contains("/image/") || urlPath.contains("/template/") ||
                            urlPath.contains("service/cloud/httpDownloadService") ||
                            urlPath.contains("service/cloud/httpUploadService")){
                        filterChain.doFilter(request,response);
                    }else {
                        result.setCode(Result.Code.AuthError.value());
                        result.setOther("接口参数缺失");
                        send(response, result);
                    }
                }else{
                    //未登录时，调用接口通过
                    String cmdOp=paramMap.get("cmd_op").toString();
                    if("getCurrentThem".equals(cmdOp) || "searchEntranceConfig".equals(cmdOp) ||
                            "sysGetAllEnableModule".equals(cmdOp) || "updateModulePosition".equals(cmdOp) ||
                            "checkUserIsFirstLogin".equals(cmdOp) || "getMobileCode".equals(cmdOp) ||
                            "userCheck".equals(cmdOp)){
                        filterChain.doFilter(request,response);
                    }else{
                        result.setCode(Result.Code.AuthError.value());
                        result.setOther("token缺失");
                        send(response, result);
                    }
                }
            }else{
                checkAuthIsValid(token , result, filterChain, request, response);
            }
        }
    }

    /**
     * 验证接口权限
     * @param token       用户token
     * @param result      返回结果
     * @param filterChain
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     * @author caoqian
     * @date 20190322
     */
    private void checkAuthIsValid(String token,Result result, FilterChain filterChain, HttpServletRequest request,
                                  HttpServletResponse response) throws IOException, ServletException {
        if (!StringUtil.isEmpty(redisUtil.hget(RedisUtil.KEY, token))) {
            filterChain.doFilter(request, response);
        } else {
            result.setCode(Result.Code.AuthError.value());
            result.setOther("用户无权限访问");
            send(response, result);
        }
    }

    @Override
    public void destroy() {

    }

    private void send(HttpServletResponse response, Object result) throws IOException, ServletException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with,Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(JSON.toJSONString(result));
        response.getWriter().flush();
        response.getWriter().close();
    }
}
