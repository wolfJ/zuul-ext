package com.silkylin.zuulext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.netflix.zuul.RoutesRefreshedEvent;
import org.springframework.cloud.netflix.zuul.ZuulServerAutoConfiguration;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.web.ZuulController;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * for support ServerName.
 *
 * @author by wolf on 2018/4/19.
 */

@Configuration
@EnableConfigurationProperties(value = { ServerProperties.class, ZuulProperties.class })
@EnableZuulProxy
@AutoConfigureAfter(value = { ServerPropertiesAutoConfiguration.class, ZuulServerAutoConfiguration.class })
public class SNZuulAutoConfiguration {

	@Autowired
	protected ZuulProperties zuulProperties;

	@Autowired
	protected ServerProperties server;

	@Bean
	public SNZuulHandlerMapping serverNameZuulHandlerMapping(ZuulController zuul) {
		SNZuulHandlerMapping mapping = new SNZuulHandlerMapping(serverNameRouteLocator(), zuul);
		return mapping;
	}

	@Bean
	public SNRouteLocator serverNameRouteLocator() {
		return new SNRouteLocator(this.server.getServletPrefix(), this.zuulProperties);
	}

	@Bean
	public ApplicationListener<ApplicationEvent> snZuulRefreshRoutesListener() {
		return new SNZuulRefreshListener();
	}

	private static class SNZuulRefreshListener
			implements ApplicationListener<ApplicationEvent> {

		@Autowired
		private SNZuulHandlerMapping snZuulHandlerMapping;

		@Override
		public void onApplicationEvent(ApplicationEvent event) {
			if (event instanceof ContextRefreshedEvent
					|| event instanceof RefreshScopeRefreshedEvent
					|| event instanceof RoutesRefreshedEvent) {
				this.snZuulHandlerMapping.setDirty(true);
			}
		}

	}
}
