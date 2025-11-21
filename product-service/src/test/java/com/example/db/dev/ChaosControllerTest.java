package com.example.db.dev;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChaosControllerTest {

    private MockMvc mockMvc;
    private ChaosService service;

    @BeforeEach
    void setup() {
        service = Mockito.mock(ChaosService.class);
        ChaosController controller = new ChaosController(service);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldCallServiceWithDefaults() throws Exception {
        when(service.induceChaos(0L, 0.0)).thenReturn("OK");

        mockMvc.perform(get("/dev/chaos"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

        verify(service).induceChaos(0L, 0.0);
    }

    @Test
    void shouldPassQueryParamsToService() throws Exception {
        when(service.induceChaos(3000L, 0.5)).thenReturn("OK");

        mockMvc.perform(get("/dev/chaos")
                        .param("delayMs", "3000")
                        .param("errorRate", "0.5"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

        verify(service).induceChaos(3000L, 0.5);
    }
}
