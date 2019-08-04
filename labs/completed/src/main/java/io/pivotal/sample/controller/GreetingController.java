package io.pivotal.sample.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@RestController
public class GreetingController {
	@Value("${greeting:Hello}")
    private String greeting;
	
	@GetMapping("/hello")
	public String hello() {
		return String.join(" ", greeting, "World!");
	}
	
}
