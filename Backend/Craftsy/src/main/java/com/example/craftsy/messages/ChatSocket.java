package com.example.craftsy.messages;


import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.messages.conversations.*;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@ServerEndpoint(value = "/chat/{convoId}/{username}")
public class ChatSocket {
    private static MessageRepository msgRepo;
    private static GroupConversationRepository groupConvoRepo;
    private static DirectConversationRepository directConvoRepo;
    private static UserRepository userRepo;

    /*
     * Grabs the MessageRepository singleton from the Spring Application
     * Context.  This works because of the @Controller annotation on this
     * class and because the variable is declared as static.
     * There are other ways to set this. However, this approach is
     * easiest.
     */
    @Autowired
    public void setMessageRepository(MessageRepository repo) {
        msgRepo = repo;  // we are setting the static variable
    }

    @Autowired
    public void setGroupConvoRepository(GroupConversationRepository repo) {
        groupConvoRepo = repo;  // we are setting the static variable
    }

    @Autowired
    public void setDirectConvoRepository(DirectConversationRepository repo) {
        directConvoRepo = repo;  // we are setting the static variable
    }

    @Autowired
    public void setUserRepository(UserRepository repo) {
        userRepo = repo;  // we are setting the static variable
    }

    // Store all socket session and their corresponding username.
    private static Map<Session, String> sessionUsernameMap = new Hashtable<>();
    private static Map<String, Session> usernameSessionMap = new Hashtable<>();

    private final Logger logger = LoggerFactory.getLogger(ChatSocket.class);

    @OnOpen
    public void onOpen(Session session,@PathParam("convoId") String convoId, @PathParam("username") String username){
        sessionUsernameMap.put(session, username);
        usernameSessionMap.put(username, session);

        Optional<DirectConversation> directConvo = directConvoRepo.findById(convoId);
        Optional<GroupConversation> groupConvo = groupConvoRepo.findById(convoId);

        Conversation convo;
        if(directConvo.isPresent()){
            convo = directConvo.orElseThrow();
        }
        else if(groupConvo.isPresent()){
            convo = groupConvo.orElseThrow();
        }
        else{
            throw new RuntimeException("group not found");
        }

        Users user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(!convo.getMembers().contains(user)){
            throw new RuntimeException("User not in conversation");
        }

        logger.info("Entered into Open");

        // store connecting user information

        //Send chat history to the newly connected user
        sendMessageToParticularUser(username, getChatHistory());

    }



    @OnMessage
    public void onMessage(Session session, String message) throws IOException {

        // Handle new messages
        logger.info("Entered into Message: Got Message:" + message);
        String username = sessionUsernameMap.get(session);

        broadcast(username + ": " + message);

        // Saving chat history to repository
        msgRepo.save(new Message(username, message));
    }


    @OnClose
    public void onClose(Session session) throws IOException {
        logger.info("Entered into Close");

        // remove the user connection information
        String username = sessionUsernameMap.get(session);
        sessionUsernameMap.remove(session);
        usernameSessionMap.remove(username);
    }


    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
        logger.info("Entered into Error");
        throwable.printStackTrace();
    }


    private void sendMessageToParticularUser(String username, String message) {
        try {
            usernameSessionMap.get(username).getBasicRemote().sendText(message);
        }
        catch (IOException e) {
            logger.info("Exception: " + e.getMessage().toString());
            e.printStackTrace();
        }
    }


    private void broadcast(String message) {
        sessionUsernameMap.forEach((session, username) -> {
            try {
                session.getBasicRemote().sendText(message);
            }
            catch (IOException e) {
                logger.info("Exception: " + e.getMessage().toString());
                e.printStackTrace();
            }

        });

    }

    private String getChatHistory() {
        List<Message> messages = msgRepo.findAll();

        // convert the list to a string
        StringBuilder sb = new StringBuilder();
        if(messages != null && messages.size() != 0) {
            for (Message message : messages) {
                sb.append(message.getSender() + ": " + message.getText() + "\n");
            }
        }
        return sb.toString();
    }



}
