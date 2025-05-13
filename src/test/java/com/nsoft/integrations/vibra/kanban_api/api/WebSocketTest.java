package com.nsoft.integrations.vibra.kanban_api.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nsoft.integrations.vibra.kanban_api.api.event.TaskEvent;
import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskPriority;
import com.nsoft.integrations.vibra.kanban_api.domain.integration.IntegrationBaseIT;
import com.nsoft.integrations.vibra.kanban_api.domain.model.Task;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebSocketTest extends IntegrationBaseIT {

  private final BlockingQueue<TaskEvent> blockingQueue = new LinkedBlockingQueue<>();
  @Autowired private ObjectMapper objectMapper;
  @LocalServerPort private int port;
  private WebSocketStompClient stompClient;

  @BeforeAll
  public void setup() {
    List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
    SockJsClient sockJsClient = new SockJsClient(transports);
    stompClient = new WebSocketStompClient(sockJsClient);
    stompClient.setMessageConverter(new MappingJackson2MessageConverter());
  }

  @Test
  void shouldReceiveBroadcastAfterTaskCreation() throws Exception {
    String wsUrl = "ws://localhost:" + port + "/ws";
    StompSession session =
        stompClient.connect(wsUrl, new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);

    session.subscribe(
        "/topic/tasks",
        new StompFrameHandler() {
          @Override
          public @NotNull Type getPayloadType(@NotNull StompHeaders headers) {
            return TaskEvent.class;
          }

          @Override
          public void handleFrame(@NotNull StompHeaders headers, Object payload) {
            blockingQueue.offer((TaskEvent) payload);
          }
        });

    Task task = new Task();
    task.setId(1L);
    task.setTitle("New task");
    task.setPriority(TaskPriority.LOW);

    TaskEvent taskEvent = new TaskEvent("CREATED", task);
    session.send("/topic/tasks", taskEvent);

    TaskEvent event = blockingQueue.poll(5, TimeUnit.SECONDS);
    assertThat(event).isNotNull();

    Task payload = objectMapper.convertValue(event.payload(), Task.class);

    assertThat(payload).isNotNull();
    assertThat(payload.getTitle()).isEqualTo("New task");
    assertThat(payload.getPriority()).isEqualTo(TaskPriority.LOW);
  }
}
