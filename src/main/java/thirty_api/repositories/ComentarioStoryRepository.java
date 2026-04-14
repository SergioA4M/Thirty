package thirty_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import thirty_api.models.ComentarioStory;
import java.util.List;

public interface ComentarioStoryRepository extends JpaRepository<ComentarioStory, Long> {
    
    List<ComentarioStory> findByStoryIdOrderByFechaAsc(Long storyId);
    
    long countByStoryId(Long storyId);
}
