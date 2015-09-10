package io.pivotal.greeting;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("fortune-service")
public interface FortuneServiceClient {

	 @RequestMapping(method = RequestMethod.GET, value = "/")
	 String getFortune();
}
