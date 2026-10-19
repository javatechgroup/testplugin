package com.example.demo.controllers;

import org.springframework.stereotype.Service;

@Service
public class TestService {

	private final TestController testController;
	private final Service2 service2;
	
	public TestService(TestController testController, Service2 service2) {	
		this.testController = testController;
		this.service2 = service2;
		//System.out.println("TestService: Constructor called");
	}
}
