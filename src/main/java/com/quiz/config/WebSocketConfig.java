package com.quiz.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	
	@Value("${spring.rabbitmq.host}")
    private String rabbitHost;

    @Value("${spring.rabbitmq.port}")
    private int rabbitPort;

    @Value("${spring.rabbitmq.username}")
    private String rabbitUser;

    @Value("${spring.rabbitmq.password}")
    private String rabbitPass;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable rabbitMQ message broker
        // /topic for broadcasting to all subscribers
        // /queue for point-to-point messaging
    	config.enableStompBrokerRelay("/topic", "/queue")
        	.setRelayHost(rabbitHost)
        	.setRelayPort(rabbitPort)
        	.setClientLogin(rabbitUser)
        	.setClientPasscode(rabbitPass)
        	.setSystemLogin(rabbitUser)
        	.setSystemPasscode(rabbitPass);

        // Prefix for messages from clients to server
        config.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint that clients will connect to
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(
            org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(new org.springframework.messaging.support.ChannelInterceptor() {
            @Override
            public org.springframework.messaging.Message<?> preSend(org.springframework.messaging.Message<?> message,
                    org.springframework.messaging.MessageChannel channel) {
                org.springframework.messaging.simp.stomp.StompHeaderAccessor accessor = org.springframework.messaging.support.MessageHeaderAccessor
                        .getAccessor(message, org.springframework.messaging.simp.stomp.StompHeaderAccessor.class);

                if (accessor != null) {
                    org.springframework.messaging.simp.stomp.StompCommand command = accessor.getCommand();
                    String destination = accessor.getDestination();

                    if (command != null) {
                        System.out.println("WebSocket INBOUND: command=" + command + ", destination=" + destination);
                        if (command == org.springframework.messaging.simp.stomp.StompCommand.SEND) {
                            Object payload = message.getPayload();
                            if (payload instanceof byte[]) {
                                System.out.println("Payload: " + new String((byte[]) payload));
                            } else {
                                System.out.println("Payload: " + payload);
                            }
                        }
                    }
                }
                return message;
            }
        });
    }
}
