package es.us.dp1.l4_01_24_25.upstream.salmon;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

public class SalmonService {

    SalmonRepository rs;

    public SalmonService(SalmonRepository rs){
        this.rs = rs;
    }

    @Transactional(readOnly=true)
    public List<Salmon> findAll(){
        return rs.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Salmon> findById(int id){
        return rs.findById(id);
    }

}
