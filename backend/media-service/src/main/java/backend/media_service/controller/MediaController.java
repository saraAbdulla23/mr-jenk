package backend.media_service.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import backend.media_service.model.Media;
import backend.media_service.model.UserAvatar;
import backend.media_service.repository.MediaRepository;
import backend.media_service.repository.UserAvatarRepository;
import backend.media_service.service.FileStorageService;
import backend.media_service.service.OwnershipVerifierService;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaRepository mediaRepository;
    private final UserAvatarRepository userAvatarRepository;
    private final FileStorageService fileStorageService;
    private final OwnershipVerifierService ownershipVerifierService;

    public MediaController(
            MediaRepository mediaRepository,
            UserAvatarRepository userAvatarRepository,
            FileStorageService fileStorageService,
            OwnershipVerifierService ownershipVerifierService
    ) {
        this.mediaRepository = mediaRepository;
        this.userAvatarRepository = userAvatarRepository;
        this.fileStorageService = fileStorageService;
        this.ownershipVerifierService = ownershipVerifierService;
    }

    // ================= PRODUCT MEDIA =================
    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedia(@RequestHeader("Authorization") String authHeader,
                                         @RequestParam("file") MultipartFile file,
                                         @RequestParam("productId") String productId) {
        try {
            String userId = ownershipVerifierService.getUserIdFromToken(authHeader);

            // Check ownership using new signature
            if (!ownershipVerifierService.verifyOwnership(productId, userId, authHeader)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "User is not the owner of this product"));
            }

            // File validation
            if (file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "File is empty"));
            if (file.getSize() > 2 * 1024 * 1024) return ResponseEntity.badRequest().body(Map.of("message", "File too large (max 2MB)"));
            String mime = file.getContentType();
            if (mime == null || !(mime.equals("image/jpeg") || mime.equals("image/png")))
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid file type"));

            // Upload & save
            String fileUrl = fileStorageService.uploadCompressedImage(file);
            Media media = new Media();
            media.setProductId(productId);
            media.setImagePath(fileUrl);
            Media savedMedia = mediaRepository.save(media);

            // Publish Kafka event
            ownershipVerifierService.publishMediaUploaded(productId, userId, fileUrl);

            return ResponseEntity.ok(Map.of("message", "Product image uploaded successfully", "media", savedMedia));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Upload failed: " + e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or missing token"));
        }
    }

    @GetMapping("/getImagesByProductId")
    public ResponseEntity<?> getMediaByProductId(@RequestParam("productId") String productId) {
        var mediaList = mediaRepository.findByProductId(productId);
        return ResponseEntity.ok(Map.of("images", mediaList, "count", mediaList.size()));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteMedia(@RequestHeader("Authorization") String authHeader,
                                         @RequestBody Media media) {
        try {
            String userId = ownershipVerifierService.getUserIdFromToken(authHeader);

            if (!ownershipVerifierService.verifyOwnership(media.getProductId(), userId, authHeader)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "User is not the owner of this product"));
            }

            fileStorageService.deleteFileByUrl(media.getImagePath());
            mediaRepository.deleteById(media.getId());
            ownershipVerifierService.publishMediaDeleted(media.getProductId(), userId, media.getImagePath());

            return ResponseEntity.ok(Map.of("message", "Product image deleted successfully"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or missing token"));
        }
    }

    // ================= AVATAR MEDIA =================
    @PostMapping("/avatar/upload")
    public ResponseEntity<?> uploadAvatar(@RequestHeader("Authorization") String authHeader,
                                          @RequestParam("file") MultipartFile file) {
        try {
            String userId = ownershipVerifierService.getUserIdFromToken(authHeader);

            if (!ownershipVerifierService.isSeller(authHeader)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Only sellers can upload avatars"));
            }

            if (file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "File is empty"));
            if (file.getSize() > 2 * 1024 * 1024) return ResponseEntity.badRequest().body(Map.of("message", "File too large (max 2MB)"));
            String mime = file.getContentType();
            if (mime == null || !(mime.equals("image/jpeg") || mime.equals("image/png")))
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid file type"));

            String avatarUrl = fileStorageService.uploadCompressedImage(file);

            Optional<UserAvatar> existingAvatar = userAvatarRepository.findByUserId(userId);
            existingAvatar.ifPresent(old -> {
                fileStorageService.deleteFileByUrl(old.getImagePath());
                userAvatarRepository.delete(old);
            });

            userAvatarRepository.save(new UserAvatar(null, userId, avatarUrl));
            ownershipVerifierService.publishAvatarUploaded(userId, avatarUrl);

            return ResponseEntity.ok(Map.of("message", "Avatar uploaded successfully", "avatarUrl", avatarUrl));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Avatar upload failed: " + e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or missing token"));
        }
    }

    @DeleteMapping("/avatar/delete")
    public ResponseEntity<?> deleteAvatar(@RequestHeader("Authorization") String authHeader) {
        try {
            String userId = ownershipVerifierService.getUserIdFromToken(authHeader);

            if (!ownershipVerifierService.isSeller(authHeader)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Only sellers can delete avatars"));
            }

            return userAvatarRepository.findByUserId(userId)
                    .map(avatar -> {
                        fileStorageService.deleteFileByUrl(avatar.getImagePath());
                        userAvatarRepository.delete(avatar);
                        ownershipVerifierService.publishAvatarUploaded(userId, null);
                        return ResponseEntity.ok(Map.of("message", "Avatar deleted successfully"));
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("message", "Avatar not found")));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or missing token"));
        }
    }
}
