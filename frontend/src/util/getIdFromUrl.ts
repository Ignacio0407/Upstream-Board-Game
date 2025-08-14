export default function getIdFromUrl(index:number) :string {
    return window.location.pathname.split('/')[index];
}