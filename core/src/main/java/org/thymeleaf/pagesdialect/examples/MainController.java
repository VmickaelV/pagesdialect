package org.thymeleaf.pagesdialect.examples;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("products", SampleProduct.loadAllProducts());
        return "index.html";
    }
}
