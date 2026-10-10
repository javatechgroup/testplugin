package com.example.demo.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	private static int counter = 0;
	@GetMapping("/test")
	public String test() {
		try {
		System.out.println("TestController: test() method called");
		}catch(Exception e) {
		}
		return "Test successful!";
	}
}
