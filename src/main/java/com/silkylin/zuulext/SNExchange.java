package com.silkylin.zuulext;

import org.apache.commons.lang.StringUtils;

/**
 * @author by wolf on 2018/4/19.
 */
public class SNExchange {

	/**
	 * 规则：注册到zuulRoute上的path，解析时也根据这个规则解析
	 *
	 * @param fromUrl
	 * @param serverName
	 * @param port
	 * @return
	 */
	public static String getServerNamePath(String fromUrl, String serverName, Integer port) {
		StringBuilder sb = new StringBuilder();
		sb.append("/sn:").append(StringUtils.isEmpty(serverName) ? "*" : serverName);
		sb.append("/port:").append(port == null ? "*" : port.intValue());
		sb.append(fromUrl);
		return sb.toString();
	}

	public static String splitServerName(String pattern) {
		//serverName
		if (pattern.startsWith("/sn:")) {
			pattern = pattern.substring(pattern.indexOf("/", 3));//去掉服务名 /sn*
		}

		//port
		if (pattern.startsWith("/port:")) {
			pattern = pattern.substring(pattern.indexOf("/", 5));//去掉端口 /port*
		}
		return pattern;
	}

}
