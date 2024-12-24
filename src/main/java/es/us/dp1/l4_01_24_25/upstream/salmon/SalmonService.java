package es.us.dp1.l4_01_24_25.upstream.salmon;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalmonService {

    SalmonRepository salmonRepository;

    public SalmonService(SalmonRepository rs){
        this.salmonRepository = rs;
    }

    @Transactional(readOnly=true)
    public List<Salmon> findAll(){
        return salmonRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Salmon> findById(int id){
        return salmonRepository.findById(id);
    }

    @Transactional()
    public Salmon create(Salmon s){
        return salmonRepository.save(s);
    }

}
