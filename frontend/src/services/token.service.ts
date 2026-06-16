import User, {emptyUser} from '../interfaces/User.ts'

interface UserWithRefreshToken extends User {refreshToken?: string};

export class TokenService {

  getLocalRefreshToken(): string | undefined {
    const stored = localStorage.getItem("user");
    if (!stored) return undefined;

    const user: UserWithRefreshToken = JSON.parse(stored);
    return user?.refreshToken;
  }

  getLocalAccessToken(): string {
  const stored = localStorage.getItem("jwt");
  if (!stored) return "";
  
  const parsed = JSON.parse(stored);
  
  // Validate JWT standard format to break sonar "taint", that is the possibility of the value beiing "evil"
  const jwtRegex = /^[A-Za-z0-9-_]+\.[A-Za-z0-9-_]+\.[A-Za-z0-9-_]+$/;
  
  if (typeof parsed === "string" && jwtRegex.test(parsed)) {
    return parsed;
  }
  
  return "";
}

  updateLocalAccessToken(token: string): void {
    localStorage.setItem("jwt", JSON.stringify(token));
  }

  getUser(): User {
    const stored = localStorage.getItem("user");
    return stored ? JSON.parse(stored) : emptyUser;
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