package es.us.dp1.l4_01_24_25.upstream.salmon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.general.BaseRestController;

@RestController
@RequestMapping("api/v1/salmons")
public class SalmonController extends BaseRestController<Salmon,Integer>{
    SalmonService salmonService;

    @Autowired
    public SalmonController(SalmonService salmonService) {
        super(salmonService);
    }
}