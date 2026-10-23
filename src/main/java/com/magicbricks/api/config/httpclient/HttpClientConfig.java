package com.magicbricks.api.config.httpclient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.Builder;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.service.registry.HttpServiceGroup.ClientType;
import org.springframework.web.service.registry.ImportHttpServices;

import com.magicbricks.api.clients.AwsS3ApiClient;
import com.magicbricks.api.clients.HomeLoanApiClient;
import com.magicbricks.api.clients.MBApiClient;
import com.magicbricks.api.clients.MBPackageApiClient;
import com.magicbricks.api.clients.MBRatingApiClient;
import com.magicbricks.api.clients.MBSellerApiClient;
import com.magicbricks.api.clients.MMIApiClient;
import com.magicbricks.api.clients.MbUtilityApiClient;
import com.magicbricks.api.clients.MmiCloudApiClient;
import com.magicbricks.api.clients.OTPApiClient;
import com.magicbricks.api.clients.PhotoApiClient;
import com.magicbricks.api.clients.PostPropertyApiClient;
import com.magicbricks.api.clients.PropertyManagementApiClient;
import com.magicbricks.api.clients.RAMApiClient;
import com.magicbricks.api.clients.RestServicesApiClient;
import com.magicbricks.api.clients.SVApiClient;
import com.magicbricks.api.clients.ServicePortalApiClient;
import com.magicbricks.api.clients.TinyUrlApiClient;
import com.magicbricks.api.clients.UserAuthApiClient;
import com.magicbricks.api.config.httpclient.interceptors.MvtEvtHeaderInterceptor;

import okhttp3.OkHttpClient;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@ImportHttpServices(clientType = ClientType.REST_CLIENT, group = "httpclientgroup")
public class HttpClientConfig {

	@Autowired
	private OkHttpClient okHttpClient;

	@Autowired
	private JsonMapper jsonMapper;

	@Autowired
	private MvtEvtHeaderInterceptor mvtEvtHeaderInterceptor;

	@Bean
	public AwsS3ApiClient awsS3ApiClient(@Autowired OkHttpClient okHttpClient, @Value("${s3_upload_service_url:http://localhost}") String url) {
		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(buildRestClient(url))).build().createClient(AwsS3ApiClient.class);
	}

	@Bean
	public HomeLoanApiClient homeLoanApiClient(@Autowired OkHttpClient okHttpClient, @Value("${homeloan.api.client:http://localhost}") String url) {
		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(buildRestClient(url))).build().createClient(HomeLoanApiClient.class);
	}

	@Bean
	public MBApiClient mbApiClient(@Autowired OkHttpClient okHttpClient, @Value("${mb.api.host:http://localhost}") String url) {
		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(buildRestClient(url))).build().createClient(MBApiClient.class);
	}

	@Bean
	public MBPackageApiClient mbPackageApiClient(@Autowired OkHttpClient okHttpClient, @Value("${mb.pkg.api.base.url:http://localhost}") String url) {
		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(buildRestClient(url))).build().createClient(MBPackageApiClient.class);
	}

	@Bean
	public MBRatingApiClient mbRatingApiClient(@Autowired OkHttpClient okHttpClient, @Value("${mbrating.api.url:http://localhost}") String url) {
		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(buildRestClient(url))).build().createClient(MBRatingApiClient.class);
	}

	@Bean
	public MBSellerApiClient mbSellerApiClient(@Autowired OkHttpClient okHttpClient, @Value("${mbseller.api.host:http://localhost}") String url) {
		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(buildRestClient(url))).build().createClient(MBSellerApiClient.class);
	}

	@Bean
	public MbUtilityApiClient mbUtilityApiClient(@Autowired OkHttpClient okHttpClient, @Value("${mbutility.api.url:http://localhost}") String url) {
		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(buildRestClient(url))).build().createClient(MbUtilityApiClient.class);
	}

	@Bean
	public MMIApiClient mmiApiClient(@Autowired OkHttpClient okHttpClient, @Value("${mapple.api.url:http://localhost}") String url) {
		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(buildRestClient(url))).build().createClient(MMIApiClient.class);
	}

	@Bean
	public OTPApiClient otpApiClient(@Autowired OkHttpClient okHttpClient, @Value("${otp.api.url:http://localhost}") String url) {
		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(buildRestClient(url))).build().createClient(OTPApiClient.class);
	}

	@Bean
	public PostPropertyApiClient postPropertyApiClient(@Autowired OkHttpClient okHttpClient, @Value("${pp.api.host:http://localhost}") String url) {
		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(buildRestClient(url))).build().createClient(PostPropertyApiClient.class);
	}

	@Bean
	public PropertyManagementApiClient propertyManagementApiClient(@Autowired OkHttpClient okHttpClient, @Value("${propmgmt.api.url:http://localhost}") String url) {
		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(buildRestClient(url))).build().createClient(PropertyManagementApiClient.class);
	}

	@Bean
	public RAMApiClient ramApiClient(@Autowired OkHttpClient okHttpClient, @Value("${ram.api.url:http://localhost}") String url) {
		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(buildRestClient(url))).build().createClient(RAMApiClient.class);
	}

	@Bean
	public RestServicesApiClient restServicesApiClient(@Autowired OkHttpClient okHttpClient, @Value("${rest.services.url:http://localhost}") String url) {
		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(buildRestClient(url))).build().createClient(RestServicesApiClient.class);
	}

	@Bean
	public TinyUrlApiClient tinyUrlApiClient(@Autowired OkHttpClient okHttpClient, @Value("${tinyurl.api.url:http://localhost}") String url) {
		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(buildRestClient(url))).build().createClient(TinyUrlApiClient.class);
	}

	@Bean
	public UserAuthApiClient userAuthApiClient(@Autowired OkHttpClient okHttpClient, @Value("${userauth.api.url:http://localhost}") String url) {
		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(buildRestClient(url))).build().createClient(UserAuthApiClient.class);
	}

	@Bean
	public ServicePortalApiClient servicePortalApiClient(@Autowired OkHttpClient okHttpClient, @Value("${service.portal.api.url:http://localhost}") String url) {
		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(buildRestClient(url))).build().createClient(ServicePortalApiClient.class);
	}
	
	@Bean
	public PhotoApiClient photoApiClient(@Autowired OkHttpClient okHttpClient, @Value("${photo.api.url:http://localhost}") String url) {
		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(buildRestClient(url))).build().createClient(PhotoApiClient.class);
	}
	
	@Bean
	public SVApiClient svApiClient(@Autowired OkHttpClient okHttpClient, @Value("${sv.api.url:http://localhost}") String url) {
		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(buildRestClient(url))).build().createClient(SVApiClient.class);
	}
	
	@Bean
	public MmiCloudApiClient mmiCloudApiClient(@Autowired OkHttpClient okHttpClient, @Value("${mapple.api.cloud.url:http://localhost}") String url) {
		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(buildRestClient(url))).build().createClient(MmiCloudApiClient.class);
	}

	@Bean(name = "multipartResolver")
	public MultipartResolver multipartResolver() {
		return new StandardServletMultipartResolver();
	}

	private RestClient buildRestClient(String url) {
		var requestFactory = new OkHttp3ClientHttpRequestFactory(okHttpClient);
		Builder builder = RestClient.builder().configureMessageConverters(t -> {
			t.addCustomConverter(new FormHttpMessageConverter());
			t.addCustomConverter(new StringHttpMessageConverter());
			t.addCustomConverter(new ByteArrayHttpMessageConverter());
			JacksonJsonHttpMessageConverter jacksonJsonHttpMessageConverter = new JacksonJsonHttpMessageConverter(jsonMapper);
			ArrayList<MediaType> arrayList = new ArrayList<>(jacksonJsonHttpMessageConverter.getSupportedMediaTypes());
			arrayList.add(MediaType.ALL);
			jacksonJsonHttpMessageConverter.setSupportedMediaTypes(arrayList);
			jacksonJsonHttpMessageConverter.setDefaultCharset(StandardCharsets.UTF_8);
			t.addCustomConverter(jacksonJsonHttpMessageConverter);
		});
		return builder.requestFactory(requestFactory).baseUrl(url).requestInterceptor(mvtEvtHeaderInterceptor).build();
	}

}
