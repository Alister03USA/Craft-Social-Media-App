package coms309.craftsy.Controller;
import coms309.craftsy.Model.User;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/users") // base URL
public class UserController {

    private HashMap<String, User> users = new HashMap<>();

    // create new user
    @PostMapping
    public User createUser(@RequestBody User user){
        users.put(user.getUserName(), user);
        return user;
    }

    // Read user info
    @GetMapping("/{userName}")
    public User getUser(@PathVariable String userName){
        return users.get(userName);
    }


    // Follow a user
    @PostMapping("/{userName}/follow/{following}")
    public String followUser(@PathVariable("userName") String userName,
                             @PathVariable("following") String following) {
        User user = users.get(userName);
        User target = users.get(following);

        if (user == null || target == null) {
            return "User not found";
        }

        if (!user.getFollowing().contains(following)) {
            user.getFollowing().add(following);
        }
        if (!target.getFollowers().contains(userName)) {
            target.getFollowers().add(userName);
        }

        return userName + " now follows " + following;
    }

    // Unfollow
    @PostMapping("/{userName}/unfollow/{targetUsername}")
    public String unfollowUser(@PathVariable("userName") String userName,
                               @PathVariable("targetUsername") String targetUsername) {
        User user = users.get(userName);
        User target = users.get(targetUsername);

        if (user == null || target == null) return "User not found";

        user.getFollowing().remove(targetUsername);
        target.getFollowers().remove(userName);

        return userName + " unfollowed " + targetUsername;
    }

    // Followers
    @GetMapping("/{userName}/followers")
    public List<String> getFollowers(@PathVariable("userName") String userName) {
        User user = users.get(userName);
        return user != null ? user.getFollowers() : new ArrayList<>();
    }

    // Following
    @GetMapping("/{userName}/following")
    public List<String> getFollowing(@PathVariable("userName") String userName) {
        User user = users.get(userName);
        return user != null ? user.getFollowing() : new ArrayList<>();
    }


    // LIST all users
    @GetMapping
    public Collection<User> listUsers() {
        return users.values();
    }




}
