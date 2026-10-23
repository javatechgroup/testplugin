package com.magicbricks.api.config.httpclient.interceptors;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import okhttp3.Interceptor;
import okhttp3.Response;

public class RequestResponseLoggingInterceptor implements Interceptor {
    private final ExecutorService executorService;

    public RequestResponseLoggingInterceptor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(chain.request());
    }
}
