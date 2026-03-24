package org.example;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/")
public class proxyController {
    @Autowired
    private proxyService proxyService;
    @CrossOrigin(origins = "*")
    @GetMapping("/proxy/**")
    public Map<String, String> proxy(HttpServletRequest request) {
        String path = request.getRequestURI().replace("/proxy", "");
        return proxyService.llamarActivo(path);
    }
}
