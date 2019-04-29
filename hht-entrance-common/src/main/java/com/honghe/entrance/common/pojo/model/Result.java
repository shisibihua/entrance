package com.honghe.entrance.common.pojo.model;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;

/**
 * @Description 返回结果实体对象
 * @Author sunchao
 * @Date: 2017-11-29 14:43
 * @Mender: libing
 */
public final class Result {

    private int code = 0;
    JSONObject other = new JSONObject();
    private Object result;
    private String type;

    public Result(){}

    /**
     * 构造方法
     * @param type 请求
     * @param statusCode 错误码
     * @param msgInfo 提示信息
     */
    public Result(String type, int statusCode,String msgInfo) {
        other.put("exception",msgInfo);
        this.type = type;
        this.code = statusCode;
    }

	public Result(String type, int statusCode, Object result) {
		this(type, statusCode, result, "");
	}

    public Result(String type, int statusCode) {
        this(type, statusCode, null, "");
    }

    public Result(String type, int statusCode,Object result, String msgInfo) {
        other.put("exception",msgInfo);
		this.type = type;
		this.code = statusCode;
		this.result = result;
	}

    public Result(int statusCode,String msgInfo) {
        other.put("exception",msgInfo);
        this.code = statusCode;
    }

    public Result(int statusCode, Object result) {
        this(statusCode, result, "");
    }

    public Result(int statusCode) {
        this(statusCode, null, "");
    }

    public Result( int statusCode,Object result, String msgInfo) {
        other.put("exception",msgInfo);
        this.code = statusCode;
        this.result = result;
    }

    public JSONObject getOther() {
        return other;
    }

    public void setOther(String msg) {
        this.other.put("exception",msg);
    }



    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int statusCode) {
        this.code = statusCode;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public enum Code{
        NoSuchMethod(-2),
        Success(0),
        ParamError(-1),
        UnKnowError(-3),
        AuthError(-4);

        private Code(int value){
            this.value = value;
        }
        private int value = 0;

        public int value(){
            return value;
        }
    }

    public enum Msg{
        NoSuchMethod("没有此方法"),
        Success("执行成功"),
        ParamError("参数错误"),
        UnKnowError("内部错误"),
        AuthError("权限错误");

        private Msg(String msg){
            this.msg = msg;
        }

        private String msg = "";

        public String value(){
            return msg;
        }
    }

}