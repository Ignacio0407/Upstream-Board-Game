package upstream.salmon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import upstream.player.Color;

@Repository
public interface  SalmonRepository extends  JpaRepository<Salmon, Integer> {

    Salmon findFirstByColor(Color color);
}