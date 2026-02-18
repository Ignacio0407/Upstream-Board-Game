package es.us.dp1.l4_01_24_25.upstream.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.us.dp1.l4_01_24_25.upstream.user.DTO.UserListAdmin;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{			
	
	public Optional<User> findByName(@Param("name") String name);

	public Boolean existsByName(@Param("name") String name);

	@Query("SELECT u FROM User u WHERE u.authority = :auth")
	List<User> findAllByAuthority(String auth);

	@Query("SELECT new es.us.dp1.l4_01_24_25.upstream.user.DTO.UserListAdmin(u.id, u.name, u.authority) FROM User u")
	List<UserListAdmin> findAllListAdmin();

}