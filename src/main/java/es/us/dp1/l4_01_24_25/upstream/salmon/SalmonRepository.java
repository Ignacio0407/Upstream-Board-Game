package es.us.dp1.l4_01_24_25.upstream.salmon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.us.dp1.l4_01_24_25.upstream.player.Color;

@Repository
public interface  SalmonRepository extends  JpaRepository<Salmon, Integer> {

    Salmon findFirstByColor(Color color);
}