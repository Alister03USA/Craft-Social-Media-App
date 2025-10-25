package com.example.craftsy.messages;

import com.example.craftsy.messages.conversations.DirectConversationRepository;
import com.example.craftsy.messages.conversations.GroupConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ConversationController {

    @Autowired
    GroupConversationRepository groupConvoRepo;

    @Autowired
    DirectConversationRepository directConvoRepo;

    @PostMapping("/messages/group")
    String createGroupConvo(@RequestBody List<String> usernames){

    }
}
