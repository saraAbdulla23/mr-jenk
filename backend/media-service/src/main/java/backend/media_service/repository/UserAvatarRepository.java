package backend.media_service.repository;

import backend.media_service.model.UserAvatar;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserAvatarRepository extends MongoRepository<UserAvatar, String> {
    Optional<UserAvatar> findByUserId(String userId);
}
