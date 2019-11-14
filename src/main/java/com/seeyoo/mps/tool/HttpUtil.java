package com.seeyoo.mps.tool;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;

public class HttpUtil {
	static public <T> T httpRequestGet(String url, Object params, Class<T> responseType) throws Exception {
		return httpRequest(HttpMethod.GET,"json", url, params, responseType);
	}

	static public <T> T httpRequestPOST(String url, Object params, Class<T> responseType) throws Exception {
		return httpRequest(HttpMethod.POST,"json", url, params, responseType);
	}

	static public <T> T httpRequest(String msgType, String url, Object params, Class<T> responseType) throws Exception {
		return httpRequest(HttpMethod.POST, msgType, url, params, responseType);
	}

	static public <T> T httpRequest(HttpMethod method, String msgType, String url, Object params, Class<T> responseType) throws Exception {
		SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
		simpleClientHttpRequestFactory.setConnectTimeout(10 * 1000);
		simpleClientHttpRequestFactory.setReadTimeout(10 * 1000);
		RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory);

		HttpHeaders headers = new HttpHeaders();
		MediaType mediaType = new MediaType("application", "json", StandardCharsets.UTF_8);
		if (msgType.compareTo("xml") == 0) {
			mediaType = new MediaType("application", "xml", StandardCharsets.UTF_8);
		}
		headers.setContentType(mediaType);

		HttpEntity<Object> requestEntity = new HttpEntity<>(params, headers);
		ResponseEntity<T> response = restTemplate.exchange(url, method, requestEntity, responseType);
		// System.out.println(response.getHeaders().toString());

		return (response.getStatusCode() == HttpStatus.OK) ? response.getBody() : null;
	}


	static public <T> T httpSSLRequest(HttpMethod method, String msgType, String url, Object params, String keyStorePath, String keyPassword,
									   Class<T> responseType) throws Exception {
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		File keyFile = new File(keyStorePath);
		if (!keyFile.exists())
			return null;
		FileInputStream instream = new FileInputStream(keyFile);
		try {
			keyStore.load(instream, keyPassword.toCharArray());
		} finally {
			instream.close();
		}

		SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, keyPassword.toCharArray()).build();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
				SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		clientHttpRequestFactory.setConnectTimeout(10 * 1000);
		clientHttpRequestFactory.setReadTimeout(10 * 1000);
		clientHttpRequestFactory.setConnectionRequestTimeout(1000);
		// clientHttpRequestFactory.setBufferRequestBody(false);

		RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

		HttpHeaders headers = new HttpHeaders();
		MediaType mediaType = new MediaType("application", "json", StandardCharsets.UTF_8);
		if (msgType.compareTo("xml") == 0) {
			mediaType = new MediaType("application", "xml", StandardCharsets.UTF_8);
		}
		headers.setContentType(mediaType);

		HttpEntity<Object> requestEntity = new HttpEntity<>(params, headers);
		ResponseEntity<T> response = restTemplate.exchange(url, method, requestEntity, responseType);
		// System.out.println(response.getHeaders().toString());

		return (response.getStatusCode() == HttpStatus.OK) ? response.getBody() : null;
	}

}
