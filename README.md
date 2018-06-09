# zuul-ext
Extension of Zuul, supporting special ServerName and Port.

### usage

autowired zuulProperties and publisher

	@Autowired
	private ZuulProperties zuulProperties;

	@Autowired
	ApplicationEventPublisher publisher;


call add route method

	void updateOrAddRoute(boolean isSnRoute) {

		Map<String, ZuulProperties.ZuulRoute> stringZuulRouteMap = zuulProperties.getRoutes();
        String fromUrl = "/prefix/**";
        String toUrl = "http://ip:port/3th/"
        String serverName = "custome.domain.com";
        Integer port = null;
		String key = "sn.routeid.1";
		if (isSnRoute) {
			if (!stringZuulRouteMap.containsKey(key)) {
				stringZuulRouteMap.put(key, new SNZuulRoute());
			}
			ZuulProperties.ZuulRoute snRoute = stringZuulRouteMap.get(key);
			snRoute.setId(key);
			snRoute.setPath(SNExchange.getServerNamePath(fromUrl, serverName,port));
			snRoute.setUrl(toUrl);
			//note this.
			((SNZuulRoute) snRoute).setServerName(serverName);
			((SNZuulRoute) snRoute).setPort(port);
			log.info("id:" + key + " from:" + snRoute.getPath() + " to:" + toUrl);
		}
		else {
			if (!stringZuulRouteMap.containsKey(key)) {
				stringZuulRouteMap.put(key, new ZuulProperties.ZuulRoute());
			}
			ZuulProperties.ZuulRoute route = stringZuulRouteMap.get(key);
			route.setId(key);
			route.setPath(fromUrl);
			route.setUrl(toUrl);
			log.info("id:" + key + " from:" + route.getPath() + " to:" + toUrl);
		}
	}

finally refresh route
    
	public void refreshRoute() {
		RoutesRefreshedEvent routesRefreshedEvent = new RoutesRefreshedEvent(routeLocator);
		publisher.publishEvent(routesRefreshedEvent);
	}