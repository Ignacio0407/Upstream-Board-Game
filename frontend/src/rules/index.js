import { React } from "react";
import "../static/css/rules/rules.css"
import pdf from "../static/UpstreamRULES.pdf"

export const BASE_URL = "http://localhost:3000"

export default function Rules() {
    return(
        <div className="c-pdf">
            <iframe 
            src={pdf}
            title="Rules" 
            width="100%"
            height="100%"/>
        </div>
    );
}

