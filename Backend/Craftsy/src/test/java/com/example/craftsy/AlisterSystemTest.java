package com.example.craftsy;
import com.example.craftsy.FollowingFollowers.Entity.Follow;
import com.example.craftsy.Group.Entity.Group;
import com.example.craftsy.Group.Entity.GroupJoinRequest;
import com.example.craftsy.Group.Repository.GroupJoinRequestRepository;
import com.example.craftsy.Group.Repository.GroupRepository;
import com.example.craftsy.Notification.Entity.Notification;
import com.example.craftsy.Notification.Repository.NotificationRepository;
import com.example.craftsy.PointsSystem.PointsService;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.boot.test.web.server.LocalServerPort;	// SBv3

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;


import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class AlisterSystemTest {


    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    int port;


    @Before
    public void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "";
    }


    // ==================== USER CONTROLLER TESTS ====================

    @Test
    public void testSignupStrongAndDeleteUser() {
        String username = "TestUser_" + System.currentTimeMillis();

        // Sign up
        String userJson = """
        {
            "username": "%s",
            "displayName": "Test Display",
            "password": "Abcdef12"
        }
        """.formatted(username);

        given()
                .contentType(ContentType.JSON)
                .body(userJson)
                .when()
                .post("/users/signup")
                .then()
                .statusCode(200)
                .body("username", equalTo(username));

        // Delete
        given()
                .when()
                .delete("/users/delete/" + username)
                .then()
                .statusCode(200)
                .body(containsString("deleted successfully"));


        // Missing username
        String userJson2 = """
{
    "displayName": "Test Display",
    "password": "Abcdef12"
}
""";

        given()
                .contentType(ContentType.JSON)
                .body(userJson2)
                .when()
                .post("/users/signup")
                .then()
                .statusCode(500);


// Missing password → isPasswordStrong(null) throws → 500
        String userJson3 = """
{
    "username": "NoPasswordUser",
    "displayName": "Test Display"
}
""";

        given()
                .contentType(ContentType.JSON)
                .body(userJson3)
                .when()
                .post("/users/signup")
                .then()
                .statusCode(500);



    }

    @Test
    public void testUsersEntityGettersSetters() {
        Users u = new Users();
        u.setUsername("user1");
        u.setDisplayName("Display1");
        u.setPassword("Password123");
        u.setBio("Some bio");
        u.setProfilePic("pic.jpg");
        u.setCraftSpecialties("Knitting");
        u.setFollowers(10);
        u.setFollowing(5);
        u.setEmail("test@example.com");



        assertEquals("user1", u.getUsername());
        assertEquals("Display1", u.getDisplayName());
        assertEquals("Password123", u.getPassword());
        assertEquals("Some bio", u.getBio());
        assertEquals("pic.jpg", u.getProfilePic());
        assertEquals("Knitting", u.getCraftSpecialties());
        assertEquals(10, u.getFollowers());
        assertEquals(5, u.getFollowing());
        assertEquals("test@example.com", u.getEmail());
    }


    // ==================== FOLLOWING/FOLLOWERS TEST ====================
    @Test
    public void testFollowFullWorkflow() {
        // Unique usernames for the test
        String follower = "Follower_" + System.currentTimeMillis();
        String target = "Target_" + System.currentTimeMillis();
        String otherUser = "Other_" + System.currentTimeMillis();

        // Create users
        createUser(follower, "Follower User", "Password123");
        createUser(target, "Target User", "Password123");
        createUser(otherUser, "Other User", "Password123"); // for followers/following edge case

        // Send follow request
        given()
                .when()
                .post("/" + follower + "/follow/" + target)
                .then()
                .statusCode(200)
                .body("message", equalTo("Follow request sent successfully"));

        // Check profile status pending
        given()
                .when()
                .get("/" + follower + "/profile/" + target)
                .then()
                .statusCode(200)
                .body("isPending", equalTo(true))
                .body("isFollowing", equalTo(false));

        // Fetch notification for the target user
        Users targetUser = userRepository.findByUsername(target)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        Long notificationId = notificationRepository
                .findByUserAndIsReadFalseOrderByCreatedAtDesc(targetUser)
                .stream()
                .filter(n -> n.getMessage().contains("Follower User"))
                .findFirst()
                .map(Notification::getId)
                .orElseThrow(() -> new RuntimeException("Notification for follower not found"));

        // Respond to follow request (accept)
        given()
                .when()
                .put("/notifications/respond/" + notificationId + "/true")
                .then()
                .statusCode(200)
                .body("message", equalTo("Follow request accepted"));

        // Check profile status following
        given()
                .when()
                .get("/" + follower + "/profile/" + target)
                .then()
                .statusCode(200)
                .body("isFollowing", equalTo(true))
                .body("isPending", equalTo(false));


        // target should have follower
        given()
                .when()
                .get("/" + target + "/followers")
                .then()
                .statusCode(200)
                .body("$", hasItem(follower));

        //  follower should be following target
        given()
                .when()
                .get("/" + follower + "/following")
                .then()
                .statusCode(200)
                .body("$", hasItem(target));

        // other user with no followers or following
        given()
                .when()
                .get("/" + otherUser + "/followers")
                .then()
                .statusCode(200)
                .body("$", empty());

        given()
                .when()
                .get("/" + otherUser + "/following")
                .then()
                .statusCode(200)
                .body("$", empty());

        // ================== Unfollow ==================
        given()
                .when()
                .delete("/" + follower + "/unfollow/" + target)
                .then()
                .statusCode(200)
                .body("message", equalTo("User unfollowed"));

        // Confirm the lists are updated after unfollow
        given()
                .when()
                .get("/" + target + "/followers")
                .then()
                .statusCode(200)
                .body("$", not(hasItem(follower)));

        given()
                .when()
                .get("/" + follower + "/following")
                .then()
                .statusCode(200)
                .body("$", not(hasItem(target)));
    }



    @Test
    public void testFollowEntity() {
        Users follower = new Users();
        Users following = new Users();

        // default constructor + setters
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        follow.setAccepted(true);
        follow.setId(123L);

        assertEquals(follower, follow.getFollower());
        assertEquals(following, follow.getFollowing());
        assertTrue(follow.isAccepted());
        assertEquals(123L, follow.getId());

        // full constructor
        Follow follow2 = new Follow(follower, following, false);
        assertEquals(follower, follow2.getFollower());
        assertEquals(following, follow2.getFollowing());
        assertFalse(follow2.isAccepted());
    }





    // ==================== GROUP SYSTEM TEST ====================
    @Test
    public void testFullGroupWorkflowWithExistingUsers() {
        String admin = "alister_gan";  // existing admin
        String member = "Fuji";        // existing member
        String extraMember = "Extra_" + System.currentTimeMillis();

        // Create extra member
        createUser(extraMember, "Extra User", "Password123");

        //  Create group
        String groupName = "SystemTestGroup_" + System.currentTimeMillis();
        String groupJson = """
    {
        "groupName": "%s",
        "description": "Test group workflow",
        "craft": "Knitting",
        "is_private": false
    }
    """.formatted(groupName);

        given()
                .contentType(ContentType.JSON)
                .body(groupJson)
                .when()
                .post("/" + admin + "/create")
                .then()
                .statusCode(200)
                .body("message", equalTo("Group created successfully"));

        //  Get group ID
        String groupId = given()
                .when()
                .get("/groupId/" + groupName)
                .then()
                .statusCode(200)
                .extract()
                .path("groupId")
                .toString();

        //  Member joins group (PUBLIC)
        given()
                .when()
                .post("/" + member + "/join/" + groupId)
                .then()
                .statusCode(200)
                .body("message", equalTo("User added successfully"));

        //  Verify notification created for admin
        given()
                .when()
                .get("/notifications/" + admin)
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));

        //  Admin adds extra member
        given()
                .when()
                .post("/" + groupId + "/" + admin + "/add-member/" + extraMember)
                .then()
                .statusCode(200)
                .body("message", equalTo("Member added successfully"));

        //  Verify notification for extraMember
        given()
                .when()
                .get("/notifications/" + extraMember)
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));

        //  Verify members list
        given()
                .when()
                .get("/" + groupId + "/members")
                .then()
                .statusCode(200)
                .body("totalMembers", equalTo(3))
                .body("members.username", hasItems(admin, member, extraMember));

        //  Update group by admin
        String updateJson = """
    {
        "description": "Updated description by admin"
    }
    """;

        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when()
                .put("/" + admin + "/update/" + groupId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Group updated successfully"));

        //  Transfer admin to Fuji
        given()
                .when()
                .put("/" + admin + "/" + groupId + "/transfer-admin/" + member)
                .then()
                .statusCode(200)
                .body("message", equalTo("Admin transferred successfully"));

        //  Verify notification for new admin (Fuji)
        given()
                .when()
                .get("/notifications/" + member)
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));

        //  Update group as new admin
        String updateJson2 = """
    {
        "description": "Updated description by new admin"
    }
    """;

        given()
                .contentType(ContentType.JSON)
                .body(updateJson2)
                .when()
                .put("/" + member + "/update/" + groupId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Group updated successfully"));

        //  Extra member leaves group
        given()
                .when()
                .delete("/" + extraMember + "/leave/" + groupId)
                .then()
                .statusCode(200)
                .body("message", equalTo("You have left the group successfully"));

        //  Verify member count now 2
        given()
                .when()
                .get("/" + groupId + "/members")
                .then()
                .statusCode(200)
                .body("totalMembers", equalTo(2));

        // New admin tries to remove themselves → should fail
        given()
                .when()
                .delete("/" + groupId + "/" + member + "/removeMember/" + member)
                .then()
                .statusCode(400);

        //  New admin removes previous admin (alister_gan)
        given()
                .when()
                .delete("/" + groupId + "/" + member + "/removeMember/" + admin)
                .then()
                .statusCode(200)
                .body("message", equalTo("Member removed successfully"));

        // Verify member count = 1
        given()
                .when()
                .get("/" + groupId + "/members")
                .then()
                .statusCode(200)
                .body("totalMembers", equalTo(1));

        // Delete group
        given()
                .when()
                .delete("/" + member + "/delete/" + groupId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Group deleted successfully"));

        // Get all groups a user is in
        given()
                .when()
                .get("/alister_gan/groups")
                .then()
                .statusCode(200);
    }


    @Test
    public void testPrivateGroupJoinRequestWorkflow() {
        String admin = "alister_gan";
        String member = "Fuji";
        String extraMember = "Extra_" + System.currentTimeMillis();

        //  Create the extra member
        createUser(extraMember, "Extra User", "Password123");

        //  Create a private group
        String groupName = "PrivateGroupTest_" + System.currentTimeMillis();
        String groupJson = """
    {
        "groupName": "%s",
        "description": "Test private group workflow",
        "craft": "Knitting",
        "isPrivate": true
    }
    """.formatted(groupName);

        given()
                .contentType(ContentType.JSON)
                .body(groupJson)
                .when()
                .post("/" + admin + "/create")
                .then()
                .statusCode(200)
                .body("message", equalTo("Group created successfully"));

        //  Get Group ID
        String groupId = given()
                .when()
                .get("/groupId/" + groupName)
                .then()
                .statusCode(200)
                .extract()
                .path("groupId")
                .toString();

        //  Member sends join request to private group
        String requestId = given()
                .when()
                .post("/" + member + "/join/" + groupId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Join request sent"))
                .body("requestId", notNullValue())
                .extract()
                .path("requestId")
                .toString();

        //  Admin accepts the join request
        given()
                .when()
                .put("/joinRequest/" + requestId + "/true")
                .then()
                .statusCode(200)
                .body("message", equalTo("User added to group"));

        //  Verify member list now contains the new member
        given()
                .when()
                .get("/" + groupId + "/members")
                .then()
                .statusCode(200)
                .body("members.username", hasItem(member));



    }






    // ==================== GROUP MESSAGE SYSTEM TEST ====================
    @Test
    public void testGroupMessageWorkflow() throws Exception {
        String admin = "alister_gan";
        String member = "Fuji";

        //  Create a group for messaging
        String groupName = "SystemTestMessageGroup_" + System.currentTimeMillis();
        String groupJson = """
        {
            "groupName": "%s",
            "description": "Group for message system test",
            "craft": "Knitting",
            "is_private": false
        }
        """.formatted(groupName);

        given()
                .contentType(ContentType.JSON)
                .body(groupJson)
                .when()
                .post("/" + admin + "/create")
                .then()
                .statusCode(200)
                .body("message", equalTo("Group created successfully"));

        //  Get group ID
        Long groupId = given()
                .when()
                .get("/groupId/" + groupName)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getLong("groupId");


        // Member joins group
        given()
                .when()
                .post("/" + member + "/join/" + groupId)
                .then()
                .statusCode(200)
                .body("message", equalTo("User added successfully"));

        // Upload a media/image message
        File testImage = new File("uploads/tutorials/1760579127952_sculptor-artist-working-with-clay-studio.jpg");
        Long messageId = given()
                .multiPart("file", testImage, "image/jpg")
                .when()
                .post("/groupMessage/" + groupId + "/" + admin + "/upload")
                .then()
                .statusCode(200)
                .body("message", equalTo("Image uploaded successfully"))
                .extract()
                .jsonPath()
                .getLong("messageId");

        // Fetch message history
        given()
                .when()
                .get("/groupMessage/" + admin + "/" + groupId + "/history")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("[0].sender.username", equalTo(admin))
                .body("[0].mediaUrl", notNullValue());

        // Retrieve the uploaded image
        given()
                .when()
                .get("/groupMessage/image/" + messageId)
                .then()
                .statusCode(200)
                .contentType(startsWith("image/"));

        //  Test unauthorized access to history
        String outsider = "Outsider_" + System.currentTimeMillis();
        createUser(outsider, "Outsider User", "Password123");

        given()
                .when()
                .get("/groupMessage/" + outsider + "/" + groupId + "/history")
                .then()
                .statusCode(403)
                .body("error", containsString("not a member"));

        //  Test image retrieval
        given()
                .when()
                .get("/groupMessage/image/" + messageId)
                .then()
                .statusCode(200)
                .contentType(startsWith("image/"));

        //  Test comments extraction
        given()
                .when()
                .get("/groupMessage/" + messageId + "/comments")
                .then()
                .statusCode(200)
                .body("$", empty());


        // Delete group
        given()
                .when()
                .delete("/" + admin + "/delete/" + groupId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Group deleted successfully"));

        // Get all group chat history
        given()
                .when()
                .get("/groupMessage/Quinn/1/history")
                .then()
                .statusCode(200);
    }

    // ==================== HELPER METHODS ====================
    private void createUser(String username, String displayName, String password) {
        String uniqueDisplayName = displayName + "_" + System.currentTimeMillis();
        String userJson = """
        {
            "username": "%s",
            "displayName": "%s",
            "password": "%s"
        }
        """.formatted(username, uniqueDisplayName, password);

        given()
                .contentType(ContentType.JSON)
                .body(userJson)
                .when()
                .post("/users/signup")
                .then()
                .statusCode(200);
    }




    // ==================== Search SYSTEM TEST ====================
    @Test
    public void testSearchUsers() {
        String query = "alister_gan";

        given()
                .contentType(ContentType.JSON)
                .queryParam("query", query)
                .when()
                .get("/search/user")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("username", everyItem(containsStringIgnoringCase(query)));
    }

    @Test
    public void testSearchGroups() {
        String query = "Knitting";

        given()
                .contentType(ContentType.JSON)
                .queryParam("query", query)
                .when()
                .get("/search/group")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("groupName", everyItem(containsStringIgnoringCase(query))
                        );
    }

    @Test
    public void testSearchProjects() {
        String query = "test";

        given()
                .contentType(ContentType.JSON)
                .queryParam("query", query)
                .when()
                .get("/search/project")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("projectName", everyItem(containsStringIgnoringCase(query))
                        );
    }

    @Test
    public void testSearchTutorials() {
        String query = "Youtube";

        given()
                .contentType(ContentType.JSON)
                .queryParam("query", query)
                .when()
                .get("/search/tutorial")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("title", everyItem(containsStringIgnoringCase(query))
                        );
    }



    // ==================== Tutorial SYSTEM TEST ====================

    @Test
    public void testTutorialWorkflow() {

        String username = "alister_gan";
        String beginnerUsername = "GeneralUser";
        String title = "SystemTestTutorial_" + System.currentTimeMillis();
        String titleFile = "SystemTestTutorial_File_" + System.currentTimeMillis();


        // create tutorial (URL upload)
        given()
                .contentType(ContentType.URLENC)
                .formParam("username", username)
                .formParam("title", title)
                .formParam("description", "A system test tutorial description")
                .formParam("category", "Knitting")
                .formParam("fileUrl", "https://www.youtube.com/watch?v=hM5M2Fu0RtY")
                .when()
                .post("/tutorial/uploadUrl")
                .then()
                .statusCode(200)
                .body(equalTo("Tutorial uploaded successfully with URL!"));

        // Search tutorial to get its ID
        Long id =
                given()
                        .queryParam("query", title)
                        .queryParam("username", username)
                        .when()
                        .get("/tutorial/search")
                        .then()
                        .statusCode(200)
                        .extract()
                        .jsonPath().getLong("[0].id");


        // update tutorial (without file)
        String updatedTitle = title + "_UPDATED";
        String updatedDescription = "Updated description for system test tutorial";

        given()
                .multiPart("title", updatedTitle)
                .multiPart("description", updatedDescription)
                .when()
                .put("/tutorial/" + id)
                .then()
                .statusCode(200)
                .body(equalTo("Tutorial Updated successfully!"));

        // verify tutorial was updated
        given()
                .queryParam("query", updatedTitle)
                .queryParam("username", username)
                .when()
                .get("/tutorial/search")
                .then()
                .statusCode(200)
                .body("title", hasItem(updatedTitle))
                .body("description", hasItem(updatedDescription));

        // Delete tutorial
        given()
                .when()
                .delete("/tutorial/" + id)
                .then()
                .statusCode(200)
                .body(equalTo("Tutorial deleted successfully!"));

        // ==================== 2. Upload tutorial via File ====================
        File testFile = new File("uploads/tutorials/1760579127952_sculptor-artist-working-with-clay-studio.jpg");


        // Upload tutorial via file
        given()
                .multiPart("username", username)
                .multiPart("title", titleFile)
                .multiPart("description", "File tutorial description")
                .multiPart("category", "Painting")
                .multiPart("file", testFile)
                .when()
                .post("/tutorial/uploadFile")
                .then()
                .statusCode(200)
                .body(equalTo("Tutorial uploaded successfully!"));

        Long fileTutorialId = given()
                .queryParam("query", titleFile)
                .queryParam("username", username)
                .when()
                .get("/tutorial/search")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getLong("[0].id");


        //  Fetch local file
        given()
                .when()
                .get("/tutorial/" + fileTutorialId + "/file")
                .then()
                .statusCode(200)
                .header("Content-Disposition", containsString("inline"))
                .header("Content-Type", notNullValue())
                .extract().asByteArray();

        // Test private tutorial visibility
        String privateTitle = "PrivateTutorial_" + System.currentTimeMillis();
        given()
                .contentType(ContentType.URLENC)
                .formParam("username", username)
                .formParam("title", privateTitle)
                .formParam("description", "Private tutorial description")
                .formParam("category", "Knitting")
                .formParam("fileUrl", "https://www.youtube.com/watch?v=private")
                .formParam("isPrivate", true)
                .when()
                .post("/tutorial/uploadUrl")
                .then()
                .statusCode(200)
                .body(equalTo("Tutorial uploaded successfully with URL!"));

        // Search tutorial to get its ID
        Long privateTutorialId = given()
                .queryParam("query", privateTitle)
                .queryParam("username", username)
                .when()
                .get("/tutorial/search")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getLong("[0].id");

        // BEGINNER user should not see it
        given()
                .queryParam("query", privateTitle)
                .queryParam("username", beginnerUsername)
                .when()
                .get("/tutorial/search")
                .then()
                .statusCode(200)
                .body("", not(hasItem(hasEntry("title", privateTitle))));

        // INTERMEDIATE user should see it
        String intermediateUser = "alister_gan";
        given()
                .queryParam("query", privateTitle)
                .queryParam("username", intermediateUser)
                .when()
                .get("/tutorial/search")
                .then()
                .statusCode(200)
                .body("title", hasItem(privateTitle));

        // BEGINNER cannot upload
        given()
                .contentType(ContentType.URLENC)
                .formParam("username", beginnerUsername)
                .formParam("title", "LowTierUpload")
                .formParam("description", "Test")
                .formParam("category", "Knitting")
                .formParam("fileUrl", "https://www.youtube.com/watch?v=test")
                .when()
                .post("/tutorial/uploadUrl")
                .then()
                .statusCode(403);


        given()
                .queryParam("viewer", "alister_gan")
                .when()
                .get("/tutorial/user/alister_gan")
                .then()
                .statusCode(200);

    }


    // ==================== Notification SYSTEM TEST ====================

    @Test
    public void testNotificationWorkflow() {

        String senderUsername = "alister_gan";
        String receiverUsername = "Quinn";
        Long notificationId;

        // CREATE a notification
        notificationId = given()
                .queryParam("receiverUsername", receiverUsername)
                .queryParam("senderUsername", senderUsername)
                .queryParam("title", "System Test Notification")
                .queryParam("message", "You have a new system notification!")
                .queryParam("referenceId", 123L)
                .when()
                .post("/notifications")
                .then()
                .statusCode(200)
                .body("title", equalTo("System Test Notification"))
                .body("message", equalTo("You have a new system notification!"))
                .extract()
                .jsonPath()
                .getLong("id");

        // GET unread notifications for receiver
        List<Map<String, Object>> unreadNotifications = given()
                .when()
                .get("/notifications/" + receiverUsername)
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .extract()
                .jsonPath()
                .getList("$");

        assertTrue(unreadNotifications.stream()
                .anyMatch(n -> ((Integer) n.get("id")).longValue() == notificationId));

        // Verify that notifications are marked as read after fetching
        Notification fetchedNotification = notificationRepository.findById(notificationId).orElse(null);
        assertNotNull(fetchedNotification);
        assertTrue(fetchedNotification.getIsRead());

        // CREATE another notification for testing markAsRead
        Long notifToMarkId = given()
                .queryParam("receiverUsername", receiverUsername)
                .queryParam("title", "MarkAsRead Test")
                .queryParam("message", "Mark this notification as read")
                .when()
                .post("/notifications")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getLong("id");

        // MARK the notification as read via PUT
        given()
                .when()
                .put("/notifications/" + notifToMarkId + "/read")
                .then()
                .statusCode(200)
                .body(equalTo("Notification marked as read"));

        // Verify in repository
        Notification markedNotification = notificationRepository.findById(notifToMarkId).orElse(null);
        assertNotNull(markedNotification);
        assertTrue(markedNotification.getIsRead());
    }




    // ==================== PointsSystem SYSTEM TEST ====================
    @Test
    public void testPointsSystemWorkflow() {

        String username = "Quinn";

        // GET user's points
        Map<String, Object> pointsResponse =
                given()
                        .pathParam("username", username)
                        .when()
                        .get("/points/{username}")
                        .then()
                        .statusCode(200)
                        .body("username", equalTo(username))
                        .body("totalPoints", notNullValue())
                        .body("currentTier", notNullValue())
                        .body("postsCount", notNullValue())
                        .extract()
                        .as(Map.class);

        Integer initialPoints = (Integer) pointsResponse.get("totalPoints");

        // GET user's points history
        given()
                .pathParam("username", username)
                .when()
                .get("/points/{username}/history")
                .then()
                .statusCode(200)
                .body("$", notNullValue());  // List of history events

        // GET leaderboard
        given()
                .when()
                .get("/points/leaderboard")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("[0].username", notNullValue())
                .body("[0].totalPoints", notNullValue());

        // GET tier information
        given()
                .when()
                .get("/points/tiers")
                .then()
                .statusCode(200)
                .body("$", hasSize(4))
                .body("[0].name", equalTo("BEGINNER"))
                .body("[3].name", equalTo("CHAMPION"));
    }


    @Autowired
    private PointsService pointsService;

    @Test
    public void testPointsHistoryAfterTutorialUpload() {

        String username = "Quinn";

        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Upload a tutorial via file
        File testFile = new File("uploads/tutorials/1760579127952_sculptor-artist-working-with-clay-studio.jpg");
        String tutorialTitle = "SystemTestTutorial_" + System.currentTimeMillis();

        given()
                .multiPart("username", username)
                .multiPart("title", tutorialTitle)
                .multiPart("description", "Testing PointsHistory")
                .multiPart("category", "Painting")
                .multiPart("file", testFile)
                .when()
                .post("/tutorial/uploadFile")
                .then()
                .statusCode(200)
                .body(equalTo("Tutorial uploaded successfully!"));

        // Search tutorial to get its ID
        Long tutorialId = given()
                .queryParam("query", tutorialTitle)
                .queryParam("username", username)
                .when()
                .get("/tutorial/search")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getLong("[0].id"); // get ID from JSON search result

        // Award points via PointsService for this tutorial
        pointsService.awardPointsForTutorial(user, tutorialId);

        // Award points for a post
        pointsService.awardPointsForPost(user, 101L);

        // Award points for a comment
        pointsService.awardPointsForComment(user, 201L);

        // Award points for a challenge completion
        pointsService.awardPointsForChallengeCompletion(user, 301L);

        // GET user's points
        Map<String, Object> userPoints =
                given()
                        .pathParam("username", username)
                        .when()
                        .get("/points/{username}")
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(Map.class);

        assertEquals(username, userPoints.get("username"));
        assertTrue((Integer) userPoints.get("totalPoints") > 0);
        assertTrue((Integer) userPoints.get("postsCount") > 0);
        assertTrue((Integer) userPoints.get("tutorialsCount") > 0);
        assertTrue((Integer) userPoints.get("commentsCount") > 0);
        assertNotNull(userPoints.get("currentTier"));

        // GET user's PointsHistory
        List<Map<String, Object>> pointsHistory =
                given()
                        .pathParam("username", username)
                        .when()
                        .get("/points/{username}/history")
                        .then()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getList("");

        // Verify that tutorial, post, comment, challenge all appear
        assertTrue(pointsHistory.size() >= 4);



        // GET leaderboard
        given()
                .when()
                .get("/points/leaderboard")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("[0].username", notNullValue())
                .body("[0].totalPoints", notNullValue())
                .body("[0].tier", notNullValue());

        // GET tier info
        given()
                .when()
                .get("/points/tiers")
                .then()
                .statusCode(200)
                .body("$", hasSize(4))
                .body("[0].name", equalTo("BEGINNER"))
                .body("[3].name", equalTo("CHAMPION"));

    }








    // ==================== Challenges SYSTEM TEST ====================

    @Test
    public void testChallengeWorkflow() {

        String championUsername = "alister_gan";
        String normalUsername = "Kkeck";
         Long challengeId;
         Long postId;

        // CREATE challenge as CHAMPION
        Map<String, Object> challengeBody = Map.of(
                "title", "SystemTestChallenge_" + System.currentTimeMillis(),
                "description", "Challenge for system test",
                "type", "PHOTO",
                "category", "Knitting",
                "startDate", LocalDate.now().toString(),
                "endDate", LocalDate.now().plusDays(7).toString()
        );

        challengeId = given()
                .contentType(ContentType.JSON)
                .body(challengeBody)
                .when()
                .post("/challenge/" + championUsername + "/post")
                .then()
                .statusCode(200)
                .body("message", equalTo("Challenge created successfully"))
                .extract()
                .jsonPath()
                .getLong("challengeId");


        // GET all active challenges
        given()
                .when()
                .get("/challenge/active")
                .then()
                .statusCode(200);


        // PARTICIPATE in challenge as normal user
        given()
                .when()
                .post("/challenge/" + normalUsername + "/participate/" + challengeId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Participation recorded"));

        // CREATE post for challenge
        postId = given()
                .multiPart("description", "My challenge post")
                .when()
                .post("/challenge/" + normalUsername + "/create/posts/" + challengeId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Post created successfully"))
                .extract()
                .jsonPath()
                .getLong("postId");

        // GET posts of challenge
        given()
                .when()
                .get("/challenge/" + challengeId + "/posts")
                .then()
                .statusCode(200)
                .body("posts[0].id", equalTo(postId.intValue()));

        // LIKE the post
        given()
                .when()
                .post("/challenge/" + championUsername + "/like/" + postId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Post liked"))
                .body("likeCount", equalTo(1));

      //  UNLIKE the post
        given()
                .when()
                .delete("/challenge/" + championUsername + "/unlike/" + postId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Post unliked"))
                .body("likeCount", equalTo(0));

        // COMMENT on the post
        Long commentId = given()
                .body("Great post!")
                .contentType(ContentType.JSON)
                .when()
                .post("/challenge/" + championUsername + "/comment/" + postId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Comment added"))
                .extract()
                .jsonPath()
                .getLong("commentId");

        // GET comments
        given()
                .when()
                .get("/challenge/" + postId + "/comments")
                .then()
                .statusCode(200)
                .body("[0].comment", equalTo("Great post!"))
                .body("[0].user.username", equalTo(championUsername));



        // COMPLETE challenge
        given()
                .when()
                .post("/challenge/" + normalUsername + "/complete/" + challengeId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Challenge marked as completed. Points awarded!"));

        // FILTER challenges by type
        given()
                .queryParam("type", "PHOTO")
                .when()
                .get("/challenge/filter")
                .then()
                .statusCode(200)
                .body("$", not(empty()));

        // EDIT challenge as creator
        Map<String, Object> editBody = Map.of("title", "Edited Title");
        given()
                .contentType(ContentType.JSON)
                .body(editBody)
                .when()
                .put("/challenge/" + championUsername + "/edit/" + challengeId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Challenge updated successfully"));

        // DEACTIVATE challenge
        given()
                .when()
                .put("/challenge/" + championUsername + "/deactivate/" + challengeId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Challenge deactivated successfully"));

        // DELETE challenge as creator
        given()
                .when()
                .delete("/challenge/" + championUsername + "/delete/" + challengeId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Challenge deleted successfully"));
    }



}















