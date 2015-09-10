package io.pivotal.fortune;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@Service
public class FortuneService {

	Logger logger = LoggerFactory
			.getLogger(FortuneService.class);
	
	@Autowired
	private RestTemplate restTemplate;
		
	@HystrixCommand(fallbackMethod = "defaultFortune")
	public String getFortune() {
        String fortune = restTemplate.getForObject("http://fortune-service", String.class);
		return fortune;
	}
	
	public String defaultFortune(){
		logger.debug("Default fortune used.");
		return "This fortune is no good. Try another.";
	}
	
	
	
}
