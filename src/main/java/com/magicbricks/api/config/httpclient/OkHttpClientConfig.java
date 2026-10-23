package com.magicbricks.api.config.httpclient;

import java.time.Duration;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.magicbricks.api.config.httpclient.interceptors.RequestResponseLoggingInterceptor;

import lombok.extern.log4j.Log4j2;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient.Builder;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@Log4j2
public class OkHttpClientConfig {

	@Bean
	public okhttp3.OkHttpClient okHttpClient(@Value("${api.okhttp.connectTimeout:3000}") String connectTimeout, @Value("${api.okhttp.readTimeout:5000}") String readTimeout, @Value("${api.okhttp.writeTimeout:5000}") String writeTimeout, @Value("${api.okhttp.connectionPool:10}") String connectionPool,
			@Autowired(required = false) @Qualifier("mbMobileUtilityAsyncTaskExecutor") ExecutorService executorService) {
		Builder builder = new Builder().connectTimeout(Duration.ofMillis(Long.valueOf(connectTimeout))).readTimeout(Duration.ofMillis(Long.valueOf(readTimeout))).writeTimeout(Duration.ofMillis(Long.valueOf(writeTimeout))).connectionPool(new ConnectionPool(Integer.valueOf(connectionPool), 5, TimeUnit.MINUTES));
		if (executorService != null) {
			builder.addInterceptor(new RequestResponseLoggingInterceptor(executorService)).dispatcher(new Dispatcher(executorService));
		}
		return builder.hostnameVerifier((h, s) -> true).build();
	}

	@Bean(name = { "objectMapper", "jsonMapper" })
	public JsonMapper jsonMapper() {
		JsonMapper.Builder builder = JsonMapper.builder();
		builder.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
		builder.changeDefaultNullHandling(v -> v == null ? JsonSetter.Value.forValueNulls(Nulls.SKIP) : v.withValueNulls(Nulls.SKIP, Nulls.SKIP));
		builder.changeDefaultPropertyInclusion(p -> JsonInclude.Value.construct(Include.NON_NULL, Include.NON_NULL));
		builder.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
		builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		builder.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES);
		builder.enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
		builder.defaultTimeZone(TimeZone.getDefault());
		return builder.build();
	}
}
