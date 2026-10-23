package com.magicbricks.api.config.httpclient;

import java.io.IOException;
import java.net.URI;
import okhttp3.OkHttpClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

public class OkHttp3ClientHttpRequestFactory implements ClientHttpRequestFactory {
    private final OkHttpClient okHttpClient;
    private final SimpleClientHttpRequestFactory delegate = new SimpleClientHttpRequestFactory();

    public OkHttp3ClientHttpRequestFactory(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        return delegate.createRequest(uri, httpMethod);
    }
}
