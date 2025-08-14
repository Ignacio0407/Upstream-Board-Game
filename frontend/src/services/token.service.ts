import User, {emptyUser, emptyUserList, UserList} from '../interfaces/User.ts'

interface UserWithRefreshToken extends User {refreshToken?: string};

class TokenService {

  getLocalRefreshToken(): string | undefined {
    const stored = localStorage.getItem("user");
    if (!stored) return undefined;

    const user: UserWithRefreshToken = JSON.parse(stored);
    return user?.refreshToken;
  }

  getLocalAccessToken(): string {
    const stored = localStorage.getItem("jwt");
    return stored ? JSON.parse(stored) : "";
  }

  updateLocalAccessToken(token: string): void {
    localStorage.setItem("jwt", JSON.stringify(token));
  }

  getUser(): User {
    const stored = localStorage.getItem("user");
    return stored ? JSON.parse(stored) : emptyUser;
  }

  getUserList(): UserList {
    const stored = localStorage.getItem("user");
    return stored ? JSON.parse(stored) : emptyUserList;
  }

  setUser(user: User): void {
    localStorage.setItem("user", JSON.stringify(user));
  }

  removeUser(): void {
    localStorage.removeItem("user");
    localStorage.removeItem("jwt");
  }
}

const tokenService = new TokenService();
export default tokenService;