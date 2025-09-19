package com.example.craftsy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class CraftsyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CraftsyApplication.class, args);
    }

}

@RestController
class HelloController{
    @GetMapping("/")
    public String index(){
        return "Greetings from Spring Boot!";
    }
}
