package com.complaintandfeedback.Controller;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
//@RequestMapping("/api")
public class PatternRecognizationController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/patternrecognization")
    public ResponseEntity<Object> getAiReport() {
    	String flaskUrl = "http://localhost:5000/aireport";  // Flask endpoint

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(flaskUrl, String.class);
          Map<String,String> map=new  HashMap<String,String>();
          map.put("text", response.getBody());
          return ResponseEntity.ok(map); 
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error calling Python API: " + e.getMessage());
        }
    }
}
