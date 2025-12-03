package com.example.craftsy.images;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    private final String directory = System.getProperty("user.home") + "/uploads";

    @Autowired
    private ImageRepository imageRepository;

    /**
     * gets an images id and path
     * @param id
     * @return
     */
    @Operation(
            summary = "Get an image by ID",
            description = "Retrieves the image file information (ID and path) by image ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image retrieved",
                    content = @Content(schema = @Schema(implementation = Image.class))),
            @ApiResponse(responseCode = "404", description = "Image not found")
    })
    @GetMapping("/images/{id}")
    public ResponseEntity<Image> getImageById(@Parameter(description = "ID of the image to retrieve")
                                                  @PathVariable Long id) {
        return imageRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * posts an image
     * @param imageFile
     * @return the image id and path
     */
    @Operation(
            summary = "Upload an image",
            description = "Uploads an image file and stores its path in the database. Returns the saved image with ID and path."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully",
                    content = @Content(schema = @Schema(implementation = Image.class))),
            @ApiResponse(responseCode = "500", description = "Failed to upload or save the image")
    })
    @PostMapping("/images")
    public ResponseEntity<Image> handleFileUpload(@Parameter(description = "Image file to upload")
                                                      @RequestParam("image") MultipartFile imageFile) {
        try {
            File uploadDir = new File(directory);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Save the uploaded file to the directory
            String uniqueName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            File destinationFile = new File(uploadDir, uniqueName);
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

    /**
     * deletes an image
     * @param id
     * @return
     */
    @Operation(
            summary = "Delete an image",
            description = "Deletes an image by ID and removes its file from disk."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Image not found"),
            @ApiResponse(responseCode = "500", description = "Failed to delete image file from disk")
    })
    @DeleteMapping("/images/{id}")
    public ResponseEntity<String> deleteImage(@Parameter(description = "ID of the image to delete")
                                                  @PathVariable Long id) {
        Optional<Image> optionalImage = imageRepository.findById(id);
        if (!optionalImage.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Image with ID " + id + " not found.");
        }

        Image image = optionalImage.get();

        File file = new File(image.getFilePath());
        if (file.exists() && !file.delete()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete file from disk.");
        }

        imageRepository.deleteById(id);

        return ResponseEntity.ok("Image deleted successfully.");
    }
}
