import "./rules.css"

export default function Rules() {
    return(
        <div className="c-pdf">
            <iframe src={'/rules_ES.pdf'} title="Rules" width="100%" height="100%"/>
        </div>
    );
}