package com.example.craftsy.messages;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.messages.conversations.DirectConversation;
import com.example.craftsy.messages.conversations.DirectConversationRepository;
import com.example.craftsy.messages.conversations.GroupConversation;
import com.example.craftsy.messages.conversations.GroupConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.client.WebSocketClient;

import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class ConversationController {

    @Autowired
    GroupConversationRepository groupConvoRepo;

    @Autowired
    DirectConversationRepository directConvoRepo;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/messages/group")
    GroupConversation createGroupConvo(@RequestBody List<String> usernames){
        List<Users> users = new ArrayList<>();
        for(int i = 0; i< usernames.size(); i++){
            Optional<Users> userOpt = userRepository.findByUsername(usernames.get(i));
            users.add(userOpt.orElseThrow(() -> new RuntimeException("User not found")));
        }
        GroupConversation newConvo = new GroupConversation(users);
        GroupConversation savedConvo = groupConvoRepo.save(newConvo);

        return savedConvo;
    }

    @PostMapping("/messages/create/{sender}/{receiver}")
    DirectConversation createDirectConvo(@PathVariable String sender, @PathVariable String receiver){
        List<Users> users = new ArrayList<>();

        Optional<Users> userOpt = userRepository.findByUsername(sender);
        users.add(userOpt.orElseThrow(() -> new RuntimeException("User not found")));

        userOpt = userRepository.findByUsername(receiver);
        users.add(userOpt.orElseThrow(() -> new RuntimeException("User not found")));

        DirectConversation newConvo = new DirectConversation(users);
        DirectConversation savedConvo = directConvoRepo.save(newConvo);

        return savedConvo;
    }
}
