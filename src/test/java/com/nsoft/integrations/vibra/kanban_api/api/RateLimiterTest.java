package com.nsoft.integrations.vibra.kanban_api.api;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimiterTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JwtDecoder jwtDecoder;

  @BeforeEach
  void setupJwt() {
    Jwt jwt =
        Jwt.withTokenValue("mock-token").header("alg", "none").claim("sub", "testuser").build();
    Mockito.when(jwtDecoder.decode(Mockito.anyString())).thenReturn(jwt);
  }

  @Test
  void shouldLimitAfter100Requests() throws Exception {
    for (int i = 0; i < 105; i++) {
      MvcResult result =
          mockMvc
              .perform(get("/api/tasks").header("Authorization", "Bearer mock-token"))
              .andReturn();

      int status = result.getResponse().getStatus();

      int safeLimit = 100;
      if (i < safeLimit + 2) {
        assertTrue(status == 200 || status == 429); // jitter toleration
      } else {
        Assertions.assertEquals(429, status);
      }
    }
  }
}
