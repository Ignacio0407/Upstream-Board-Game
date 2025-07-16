import {useState} from 'react'
import { Table } from "reactstrap";
import useFetchState from "../util/useFetchState";
import SearchBar from '../util/SearchBar'  
   
export default function DeveloperList() { 
    const [developers, setDevelopers] = useFetchState( [], `/api/v1/developers` );
    const [filtered, setFiltered] = useState([]) 

    const imgnotfound = "/images/achievements/NotFoundImage.png";  
    
    function developersList (developersToList) {
        developersToList.map((d) => { 
        return ( 
            <tr key={d.id}> 
                <td className="text-center">{d.name}</td> 
                <td className="text-center"> {d.email} </td> 
                <td className="text-center"> <a href={d.url}>{d.url}</a> </td> 
                <td className="text-center"> <img src={d.properties.picUrl ? d.properties.picUrl : imgnotfound } 
                                                alt={d.name} width="50px"/>   
                </td> 
            </tr> 
        ); 
    });
    }
    
    return ( 
        <> 
         <div className="admin-page-container"> 
            <h1></h1>
           <h1 className="text-center">Developers</h1>
           <SearchBar data={developers} setFiltered={setFiltered} placeholder = {'Search developers'} />
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
                    <tbody>{filtered ? developersList(filtered) : developersList(developers)}</tbody> 
                </Table> 
            </div> 
            </div> 
        </> 
); 
} 