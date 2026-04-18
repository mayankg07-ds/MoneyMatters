import { useEffect } from 'react';
import { useAuth } from '@clerk/react';
import API from './api';

export function useAxiosInterceptor() {
    const { getToken } = useAuth();

    useEffect(() => {
        const interceptor = API.interceptors.request.use(
            async (config) => {
                const token = await getToken();
                if (token) {
                    config.headers.Authorization = `Bearer ${token}`;
                }
                return config;
            },
            (error) => Promise.reject(error)
        );
        return () => API.interceptors.request.eject(interceptor);
    }, [getToken]);
}
