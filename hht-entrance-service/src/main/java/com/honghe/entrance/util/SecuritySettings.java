package com.honghe.entrance.util;

import com.honghe.entrance.common.util.MD5Util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *  对网络访问的参数进行加密
 *  流程是：首先要在现有接口参数的基础上添加sessionkey、restauth、resttime三个隐式参数
 *         其次对所有参数的key进行排序，然后在把所有的参数拼接成一个字符串。其中要对value的值进行UTF-8编码，并替换特殊字符（\）
 *         例子：restauth=_restauth&resttime=1527901916&sessionkey=phpclient&type=delete&
 *         再次，对拼接后的字符串添加一个规定的值，替换字符串，然后获取该字符串的MD5值。
 *         最后把该MD5值赋给restauth，其余的不变，然后进行网络请求。
 */
public class SecuritySettings {

	private static final String SESSION_KEY ="androidclient";
	private static final String SESSION_VALUE ="9u8fjk3d02dv";

	/**
	 * 参数中没有文件的可以使用这个
	 * @param params
	 * @return
	 */
	public static List<Params> setting(Map<String, String[]> params) {
		params.put("sessionkey", new String[] {SESSION_KEY});
		params.put("restauth", new String[] { "_restauth" });
		params.put("resttime", new String[] { String.valueOf(System.currentTimeMillis()).substring(0, 10) });
		String urlParams = "";
		String[] keys = (String[]) params.keySet().toArray(new String[params.size()]);
		Arrays.sort(keys);  //对key值进行排序
		for (String key : keys) {
			String[] value = (String[]) params.get(key);
			try {
				if (value.length == 1) {
					urlParams = urlParams + key + "=" + URLEncoder.encode(value[0], "UTF-8").replaceAll("\\*", "%2A") + "&";
				}else {
					for (String s : value) {
						urlParams = urlParams + key + "=" + URLEncoder.encode(s, "UTF-8").replaceAll("\\*", "%2A") + "&";
					}
				}
			} catch (UnsupportedEncodingException e) {
				System.out.println("Un supported Encoding Exception");
			}
		}
		urlParams = urlParams + SESSION_VALUE;
		String _urlParams = urlParams.replaceAll("&restauth=_restauth", "").replaceAll("restauth=_restauth&", "");
		String md5_1 = MD5Util.getMD5(_urlParams.getBytes());
		List<Params> result = new ArrayList<Params>();
		String[] paramsArray = urlParams.replaceAll("&"+ SESSION_VALUE, "").replaceAll("_restauth", md5_1).split("&");
		for (String _paramsArray : paramsArray) {
			String[] temp = _paramsArray.split("=");
			if (temp.length == 1) {
				result.add(new Params(temp[0], ""));
			}
			else {
				try {
					result.add(new Params(temp[0], URLDecoder.decode(temp[1], "UTF-8")));
				} catch (UnsupportedEncodingException e) {
					System.out.println("Un supported Encoding Exception");
				}
			}
		}
		return result;
	}

	/**
	 * 因为修改头像的需要传递图片的数据，所以才又添加了该方法
	 * @param params
	 * @return
	 */
	public static List<Params> setting2(Map<String, String> params) {
		params.put("sessionkey", SESSION_KEY);
		params.put("restauth", "_restauth");
		params.put("resttime", String.valueOf(System.currentTimeMillis()).substring(0, 10));
		String urlParams = "";
		String[] keys = (String[]) params.keySet().toArray(new String[params.size()]);
		Arrays.sort(keys);
		for (String key : keys) {
			String value = (String) params.get(key);
			try {
				urlParams = urlParams + key + "=" + URLEncoder.encode(value, "UTF-8").replaceAll("\\*", "%2A") + "&";
			} catch (UnsupportedEncodingException e) {
				System.out.println("Un supported Encoding Exception");
			}
		}
		urlParams = urlParams + SESSION_VALUE;
		String _urlParams = urlParams.replaceAll("&restauth=_restauth", "").replaceAll("restauth=_restauth&", "");
		String md5_1 = MD5Util.getMD5(_urlParams.getBytes());
		List<Params> result = new ArrayList<Params>();
		String[] paramsArray = urlParams.replaceAll("&"+ SESSION_VALUE, "").replaceAll("_restauth", md5_1).split("&");
		for (String _paramsArray : paramsArray) {
			String[] temp = _paramsArray.split("=");
			if (temp.length == 1)
				result.add(new Params(temp[0], ""));
			else
				try {
					result.add(new Params(temp[0], URLDecoder.decode(temp[1], "UTF-8")));
				} catch (UnsupportedEncodingException e) {
					System.out.println("Un supported Encoding Exception");
				}
		}
		return result;
	}

	/**
	 * 图片的数据不能加密，所以需要单独处理
	 * @param params
	 * @param FileData
	 * @return
	 */
	public static List<Params> setting2(Map<String, String> params, String FileData) {
		List<Params> result = setting2(params);
		try {
			result.add(new Params("FileData", URLDecoder.decode(FileData, "UTF-8")));
		} catch (UnsupportedEncodingException e) {
			System.out.println("Un supported Encoding Exception");
		}
		return result;
	}

	/**
	 * 初始化get方式请求参数，包括认证等参数的添加
	 *
	 * @param paramsMap 参数的集合
	 * @return md5处理，拼接好的参数
	 */
	public  String initGetParams(Map<String, String> paramsMap) {
		String parameter = "";
		List<Params> paramses = SecuritySettings.setting2(paramsMap);
		int temp = 1;
		for (SecuritySettings.Params p : paramses) {
			if (temp == 1) {
				parameter += "?" + p.getKey() + "=" + p.getValue();
			} else {
				parameter += "&" + p.getKey() + "=" + p.getValue();
			}
			temp++;
		}
		return parameter;
	}

	/**
	 * 初始化Post方式参数
	 *
	 * @param params 参数
	 * @return 拼接的字符串
	 * @throws UnsupportedEncodingException 编码异常
	 */
	public String initPostParams(Map<String, String> params) throws UnsupportedEncodingException {
		int pos = 0;
		StringBuilder tempParams = new StringBuilder();
		List<Params> paramses = SecuritySettings.setting2(params);
		for (SecuritySettings.Params p : paramses) {
			if (pos > 0) {
				tempParams.append("&");
			}
			tempParams.append(String.format("%s=%s", p.getKey(), URLEncoder.encode(p.getValue(), "utf-8")));
			pos++;
		}
		return tempParams.toString();
	}



	public static class Params {
		private String key;
		private String value;

		public Params(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return this.key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getValue() {
			return this.value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
}