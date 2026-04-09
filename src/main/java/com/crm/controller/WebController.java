package com.crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "forward:/websi.html";
    }

    @GetMapping("/index")
    public String home() {
        return "forward:/websi.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/admin")
    public String adminDashboard() {
        return "forward:/admin.html";
    }

    @GetMapping("/employee")
    public String employeeDashboard() {
        return "forward:/employee.html";
    }
}