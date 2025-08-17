package es.us.dp1.l4_01_24_25.upstream.salmon;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.model.BaseService;
import es.us.dp1.l4_01_24_25.upstream.player.Color;

@Service
public class SalmonService extends BaseService<Salmon,Integer>{

    SalmonRepository salmonRepository;

    public SalmonService(SalmonRepository salmonRepository){
        super(salmonRepository);
        this.salmonRepository = salmonRepository;
    }

    @Override
    @Transactional
    protected void updateEntityFields(Salmon newSalmon, Salmon salmonToUpdate) {
        salmonToUpdate.setImage(newSalmon.getImage());
    }

    @Transactional(readOnly = true)
    public Salmon findFirstByColor(Color color) {
        return this.salmonRepository.findFirstByColor(color);
    }
}
