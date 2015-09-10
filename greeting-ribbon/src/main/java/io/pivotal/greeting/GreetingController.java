package io.pivotal.greeting;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class GreetingController {

	Logger logger = LoggerFactory
			.getLogger(GreetingController.class);

	

	
	@Autowired
	private LoadBalancerClient loadBalancerClient;
	
	@RequestMapping("/")
	String getGreeting(Model model){
        
		logger.debug("Adding greeting");
		model.addAttribute("msg", "Greetings!!!");
			
		
		RestTemplate restTemplate = new RestTemplate();
        String fortune = restTemplate.getForObject(fetchFortuneServiceUrl(), String.class);

		logger.debug("Adding fortune");
		model.addAttribute("fortune", fortune);
		
		//resolves to the greeting.vm velocity template
		return "greeting";
	}
	
	private String fetchFortuneServiceUrl() {
	    ServiceInstance instance = loadBalancerClient.choose("fortune-service");
	    logger.debug("uri: {}",instance.getUri().toString());
	    logger.debug("serviceId: {}", instance.getServiceId());

	    URI producerUri = URI.create(String.format("http://%s:%d", instance.getHost(), instance.getPort()));
	    		

		logger.debug("fortune service url: {}", producerUri.toString());

	    return producerUri.toString();
	}	
	
}
