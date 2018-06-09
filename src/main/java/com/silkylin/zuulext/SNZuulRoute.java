package com.silkylin.zuulext;

import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

/**
 * @author by wolf on 2018/4/18.
 */
public class SNZuulRoute extends ZuulProperties.ZuulRoute {

	private String serverName;

	private Integer port;

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}
}
