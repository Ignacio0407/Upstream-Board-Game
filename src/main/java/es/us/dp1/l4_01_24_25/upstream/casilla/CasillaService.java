package es.us.dp1.l4_01_24_25.upstream.casilla;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CasillaService {
    
    CasillaRepository cr;

    public CasillaService(CasillaRepository cr) {
        this.cr = cr;
    }

    @Transactional(readOnly = true)
    public List<Casilla> findAll() {
        return cr.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Casilla> findById(int id) {
        return cr.findById(id);
    }

    @Transactional(readOnly = true)
    public List<TipoCasilla> findAllType() {
        return cr.findAllType();
    }
}
