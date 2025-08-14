import Authority, {emptyAuthority} from "./Authority.ts";

export default interface User {
  id: number;
  username: string;
  password: string,
  authority: Authority,
  victories: number;
  playedgames: number;
  totalpoints: number;
}

export const emptyUser: User = 
{id: 0, username: "", password: "", authority: emptyAuthority, victories: 0, playedgames: 0, totalpoints: 0};

export interface UserList {
  id: number;
  username: string;
  password: string,
  authority: Authority,
  roles: string;
}

export const emptyUserList:UserList = {id:0, username:"", password:"", authority:emptyAuthority, roles:""}; 