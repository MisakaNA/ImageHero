package com.project.imgcrawler.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloWorld {
    @RequestMapping(value = "hello", method = RequestMethod.GET)
    public String hello() {
        return "Hello World!";
    }
}
