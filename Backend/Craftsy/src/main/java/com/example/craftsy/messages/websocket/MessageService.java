package com.example.craftsy.messages.websocket;

import com.example.craftsy.messages.Message;
import com.example.craftsy.messages.MessageRepository;
import com.example.craftsy.messages.conversations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    private final MessageRepository msgRepo;
    private final GroupConversationRepository groupRepo;
    private final DirectConversationRepository directRepo;

    @Autowired
    public MessageService(MessageRepository msgRepo,
                       GroupConversationRepository groupRepo,
                       DirectConversationRepository directRepo) {
        this.msgRepo = msgRepo;
        this.groupRepo = groupRepo;
        this.directRepo = directRepo;
    }

    @Transactional
    public String react(String text){
        int idIndex = text.indexOf(":",7);
        Long messageId = Long.parseLong(text.substring(7,idIndex));
        String reaction = text.substring(idIndex + 1);
        Message message = msgRepo.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        message.addReaction(reaction);
        msgRepo.save(message);

        return messageId + ":" + reaction + ":" + message.getReactions().get(reaction);
    }

    @Transactional
    public void saveMessageAndUpdateConversation(String convoId, Message message) {
        msgRepo.save(message);

        Conversation convo;
        if (convoId.startsWith("G-")) {
            convo = groupRepo.findByIdWithMembers(convoId)
                    .orElseThrow(() -> new RuntimeException("Group conversation not found"));
        } else if (convoId.startsWith("D-")) {
            convo = directRepo.findByIdWithMembers(convoId)
                    .orElseThrow(() -> new RuntimeException("Direct conversation not found"));
        } else {
            throw new RuntimeException("Invalid conversation ID");
        }

        convo.addMessage(message);

        if (convo instanceof GroupConversation) {
            groupRepo.save((GroupConversation) convo);
        } else if (convo instanceof DirectConversation) {
            directRepo.save((DirectConversation) convo);
        }
    }

    @Transactional
    public String reply(String text, String username, String convoId){
        int idIndex = text.indexOf(":",7);
        Long messageId = Long.parseLong(text.substring(7,idIndex));
        String words = text.substring(idIndex + 1);
        Message message = msgRepo.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        Conversation convo;
        if (convoId.startsWith("G-")) {
            convo = groupRepo.findByIdWithMembers(convoId)
                    .orElseThrow(() -> new RuntimeException("Group conversation not found"));
        } else if (convoId.startsWith("D-")) {
            convo = directRepo.findByIdWithMembers(convoId)
                    .orElseThrow(() -> new RuntimeException("Direct conversation not found"));
        } else {
            throw new RuntimeException("Invalid conversation ID");
        }

        Message reply = new Message(username, words,null, message);
        message.addReply(reply);
        msgRepo.save(reply);
        msgRepo.save(message);
        return messageId + "-" + words;
    }

    @Transactional
    public String removeReaction(String text){
        int idIndex = text.indexOf(":",8);
        Long messageId = Long.parseLong(text.substring(8,idIndex));
        String reaction = text.substring(idIndex + 1);
        Message message = msgRepo.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.removeReaction(reaction);
        msgRepo.save(message);
        return messageId + ":" + reaction + ":" + message.getReactions().get(reaction);
    }
}
