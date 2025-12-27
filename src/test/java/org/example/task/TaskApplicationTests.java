package org.example.task;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class TaskApplicationTests {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void shouldReturnRepositories() {
        webTestClient.get()
                .uri("/api/mfigarasgfafd/repos")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldReturn404WhenUserDoesNotExist() {
        webTestClient.get()
                .uri("/api/mfigarasgfafd123/repos")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").exists();
    }

}