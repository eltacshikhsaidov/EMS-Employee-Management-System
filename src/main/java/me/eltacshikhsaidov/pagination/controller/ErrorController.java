package me.eltacshikhsaidov.pagination.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    @GetMapping("/error")
    public String errorPage() {
        return "error";
    }
}
