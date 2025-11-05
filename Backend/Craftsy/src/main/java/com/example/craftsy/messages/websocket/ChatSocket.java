package com.example.craftsy.messages.websocket;


import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.messages.Message;
import com.example.craftsy.messages.MessageRepository;
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

@Controller
@ServerEndpoint(value = "/chat/{convoId}/{username}", configurator = SpringConfigurator.class)
public class ChatSocket {
    private static final Logger logger = LoggerFactory.getLogger(ChatSocket.class);

    private static Map<Session, String> sessionUsernameMap = new Hashtable<>();
    private static Map<String, Session> usernameSessionMap = new Hashtable<>();

    private MessageService messageService;
    private UserRepository userRepo;
    private MessageRepository msgRepo;
    private GroupConversationRepository groupConvoRepo;
    private DirectConversationRepository directConvoRepo;

    private static Map<Session, Conversation> sessionConvoMap = new Hashtable<>();
    private static Map<Session, String> sessionConvoTypeMap = new Hashtable<>();

    

    public ChatSocket(){}

    public void setGroupConvoRepo(GroupConversationRepository repo) { this.groupConvoRepo = repo; }
    public void setDirectConvoRepo(DirectConversationRepository repo) { this.directConvoRepo = repo; }
    public void setMsgRepo(MessageRepository repo) { this.msgRepo = repo; }
    public void setUserRepo(UserRepository repo) { this.userRepo = repo; }
    public void setMessageService(MessageService service) { this.messageService = service; }

    /**
     * opens server to convo
     * @param session
     * @param convoId
     * @param username
     */
    @OnOpen
    public void onOpen(Session session,@PathParam("convoId") String convoId, @PathParam("username") String username){
        sessionUsernameMap.put(session, username);
        usernameSessionMap.put(username, session);

        Conversation convo;
        if (convoId.startsWith("D-")) {
            convo = directConvoRepo.findByIdWithMembers(convoId)
                    .orElseThrow(() -> new RuntimeException("Direct conversation not found"));
        } else if (convoId.startsWith("G-")) {
            convo = groupConvoRepo.findByIdWithMembers(convoId)
                    .orElseThrow(() -> new RuntimeException("Group conversation not found"));
        } else {
            throw new RuntimeException("Invalid conversation ID format");
        }
        sessionConvoMap.put(session, convo);
        sessionConvoTypeMap.put(session, convoId.startsWith("G-") ? "GROUP" : "DIRECT");

        boolean inConversation = convo.getMembers()
                .stream()
                .anyMatch(member -> member.getUsername().equals(username));

        if (!inConversation) {
            throw new RuntimeException("User not in conversation");
        }
        logger.info("Entered into Open");

    }


    /**
     * when message is sent
     * @param session
     * @param text
     * @throws IOException
     */
    @OnMessage
    public void onMessage(Session session, String text) throws IOException {
        // Handle new messages
        logger.info("Entered into Message: Got Message:" + text);
        String username = sessionUsernameMap.get(session);
        Conversation convo = sessionConvoMap.get(session);
        String convoType = sessionConvoTypeMap.get(session);
        String convoId = sessionConvoMap.get(session).getId();

        //call if message is reaction
        if (text.startsWith("#react:")) {
            broadcast(messageService.react(text));
            return;
        }

        //call if message is a reply
        if(text.startsWith("#reply:")){
            broadcast(messageService.reply(text, username, convoId));
            return;
        }

        //call if message is removing a reaction
        if(text.startsWith("#!react:")){
            broadcast(messageService.removeReaction(text));
            return;
        }

        //broadcasts and saves standard message
        Message message = new Message(username, text, convo);
        messageService.saveMessageAndUpdateConversation(convo.getId(), message);

        broadcast(username + ": " + text);
    }

    /**
     * when server is closed
     * @param session
     * @throws IOException
     */
    @OnClose
    public void onClose(Session session) throws IOException {
        logger.info("Entered into Close");

        // remove the user connection information
        String username = sessionUsernameMap.get(session);
        sessionUsernameMap.remove(session);
        usernameSessionMap.remove(username);
    }


    /**
     * when error is thrown
     * @param session
     * @param throwable
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
        logger.info("Entered into Error");
        logger.error("WebSocket error for session {}: {}", session.getId(), throwable.getMessage(), throwable);
        throwable.printStackTrace();
    }


    /**
     * private method that send message to one user
     * @param username
     * @param message
     */
    private void sendMessageToParticularUser(String username, String message) {
        try {
            usernameSessionMap.get(username).getBasicRemote().sendText(message);
        }
        catch (IOException e) {
            logger.info("Exception: " + e.getMessage().toString());
            e.printStackTrace();
        }
    }

    /**
     * private method that broadcasts message to all users
     * @param message
     */
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

    /**
     * gets the complete history of the chat
     * @return
     */
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
