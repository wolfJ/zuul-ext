package com.silkylin.zuulext;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.filters.RefreshableRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.web.ZuulController;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * @author by wolf on 2018/4/19.
 */
@Slf4j
public class SNZuulHandlerMapping extends AbstractUrlHandlerMapping {

	private final RouteLocator routeLocator;

	private final ZuulController zuul;

	private volatile boolean dirty = true;

	public SNZuulHandlerMapping(RouteLocator routeLocator, ZuulController zuul) {
		this.routeLocator = routeLocator;
		this.zuul = zuul;
		setOrder(-199);//after and follow ZuulHandlerMapping order.
	}

	@Override
	protected Object lookupHandler(String urlPath, HttpServletRequest request) throws Exception {

		if (this.dirty) {
			synchronized (this) {
				if (this.dirty) {
					registerHandlers();
					this.dirty = false;
				}
			}
		}

		/**
		 * @see SNExchange#getServerNamePath
		 * 注册时的规则为： /sn{*}/port{*}+path, eg: /snservername/port* xxxx
		 * 此处查找时，把/servername带上，做为map的key.
		 */
		urlPath = SNExchange.getServerNamePath(urlPath, request.getServerName(), request.getServerPort());
		return super.lookupHandler(urlPath, request);
	}


	public void setDirty(boolean dirty) {
		this.dirty = dirty;
		if (this.routeLocator instanceof RefreshableRouteLocator) {
			((RefreshableRouteLocator) this.routeLocator).refresh();
		}
	}

	private void registerHandlers() {
		Collection<Route> routes = this.routeLocator.getRoutes();
		if (routes.isEmpty()) {
			this.logger.warn("No routes found from RouteLocator");
		}
		else {
			for (Route route : routes) {
				registerHandler(route.getFullPath(), this.zuul);
			}
		}
	}

}
