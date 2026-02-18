export default interface User {
  id: number;
  username: string;
  password: string,
  authority: Authority,
  roles?: string[];
  victories: number;
  playedgames: number;
  totalpoints: number;
}

export enum Authority {
    ADMIN = "ADMIN", PLAYER = "PLAYER"
}

export const emptyUser: User = 
{id: 0, username: "", password: "", authority: Authority.PLAYER, roles: [], victories: 0, playedgames: 0, totalpoints: 0};

export interface UserList {
  id: number;
  name: string;
  authority: Authority
}

export interface BaseUser {
  id: number;
  username: string;
  password?: string,
  authority?: Authority,
  roles?: string[];
}

export const emptyUserList:UserList = {id:0, name:"", authority:Authority.PLAYER}; 