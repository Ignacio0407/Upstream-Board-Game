package es.us.dp1.l4_01_24_25.upstream.salmon;

import org.springframework.stereotype.Service;
import es.us.dp1.l4_01_24_25.upstream.general.BaseService;

@Service
public class SalmonService extends BaseService<Salmon,Integer>{

    SalmonRepository salmonRepository;

    public SalmonService(SalmonRepository salmonRepository){
        super(salmonRepository);
    }
}
