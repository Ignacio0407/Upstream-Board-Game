package es.us.dp1.l4_01_24_25.upstream.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


public interface UserRepository extends  CrudRepository<User, Integer>{			


	Optional<User> findByUsername(String username);

	Boolean existsByUsername(String username);

	@SuppressWarnings("null")
    @Override
	Optional<User> findById(Integer id);
	
	@Query("SELECT u FROM User u WHERE u.authority.authority = :auth")
	Iterable<User> findAllByAuthority(String auth);
	
	//@Query("DELETE FROM Player o WHERE o.user.id = :userId")
	//@Modifying
	//void deletePlayerRelation(int userId);
	
	
	
}
