package es.us.dp1.l4_01_24_25.upstream.partidas;

import java.util.List;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


@Service
public class PartidaService {

    PartidaRepository pr;

    public PartidaService(PartidaRepository pr) {
        this.pr = pr;
    }

    @Transactional(readOnly = true)
    public List<Partida> findAll() {
        return pr.findAll();
    }
}
