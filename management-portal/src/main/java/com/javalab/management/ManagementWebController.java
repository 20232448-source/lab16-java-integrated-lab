package com.javalab.management;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ManagementWebController {

    @GetMapping({"/", "/overview", "/orders", "/products"})
    public String index() {
        return "forward:/index.html";
    }
}
