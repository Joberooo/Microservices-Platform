package com.example.api.config;

import com.example.common.CorrelationIdConstants;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder(
            @Qualifier("correlationIdClientFilter") ExchangeFilterFunction correlationIdClientFilter
    ) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(correlationIdClientFilter);
    }

    @Bean(name = "correlationIdClientFilter")
    public ExchangeFilterFunction correlationIdClientFilter() {
        return (request, next) -> {
            String correlationId = MDC.get(CorrelationIdConstants.MDC_KEY);
            if (correlationId != null && !correlationId.isBlank()) {
                ClientRequest mutated = ClientRequest.from(request)
                        .header(CorrelationIdConstants.HEADER_NAME, correlationId)
                        .build();
                return next.exchange(mutated);
            }
            return next.exchange(request);
        };
    }
}
