package com.example.api.config.security;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "jwt.secret=0123456789_0123456789_0123456789_01"
        }
)
@AutoConfigureMockMvc
class JwtSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class TestControllerConfig {

        @RestController
        static class SecuredTestController {

            @GetMapping("/secured/hello")
            public void hello(HttpServletResponse response) {
                response.setStatus(HttpServletResponse.SC_OK);
            }
        }
    }

    @Test
    void shouldReturn401ForSecuredEndpointWithoutJwt() throws Exception {
        mockMvc.perform(get("/secured/hello"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn200ForSecuredEndpointWithValidJwt() throws Exception {
        mockMvc.perform(get("/secured/hello")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()))
                .andExpect(status().isOk());
    }
}
