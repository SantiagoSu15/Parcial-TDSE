package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class mathController {
    @Autowired
    public final mathService servicio;

    public mathController(org.example.mathService mathService) {
        this.servicio = mathService;
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/collat")
    public Map<String, String> getCollatz(@RequestParam(defaultValue = "1") String value){
        int v = Integer.parseInt(value);
        Map<String, String> result = new HashMap<>();
        result.put("operation", "collatzsequence");
        result.put("input", value);
        result.put("output", servicio.collatz(v));

        servicio.resetear();


        return result;
    }
}
