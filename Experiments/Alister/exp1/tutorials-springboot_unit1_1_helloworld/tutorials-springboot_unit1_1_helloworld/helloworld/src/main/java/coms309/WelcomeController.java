package coms309;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
// Tells SB to handle http request, send text back to request
@RestController
class WelcomeController {

    // "/" is a placeholder in the URL
    @GetMapping("/{name}")
    public String welcome(@PathVariable String name) {
        return "Hello and welcome to Craftsy " + name;
    }

    @GetMapping("/about")
    public String about() {
        return "Craftsy helps creative community connect, share, and inspire.";
    }

    @GetMapping("/info")
    public String info() {
        return "Craftsy Platform\n- Share projects\n- Join groups\n- Discover events";
    }

    @GetMapping("/all")
    public String all() {
        return "Hello and welcome to Craftsy!\n"
                + "Craftsy helps creative community connect, share, and inspire.\n"
                + "Craftsy Platform\n- Share projects\n- Join groups\n- Discover events";
    }

}
