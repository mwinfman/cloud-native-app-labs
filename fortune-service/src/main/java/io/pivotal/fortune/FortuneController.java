package io.pivotal.fortune;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FortuneController {

	@Autowired
	private FortuneService fortuneService;
	
	
	@RequestMapping("/")
	String getQuote(){
		return fortuneService.getFortune();
	}
		
	
}
