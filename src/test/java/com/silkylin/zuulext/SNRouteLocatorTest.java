package com.silkylin.zuulext;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import java.util.List;

/**
 * @author by wolf on 2018/5/16.
 */
//@RunWith(SpringRunner.class)
public class SNRouteLocatorTest {

	ZuulProperties prop = new ZuulProperties();

	@Test
	public void testSimpleRouteGetRoutes() {
		SNZuulRoute zuulRoute = new SNZuulRoute();
		zuulRoute.setServerName("api.d-name.org");
		zuulRoute.setPort(null);
		zuulRoute.setId("route-a");
		zuulRoute.setUrl("http://localhost:8080/api/");
		zuulRoute.setPath("/xx/**");
		prop.getRoutes().put(zuulRoute.getId(), zuulRoute);

		SimpleRouteLocator routeLocator = new SimpleRouteLocator("/", prop);
		List<Route> routes = routeLocator.getRoutes();
		System.out.println("route info:\r\n" + routes.get(0));
		Assert.assertEquals(zuulRoute.getPath(), routes.get(0).getFullPath());
	}

	@Test
	public void testSimpleRouteMatchingRoute() {
		SNZuulRoute zuulRoute = new SNZuulRoute();
		zuulRoute.setServerName("api.d-name.org");
		zuulRoute.setPort(null);
		zuulRoute.setId("route-a");
		zuulRoute.setUrl("http://localhost:8080/api/");
		zuulRoute.setPath("/xx/**");
		prop.getRoutes().put(zuulRoute.getId(), zuulRoute);

		SimpleRouteLocator routeLocator = new SimpleRouteLocator("/", prop);
		Route route = routeLocator.getMatchingRoute("/xx/login.html");
		System.out.println("route info:\r\n" + route);
		Assert.assertEquals("/xx/login.html", route.getFullPath());
		Assert.assertEquals("/login.html", route.getPath());
	}

	@Test
	public void testSimpleRoutePrefixMatchingRoute() {
		prop.setPrefix("/zuul");
		SNZuulRoute zuulRoute = new SNZuulRoute();
		zuulRoute.setServerName("api.d-name.org");
		zuulRoute.setPort(null);
		zuulRoute.setId("route-a");
		zuulRoute.setUrl("http://localhost:8080/api/");
		zuulRoute.setPath("/**");
		prop.getRoutes().put(zuulRoute.getId(), zuulRoute);

		SimpleRouteLocator routeLocator = new SimpleRouteLocator("/", prop);
		Route route = routeLocator.getMatchingRoute("/zuul/login.html");
		System.out.println("route info:\r\n" + route);
		Assert.assertEquals("/login.html", route.getPath());
		Assert.assertEquals("/zuul/login.html", route.getFullPath());
	}

	@Test
	public void testSNRouteGetRoutes() {
		SNRouteLocator snRouteLocator = new SNRouteLocator("/", prop);

		SNZuulRoute zuulRoute = new SNZuulRoute();
		zuulRoute.setServerName("api.d-name.org");
		zuulRoute.setPort(null);
		zuulRoute.setId("sn-route-a");
		zuulRoute.setUrl("http://localhost:8080/api/");
		zuulRoute.setPath(
				SNExchange.getServerNamePath("/xx/**", zuulRoute.getServerName(), zuulRoute.getPort()));
		prop.getRoutes().put(zuulRoute.getId(), zuulRoute);

		List<Route> routes = snRouteLocator.getRoutes();
		System.out.println("route info:\r\n" + routes.get(0).toString());
		Assert.assertEquals(zuulRoute.getPath(), routes.get(0).getFullPath());

	}

	@Test
	public void testSNRouteMatchingRoute() {
		SNRouteLocator snRouteLocator = new SNRouteLocator("/", prop);

		SNZuulRoute zuulRoute = new SNZuulRoute();
		zuulRoute.setServerName("api.d-name.org");
		zuulRoute.setPort(null);
		zuulRoute.setId("sn-route-a");
		zuulRoute.setUrl("http://localhost:8080/api/");
		zuulRoute.setPath(
				SNExchange.getServerNamePath("/xx/**", zuulRoute.getServerName(), zuulRoute.getPort()));
		prop.getRoutes().put(zuulRoute.getId(), zuulRoute);

		String path = SNExchange.getServerNamePath("/xx/login.html", zuulRoute.getServerName(),
				zuulRoute.getPort());
		Route route = snRouteLocator.getRoute(zuulRoute, path);
		System.out.println("route info:\r\n" + route);
		Assert.assertEquals(path, route.getFullPath());
		Assert.assertEquals("/login.html", route.getPath());

	}

	@Test
	public void testSNRoutePrefix() {
		SNRouteLocator snRouteLocator = new SNRouteLocator("/", prop);

		prop.setPrefix("/zuul");
		prop.getRoutes().clear();
		SNZuulRoute zuulRoute = new SNZuulRoute();
		zuulRoute.setServerName("");
		zuulRoute.setPort(null);
		zuulRoute.setId("sn-route-b");
		zuulRoute.setUrl("http://localhost:8080/api/");
		zuulRoute.setPath(
				SNExchange.getServerNamePath("/**", zuulRoute.getServerName(), zuulRoute.getPort()));
		prop.getRoutes().put(zuulRoute.getId(), zuulRoute);

		String path = SNExchange.getServerNamePath("/zuul/login.html", zuulRoute.getServerName(),
				zuulRoute.getPort());
		Route route = snRouteLocator.getRoute(zuulRoute, path);
		System.out.println("route info:\r\n" + route.toString());
		Assert.assertEquals(path, route.getFullPath());
	}
}
