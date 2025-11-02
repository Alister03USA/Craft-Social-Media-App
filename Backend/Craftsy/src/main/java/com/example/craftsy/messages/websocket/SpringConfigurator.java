package com.example.craftsy.messages.websocket;

import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.messages.MessageRepository;
import com.example.craftsy.messages.conversations.DirectConversationRepository;
import com.example.craftsy.messages.conversations.GroupConversationRepository;
import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.stereotype.Component;

@Component
public class SpringConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException {
        if (clazz.equals(ChatSocket.class)) {
            ChatSocket socket = new ChatSocket();

            socket.setGroupConvoRepo(SpringContext.getBean(GroupConversationRepository.class));
            socket.setDirectConvoRepo(SpringContext.getBean(DirectConversationRepository.class));
            socket.setMsgRepo(SpringContext.getBean(MessageRepository.class));
            socket.setUserRepo(SpringContext.getBean(UserRepository.class));
            socket.setMessageService(SpringContext.getBean(MessageService.class));

            return (T) socket;
        }
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new InstantiationException(e.getMessage());
        }
    }
}
