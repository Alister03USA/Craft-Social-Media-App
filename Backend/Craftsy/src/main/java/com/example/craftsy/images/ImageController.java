package com.example.craftsy.images;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

@RestController
public class ImageController {

    // replace this! careful with the operating system in use
    private final String directory = "/uploads"; // ✅ Change to your desired folder

    @Autowired
    private ImageRepository imageRepository;

    @GetMapping("/images/{id}")
    public ResponseEntity<Image> getImageById(@PathVariable Long id) {
        return imageRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/images")
    public ResponseEntity<Image> handleFileUpload(@RequestParam("image") MultipartFile imageFile) {
        try {
            // ✅ Step 2: Ensure the upload directory exists
            File uploadDir = new File(directory);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs(); // creates the folder if it doesn't exist
            }

            // Save the uploaded file to the directory
            File destinationFile = new File(uploadDir, imageFile.getOriginalFilename());
            imageFile.transferTo(destinationFile);

            // Save file info in the database
            Image image = new Image();
            image.setFilePath(destinationFile.getAbsolutePath());
            Image savedImage = imageRepository.save(image);

            return ResponseEntity.ok(savedImage);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/images/{id}")
    public ResponseEntity<String> deleteImage(@PathVariable Long id) {
        // 1️⃣ Find the image record in the database
        Optional<Image> optionalImage = imageRepository.findById(id);
        if (!optionalImage.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Image with ID " + id + " not found.");
        }

        Image image = optionalImage.get();

        // 2️⃣ Delete the file from the filesystem
        File file = new File(image.getFilePath());
        if (file.exists() && !file.delete()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete file from disk.");
        }

        // 3️⃣ Delete the record from the database
        imageRepository.deleteById(id);

        return ResponseEntity.ok("Image deleted successfully.");
    }
}
