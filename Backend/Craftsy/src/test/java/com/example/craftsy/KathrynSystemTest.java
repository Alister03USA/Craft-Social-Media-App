package com.example.craftsy;

import com.example.craftsy.FollowingFollowers.Entity.Follow;
import com.example.craftsy.FollowingFollowers.Repository.FollowRepository;
import com.example.craftsy.Group.Entity.Group;
import com.example.craftsy.Group.Repository.GroupRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.Tutorial.Entity.Tutorial;
import com.example.craftsy.Tutorial.Repository.TutorialRepository;
import com.example.craftsy.messages.Message;
import com.example.craftsy.messages.MessageRepository;
import com.example.craftsy.messages.conversations.DirectConversation;
import com.example.craftsy.messages.conversations.DirectConversationRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.with;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KathrynSystemTest {

    @LocalServerPort
    int port;

    Users testUser;

    @Autowired
    UserRepository userRepo;

    @Autowired
    TutorialRepository tutorialRepo;

    @Autowired
    GroupRepository groupRepo;

    @Autowired
    FollowRepository followRepo;

    @Autowired
    DirectConversationRepository directConvoRepo;

    @Autowired
    MessageRepository messageRepo;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        if (userRepo.findByUsername("kkeckTestUser").isEmpty()) {
            Users u = new Users();
            u.setUsername("kkeckTestUser");
            u.setDisplayName("katie test user");
            u.setPassword("password123");
            userRepo.save(u);
        }
        testUser = userRepo.findByUsername("kkeckTestUser").orElseThrow();
    }

    // ---------------- BoardController tests ----------------
    @Test
    public void createAndDeleteBoard(){
        String requestBody = """
                {
                    "boardName":"test board",
                    "description":"testing board"
                }
                """;

        //create board
        int boardId = given().contentType("application/json").body(requestBody).when()
                .post("/board/create/{username}", testUser.getUsername())
                .then().statusCode(200).extract().path("id");

        //get board
        RestAssured.when()
                .get("board/{boardId}", boardId)
                .then().statusCode(200).assertThat()
                .body("boardName", equalTo("test board"))
                .body("description", equalTo("testing board"))
                .body("user.id", equalTo(testUser.getId().intValue()));

        //delete board
        RestAssured.when()
                .delete("/board/{boardId}", boardId)
                .then()
                .statusCode(200);

        //confirm board deleted
        RestAssured.when().get("board/{boardId}", boardId)
                .then().statusCode(500);
    }

    @Test
    public void addAndDeletePatternFromBoard(){
        String requestBody = """
                {
                    "boardName":"test board",
                    "description":"testing board"
                }
                """;
        //createBoard
        int boardId = given().contentType("application/json").body(requestBody).when()
                .post("/board/create/GeneralUser")
                .then().statusCode(200).extract().path("id");

        //createPattern
        requestBody = """
                {
                    "patternName": "test pattern",
                    "patternType": "test"
                }
                """;
        String patternName = given().contentType("application/json").body(requestBody).when()
                .post("/patterns/{username}", testUser.getUsername())
                        .then().statusCode(200).extract().path("patternName");

        //add pattern
        RestAssured.when()
                .put("/board/{boardId}/pattern/{username}/{patternName}", boardId, testUser.getUsername(), patternName)
                .then().statusCode(200);

        //check board has pattern
        RestAssured.when()
                .get("board/{boardId}", boardId)
                .then().statusCode(200).assertThat()
                .body("patterns[0].patternName", equalTo(patternName));

        //remove pattern
        RestAssured.when()
                .put("/board/{boardId}/pattern/{username}/{patternName}/delete", boardId, testUser.getUsername(), patternName)
                .then().statusCode(200);

        //check pattern is gone
        RestAssured.when()
                .get("board/{boardId}", boardId)
                .then().statusCode(200).assertThat()
                .body("patterns.size()", equalTo(0));

        //delete pattern and board
        RestAssured.when()
                .delete("/patterns/{username}/{patternName}", testUser.getUsername(), patternName)
                .then().statusCode(200);

        RestAssured.when()
                .delete("/board/{boardId}", boardId)
                .then()
                .statusCode(200);
    }

    @Test
    public void addAndDeleteProjectFromBoard(){
        String requestBody = """
                {
                    "boardName":"test board",
                    "description":"testing board"
                }
                """;
        //createBoard
        int boardId = given().contentType("application/json").body(requestBody).when()
                .post("/board/create/GeneralUser")
                .then().statusCode(200).extract().path("id");

        //create project
        requestBody = """
                {
                    "projectName": "test project",
                    "visibility": "public"
                }
                """;
        String projectName = given().contentType("application/json").body(requestBody).when()
                .post("/feed/{username}", testUser.getUsername())
                .then().statusCode(200).extract().path("projectName");

        //add project
        RestAssured.when()
                .put("/board/{boardId}/project/{username}/{projectName}", boardId, testUser.getUsername(), projectName)
                .then().statusCode(200);

        //check board has project
        RestAssured.when()
                .get("board/{boardId}", boardId)
                .then().statusCode(200).assertThat()
                .body("projects[0].projectName", equalTo(projectName));

        //remove project
        RestAssured.when()
                .put("/board/{boardId}/project/{username}/{projectName}/delete", boardId, testUser.getUsername(), projectName)
                .then().statusCode(200);

        //check project is gone
        RestAssured.when()
                .get("board/{boardId}", boardId)
                .then().statusCode(200).assertThat()
                .body("projects.size()", equalTo(0));

        //delete project and board
        RestAssured.when()
                .delete("/feed/{username}/{projectName}", testUser.getUsername(), projectName)
                .then().statusCode(200);

        RestAssured.when()
                .delete("/board/{boardId}", boardId)
                .then()
                .statusCode(200);
    }

    @Test
    public void addAndDeleteTutorialFromBoard(){
        String requestBody = """
                {
                    "boardName":"test board",
                    "description":"testing board"
                }
                """;
        //createBoard
        int boardId = given().contentType("application/json").body(requestBody).when()
                .post("/board/create/GeneralUser")
                .then().statusCode(200).extract().path("id");

        //create tutorial
        Tutorial tutorial = new Tutorial();
        tutorial.setUser(testUser);
        tutorial.setTitle("test tutorial");
        tutorial.setCategory("test");
        tutorialRepo.save(tutorial);

        //add tutorial
        RestAssured.when()
                .put("/board/{boardId}/tutorial/{tutorialId}", boardId, tutorial.getId())
                .then().statusCode(200);

        //check board has tutorial
        RestAssured.when()
                .get("board/{boardId}", boardId)
                .then().statusCode(200).assertThat()
                .body("tutorials[0].title", equalTo(tutorial.getTitle()));

        //remove tutorial
        RestAssured.when()
                .put("board/{boardId}/tutorial/{tutorialId}/delete", boardId, tutorial.getId())
                .then().statusCode(200);

        //check tutorial is gone
        RestAssured.when()
                .get("board/{boardId}", boardId)
                .then().statusCode(200).assertThat()
                .body("tutorials.size()", equalTo(0));

        //delete tutorial and board
        RestAssured.when()
                .delete("/tutorial/{id}", tutorial.getId())
                .then().statusCode(200);

        RestAssured.when()
                .delete("/board/{boardId}", boardId)
                .then()
                .statusCode(200);
    }

    @Test
    public void getAllUsersBoards(){
        String requestBody = """
                {
                    "boardName":"test board 1",
                    "description":"testing board"
                }
                """;
        //create Board 1
        int boardId1 = given().contentType("application/json").body(requestBody).when()
                .post("/board/create/{username}", testUser.getUsername())
                .then().statusCode(200).extract().path("id");

        requestBody = """
                {
                    "boardName":"test board 2",
                    "description":"testing board"
                }
                """;
        //create Board 2
        int boardId2 = given().contentType("application/json").body(requestBody).when()
                .post("/board/create/{username}", testUser.getUsername())
                .then().statusCode(200).extract().path("id");

        //get users boards
        RestAssured.when()
                .get("board/user/{username}", testUser.getUsername())
                .then().statusCode(200).assertThat()
                .body("size()", equalTo(2));

        //delete boards
        RestAssured.when()
                .delete("/board/{boardId}", boardId1)
                .then()
                .statusCode(200);

        RestAssured.when()
                .delete("/board/{boardId}", boardId2)
                .then()
                .statusCode(200);
    }

    @Test
    public void editBoardDescription(){
        String requestBody = """
                {
                    "boardName":"test board",
                    "description":"testing board"
                }
                """;
        //createBoard
        int boardId = given().contentType("application/json").body(requestBody).when()
                .post("/board/create/GeneralUser")
                .then().statusCode(200).extract().path("id");

        //edit description
        given().contentType("application/json").body("new description").when()
                .put("/board/{boardId}/description", boardId)
                .then().statusCode(200);

        //check description
        RestAssured.when()
                .get("board/{boardId}", boardId)
                .then().statusCode(200).assertThat()
                .body("description", equalTo("new description"));

        //delete board
        RestAssured.when()
                .delete("/board/{boardId}", boardId)
                .then()
                .statusCode(200);
    }

    // ---------------- EventController tests ----------------
    @Test
    public void createPublicEventAndUpdates(){
        String requestBody = """
                {
                    "eventName":"test event",
                    "craftType":"test",
                    "description":"testing"
                }
                """;

        //create public event
        int eventID = given().contentType("application/json").body(requestBody).when()
                .post("/event/create/{username}/{eventDate}", testUser.getUsername(), "2026-11-12T14:00:00")
                .then().statusCode(200).extract().path("id");

        //get event
        RestAssured.when()
                .get("/event/{eventID}", eventID)
                .then().statusCode(200).assertThat()
                .body("eventName", equalTo("test event"));

        //edit event
        requestBody = """
                {
                    "eventName":"test update name",
                    "craftType":"test update type",
                    "description":"test update description"
                }
                """;
        given().contentType("application/json").body(requestBody).when()
                .put("/event/{eventId}", eventID)
                .then().statusCode(200);

        //confirm update
        RestAssured.when()
                .get("/event/{eventID}", eventID)
                .then().statusCode(200).assertThat()
                .body("eventName", equalTo("test update name"))
                .body("craftType", equalTo("test update type"))
                .body("description", equalTo("test update description"));

        //delete event
        RestAssured.when()
                .delete("/event/{eventID}", eventID)
                .then()
                .statusCode(200);
    }

    @Test
    public void createGroupEventUpdateEventDate(){

        //create group
        Group group = new Group();
        group.setGroupAdmin(testUser);
        group.setGroupName("test group");
        groupRepo.save(group);

        String requestBody = """
                {
                    "eventName":"test event",
                    "craftType":"test",
                    "description":"testing"
                }
                """;

        //create group event
        int eventID = given().contentType("application/json").body(requestBody).when()
                .post("/event/create/{username}/{groupId}/{eventDate}", testUser.getUsername(), group.getId(), "2026-11-12T14:00:00")
                .then().statusCode(200).extract().path("id");

        //get events for group
        RestAssured.when()
                .get("/event/group/{groupID}", group.getId())
                .then().statusCode(200).assertThat()
                .body("size()", equalTo(1));

        //update event date
        RestAssured.when()
                .put("/event/{eventID}/date/{eventDate}", eventID, "2026-11-13T14:00:00")
                .then().statusCode(200);

        //confirm date update
        RestAssured.when()
                .get("/event/{eventID}", eventID)
                .then().statusCode(200).assertThat()
                .body("eventDate", equalTo("2026-11-13T14:00:00"));

        //delete event and group
        RestAssured.when()
                .delete("/event/{eventID}", eventID)
                .then()
                .statusCode(200);

        groupRepo.delete(group);
    }

    @Test
    public void rsvps(){
        String requestBody = """
                {
                    "eventName":"test event",
                    "craftType":"test",
                    "description":"testing"
                }
                """;

        //create public event
        int eventID = given().contentType("application/json").body(requestBody).when()
                .post("/event/create/{username}/{eventDate}", testUser.getUsername(), "2026-11-12T14:00:00")
                .then().statusCode(200).extract().path("id");


        //rsvp yes
        RestAssured.when()
                .put("/event/{eventID}/{username}/{status}", eventID, testUser.getUsername(), "yes")
                .then().statusCode(200);

        //get rsvp yes list
        RestAssured.when()
                .get("/event/{eventID}/yes", eventID)
                .then().statusCode(200)
                .body("size()", equalTo(1));

        //get all events where user has rsvped yes
        RestAssured.when()
                .get("/event/user/{username}", testUser.getUsername())
                .then().statusCode(200)
                .body("size()", equalTo(1));

        //rsvp no
        RestAssured.when()
                .put("/event/{eventID}/{username}/{status}", eventID, testUser.getUsername(), "no")
                .then().statusCode(200);

        //get rsvp no list
        RestAssured.when()
                .get("/event/{eventID}/no", eventID)
                .then().statusCode(200)
                .body("size()", equalTo(1));

        //delete event
        RestAssured.when()
                .delete("/event/{eventID}", eventID)
                .then()
                .statusCode(200);
    }

    @Test
    public void searchEvents(){
        String requestBody = """
                {
                    "eventName":"super specific test event in order to test event search feature",
                    "craftType":"test",
                    "description":"testing"
                }
                """;

        //create public event
        int eventID = given().contentType("application/json").body(requestBody).when()
                .post("/event/create/{username}/{eventDate}", testUser.getUsername(), "2026-11-12T14:00:00")
                .then().statusCode(200).extract().path("id");

        //search for event
        RestAssured.when()
                .get("/event/search/{eventName}", "super specific test event in order to test event search feature")
                .then().statusCode(200)
                .body("size()", equalTo(1));

        //delete event
        RestAssured.when()
                .delete("/event/{eventID}", eventID)
                .then()
                .statusCode(200);
    }

    @Test
    public void eventComments(){
        String requestBody = """
                {
                    "eventName":"super specific test event in order to test event search feature",
                    "craftType":"test",
                    "description":"testing"
                }
                """;

        //create public event
        int eventID = given().contentType("application/json").body(requestBody).when()
                .post("/event/create/{username}/{eventDate}", testUser.getUsername(), "2026-11-12T14:00:00")
                .then().statusCode(200).extract().path("id");

        //add comment
        int commentID = given().contentType("application/json").body("test comment").when()
                .post("/event/{eventID}/{username}/comment", eventID, testUser.getUsername())
                .then().statusCode(200).extract().path("comments[0].id");

        //like comment
        RestAssured.when()
                .put("/event/{commentID}/like", commentID)
                .then()
                .statusCode(200);

        //confirm liked
        RestAssured.when()
                .get("/event/{eventID}", eventID)
                .then().statusCode(200).assertThat()
                .body("comments[0].likes", equalTo(1));

        //unlike comment
        RestAssured.when()
                .put("/event/{commentID}/unlike", commentID)
                .then()
                .statusCode(200);

        //confirm unliked
        RestAssured.when()
                .get("/event/{eventID}", eventID)
                .then().statusCode(200).assertThat()
                .body("comments[0].likes", equalTo(0));

        //delete comment
        RestAssured.when()
                .delete("/event/comment/{commentID}", commentID)
                .then().statusCode(200);

        //confirm comment deleted
        RestAssured.when()
                .get("/event/{eventID}", eventID)
                .then().statusCode(200).assertThat()
                .body("comments.size()", equalTo(0));

        //delete event
        RestAssured.when()
                .delete("/event/{eventID}", eventID)
                .then()
                .statusCode(200);
    }

    // ---------------- FeedController tests ----------------
    @Test
    public void postProjectsUpdateProjects(){
        //create project 1
        String requestBody = """
                {
                    "projectName": "test project 1",
                    "visibility": "public"
                }
                """;

        String projectName1 = given().contentType("application/json").body(requestBody).when()
                .post("/feed/{username}", testUser.getUsername())
                .then().statusCode(200).extract().path("projectName");

        //create project 2
        requestBody = """
                {
                    "projectName": "test project 2",
                    "visibility": "public"
                }
                """;
        String projectName2 = given().contentType("application/json").body(requestBody).when()
                .post("/feed/{username}", testUser.getUsername())
                .then().statusCode(200).extract().path("projectName");

        //get projects posted by user
        RestAssured.when()
                .get("/feed/home/{username}", testUser.getUsername())
                .then().statusCode(200).assertThat()
                .body("size()", equalTo(2));

        //update project
        requestBody = """
                {
                    "projectName": "test update name",
                    "projectDesc": "test update description",
                    "projectType": "test update type",
                    "supplies": "test update supplies",
                    "visibility": "test update visibility"
                }
                """;
        projectName1 = given().contentType("application/json").body(requestBody).when()
                .put("/feed/{username}/{projectName}", testUser.getUsername(), projectName1)
                .then().statusCode(200).extract().path("projectName");

        //confirm update
        RestAssured.when()
                .get("/feed/{username}/{projectName}", testUser.getUsername(), projectName1)
                .then().statusCode(200).assertThat()
                .body("projectName", equalTo("test update name"))
                .body("projectDesc", equalTo("test update description"))
                .body("projectType", equalTo("test update type"))
                .body("supplies", equalTo("test update supplies"))
                .body("visibility", equalTo("test update visibility"));

        //delete projects
        RestAssured.when()
                .delete("/feed/{username}/{projectName}", testUser.getUsername(), projectName1)
                .then()
                .statusCode(200);

        RestAssured.when()
                .delete("/feed/{username}/{projectName}", testUser.getUsername(), projectName2)
                .then()
                .statusCode(200);
    }

    @Test
    public void getUserFeed(){
        //create new user
        if (userRepo.findByUsername("kkeckOtherTestUser").isEmpty()) {
            Users u = new Users();
            u.setUsername("kkeckOtherTestUser");
            u.setDisplayName("katie other test user");
            u.setPassword("password123");
            userRepo.save(u);
        }
        Users otherUser = userRepo.findByUsername("kkeckOtherTestUser").orElseThrow();

        //create project 1
        String requestBody = """
                {
                    "projectName": "test project 1",
                    "visibility": "public"
                }
                """;

        String projectName1 = given().contentType("application/json").body(requestBody).when()
                .post("/feed/{username}", testUser.getUsername())
                .then().statusCode(200).extract().path("projectName");

        //create project 2
        requestBody = """
                {
                    "projectName": "test project 2",
                    "visibility": "public"
                }
                """;
        String projectName2 = given().contentType("application/json").body(requestBody).when()
                .post("/feed/{username}", otherUser.getUsername())
                .then().statusCode(200).extract().path("projectName");

        //testUser follow otherUser
        if(!followRepo.existsByFollowerAndFollowing(testUser, otherUser)) {
            Follow follow = new Follow(testUser, otherUser, true);
            followRepo.save(follow);
        }
        //get testUser feed
        RestAssured.when()
                .get("/feed/{username}", testUser.getUsername())
                .then().statusCode(200).assertThat()
                .body("size()", equalTo(2));

        //delete projects and user
        RestAssured.when()
                .delete("/feed/{username}/{projectName}", testUser.getUsername(), projectName1)
                .then()
                .statusCode(200);

        RestAssured.when()
                .delete("/feed/{username}/{projectName}", otherUser.getUsername(), projectName2)
                .then()
                .statusCode(200);
    }

    @Test
    public void feedComments(){
        //create project
        String requestBody = """
                {
                    "projectName": "test project",
                    "visibility": "public"
                }
                """;

        String projectName = given().contentType("application/json").body(requestBody).when()
                .post("/feed/{username}", testUser.getUsername())
                .then().statusCode(200).extract().path("projectName");

        //add comment
        requestBody = """
                {
                    "text":"test comment"
                }
                """;
        int commentID = given().contentType("application/json").body(requestBody).when()
                .post("/feed/{username}/{projectName}/comment", testUser.getUsername(), projectName)
                .then().statusCode(200).extract().path("comments[0].id");

        //like comment
        RestAssured.when()
                .put("/feed/{username}/{projectName}/{id}", testUser.getUsername(), projectName, commentID)
                .then()
                .statusCode(200);

        //confirm liked
        RestAssured.when()
                .get("/feed/{username}/{projectName}", testUser.getUsername(), projectName)
                .then().statusCode(200).assertThat()
                .body("comments[0].likes", equalTo(1));

        //unlike comment
        RestAssured.when()
                .put("/feed/{username}/{projectName}/{id}/unlike", testUser.getUsername(), projectName, commentID)
                .then()
                .statusCode(200);

        //confirm unliked
        RestAssured.when()
                .get("/feed/{username}/{projectName}", testUser.getUsername(), projectName)
                .then().statusCode(200).assertThat()
                .body("comments[0].likes", equalTo(0));

        //delete comment
        RestAssured.when()
                .delete("/feed/{username}/{projectName}/{id}", testUser.getUsername(), projectName, commentID)
                .then().statusCode(200);

        //confirm comment deleted
        RestAssured.when()
                .get("/feed/{username}/{projectName}", testUser.getUsername(), projectName)
                .then().statusCode(200).assertThat()
                .body("comments.size()", equalTo(0));

        //delete project
        RestAssured.when()
                .delete("/feed/{username}/{projectName}", testUser.getUsername(), projectName)
                .then()
                .statusCode(200);
    }

    // ---------------- ImageController tests ----------------
    @Test
    public void getPostDeleteImage(){
        //create file
        File temp = null;
        try {
            temp = File.createTempFile("test-image", ".jpg");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            Files.write(temp.toPath(), "fake-image-data".getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //upload image
        int imageId =
                given()
                        .multiPart("image", temp, "image/jpeg")
                        .when()
                        .post("/images")
                        .then()
                        .statusCode(200)
                        .extract().path("id");

        assertNotNull(imageId);


        //get image
        given()
                .when()
                .get("/images/{id}", imageId)
                .then()
                .statusCode(200)
                .body("id", equalTo(imageId))
                .body("filePath", notNullValue());


        //delete image
        given()
                .when()
                .delete("/images/{id}", imageId)
                .then()
                .statusCode(200);


        //confirm deletion
        given()
                .when()
                .get("/images/{id}", imageId)
                .then()
                .statusCode(404);
    }

    // ---------------- LoginEditUserController tests ----------------
    @Test
    public void updateAndLoginUser(){
        //create user
        Users u = new Users();
        u.setUsername("loginEditUser");
        u.setDisplayName("login edit test user");
        u.setPassword("password123");
        userRepo.save(u);

        //update user
        String requestBody = """
                {
                    "username": "updatedUsername",
                    "bio": "update bio",
                    "password": "update Password123",
                    "followers": 100,
                    "following": 100,
                    "displayName": "update login edit user display name",
                    "email": "update email",
                    "craftSpecialties": "update craft specialties"
                }
                """;
        String username = given().contentType("application/json").body(requestBody).when()
                .put("/user/{username}", u.getUsername())
                .then().statusCode(200).extract().path("username");

        //check login
        RestAssured.when()
                .get("/login/{username}/{password}", username, "update Password123")
                .then().statusCode(200).assertThat()
                .body("username", equalTo("updatedUsername"))
                .body("bio", equalTo("update bio"))
                .body("followers", equalTo(100))
                .body("following", equalTo(100))
                .body("displayName", equalTo("update login edit user display name"))
                .body("email", equalTo("update email"))
                .body("craftSpecialties", equalTo("update craft specialties"));

        //delete user
        RestAssured.when()
                .delete("users/delete/{username}", username)
                .then()
                .statusCode(200);
    }

    // ---------------- ConversationController tests ----------------
    @Test
    public void createGroupConvoAddRemoveUser(){
        //create group
        String requestBody = """
                ["kkeckTestUser", "Kkeck"]
                """;

        String groupID = given().contentType("application/json").body(requestBody).when()
                .post("/messages/group")
                .then().statusCode(200).extract().path("id");

        //update group pic
        requestBody = """
        {
            "id": 1,
            "filePath": "/fake/path/group-pic.jpg"
        }
        """;
        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .put("/messages/{groupId}/pic", groupID)
                .then()
                .statusCode(200)
                .body("groupPic.id", equalTo(1))
                .body("groupPic.filePath", equalTo("/fake/path/group-pic.jpg"));

        //remove user from group
        RestAssured.when()
                .put("/messages/{groupId}/remove/{username}", groupID, "Kkeck")
                .then()
                .statusCode(200);

        //confirm user removed
        RestAssured.when()
                .get("/messages/{convoId}", groupID)
                .then()
                .statusCode(200)
                .body("members.size()", equalTo(1));

        //add user to group
        RestAssured.when()
                .put("/messages/{groupId}/add/{username}", groupID, "Kkeck")
                .then()
                .statusCode(200);

        //confirm user added
        RestAssured.when()
                .get("/messages/{convoId}", groupID)
                .then()
                .statusCode(200)
                .body("members.size()", equalTo(2));

        //delete convo
        RestAssured.when()
                .delete("/messages/convo/{convoId}", groupID)
                .then()
                .statusCode(200);
    }

    @Test
    public void directConvosGetUserConvosDeleteMessage(){
        //create convos
        String convo1 = RestAssured.when()
                .post("/messages/create/{sender}/{receiver}", testUser.getUsername(), "Kkeck")
                .then()
                .statusCode(200).extract().path("id");

        String convo2 = RestAssured.when()
                .post("/messages/create/{sender}/{receiver}", testUser.getUsername(), "kkeckOtherTestUser")
                .then()
                .statusCode(200).extract().path("id");

        //get all user convos
        RestAssured.when()
                .get("/messages/convos/{username}", testUser.getUsername())
                .then()
                .statusCode(200)
                .body("size()", equalTo(2));

        //add message
        DirectConversation convo = directConvoRepo.findById(convo1).orElseThrow();
        Message message = new Message(testUser.getUsername(), "test message", convo);
        messageRepo.save(message);

        //confirm message is added
        RestAssured.when()
                .get("/messages/{convoId}", convo1)
                .then()
                .statusCode(200)
                .body("messages.size()", equalTo(1));

        //delete message
        RestAssured.when()
                .delete("/messages/{messageId}", message.getId())
                .then()
                .statusCode(200);

        //confirm message is deleted
        RestAssured.when()
                .get("/messages/{convoId}", convo1)
                .then()
                .statusCode(200)
                .body("messages.size()", equalTo(0));

        //delete convos
        RestAssured.when()
                .delete("/messages/convo/{convoId}", convo1)
                .then()
                .statusCode(200);

        RestAssured.when()
                .delete("/messages/convo/{convoId}", convo2)
                .then()
                .statusCode(200);
    }

    // ---------------- patternsController tests ----------------
    @Test
    public void postPatternUpdateDescription(){
        //create Patterns
        String requestBody = """
                {
                    "patternName": "test pattern 1",
                    "patternType": "test"
                }
                """;
        String patternName1 = given().contentType("application/json").body(requestBody).when()
                .post("/patterns/{username}", testUser.getUsername())
                .then().statusCode(200).extract().path("patternName");

        requestBody = """
                {
                    "patternName": "test pattern 2",
                    "patternType": "test"
                }
                """;
        String patternName2 = given().contentType("application/json").body(requestBody).when()
                .post("/patterns/{username}", testUser.getUsername())
                .then().statusCode(200).extract().path("patternName");

        //get user patterns
        RestAssured.when()
                .get("/patterns/author/{username}", testUser.getUsername())
                .then()
                .statusCode(200)
                .body("size()", equalTo(2));

        //update pattern description
        given().contentType("application/json").body("updated pattern description").when()
                .put("/patterns/{username}/{patternName}", testUser.getUsername(), patternName1)
                .then()
                .statusCode(200);

        //confirm description update
        RestAssured.when()
                .get("/patterns/{username}/{patternName}", testUser.getUsername(), patternName1)
                .then()
                .statusCode(200)
                .body("description", equalTo("updated pattern description"));

        //delete patterns
        RestAssured.when()
                .delete("/patterns/{username}/{patternName}", testUser.getUsername(), patternName1)
                .then()
                .statusCode(200);

        RestAssured.when()
                .delete("/patterns/{username}/{patternName}", testUser.getUsername(), patternName2)
                .then()
                .statusCode(200);
    }

    @Test
    public void followersPatternsSearch(){
        //create new user
        if (userRepo.findByUsername("kkeckOtherTestUser").isEmpty()) {
            Users u = new Users();
            u.setUsername("kkeckOtherTestUser");
            u.setDisplayName("katie other test user");
            u.setPassword("password123");
            userRepo.save(u);
        }
        Users otherUser = userRepo.findByUsername("kkeckOtherTestUser").orElseThrow();

        if(!followRepo.existsByFollowerAndFollowing(testUser, otherUser)) {
            Follow follow = new Follow(testUser, otherUser, true);
            followRepo.save(follow);
        }
        //create Patterns
        String requestBody = """
                {
                    "patternName": "test pattern 1 specific title so easily searched",
                    "patternType": "test"
                }
                """;
        String patternName1 = given().contentType("application/json").body(requestBody).when()
                .post("/patterns/{username}", otherUser.getUsername())
                .then().statusCode(200).extract().path("patternName");

        requestBody = """
                {
                    "patternName": "test pattern 2 specific title so easily searched",
                    "patternType": "test"
                }
                """;
        String patternName2 = given().contentType("application/json").body(requestBody).when()
                .post("/patterns/{username}", otherUser.getUsername())
                .then().statusCode(200).extract().path("patternName");

        //get followers patterns
        RestAssured.when()
                .get("/patterns/{username}", testUser.getUsername())
                .then()
                .statusCode(200)
                .body("size()", equalTo(2));

        //search patterns
        RestAssured.when()
                .get("/patterns/title/{search}", "specific title so easily searched")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2));

        //delete patterns
        RestAssured.when()
                .delete("/patterns/{username}/{patternName}", otherUser.getUsername(), patternName1)
                .then()
                .statusCode(200);

        RestAssured.when()
                .delete("/patterns/{username}/{patternName}", otherUser.getUsername(), patternName2)
                .then()
                .statusCode(200);
    }

    @Test
    public void patternComments(){
        //create Pattern
        String requestBody = """
                {
                    "patternName": "test pattern",
                    "patternType": "test"
                }
                """;
        String patternName = given().contentType("application/json").body(requestBody).when()
                .post("/patterns/{username}", testUser.getUsername())
                .then().statusCode(200).extract().path("patternName");

        //add comment
        requestBody = """
                {
                    "text":"test comment",
                    "rating": 4
                }
                """;
        int commentID = given().contentType("application/json").body(requestBody).when()
                .post("/patterns/{username}/{patternName}/comment/{commentUsername}"
                        , testUser.getUsername(), patternName, testUser.getUsername())
                .then().statusCode(200).extract().path("comments[0].id");

        //like comment
        RestAssured.when()
                .put("/patterns/{username}/{patternName}/{id}/like", testUser.getUsername(), patternName, commentID)
                .then()
                .statusCode(200);

        //confirm liked
        RestAssured.when()
                .get("/patterns/{username}/{patternName}", testUser.getUsername(), patternName)
                .then().statusCode(200).assertThat()
                .body("comments[0].likes", equalTo(1));

        //update comment
        given().contentType("application/json").body("update comment text").when()
                .put("patterns/{username}/{patternName}/{id}", testUser.getUsername(), patternName, commentID)
                .then()
                .statusCode(200);

        //confirm update
        RestAssured.when()
                .get("/patterns/{username}/{patternName}", testUser.getUsername(), patternName)
                .then().statusCode(200).assertThat()
                .body("comments[0].text", equalTo("update comment text"));

        //get all comments user has posted
        RestAssured.when()
                .get("/patterns/{username}/ratings", testUser.getUsername())
                .then().statusCode(200).assertThat()
                .body("size()", equalTo(1));

        //delete comment
        RestAssured.when()
                .delete("/patterns/{username}/{patternName}/{id}", testUser.getUsername(), patternName, commentID)
                .then().statusCode(200);

        //confirm comment deleted
        RestAssured.when()
                .get("/patterns/{username}/{patternName}", testUser.getUsername(), patternName)
                .then().statusCode(200).assertThat()
                .body("comments.size()", equalTo(0));

        //delete pattern
        RestAssured.when()
                .delete("/patterns/{username}/{patternName}", testUser.getUsername(), patternName)
                .then()
                .statusCode(200);
    }
}
