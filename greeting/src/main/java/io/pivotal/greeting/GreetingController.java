package io.pivotal.greeting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;

@Controller
public class GreetingController {

	Logger logger = LoggerFactory
			.getLogger(GreetingController.class);

	

	
	@Autowired
	private DiscoveryClient discoveryClient;
	
	@RequestMapping("/")
	String getGreeting(Model model){
        
		logger.debug("Adding greeting");
		model.addAttribute("msg", "Hello World!!!");
			
		String fortuneServiceUrl = fetchFortuneServiceUrl();
		logger.debug("fortune service url: {}", fortuneServiceUrl);
		
		RestTemplate restTemplate = new RestTemplate();
        String fortune = restTemplate.getForObject(fortuneServiceUrl, String.class);

		logger.debug("Adding fortune");
		model.addAttribute("fortune", fortune);
		
		//resolves to the greeting.vm velocity template
		return "greeting";
	}
	
	private String fetchFortuneServiceUrl() {
	    InstanceInfo instance = discoveryClient.getNextServerFromEureka("FORTUNE-SERVICE", false);
	    return instance.getHomePageUrl();
	}	
	
}
