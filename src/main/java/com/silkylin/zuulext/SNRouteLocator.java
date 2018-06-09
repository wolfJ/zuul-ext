package com.silkylin.zuulext;

import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.RefreshableRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author by wolf on 2018/4/18.
 */
@Slf4j
public class SNRouteLocator extends SimpleRouteLocator implements RefreshableRouteLocator {

	private PathMatcher pathMatcher = new AntPathMatcher();

	private ZuulProperties properties;

	private AtomicReference<Map<String, ZuulProperties.ZuulRoute>> parentRoute;

	public SNRouteLocator(String servletPath,
			ZuulProperties properties) {
		super(servletPath, properties);
		this.properties = properties;
	}

	private AtomicReference<Map<String, ZuulProperties.ZuulRoute>> getParentRoutes() {
		if (parentRoute == null) {
			Field routesField = ReflectionUtils.findField(SNRouteLocator.class, "routes");
			ReflectionUtils.makeAccessible(routesField);
			parentRoute = (AtomicReference<Map<String, ZuulProperties.ZuulRoute>>) ReflectionUtils
					.getField(routesField, this);
		}
		return parentRoute;
	}

	@Override
	protected Map<String, ZuulProperties.ZuulRoute> locateRoutes() {
		LinkedHashMap<String, ZuulProperties.ZuulRoute> routesMap = new LinkedHashMap<String, ZuulProperties.ZuulRoute>();
		for (ZuulProperties.ZuulRoute route : this.properties.getRoutes().values()) {
			String key = route.getPath();
			if (route instanceof SNZuulRoute) {
				routesMap.put(key, route);
			}
		}
		return routesMap;
	}

	@Override
	protected ZuulProperties.ZuulRoute getZuulRoute(String adjustedPath) {
		if (!matchesIgnoredPatterns(adjustedPath)) {
			//sort path
			Map<String, ZuulProperties.ZuulRoute> map = getParentRoutes().get();
			List<String> keyList = new ArrayList<>(map.keySet());
			Comparator<String> patternComparator = pathMatcher.getPatternComparator(adjustedPath);
			if (!keyList.isEmpty()) {
				Collections.sort(keyList, patternComparator);
			}
			for (String key : keyList) {
				String pattern = key;

				log.debug("Matching pattern:" + pattern);
				ZuulProperties.ZuulRoute route = map.get(key);

				if (route instanceof SNZuulRoute) {
					SNZuulRoute snRoute = (SNZuulRoute) route;

					RequestContext ctx = RequestContext.getCurrentContext();
					if (ctx != null && ctx.getRequest() != null &&
							(StringUtils.isNotEmpty(snRoute.getServerName()) || snRoute.getPort() != null)) {

						boolean match = true;
						//采用存在就校验的原则
						if (StringUtils.isNotEmpty(snRoute.getServerName())) {
							match = ctx.getRequest().getServerName().equals(snRoute.getServerName());
						}

						if (match && snRoute.getPort() != null) {
							match = ctx.getRequest().getServerPort() == snRoute.getPort().intValue();
						}
						if (match) {
							pattern = SNExchange.splitServerName(pattern);
							//再加上原来的匹配规则
							if (this.pathMatcher.match(pattern, adjustedPath)) {
								return route;
							}
						}
					}
					//其它情况都返回空
				}
				else {
					if (this.pathMatcher.match(pattern, adjustedPath)) {
						return route;
					}
				}
			}
		}
		return null;
	}

	@Override
	protected Route getRoute(ZuulProperties.ZuulRoute route, String path) {
		/**
		 * path e.g.:
		 * /snwww.domain.com/port8000 + /**
		 * /snwww.domain.com/port8000 + /xx/**
		 * targetPath: path
		 * routePath: route.getPath
		 * sn_prefix = /snwww.domain.com/port8000
		 * properties.prefix=/zuul
		 * realPath = path.trim sn_prefix
		 */
		if (route == null) {
			return null;
		}
		if (log.isDebugEnabled()) {
			log.debug("route matched=" + route);
		}

		//target path.
		String targetRealPath = SNExchange.splitServerName(path);
		String targetPath = targetRealPath;
		String prefix = this.properties.getPrefix();
		if (targetRealPath.startsWith(prefix) && this.properties.isStripPrefix()) {
			targetPath = targetRealPath.substring(prefix.length());
		}
		//route path.
		if (route.isStripPrefix()) {
			String routePath = SNExchange.splitServerName(route.getPath());
			int index = routePath.indexOf("*") - 1;
			if (index > 0) {
				String routePrefix = routePath.substring(0, index);
				targetPath = targetPath.replaceFirst(routePrefix, "");
				prefix = prefix + routePrefix;
			}
		}

		//加上 /sn*/port* 的前缀
		prefix = path.substring(0, path.lastIndexOf(targetRealPath)) + prefix;
		Boolean retryable = this.properties.getRetryable();
		if (route.getRetryable() != null) {
			retryable = route.getRetryable();
		}
		return new Route(route.getId(), targetPath, route.getLocation(), prefix,
				retryable,
				route.isCustomSensitiveHeaders() ? route.getSensitiveHeaders() : null);
//		return super.getRoute(route, path);
	}

	@Override
	public void refresh() {
		doRefresh();
	}
}
