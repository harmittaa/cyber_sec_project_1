package sec.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sec.project.domain.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
       
}
