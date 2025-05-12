import SockJS from 'sockjs-client';
import {Client} from '@stomp/stompjs';

const stompClient = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws/tasks'),
  reconnectDelay: 5000,
  debug: (msg) => console.debug('[STOMP]', msg),
});

stompClient.onConnect = frame => {
  console.log('Connected to STOMP, user:',
      frame.headers['user-name'] || 'n/a');

  const subscription = stompClient.subscribe('/topic/tasks', message => {
    const event = JSON.parse(message.body);
    console.log('Received TaskEvent:', event);
  });
};

stompClient.onStompError = frame => {
  console.error('Error:', frame.headers['message']);
  console.error('Details:', frame.body);
};

stompClient.activate();
