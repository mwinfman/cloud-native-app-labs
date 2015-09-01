package io.pivotal.fortune;

import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class FortuneService {
	
	public String getFortune(){
		Random random = new Random();
		
		String fortune = null;
		switch (random.nextInt(3)){
		
		case 0:
			fortune = "You learn from your mistakes... You will learn a lot today.";
			break;
		case 1:
			fortune = "You can always find happiness at work on Friday";
			break;
		case 2:
			fortune = "You will be hungry again in one hour.";
			break;
		default:
			fortune = "Today will be an awesome day!";
			break;
		}
		
		
		return fortune;
	}

}
