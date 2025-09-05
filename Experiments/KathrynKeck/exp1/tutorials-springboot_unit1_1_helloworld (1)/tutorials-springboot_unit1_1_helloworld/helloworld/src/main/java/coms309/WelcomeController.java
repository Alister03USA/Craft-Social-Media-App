package coms309;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
class WelcomeController {

    @GetMapping("/")
    public String welcome() {
        return "Hello! My name is Kathryn Keck";
    }

    @GetMapping("/{name}")
    public String welcome(@PathVariable String name) {
        return "Hello! My name is Kathryn Keck : " + name;
    }

    @GetMapping("/create/{item}")
    public String create(@PathVariable String item){return item + " has been created!";}

    @GetMapping("/next/thing")
    public String nextThing(){return "And this is the next thing";}
}

