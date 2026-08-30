import { Table } from "reactstrap";
import useFetchState from "../util/useFetchState.ts";
import tokenService from '../services/token.service.ts';
import Developer from '../interfaces/Developer.ts'  
   
export default function DeveloperList() {
    const jwt = tokenService.getLocalAccessToken();
    const [developers] = useFetchState<Developer[]>([], `/api/v1/developers`, jwt);

    const imgnotfound = "/images/achievements/NotFoundImage.png";  
    
    function developersList (developersToList:Developer[]) {
        return developersToList.map((d) => (
            <tr key={d.id}> 
                <td className="text-center">{d.name}</td> 
                <td className="text-center"> {d.email} </td> 
                <td className="text-center"> <a href={d.url}>{d.url}</a> </td> 
                <td className="text-center"> <img src={d.properties.picUrl ? d.properties.picUrl : imgnotfound } 
                                                alt={d.name} width="50px"/>   
                </td> 
            </tr> 
        ));
    }
    
    return ( 
        <div className="admin-page-container"> 
            <br />
            <h1 className="text-center">Developers</h1>
            <div> 
                <Table aria-label="developers" className="mt-4"> 
                    <thead> 
                        <tr> 
                            <th className="text-center">Name</th> 
                            <th className="text-center">e-mail</th> 
                            <th className="text-center">URL</th> 
                            <th className="text-center">Picture</th> 
                        </tr> 
                    </thead> 
                    <tbody>{developersList(developers)}</tbody> 
                </Table> 
            </div> 
        </div> 
    ); 
} 