package io.pivotal.greeting;

import io.pivotal.fortune.FortuneService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@EnableConfigurationProperties(GreetingProperties.class)
public class GreetingController {

	Logger logger = LoggerFactory
			.getLogger(GreetingController.class);

	
	@Autowired
	GreetingProperties greetingProperties;
	
	@Autowired
	FortuneService fortuneService;
	
	@RequestMapping("/")
	String getGreeting(Model model){
		
		logger.debug("Adding greeting");
		model.addAttribute("msg", "Greetings!!!");
		
		if(greetingProperties.isDisplayFortune()){
			logger.debug("Adding fortune");
			model.addAttribute("fortune", fortuneService.getFortune());
		}
		
		//resolves to the greeting.vm velocity template
		return "greeting";
	}
	
	
}
