package com.example.db.dev;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
public class ChaosService {

    private final Random random = new Random();

    public String induceChaos(long delayMs, double errorRate) throws InterruptedException {
        if (delayMs > 0) {
            Thread.sleep(delayMs);
        }

        double r = random.nextDouble();
        if (errorRate > 0 && r < errorRate) {
            log.warn("Chaos endpoint inducing error. delayMs={}, errorRate={}, r={}", delayMs, errorRate, r);
            throw new RuntimeException("Chaos induced error");
        }

        return "OK";
    }
}
