package com.example.api.dev;

import com.example.api.products.client.ProductDbClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/dev/chaos")
@RequiredArgsConstructor
@Tag(name = "Chaos", description = "Endpoints for simulating latency and failures in the product service")
public class ChaosController {

    private final ProductDbClient productDbClient;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Trigger chaos in the product-service",
            description = "Introduces artificial latency and random errors in the downstream product-service to test resiliency.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Mono<String> induceChaos(
            @RequestParam(required = false, defaultValue = "100")
            @Parameter(description = "Artificial delay in milliseconds added before the response is returned")
            long delayMs,
            @RequestParam(required = false, defaultValue = "0.5")
            @Parameter(description = "Probability in range [0, 1] of forcing an error response from the downstream service")
            double errorRate
    ) {
        return productDbClient.induceChaos(delayMs, errorRate);
    }
}
