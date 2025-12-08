package com.example.craftsy.messages;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.feed.Feed;
import com.example.craftsy.images.Image;
import com.example.craftsy.messages.conversations.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "Create a group conversation",
            description = "Creates a new group chat with the provided list of usernames."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group conversation created",
                    content = @Content(schema = @Schema(implementation = GroupConversation.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/messages/group")
    GroupConversation createGroupConvo(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "List of usernames to include in the group",
            required = true)
            @RequestBody List<String> usernames){
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
    @Operation(
            summary = "Create a direct conversation",
            description = "Creates a private conversation between the sender and receiver."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Direct conversation created",
                    content = @Content(schema = @Schema(implementation = DirectConversation.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/messages/create/{sender}/{receiver}")
    DirectConversation createDirectConvo(@Parameter(description = "Username of the sender") @PathVariable String sender,
                                         @Parameter(description = "Username of the receiver") @PathVariable String receiver){
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
    @Operation(
            summary = "Get all conversations for a user",
            description = "Returns both direct and group conversations the user participates in, "
                    + "sorted by most recent activity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of conversations",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Conversation.class)))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/messages/convos/{username}")
    List<Conversation> getConvosUser(@Parameter(description = "Username to search for") @PathVariable String username){
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
    @Operation(
            summary = "Get a conversation by ID",
            description = "Fetches either a group or direct conversation depending on its ID prefix."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversation found",
                    content = @Content(schema = @Schema(implementation = Conversation.class))),
            @ApiResponse(responseCode = "404", description = "Conversation not found")
    })
    @GetMapping("/messages/{convoId}")
    Conversation getConversation(@Parameter(description = "Conversation ID (G-xxx or D-xxx)") @PathVariable String convoId){
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
    @Operation(
            summary = "Delete a conversation",
            description = "Deletes a conversation of any type based on its conversation ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversation deleted"),
            @ApiResponse(responseCode = "404", description = "Conversation not found")
    })
    @DeleteMapping("/messages/convo/{convoId}")
    String deleteConversation(@Parameter(description = "Conversation ID") @PathVariable String convoId){

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
    @Operation(
            summary = "Update group picture",
            description = "Sets or updates the image used as the group chat's profile picture."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group picture updated",
                    content = @Content(schema = @Schema(implementation = GroupConversation.class))),
            @ApiResponse(responseCode = "404", description = "Group conversation not found")
    })
    @PutMapping("/messages/{groupId}/pic")
    GroupConversation updateGroupPic(@Parameter(description = "Group ID") @PathVariable String groupId,
                                     @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                             description = "Image object representing the group picture",
                                             required = true,
                                             content = @Content(schema = @Schema(implementation = Image.class))
                                     )
                                     @RequestBody Image image){
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
    @Operation(
            summary = "Add a user to a group conversation",
            description = "Adds a new user to the specified group chat."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User added",
                    content = @Content(schema = @Schema(implementation = GroupConversation.class))),
            @ApiResponse(responseCode = "404", description = "Group or user not found")
    })
    @PutMapping("/messages/{groupId}/add/{username}")
    GroupConversation addUser(@Parameter(description = "Group ID") @PathVariable String groupId,
                              @Parameter(description = "Username to add") @PathVariable String username){
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
    @Operation(
            summary = "Remove a user from a group conversation",
            description = "Removes the specified user from the group."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User removed",
                    content = @Content(schema = @Schema(implementation = GroupConversation.class))),
            @ApiResponse(responseCode = "404", description = "Group or user not found")
    })
    @PutMapping("/messages/{groupId}/remove/{username}")
    GroupConversation removeUser(@Parameter(description = "Group ID") @PathVariable String groupId,
                                 @Parameter(description = "Username to remove") @PathVariable String username){
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
    @Operation(
            summary = "Delete a message",
            description =   "Deletes a single message by its message ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Message deleted"),
            @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @DeleteMapping("/messages/{messageId}")
    String deleteMessage(@Parameter(description = "Message ID") @PathVariable Long messageId){
        Message message = msgRepo.findById(messageId)
                .orElseThrow(()->new RuntimeException("Message not found"));
        msgRepo.delete(message);
        return "Message deleted";
    }
}
