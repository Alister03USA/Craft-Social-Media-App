package com.example.craftsy.events;

import com.example.craftsy.Group.Entity.Group;
import com.example.craftsy.Group.Repository.GroupRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.events.eventsComments.EventComment;
import com.example.craftsy.events.eventsComments.EventCommentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
public class EventController {

    @Autowired
    EventRepository eventRepo;

    @Autowired
    UserRepository userRepo;

    @Autowired
    GroupRepository groupRepo;

    @Autowired
    EventCommentRepository eventCommentRepo;

    /**
     * Creates a public event that is not included in a group
     * @param username
     * @param event
     * @param eventDate
     * @return saved event
     */
    @Operation(
            summary = "Create a public event",
            description = "Creates a public event that is not included in a group."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event created",
                    content = @Content(schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/event/create/{username}/{eventDate}")
    Event createPublicEvent(@Parameter(description = "Username of the event host") @PathVariable String username,
                            @Parameter(description = "Event details") @RequestBody Event event,
                            @Parameter(description = "Event date in ISO-8601 format") @PathVariable String eventDate){
        Users user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LocalDateTime date = LocalDateTime.parse(eventDate);
        event.setEventDate(date);
        event.setEventHost(user);
        event.setPublic(true);
        event.setDateCreated(LocalDateTime.now());
        return eventRepo.save(event);
    }

    /**
     * creates a private event that is within a certain group
     * @param username
     * @param groupId
     * @param eventDate
     * @param event
     * @return saved event
     */
    @Operation(
            summary = "Create a group event",
            description = "Creates a private event that belongs to a specific group."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event created",
                    content = @Content(schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "404", description = "User or Group not found")
    })
    @PostMapping("/event/create/{username}/{groupId}/{eventDate}")
    Event createGroupEvent(@Parameter(description = "Username of the event host") @PathVariable String username,
                           @Parameter(description = "ID of the group") @PathVariable Long groupId,
                           @Parameter(description = "Event date in ISO-8601 format") @PathVariable String eventDate,
                           @Parameter(description = "Event details") @RequestBody Event event){
        Users user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        LocalDateTime date = LocalDateTime.parse(eventDate);
        event.setEventDate(date);
        event.setEventHost(user);
        event.setGroup(group);
        event.setPublic(false);
        event.setDateCreated(LocalDateTime.now());
        return eventRepo.save(event);
    }

    /**
     * deletes an event
     * @param eventId
     * @return "Event deleted"
     */
    @Operation(
            summary = "Delete an event",
            description = "Deletes an event by ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event deleted"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @DeleteMapping("/event/{eventId}")
    String deleteEvent(@Parameter(description = "ID of the event to delete")
                       @PathVariable Long eventId){
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        eventRepo.delete(event);
        return "Event deleted";
    }

    /**
     * update event with event body info
     * @param eventId
     * @param newEvent
     * @return updated event
     */
    @Operation(
            summary = "Update an event",
            description = "Updates an event's details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event updated",
                    content = @Content(schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @PutMapping("/event/{eventId}")
    Event updateEvent(@Parameter(description = "ID of the event to update") @PathVariable Long eventId,
                      @Parameter(description = "Updated event details") @RequestBody Event newEvent){
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        if(newEvent.getEventName() != null){
            event.setEventName(newEvent.getEventName());
        }
        if(newEvent.getCraftType() != null){
            event.setCraftType(newEvent.getCraftType());
        }
        if(newEvent.getDescription() != null){
            event.setDescription(newEvent.getDescription());
        }
        if(newEvent.getImage() != null){
            event.setImage(newEvent.getImage());
        }

        return eventRepo.save(event);
    }

    /**
     * updates the date of an event
     * @param eventID
     * @param eventDate
     * @return updated event
     */
    @Operation(
            summary = "Update event date",
            description = "Updates the date of a specific event."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event date updated",
                    content = @Content(schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @PutMapping("/event/{eventID}/date/{eventDate}")
    Event updateEventDate(@Parameter(description = "ID of the event") @PathVariable Long eventID,
                          @Parameter(description = "New event date in ISO-8601 format") @PathVariable String eventDate){
        Event event = eventRepo.findById(eventID)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        LocalDateTime date = LocalDateTime.parse(eventDate);
        event.setEventDate(date);
        return eventRepo.save(event);
    }

    /**
     * gets one event
     * @param eventID
     * @return
     */
    @Operation(
            summary = "Get an event",
            description = "Fetch a specific event by ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event retrieved",
                    content = @Content(schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @GetMapping("/event/{eventID}")
    Event getEvent(@Parameter(description = "ID of the event") @PathVariable Long eventID){
        Event event = eventRepo.findById(eventID)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return event;
    }

    /**
     * add a users rsvp to event
     * @param eventID
     * @param username
     * @param status "yes" or "no"
     * @return rsvp yes list
     */
    @Operation(
            summary = "RSVP to an event",
            description = "Adds a user's RSVP ('yes' or 'no') to the event."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "RSVP updated",
                    content = @Content(schema = @Schema(implementation = Users.class))),
            @ApiResponse(responseCode = "404", description = "User or Event not found")
    })
    @PutMapping("/event/{eventID}/{username}/{status}")
    List<Users> rsvpToEvent(@Parameter(description = "ID of the event") @PathVariable Long eventID,
                            @Parameter(description = "Username of the user RSVPing") @PathVariable String username,
                            @Parameter(description = "'yes' or 'no'") @PathVariable String status){
        Event event = eventRepo.findById(eventID)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Users user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(status.equals("yes")){
            event.addRsvpYes(user);
        }
        else if(status.equals("no")){
            event.addRsvpNo(user);
        }
        eventRepo.save(event);
        return event.getRsvpYes();
    }

    /**
     * get rsvp yes list
     * @param eventID
     * @return
     */
    @Operation(
            summary = "Get RSVP yes list",
            description = "Returns all users who RSVPed yes for an event."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "RSVP yes list retrieved"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @GetMapping("/event/{eventID}/yes")
    List<Users> getRsvpYes(@Parameter(description = "ID of the event") @PathVariable Long eventID){
        Event event = eventRepo.findById(eventID)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return event.getRsvpYes();
    }

    /**
     * get rsvp no list
     * @param eventID
     * @return
     */
    @Operation(
            summary = "Get RSVP no list",
            description = "Returns all users who RSVPed no for an event."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "RSVP no list retrieved"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @GetMapping("/event/{eventID}/no")
    List<Users> getRsvpNo(@Parameter(description = "ID of the event") @PathVariable Long eventID){
        Event event = eventRepo.findById(eventID)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return event.getRsvpNo();
    }

    /**
     * Gets all events where the user has rsvped yes
     * @param username
     * @return list of events in chronological order
     */
    @Operation(
            summary = "Get user's events",
            description = "Returns all events where the user RSVPed yes, in chronological order."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Events retrieved",
                    content = @Content(schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/event/user/{username}")
    List<Event> getUsersEvents(@Parameter(description = "Username of the user") @PathVariable String username){
        Users user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Optional<List<Event>> optEvents = eventRepo.findByRsvpYesContaining(user);
        List<Event> events = new ArrayList<>();
        if(optEvents.isPresent()){
            events = optEvents.orElseThrow();
        }
        events.sort(Comparator.comparing(Event::getEventDate));
        return events;
    }

    /**
     * gets all events for a certain group
     * @param groupID
     * @return list of events in chronological order
     */
    @Operation(
            summary = "Get group's events",
            description = "Returns all events for a specific group, in chronological order."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Events retrieved",
                    content = @Content(schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/event/group/{groupID}")
    List<Event> getGroupEvents(@Parameter(description = "ID of the group") @PathVariable Long groupID){
        Group group = groupRepo.findById(groupID)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        Optional<List<Event>> optEvents = eventRepo.findByGroup(group);
        List<Event> events = new ArrayList<>();
        if(optEvents.isPresent()){
            events = optEvents.orElseThrow();
            events.sort(Comparator.comparing(Event::getEventDate));
        }
        return events;
    }

    /**
     * search events
     * @param eventName
     * @return any event that contains part of the search
     */
    @Operation(
            summary = "Search events",
            description = "Searches events by name containing the given string."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Events found",
                    content = @Content(schema = @Schema(implementation = Event.class)))
    })
    @GetMapping("/event/search/{eventName}")
    List<Event> searchEvents(@Parameter(description = "Event name to search for") @PathVariable String eventName){
        Optional<List<Event>> optEvents = eventRepo.findByEventNameContaining(eventName);
        List<Event> events = new ArrayList<>();
        if(optEvents.isPresent()){
            events = optEvents.orElseThrow();
            events.sort(Comparator.comparing(Event::getEventDate));
        }
        return events;
    }

    /**
     * add a comment to the event
     * @param eventID
     * @param text
     * @return
     */
    @Operation(
            summary = "Add a comment to an event",
            description = "Adds a comment to a specific event."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment added",
                    content = @Content(schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @PostMapping("/event/{eventID}/{username}/comment")
    Event addComment(@Parameter(description = "ID of the event") @PathVariable Long eventID,
                     @Parameter(description = "username of user commenting") @PathVariable String username,
                     @Parameter(description = "Comment text") @RequestBody String text){
        Event event = eventRepo.findById(eventID)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Users user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LocalDateTime time = LocalDateTime.now();
        EventComment comment = new EventComment(text.replaceAll("^\"|\"$", ""), event, time);
        comment.setUser(user);
        eventCommentRepo.save(comment);
        return event;
    }

    /**
     * deleted a comment
     * @param commentID
     * @return updated event
     */
    @Operation(
            summary = "Delete a comment",
            description = "Deletes a comment by its ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment deleted",
                    content = @Content(schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @DeleteMapping("/event/comment/{commentID}")
    Event deleteComment(@Parameter(description = "ID of the comment to delete") @PathVariable Long commentID){
        EventComment comment = eventCommentRepo.findById(commentID)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        Event event = comment.getEvent();
        eventCommentRepo.delete(comment);
        return event;
    }

    /**
     * likes a comment
     * @param commentID
     * @return event
     */
    @Operation(
            summary = "Like a comment",
            description = "Adds a like to a comment."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment liked",
                    content = @Content(schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @PutMapping("/event/{commentID}/like")
    Event likeComment(@Parameter(description = "ID of the comment to like") @PathVariable Long commentID){
        EventComment comment = eventCommentRepo.findById(commentID)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.addLike();
        eventCommentRepo.save(comment);
        Event event = comment.getEvent();
        return event;
    }

    /**
     * unlikes comment
     * @param commentID
     * @return event
     */
    @Operation(
            summary = "Unlike a comment",
            description = "Removes a like from a comment."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment unliked",
                    content = @Content(schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @PutMapping("/event/{commentID}/unlike")
    Event unlikeComment(@Parameter(description = "ID of the comment to unlike") @PathVariable Long commentID){
        EventComment comment = eventCommentRepo.findById(commentID)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.removeLike();
        eventCommentRepo.save(comment);
        Event event = comment.getEvent();
        return event;
    }
}
