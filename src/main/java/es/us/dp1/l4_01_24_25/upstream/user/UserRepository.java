package es.us.dp1.l4_01_24_25.upstream.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{			
	
	public Optional<User> findByName(@Param("name") String name);

	public Boolean existsByName(@Param("name") String name);

	@Query("SELECT u FROM User u WHERE u.authority.authority = :auth")
	List<User> findAllByAuthority(String auth);

}