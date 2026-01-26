package backend.media_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_avatars")
public class UserAvatar {

    @Id
    private String id;

    private String userId;
    private String imagePath;
}
