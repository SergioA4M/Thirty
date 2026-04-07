package thirty_api.repositories;

import thirty_api.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Boot crea la consulta SQL por ti solo con leer el nombre del método
    User findByEmail(String email);

}
