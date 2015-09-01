package io.pivotal.fortune;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@Service
public class FortuneService {

	Logger logger = LoggerFactory
			.getLogger(FortuneService.class);
	
	@Autowired
	private DiscoveryClient discoveryClient;
	
	
	@HystrixCommand(fallbackMethod = "defaultFortune")
	public String getFortune() {
		RestTemplate restTemplate = new RestTemplate();
        String fortune = restTemplate.getForObject(fetchFortuneServiceUrl(), String.class);
		return fortune;
	}
	
	public String defaultFortune(){
		logger.debug("Default fortune used.");
		return "This fortune is no good. Try another.";
	}
	
	private String fetchFortuneServiceUrl() {
	    InstanceInfo instance = discoveryClient.getNextServerFromEureka("FORTUNE-SERVICE", false);
	    logger.debug("instanceID: {}", instance.getId());

	    String fortuneServiceUrl = instance.getHomePageUrl();
		logger.debug("fortune service url: {}", fortuneServiceUrl);

	    return fortuneServiceUrl;
	}		
	
}
