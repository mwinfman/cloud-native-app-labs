package io.pivotal.quote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class QuoteController {

	Logger logger = LoggerFactory
			.getLogger(QuoteController.class);
	
	@Autowired
	private QuoteService quoteService;

	@RequestMapping("/quote-of-the-day")
	String getView(Model model) {
	
		model.addAttribute("quote", quoteService.getQuote());
		model.addAttribute("uri", quoteService.getQuoteServiceURI());
		return "quote";
	}
}