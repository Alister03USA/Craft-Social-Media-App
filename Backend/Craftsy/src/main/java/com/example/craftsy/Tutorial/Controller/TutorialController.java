package com.example.craftsy.Tutorial.Controller;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.Tutorial.Entity.Tutorial;
import com.example.craftsy.Tutorial.Repository.TutorialRepository;
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

@RestController
@RequestMapping("/tutorial")
public class TutorialController {

    @Autowired
    private TutorialRepository tutorialRepository;

    @Autowired
    private UserRepository userRepository;




    /**
     * Upload the image/video by File
     * @param title
     * @param description
     * @param file
     * @return
     */
    @PostMapping(value = "/uploadFile", consumes =  MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadTutorialFile(
            @RequestParam("username") String username,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file) { // MultipartFile is a spring class representing an uploaded file
            // Extracts the title, desc, and file from the request
        try {

            // Find user
            Optional<Users> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            Users user = userOpt.get();

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
            tutorial.setType(type);
            tutorial.setUser(user);

            tutorialRepository.save(tutorial);

            return ResponseEntity.ok("Tutorial uploaded successfully!");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("File upload failed: " + e.getMessage());
        }
    }

    /**
     * POST image/video by URL
     */
    @PostMapping("/uploadUrl")
    public ResponseEntity<String> uploadTutorialUrl(
            @RequestParam String username,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam String type,        // "image" or "video"
            @RequestParam String fileUrl      // URL to external image/video
    ) {
        // Find user
        Optional<Users> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        Users user = userOpt.get();

        Tutorial tutorial = new Tutorial();
        tutorial.setTitle(title);
        tutorial.setDescription(description);
        tutorial.setCategory(category);
        tutorial.setType(type);
        tutorial.setFileUrl(fileUrl); // store URL instead of file path
        tutorial.setUser(user);
        tutorialRepository.save(tutorial);

        return ResponseEntity.ok("Tutorial uploaded successfully with URL!");
    }


    /**
     * GET "/tutorials/{id}"
     * Fetch the file of the image/videos
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTutorialFile(@PathVariable Long id) {
        Optional<Tutorial> tutorialOpt = tutorialRepository.findById(id);
        if (tutorialOpt.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        Tutorial tutorial = tutorialOpt.get();

        // External URL
        if (tutorial.getFileUrl() != null) {
            return ResponseEntity.ok(Map.of("fileUrl", tutorial.getFileUrl()));
        }

        // Local file
        if (tutorial.getFilePath() != null) {
            Path path = Paths.get(tutorial.getFilePath());
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(path); // Data is read by chunk
            String contentType;
            try {
                contentType = Files.probeContentType(path); // build-in system to detect file type
            } catch (IOException e) {
                contentType = "application/octet-stream";
            }
            if (contentType == null) contentType = "application/octet-stream"; // For unknown binary file

            return ResponseEntity.ok() // Resource will be streamed in the response body
                    .contentType(MediaType.parseMediaType(contentType)) // sent the type of file
                    .body(resource);
        }

        return ResponseEntity.noContent().build();
    }



    /**
     * GET {/search?query={}}
     * Search tutorials by title, description, or category
     * Returns correct file URL (local or external)
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchTutorials(@RequestParam String query) {
        List<Tutorial> tutorials = tutorialRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                        query, query, query);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Tutorial t : tutorials) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("title", t.getTitle());
            map.put("description", t.getDescription());
            map.put("category", t.getCategory());
            map.put("type", t.getType());

            // Choose the correct URL
            String fileUrl = t.getFileUrl() != null ? t.getFileUrl() : "/tutorial/" + t.getId() + "/file"; // file path
            map.put("fileURL", fileUrl);
            result.add(map);
        }

        return ResponseEntity.ok(result);
    }


    /**
     * PUT "/tutorial/{id}"
     * Update the tutorials
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // Endpoint expects data in form-data format
    public ResponseEntity<String> updateTutorials(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) MultipartFile file // 9nterface that handle file uploads
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

        if (type != null) {
            tutorial.setType(type);
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












}







