package upstream.salmon;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import upstream.model.BaseService;
import upstream.player.Color;

@Service
public class SalmonService extends BaseService<Salmon,Integer>{

    SalmonRepository salmonRepository;

    public SalmonService(SalmonRepository salmonRepository){
        super(salmonRepository);
        this.salmonRepository = salmonRepository;
    }

    @Override
    @Transactional
    protected Salmon updateEntityFields(Salmon newSalmon, Salmon salmonToUpdate) {
        salmonToUpdate.setImage(newSalmon.getImage());
        return salmonToUpdate;
    }

    @Transactional(readOnly = true)
    public Salmon findFirstByColor(Color color) {
        return this.salmonRepository.findFirstByColor(color);
    }
}
