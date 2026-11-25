
package com.gstcopilot.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> healthCheck() {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("status", "UP");
        response.put("message", "GST Notice Copilot Backend is running");
        response.put("application", "GST Notice Copilot");

        return response;
    }
}
