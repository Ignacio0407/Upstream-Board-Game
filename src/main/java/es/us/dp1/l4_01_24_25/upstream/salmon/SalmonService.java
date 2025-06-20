package es.us.dp1.l4_01_24_25.upstream.salmon;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.model.BaseService;

@Service
public class SalmonService extends BaseService<Salmon,Integer>{

    SalmonRepository salmonRepository;

    public SalmonService(SalmonRepository salmonRepository){
        super(salmonRepository);
    }

    @Override
    @Transactional
    protected void updateEntityFields(Salmon newSalmon, Salmon salmonToUpdate) {
        salmonToUpdate.setColor(newSalmon.getColor());
        salmonToUpdate.setImage(newSalmon.getImage());
    }
}
