package com.dah.cem.app.sc.worker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
	
	@GetMapping(path = "/", produces = "text/html")
	public String indexPage() {
		return "/view/index";
	}
	
}
