//package com.example.craftsy;
//import com.example.craftsy.SignUpDelete.Entity.Users;
//import io.restassured.RestAssured;
//import io.restassured.http.ContentType;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDate;
//import java.util.Map;
//
//import static io.restassured.RestAssured.*;
//import static org.hamcrest.Matchers.*;
//
//
//public class AlisterSystemTest {
//
//
//    @BeforeEach
//    public void setup() {
//        RestAssured.baseURI = "http://coms-3090-028.class.las.iastate.edu";
//        RestAssured.port = 8080;
//        RestAssured.basePath = "";
//    }
//
//
//    // ==================== USER CONTROLLER TESTS ====================
//
//    @Test
//    public void testSignupStrongAndDeleteUser() {
//        String username = "TestUser_" + System.currentTimeMillis();
//
//        // Sign up
//        String userJson = """
//        {
//            "username": "%s",
//            "displayName": "Test Display",
//            "password": "Abcdef12"
//        }
//        """.formatted(username);
//
//        given()
//                .contentType(ContentType.JSON)
//                .body(userJson)
//                .when()
//                .post("/users/signup")
//                .then()
//                .statusCode(200)
//                .body("username", equalTo(username));
//
//        // Delete
//        given()
//                .when()
//                .delete("/users/delete/" + username)
//                .then()
//                .statusCode(200)
//                .body(containsString("deleted successfully"));
//    }
//
//    // ==================== FOLLOWING/FOLLOWERS TEST ====================
//    @Test
//    public void testFollowWorkflow() {
//        String follower = "Follower_" + System.currentTimeMillis();
//        String target = "Target_" + System.currentTimeMillis();
//
//        createUser(follower, "Follower User", "Password123");
//        createUser(target, "Target User", "Password123");
//
//        // Send follow request
//        given()
//                .when()
//                .post("/" + follower + "/follow/" + target)
//                .then()
//                .statusCode(200)
//                .body("message", equalTo("Follow request sent successfully"));
//
//        // Check profile status pending
//        given()
//                .when()
//                .get("/" + follower + "/profile/" + target)
//                .then()
//                .statusCode(200)
//                .body("isPending", equalTo(true))
//                .body("isFollowing", equalTo(false));
//
//        // Unfollow (cancel request)
//        given()
//                .when()
//                .delete("/" + follower + "/unfollow/" + target)
//                .then()
//                .statusCode(200)
//                .body("message", equalTo("User unfollowed"));
//    }
//
//
//
//    // ==================== GROUP SYSTEM TEST ====================
//    @Test
//    public void testFullGroupWorkflow() {
//        String admin = "alister_gan";
//        String member = "Fuji";
//
//
//        String groupName = "SystemTestGroup_" + System.currentTimeMillis();
//
//        // 1. Create Group
//        String groupJson = """
//        {
//            "groupName": "%s",
//            "description": "Test group workflow",
//            "craft": "Knitting",
//            "isPrivate": false
//        }
//        """.formatted(groupName);
//
//        given()
//                .contentType(ContentType.JSON)
//                .body(groupJson)
//                .when()
//                .post("/" + admin + "/create")
//                .then()
//                .statusCode(200)
//                .body("message", equalTo("Group created successfully"));
//
//        // 2. Get Group ID
//        String groupId = given()
//                .when()
//                .get("/groupId/" + groupName)
//                .then()
//                .statusCode(200)
//                .extract()
//                .path("groupId")
//                .toString();
//
//        // 3. Member joins group
//        given()
//                .when()
//                .post("/" + member + "/join/" + groupId)
//                .then()
//                .statusCode(200)
//                .body("message", equalTo("User added successfully"));
//
//        // 4. Update group
//        String updateJson = """
//        {
//            "groupName": "%s_Updated",
//            "description": "Updated description"
//        }
//        """.formatted(groupName);
//
//        given()
//                .contentType(ContentType.JSON)
//                .body(updateJson)
//                .when()
//                .put("/" + admin + "/update/" + groupId)
//                .then()
//                .statusCode(200)
//                .body("message", equalTo("Group updated successfully"));
//
//        // 5. Delete group
//        given()
//                .when()
//                .delete("/" + admin + "/delete/" + groupId)
//                .then()
//                .statusCode(200)
//                .body("message", equalTo("Group deleted successfully"));
//    }
//
//    // ==================== HELPER METHODS ====================
//    private void createUser(String username, String displayName, String password) {
//        String uniqueDisplayName = displayName + "_" + System.currentTimeMillis();
//        String userJson = """
//        {
//            "username": "%s",
//            "displayName": "%s",
//            "password": "%s"
//        }
//        """.formatted(username, uniqueDisplayName, password);
//
//        given()
//                .contentType(ContentType.JSON)
//                .body(userJson)
//                .when()
//                .post("/users/signup")
//                .then()
//                .statusCode(200);
//    }
//
//
//
//
//    // ==================== Search SYSTEM TEST ====================
//    @Test
//    public void testSearchUsers() {
//        String query = "alister_gan";
//
//        given()
//                .contentType(ContentType.JSON)
//                .queryParam("query", query)
//                .when()
//                .get("/search/user")
//                .then()
//                .statusCode(200)
//                .body("$", not(empty()))
//                .body("username", everyItem(containsStringIgnoringCase(query)));
//    }
//
//    @Test
//    public void testSearchGroups() {
//        String query = "Knitting";
//
//        given()
//                .contentType(ContentType.JSON)
//                .queryParam("query", query)
//                .when()
//                .get("/search/group")
//                .then()
//                .statusCode(200)
//                .body("$", not(empty()))
//                .body("groupName", everyItem(containsStringIgnoringCase(query))
//                        );
//    }
//
//    @Test
//    public void testSearchProjects() {
//        String query = "Craft Project";
//
//        given()
//                .contentType(ContentType.JSON)
//                .queryParam("query", query)
//                .when()
//                .get("/search/project")
//                .then()
//                .statusCode(200)
//                .body("$", not(empty()))
//                .body("projectName", everyItem(containsStringIgnoringCase(query))
//                        );
//    }
//
//    @Test
//    public void testSearchTutorials() {
//        String query = "Youtube";
//
//        given()
//                .contentType(ContentType.JSON)
//                .queryParam("query", query)
//                .when()
//                .get("/search/tutorial")
//                .then()
//                .statusCode(200)
//                .body("$", not(empty()))
//                .body("title", everyItem(containsStringIgnoringCase(query))
//                        );
//    }
//
//
//
//    // ==================== Tutorial SYSTEM TEST ====================
//
//    @Test
//    public void testTutorialWorkflow() {
//
//        String username = "alister_gan";
//        String title = "SystemTestTutorial_" + System.currentTimeMillis();
//
//        // CREATE tutorial (URL upload)
//        given()
//                .contentType(ContentType.URLENC)
//                .formParam("username", username)
//                .formParam("title", title)
//                .formParam("description", "A system test tutorial description")
//                .formParam("category", "Knitting")
//                .formParam("fileUrl", "https://www.youtube.com/watch?v=hM5M2Fu0RtY")
//                .when()
//                .post("/tutorial/uploadUrl")
//                .then()
//                .statusCode(200)
//                .body(equalTo("Tutorial uploaded successfully with URL!"));
//
//        // SEARCH tutorial to get its ID
//        Long id =
//                given()
//                        .queryParam("query", title)
//                        .queryParam("username", username)
//                        .when()
//                        .get("/tutorial/search")
//                        .then()
//                        .statusCode(200)
//                        .extract()
//                        .jsonPath().getLong("[0].id");
//
//        // UPDATE tutorial (without file)
//        String updatedTitle = title + "_UPDATED";
//        String updatedDescription = "Updated description for system test tutorial";
//
//        given()
//                .multiPart("title", updatedTitle)
//                .multiPart("description", updatedDescription)
//                .when()
//                .put("/tutorial/" + id)
//                .then()
//                .statusCode(200)
//                .body(equalTo("Tutorial Updated successfully!"));
//
//        // VERIFY tutorial was updated
//        given()
//                .queryParam("query", updatedTitle)
//                .queryParam("username", username)
//                .when()
//                .get("/tutorial/search")
//                .then()
//                .statusCode(200)
//                .body("title", hasItem(updatedTitle))
//                .body("description", hasItem(updatedDescription));
//
//        // DELETE tutorial
//        given()
//                .when()
//                .delete("/tutorial/" + id)
//                .then()
//                .statusCode(200)
//                .body(equalTo("Tutorial deleted successfully!"));
//
//    }
//
//
//
//
//    // ==================== PointsSystem SYSTEM TEST ====================
//    @Test
//    public void testPointsSystemWorkflow() {
//
//        String username = "Quinn";
//
//        // 1. GET user's points
//        Map<String, Object> pointsResponse =
//                given()
//                        .pathParam("username", username)
//                        .when()
//                        .get("/points/{username}")
//                        .then()
//                        .statusCode(200)
//                        .body("username", equalTo(username))
//                        .body("totalPoints", notNullValue())
//                        .body("currentTier", notNullValue())
//                        .body("postsCount", notNullValue())
//                        .extract()
//                        .as(Map.class);
//
//        Integer initialPoints = (Integer) pointsResponse.get("totalPoints");
//
//        // 2. GET user's points history
//        given()
//                .pathParam("username", username)
//                .when()
//                .get("/points/{username}/history")
//                .then()
//                .statusCode(200)
//                .body("$", notNullValue());  // List of history events
//
//        // 3. GET leaderboard
//        given()
//                .when()
//                .get("/points/leaderboard")
//                .then()
//                .statusCode(200)
//                .body("$", not(empty()))
//                .body("[0].username", notNullValue())
//                .body("[0].totalPoints", notNullValue());
//
//        // 4. GET tier information
//        given()
//                .when()
//                .get("/points/tiers")
//                .then()
//                .statusCode(200)
//                .body("$", hasSize(4))
//                .body("[0].name", equalTo("BEGINNER"))
//                .body("[3].name", equalTo("CHAMPION"));
//    }
//
//
//
//
//    @Test
//    public void testChallengeWorkflow() {
//
//        String championUsername = "alister_gan";
//        String normalUsername = "Kkeck";
//         Long challengeId;
//         Long postId;
//
//        // CREATE challenge as CHAMPION
//        Map<String, Object> challengeBody = Map.of(
//                "title", "SystemTestChallenge_" + System.currentTimeMillis(),
//                "description", "Challenge for system test",
//                "type", "PHOTO",
//                "category", "Knitting",
//                "startDate", LocalDate.now().toString(),
//                "endDate", LocalDate.now().plusDays(7).toString()
//        );
//
//        challengeId = given()
//                .contentType(ContentType.JSON)
//                .body(challengeBody)
//                .when()
//                .post("/challenge/" + championUsername + "/post")
//                .then()
//                .statusCode(200)
//                .body("message", equalTo("Challenge created successfully"))
//                .extract()
//                .jsonPath()
//                .getLong("id");
//
//
//        // PARTICIPATE in challenge as normal user
//        given()
//                .when()
//                .post("/challenge/" + normalUsername + "/participate/" + challengeId)
//                .then()
//                .statusCode(200)
//                .body("message", equalTo("Participation recorded"));
//
//        // CREATE post for challenge
//        postId = given()
//                .multiPart("description", "My challenge post")
//                .when()
//                .post("/challenge/" + normalUsername + "/create/posts/" + challengeId)
//                .then()
//                .statusCode(200)
//                .body("message", equalTo("Post created successfully"))
//                .extract()
//                .jsonPath()
//                .getLong("postId");
//
//        // LIKE the post
//        given()
//                .when()
//                .post("/challenge/" + championUsername + "/like/" + postId)
//                .then()
//                .statusCode(200)
//                .body("message", equalTo("Post liked"))
//                .body("likeCount", equalTo(1));
//
//        // COMMENT on the post
//        Long commentId = given()
//                .body("Great post!")
//                .contentType(ContentType.JSON)
//                .when()
//                .post("/challenge/" + championUsername + "/comment/" + postId)
//                .then()
//                .statusCode(200)
//                .body("message", equalTo("Comment added"))
//                .extract()
//                .jsonPath()
//                .getLong("commentId");
//
//        // GET comments
//        given()
//                .when()
//                .get("/challenge/" + postId + "/comments")
//                .then()
//                .statusCode(200)
//                .body("[0].comment", equalTo("Great post!"))
//                .body("[0].user.username", equalTo(championUsername));
//
//        // COMPLETE challenge
//        given()
//                .when()
//                .post("/challenge/" + normalUsername + "/complete/" + challengeId)
//                .then()
//                .statusCode(200)
//                .body("message", equalTo("Challenge marked as completed. Points awarded!"));
//
//        // DELETE challenge as creator
//        given()
//                .when()
//                .delete("/challenge/" + championUsername + "/delete/" + challengeId)
//                .then()
//                .statusCode(200)
//                .body("message", equalTo("Challenge deleted successfully!"));
//    }
//
//
//
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
