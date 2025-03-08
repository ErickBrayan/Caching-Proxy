package com.eb2.cachingproxy;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class ProxyController {

    @Value("${url.origin}")
    String origin;



    private final RestTemplate restTemplate;

    Cache<String, ResponseEntity<String>> cache =   Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(100)
            .build();

    @GetMapping("/**")
    public ResponseEntity<?> proxy(HttpServletRequest request) {



        String path = request.getServletPath();
        String query = request.getQueryString();
        String finalPath = origin + path + (query != null ? query : "");


        ResponseEntity<String> present = cache.getIfPresent(finalPath);

        if (present != null) {

            return ResponseEntity
                    .status(present.getStatusCode())
                    .headers(present.getHeaders())
                    .header("X-Cache", "HIT")
                    .body("");
        }

        ResponseEntity<String> forEntity = restTemplate.getForEntity(finalPath, String.class);

        cache.put(finalPath, forEntity);


        return ResponseEntity
                .status(forEntity.getStatusCode())
                .headers(forEntity.getHeaders())
                .header("X-Cache", "MISS")
                .body("");

    }

    @PostMapping("/clear-cache")
    public String clearCache(){
        cache.invalidateAll();
        return "Cache cleared.";
    }
}
