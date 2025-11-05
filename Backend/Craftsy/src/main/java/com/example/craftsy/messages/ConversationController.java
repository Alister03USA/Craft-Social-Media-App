package com.example.craftsy.messages;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.feed.Feed;
import com.example.craftsy.images.Image;
import com.example.craftsy.messages.conversations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.client.WebSocketClient;

import java.net.http.WebSocket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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

    @Autowired
    MessageRepository msgRepo;

    /**
     * creates a group conversation among users
     * @param usernames
     * @return
     */
    @PostMapping("/messages/group")
    GroupConversation createGroupConvo(@RequestBody List<String> usernames){
        List<Users> users = new ArrayList<>();
        for(int i = 0; i< usernames.size(); i++){
            Optional<Users> userOpt = userRepository.findByUsername(usernames.get(i));
            users.add(userOpt.orElseThrow(() -> new RuntimeException("User not found")));
        }
        GroupConversation newConvo = new GroupConversation(users);
        newConvo.setLastMessage(LocalDateTime.now());
        GroupConversation savedConvo = groupConvoRepo.save(newConvo);

        return savedConvo;
    }

    /**
     * creates a direct convo between the two users
     * @param sender
     * @param receiver
     * @return
     */
    @PostMapping("/messages/create/{sender}/{receiver}")
    DirectConversation createDirectConvo(@PathVariable String sender, @PathVariable String receiver){
        List<Users> users = new ArrayList<>();

        Optional<Users> userOpt = userRepository.findByUsername(sender);
        users.add(userOpt.orElseThrow(() -> new RuntimeException("User not found")));

        userOpt = userRepository.findByUsername(receiver);
        users.add(userOpt.orElseThrow(() -> new RuntimeException("User not found")));

        DirectConversation newConvo = new DirectConversation(users);
        newConvo.setLastMessage(LocalDateTime.now());
        DirectConversation savedConvo = directConvoRepo.save(newConvo);

        return savedConvo;
    }

    /**
     * gets all conversations that a user is a member of
     * @param username
     * @return
     */
    @GetMapping("/messages/convos/{username}")
    List<Conversation> getConvosUser(@PathVariable String username){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Conversation> convos = new ArrayList<>();
        Optional<List<DirectConversation>> directOpt = directConvoRepo.findAllByMemberUsername(username);
        Optional<List<GroupConversation>> groupOpt = groupConvoRepo.findAllByMemberUsername(username);
        if(directOpt.isPresent()){
            convos.addAll(directOpt.orElseThrow(() -> new RuntimeException("conversation not found")));
        }
        if(groupOpt.isPresent()){
            convos.addAll(groupOpt.orElseThrow(() -> new RuntimeException("conversation not found")));
        }
        convos.sort(Comparator.comparing(
                        Conversation::getLastMessage,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).reversed()
        );
        return convos;
    }

    /**
     * gets a conversation
     * @param convoId
     * @return
     */
    @GetMapping("/messages/{convoId}")
    Conversation getConversation(@PathVariable String convoId){
        Conversation convo;
        if(convoId.startsWith("G-")){
            convo = groupConvoRepo.findById(convoId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
        }
        else if(convoId.startsWith("D-")){
            convo = directConvoRepo.findById(convoId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
        }
        else{
            throw(new RuntimeException("Conversation not found"));
        }
        return convo;
    }

    /**
     * deletes a conversation
     * @param convoId
     * @return
     */
    @DeleteMapping("/messages/convo/{convoId}")
    String deleteConversation(@PathVariable String convoId){

        if(convoId.startsWith("G-")){
            GroupConversation gconvo = groupConvoRepo.findById(convoId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
            groupConvoRepo.delete(gconvo);
        }
        else if(convoId.startsWith("D-")){
            DirectConversation dconvo = directConvoRepo.findById(convoId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
            directConvoRepo.delete(dconvo);
        }
        else{
            throw(new RuntimeException("Conversation not found"));
        }
        return "Conversation deleted";
    }

    /**
     * sets the group pic
     * @param groupId
     * @param image
     * @return
     */
    @PutMapping("/messages/{groupId}/pic")
    GroupConversation updateGroupPic(@PathVariable String groupId, @RequestBody Image image){
        GroupConversation convo = groupConvoRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        convo.setGroupPic(image);
        return groupConvoRepo.save(convo);
    }

    /**
     * add a user to a group
     * @param groupId
     * @param username
     * @return
     */
    @PutMapping("/messages/{groupId}/add/{username}")
    GroupConversation addUser(@PathVariable String groupId, @PathVariable String username){
        GroupConversation convo = groupConvoRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        convo.addMember(user);
        return groupConvoRepo.save(convo);
    }

    /**
     * remove a user from a group
     * @param groupId
     * @param username
     * @return
     */
    @PutMapping("/messages/{groupId}/remove/{username}")
    GroupConversation removeUser(@PathVariable String groupId, @PathVariable String username){
        GroupConversation convo = groupConvoRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        convo.removeMember(user);
        return groupConvoRepo.save(convo);
    }

    /**
     * deletes a message
     * @param messageId
     * @return
     */
    @DeleteMapping("/messages/{messageId}")
    String deleteMessage(@PathVariable Long messageId){
        Message message = msgRepo.findById(messageId)
                .orElseThrow(()->new RuntimeException("Message not found"));
        msgRepo.delete(message);
        return "Message deleted";
    }
}
