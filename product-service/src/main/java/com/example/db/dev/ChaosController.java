package com.example.db.dev;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
public class ChaosController {

    private final ChaosService chaosService;

    @GetMapping("/chaos")
    @ResponseStatus(HttpStatus.OK)
    public String chaos(
            @RequestParam(required = false, defaultValue = "0") long delayMs,
            @RequestParam(required = false, defaultValue = "0") double errorRate
    ) throws InterruptedException {
        return chaosService.induceChaos(delayMs, errorRate);
    }
}
