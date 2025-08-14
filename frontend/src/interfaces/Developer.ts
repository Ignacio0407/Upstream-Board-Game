interface DeveloperProperties {
  picUrl?: string;
}

export default interface Developer {
  id:number,
  name: string;
  email: string;
  url: string;
  properties: DeveloperProperties;
}