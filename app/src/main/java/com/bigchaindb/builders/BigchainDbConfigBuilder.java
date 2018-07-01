package com.bigchaindb.builders;

import android.util.Log;

import com.bigchaindb.constants.BigchainDbApi;
import com.bigchaindb.model.ApiEndpoints;
import com.bigchaindb.model.BigChainDBGlobals;
import com.bigchaindb.util.JsonUtils;
import com.bigchaindb.util.NetworkUtils;
import com.bigchaindb.ws.BigchainDbWSSessionManager;
import com.bigchaindb.ws.MessageHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * The Class BigchainDbConfigBuilder.
 */
public class BigchainDbConfigBuilder {

	private static final Logger log = LoggerFactory.getLogger( BigchainDbConfigBuilder.class );
	
	/**
	 * Instantiates a new bigchain db config builder.
	 */
	private BigchainDbConfigBuilder() {
	}

	/**
	 * Base url.
	 *
	 * @param baseUrl
	 *            the base url
	 * @return the i tokens
	 */
	public static ITokens baseUrl(String baseUrl) {
		return new BigchainDbConfigBuilder.Builder(baseUrl);
	}

	/**
	 * The Interface ITokens.
	 */
	public interface ITokens {

		/**
		 * Adds the token.
		 *
		 * @param key
		 *            the key
		 * @param map
		 *            the map
		 * @return the i tokens
		 */
		ITokens addToken(String key, String map);

		/**
		 * Web socket monitor.
		 *
		 * @param messageHandler
		 *            the message handler
		 * @return the i tokens
		 */
		ITokens webSocketMonitor(MessageHandler messageHandler);

		/**
		 * Setup.
		 */
		void setup();
	}

	/**
	 * The Class Builder.
	 */
	private static class Builder implements ITokens {

		/** The baser url. */
		String baserUrl;

		/** The tokens. */
		Map<String, String> tokens = new HashMap<String, String>();

		/** The http client. */
		OkHttpClient httpClient;

		/** The setup wsockets. */
		boolean setupWsockets = false;

		/** The message handler. */
		MessageHandler messageHandler = null;

		/**
		 * Instantiates a new builder.
		 *
		 * @param baseUrl
		 *            the base url
		 */
		public Builder(String baseUrl) {
			this.baserUrl = baseUrl;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.example.gerardogarcia.mylibrary.builders.BigchainDbConfigBuilder.ITokens#addToken(java.
		 * lang.String, java.lang.String)
		 */
		@Override
		public ITokens addToken(String key, String value) {
			tokens.put(key, value);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.example.gerardogarcia.mylibrary.builders.BigchainDbConfigBuilder.ITokens#setup()
		 */
		@Override
		public void setup() {
			Log.d("BASEURL", this.baserUrl );
			BigChainDBGlobals.setAuthorizationTokens(tokens);
			BigChainDBGlobals.setBaseUrl(this.baserUrl + "/api" + BigchainDbApi.API_VERSION);
			BigChainDBGlobals.setWsSocketUrl(this.baserUrl + "/api" + BigchainDbApi.API_VERSION + BigchainDbApi.STREAMS);
			
			if (this.httpClient == null && BigChainDBGlobals.getHttpClient() == null ) {
				BigChainDBGlobals.setHttpClient(buildDefaultHttpClient());
			}

			try {
				BigChainDBGlobals.setApiEndpoints(JsonUtils.fromJson(
						NetworkUtils.sendGetRequest(this.baserUrl + "/api" + BigchainDbApi.API_VERSION).body().string(),
						ApiEndpoints.class));
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (this.setupWsockets) {

				// we create another thread for processing the endpoint.
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							new BigchainDbWSSessionManager(new URI(BigChainDBGlobals.getApiEndpoints().getStreams()),
									messageHandler);
						} catch (URISyntaxException e) {
							e.printStackTrace();
						}

					}
				}).start();
			}
		}

		/**
		 * Builds the default http client.
		 *
		 * @return the ok http client
		 */
		private OkHttpClient buildDefaultHttpClient() {
			return new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS)
					.readTimeout(60, TimeUnit.SECONDS).addInterceptor(authInterceptor).build();
		}

		/** The auth interceptor. */
		private Interceptor authInterceptor = new Interceptor() {
			@Override
			public Response intercept(Interceptor.Chain chain) throws IOException {
				Request originalRequest = chain.request();

				if (tokens == null)
					return chain.proceed(originalRequest);

				// Add authorization header with updated authorization value to
				// intercepted request
				Request.Builder authorisedRequest = originalRequest.newBuilder();

				for (String key : tokens.keySet()) {
					authorisedRequest = authorisedRequest.addHeader(key, tokens.get(key));
				}

				return chain.proceed(authorisedRequest.build());
			}
		};

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.example.gerardogarcia.mylibrary.builders.BigchainDbConfigBuilder.ITokens#
		 * webSocketMonitor(com.example.gerardogarcia.mylibrary.ws.MessageHandler)
		 */
		@Override
		public ITokens webSocketMonitor(MessageHandler messageHandler) {
			this.setupWsockets = true;
			this.messageHandler = messageHandler;
			return this;
		}
	}
}
