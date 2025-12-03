package com.example.craftsy.Tutorial.Controller;

import com.example.craftsy.FollowingFollowers.Entity.Follow;
import com.example.craftsy.FollowingFollowers.Repository.FollowRepository;
import com.example.craftsy.Notification.Entity.Notification;
import com.example.craftsy.Notification.NotificationWebSocket;
import com.example.craftsy.Notification.Repository.NotificationRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.Tutorial.Entity.Tutorial;
import com.example.craftsy.Tutorial.Entity.TutorialLikes;
import com.example.craftsy.Tutorial.Repository.TutorialLikesRepository;
import com.example.craftsy.Tutorial.Repository.TutorialRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tutorial")
public class TutorialController {

    @Autowired
    private TutorialRepository tutorialRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TutorialLikesRepository likesRepository;

    @Autowired
    private com.example.craftsy.PointsSystem.PointsService pointsService;




    /**
     * Upload the video by File
     * @param title
     * @param description
     * @param file
     * @return
     */
    @Operation(
            summary = "Upload a tutorial using a file",
            description = "Allows EXPERT or CHAMPION users to upload a tutorial video/image file. "
                    + "Saves uploaded file locally and notifies all followers."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tutorial uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user or file upload failure"),
            @ApiResponse(responseCode = "403", description = "User does not meet tier requirements")
    })
    @PostMapping(value = "/uploadFile", consumes =  MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadTutorialFile(
            @RequestParam("username") String username,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isPrivate", required = false, defaultValue = "false") boolean isPrivate
    ) { // MultipartFile is a spring class representing an uploaded file
        // Extracts the title, desc, and file from the request
        try {

            // Find user
            Optional<Users> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            Users user = userOpt.get();

            // Get user tier
            String userTier = pointsService.getUserPoints(user).getCurrentTier();

            // Only allow EXPERT or CHAMPION users to upload
            if (!(userTier.equals("EXPERT") || userTier.equals("CHAMPION"))) {
                return ResponseEntity.status(403).body("You must be EXPERT level or higher to upload tutorials.");

            }

            //  Create folder if not exist
            String uploadDir = "uploads/tutorials";
            Files.createDirectories(Paths.get(uploadDir)); //Converts string path into a path object

            //  Generate a unique file name
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename(); // Adds time of upload to make the file unique
            Path filePath = Paths.get(uploadDir, fileName); // full folder and file path

            //  Save the file to disk, write the file stream into the location filePath and replace any file with same name
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            //  Save metadata to DB
            Tutorial tutorial = new Tutorial();
            tutorial.setTitle(title);
            tutorial.setDescription(description);
            tutorial.setFileName(fileName);
            tutorial.setFileType(file.getContentType());
            tutorial.setFilePath(filePath.toString());
            tutorial.setCategory(category);
            tutorial.setUser(user);
            tutorial.setIsPrivate(isPrivate);

            tutorialRepository.save(tutorial);


            // ===== Notify followers =====

            List<Follow> followList = followRepository.findByFollowing(user);
            List<Users> followers = followList.stream()
                    .map(Follow::getFollower)
                    .toList();

            for (Users follower : followers) {
                Notification notif = new Notification();
                notif.setUser(follower);
                notif.setTitle("New Tutorial Uploaded");
                notif.setMessage(user.getUsername() + " uploaded a new tutorial: " + tutorial.getTitle());
                notif.setCreatedAt(new Date());
                notif.setIsRead(false);
                notificationRepository.save(notif);

                // Push notification via WebSocket
                NotificationWebSocket.pushNotification(follower.getUsername(), notif);
            }

            return ResponseEntity.ok("Tutorial uploaded successfully!");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("File upload failed: " + e.getMessage());
        }
    }

    /**
     * POST image/video by URL
     */
    @Operation(
            summary = "Upload a tutorial using an external URL",
            description = "Stores external video/image URL instead of uploading a file."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tutorial uploaded successfully with URL"),
            @ApiResponse(responseCode = "400", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "User does not meet tier requirements")
    })
    @PostMapping("/uploadUrl")
    public ResponseEntity<String> uploadTutorialUrl(
            @RequestParam String username,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam String fileUrl,
            @RequestParam(value = "isPrivate", required = false, defaultValue = "false") boolean isPrivate// URL to external image/video
    ) {
        // Find user
        Optional<Users> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        Users user = userOpt.get();

        // Get user tier
        String userTier = pointsService.getUserPoints(user).getCurrentTier();

        // Only allow EXPERT or CHAMPION users to upload
        if (!(userTier.equals("EXPERT") || userTier.equals("CHAMPION"))) {
            return ResponseEntity.status(403).body("You must be EXPERT level or higher to upload tutorials.");

        }

        Tutorial tutorial = new Tutorial();
        tutorial.setTitle(title);
        tutorial.setDescription(description);
        tutorial.setCategory(category);
        tutorial.setFileUrl(fileUrl); // store URL instead of file path
        tutorial.setUser(user);
        tutorial.setIsPrivate(isPrivate);
        tutorialRepository.save(tutorial);

        return ResponseEntity.ok("Tutorial uploaded successfully with URL!");
    }


    /**
     * GET "/tutorial/{id}/file"
     * * Fetch the file of the Videos - Optimized for direct streaming
     */
    @Operation(
            summary = "Fetch or stream tutorial file",
            description = "Streams local file or redirects to external file URL."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File streamed successfully"),
            @ApiResponse(responseCode = "500", description = "Error reading file")
    })
    @GetMapping("/{id}/file")
    public ResponseEntity<?> getTutorialFile(@PathVariable Long id) {
        Optional<Tutorial> tutorialOpt = tutorialRepository.findById(id);
        if (tutorialOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tutorial tutorial = tutorialOpt.get();

        // External URL - Redirect to the external source
        if (tutorial.getFileUrl() != null && !tutorial.getFileUrl().isEmpty()) {
            // Return a redirect response so the client fetches from the external URL directly
            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                    .location(java.net.URI.create(tutorial.getFileUrl()))
                    .build();
        }

        // Local file - Stream directly
        if (tutorial.getFilePath() != null && !tutorial.getFilePath().isEmpty()) {
            Path path = Paths.get(tutorial.getFilePath());
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            try {
                Resource resource = new FileSystemResource(path);

                // Detect content type
                String contentType = Files.probeContentType(path);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                // Return the file with proper headers for streaming
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header("Content-Disposition", "inline; filename=\"" + path.getFileName() + "\"")
                        .body(resource);

            } catch (IOException e) {
                return ResponseEntity.status(500)
                        .body("Error reading file: " + e.getMessage());
            }
        }

        return ResponseEntity.noContent().build();
    }



    /**
     * GET {/search?query={}}
     * Search tutorials by username, title, description, or category
     * Returns correct file URL (local or external)
     */
    @Operation(
            summary = "Search tutorials",
            description = "Search by username, title, description, or category. Returns valid local/external file URL."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchTutorials(@RequestParam String query, @RequestParam String username) {
        List<Tutorial> tutorials = tutorialRepository
                .findByUser_UsernameContainingIgnoreCaseOrTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                        query,  query, query, query);

        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String userTier = pointsService.getUserPoints(user).getCurrentTier();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Tutorial t : tutorials) {

            // Hide private tutorials if user is below Intermediate
            if (t.isPrivate() && !( userTier.equals("INTERMEDIATE")||userTier.equals("EXPERT") || userTier.equals("CHAMPION"))) {
                continue;
            }


            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("username", t.getUser().getUsername());
            map.put("title", t.getTitle());
            map.put("description", t.getDescription());
            map.put("category", t.getCategory());

            // Choose the correct URL
            String fileUrl = t.getFileUrl() != null ? t.getFileUrl() : "/tutorial/" + t.getId() + "/file"; // file path
            map.put("fileURL", fileUrl);
            result.add(map);
        }

        return ResponseEntity.ok(result);
    }


    /**
     * GET /tutorial/user/{username}
     * Fetch all tutorials uploaded by a specific user (For Main Search Tab)
     */
    @Operation(
            summary = "Get all tutorials uploaded by a specific user",
            description = "Viewer tier determines visibility of private tutorials."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tutorial list returned"),
            @ApiResponse(responseCode = "404", description = "User or viewer not found")
    })
    @GetMapping("/user/{username}")
    public ResponseEntity<List<Map<String, Object>>> getTutorialsByUser(
            @PathVariable String username,
            @RequestParam String viewer // viewer = the user making the request
    ) {
        Optional<Users> userOpt = userRepository.findByUsername(username);
        Optional<Users> viewerOpt = userRepository.findByUsername(viewer);
        if (userOpt.isEmpty() || viewerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Users user = userOpt.get();
        Users viewerUser = viewerOpt.get();
        String viewerTier = pointsService.getUserPoints(viewerUser).getCurrentTier();

        List<Tutorial> tutorials = tutorialRepository.findByUser(user);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Tutorial t : tutorials) {
            if (t.isPrivate() && !(viewerTier.equals("INTERMEDIATE")||viewerTier.equals("EXPERT") || viewerTier.equals("CHAMPION"))) {
                continue; // skip private tutorials for Beginner users
            }

            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("title", t.getTitle());
            map.put("description", t.getDescription());
            map.put("category", t.getCategory());
            map.put("username", t.getUser().getUsername());
            map.put("isPrivate", t.isPrivate());

            String fileUrl = (t.getFileUrl() != null)
                    ? t.getFileUrl()
                    : "/tutorial/" + t.getId();
            map.put("fileURL", fileUrl);

            result.add(map);
        }

        return ResponseEntity.ok(result);
    }




    /**
     * PUT "/tutorial/{id}"
     * Update the tutorials
     */
    @Operation(
            summary = "Update an existing tutorial",
            description = "Updates title, description, category, or replaces the file."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tutorial updated successfully"),
            @ApiResponse(responseCode = "400", description = "File upload error"),
            @ApiResponse(responseCode = "404", description = "Tutorial not found")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // Endpoint expects data in form-data format
    public ResponseEntity<String> updateTutorials(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) MultipartFile file // interface that handle file uploads
    ) {
        Optional<Tutorial> tutorialOpt = tutorialRepository.findById(id);
        if (tutorialOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tutorial tutorial = tutorialOpt.get();

        // Update fields
        if (title != null) {
            tutorial.setTitle(title);
        }

        if (description != null) {
            tutorial.setDescription(description);
        }

        if (category != null) {
            tutorial.setCategory(category);
        }


        // update new file if provided
        if (file != null) {
            try {
                String uploadDir = "uploads/tutorials";
                Files.createDirectories((Paths.get(uploadDir)));

                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Delete old file
                if (tutorial.getFileUrl() != null) {
                    Files.deleteIfExists(Paths.get(tutorial.getFilePath()));
                }

                tutorial.setFileName(fileName);
                tutorial.setFileType(file.getContentType());
                tutorial.setFilePath(filePath.toString());
                tutorial.setFileUrl(null);


            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.badRequest().body("File upload failed: " + e.getMessage());
            }


        }

        tutorialRepository.save(tutorial);
        return ResponseEntity.ok("Tutorial Updated successfully!");


    }



    /**
     * DELETE "/tutorial/{id}"
     * Delete the tutorial
     *
     */
    @Operation(
            summary = "Delete a tutorial",
            description = "Deletes database record and any associated local file."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tutorial deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Tutorial not found"),
            @ApiResponse(responseCode = "400", description = "File delete error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTutorial(@PathVariable Long id) {
        Optional<Tutorial> tutorialOpt = tutorialRepository.findById(id);
        if (tutorialOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tutorial tutorial = tutorialOpt.get();

        try {
            // Delete local file if present
            if (tutorial.getFilePath() != null) {
                Files.deleteIfExists(Paths.get(tutorial.getFilePath()));
            }

            // Delete DB entry
            tutorialRepository.delete(tutorial);
            return ResponseEntity.ok("Tutorial deleted successfully!");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to delete tutorial: " + e.getMessage());
        }


    }




    // LIKE features in Tutorials


    // add likes
    @PostMapping("/{username}/like/{tutorialId}")
    public ResponseEntity<?> likeTutorial(
            @PathVariable Long tutorialId,
            @PathVariable String username
    ) {
        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new RuntimeException("Tutorial not found"));

        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Prevent duplicate likes
        Optional<TutorialLikes> existing = likesRepository
                .findByTutorialIdAndUserId(tutorialId, user.getId());

        if (existing.isPresent()) {
            return ResponseEntity.ok(Map.of("message", "Already liked"));
        }

        likesRepository.save(new TutorialLikes(tutorial, user));

        return ResponseEntity.ok(Map.of("message", "Liked successfully"));
    }


    // dislike the tutorial
    @DeleteMapping("/{username}/dislike/{tutorialId}")
    public ResponseEntity<?> unlikeTutorial(
            @PathVariable Long tutorialId,
            @PathVariable String username
    ) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TutorialLikes like = likesRepository
                .findByTutorialIdAndUserId(tutorialId, user.getId())
                .orElseThrow(() -> new RuntimeException("Like not found"));

        likesRepository.delete(like);

        return ResponseEntity.ok(Map.of("message", "Unlike successful"));
    }


    // GET like count for a tutorial
    @GetMapping("/{tutorialId}/totalLikes")
    public ResponseEntity<?> getTotalLikes(
            @PathVariable Long tutorialId
    ) {
        long count = likesRepository.countByTutorialId(tutorialId);
        return ResponseEntity.ok(Map.of("tutorialId", tutorialId,"totalLikes", count));
    }


    // leaderboard of top tutorials by likes
    @GetMapping("/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard(){

        List<Tutorial> tutorials = tutorialRepository.findAll();

        // for each tutorial, attached its like count
        List<Map<String, Object>> leaderboard = tutorials.stream()
                .map(t -> {
                    long likes = likesRepository.countByTutorialId(t.getId());

                    Map<String, Object> map = new HashMap<>();
                    map.put("id", t.getId());
                    map.put("title", t.getTitle());
                    map.put("username", t.getUser().getUsername());
                    map.put("category", t.getCategory());
                    map.put("likes", likes);
                    return map;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("likes"), (Long) a.get("likes")))
                .toList();

        return ResponseEntity.ok(leaderboard);

    }
















}








