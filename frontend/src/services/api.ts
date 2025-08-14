import axios, { AxiosError, InternalAxiosRequestConfig , AxiosResponse } from "axios";
import TokenService from "./token.service";

const instance = axios.create({
  baseURL: "http://localhost:8080/api/v1",
  headers: {
    "Content-Type": "application/json",
  },
});

// ⛑️ Request interceptor: attaches the JWT token
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig): InternalAxiosRequestConfig => {
    const token = TokenService.getLocalAccessToken();
    if (token && config.headers) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// 🧰 Response interceptor: handles 401 & retries with refresh token
instance.interceptors.response.use(
  (res: AxiosResponse): AxiosResponse => res,
  async (err: AxiosError): Promise<AxiosResponse | never> => {
    const originalConfig = err.config as InternalAxiosRequestConfig & { _retry?: boolean };

    if (originalConfig?.url !== "/auth/signin" && err.response?.status === 401 && !originalConfig._retry) {
      originalConfig._retry = true;

      try {
        const rs = await instance.post("/auth/refreshtoken", {
          refreshToken: TokenService.getLocalRefreshToken(),
        });

        const { accessToken }: { accessToken: string } = rs.data;
        TokenService.updateLocalAccessToken(accessToken);

        return instance(originalConfig);
      } catch (_error) {
        return Promise.reject(_error);
      }
    }

    return Promise.reject(err);
  }
);

export default instance;