package com.example.craftsy.events;

import com.example.craftsy.Group.Entity.Group;
import com.example.craftsy.Group.Repository.GroupRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.events.eventsComments.EventComment;
import com.example.craftsy.events.eventsComments.EventCommentRepository;
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
    @PostMapping("/event/create/{username}/{eventDate}")
    Event createPublicEvent(@PathVariable String username, @RequestBody Event event, @PathVariable String eventDate){
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
    @PostMapping("/event/create/{username}/{groupId}/{eventDate}")
    Event createGroupEvent(@PathVariable String username, @PathVariable Long groupId
            , @PathVariable String eventDate, @RequestBody Event event){
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
    @DeleteMapping("/event/{eventId}")
    String deleteEvent(@PathVariable Long eventId){
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
    @PutMapping("/event/{eventId}")
    Event updateEvent(@PathVariable Long eventId, @RequestBody Event newEvent){
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
    @PutMapping("/event/{eventID}/date/{eventDate}")
    Event updateEventDate(@PathVariable Long eventID, @PathVariable String eventDate){
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
    @GetMapping("/event/{eventID}")
    Event getEvent(@PathVariable Long eventID){
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
    @PutMapping("/event/{eventID}/{username}/{status}")
    List<Users> rsvpToEvent(@PathVariable Long eventID, @PathVariable String username, @PathVariable String status){
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
    @GetMapping("/event/{eventID}/yes")
    List<Users> getRsvpYes(@PathVariable Long eventID){
        Event event = eventRepo.findById(eventID)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return event.getRsvpYes();
    }

    /**
     * get rsvp no list
     * @param eventID
     * @return
     */
    @GetMapping("/event/{eventID}/no")
    List<Users> getRsvpNo(@PathVariable Long eventID){
        Event event = eventRepo.findById(eventID)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return event.getRsvpNo();
    }

    /**
     * Gets all events where the user has rsvped yes
     * @param username
     * @return list of events in chronological order
     */
    @GetMapping("/event/user/{username}")
    List<Event> getUsersEvents(@PathVariable String username){
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
    @GetMapping("/event/group/{groupID}")
    List<Event> getGroupEvents(@PathVariable Long groupID){
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
    @GetMapping("/event/search/{eventName}")
    List<Event> searchEvents(@PathVariable String eventName){
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
    @PostMapping("/event/{eventID}/comment")
    Event addComment(@PathVariable Long eventID, @RequestBody String text){
        Event event = eventRepo.findById(eventID)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        LocalDateTime time = LocalDateTime.now();
        EventComment comment = new EventComment(text.replaceAll("^\"|\"$", ""), event, time);
        eventCommentRepo.save(comment);
        return event;
    }

    /**
     * deleted a comment
     * @param commentID
     * @return updated event
     */
    @DeleteMapping("/event/comment/{commentID}")
    Event deleteComment(@PathVariable Long commentID){
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
    @PutMapping("/event/{commentID}/like")
    Event likeComment(@PathVariable Long commentID){
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
    @PutMapping("/event/{commentID}/unlike")
    Event unlikeComment(@PathVariable Long commentID){
        EventComment comment = eventCommentRepo.findById(commentID)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.removeLike();
        eventCommentRepo.save(comment);
        Event event = comment.getEvent();
        return event;
    }
}
