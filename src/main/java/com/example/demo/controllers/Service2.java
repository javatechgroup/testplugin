package com.example.demo.controllers;

import org.springframework.stereotype.Service;

@Service
public class Service2 {
	private final TestService testService;
	private final TestController testController;

	public Service2(TestService testService, TestController testController) {
		this.testService = testService;
		this.testController = testController;
	}
}
