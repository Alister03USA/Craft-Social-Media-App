package com.example.craftsy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@SpringBootTest
@ActiveProfiles("test")
class CraftsyApplicationTests {

    @Test
    void contextLoads() {
    }


}

