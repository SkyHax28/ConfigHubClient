package com.dew.system.altmanager.login;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.altmanager.Alt;
import com.dew.system.altmanager.HttpUtils;
import com.dew.utils.LogUtil;
import com.dew.utils.MiscUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraft.util.Util;

public class MicrosoftAuth {
	private static final String CLIENT_ID = "e92d34e9-149f-40fc-bbf2-5e5d4f3c43f4";
	private static final URI TOKEN_URL = HttpUtils.createURI("https://login.live.com/oauth20_token.srf");
	private static final URI XBL_URL = HttpUtils.createURI("https://user.auth.xboxlive.com/user/authenticate");
	private static final URI XBLAUTH_URL = HttpUtils.createURI("https://xsts.auth.xboxlive.com/xsts/authorize");
	private static final URI XBLOGIN_URL = HttpUtils
			.createURI("https://api.minecraftservices.com/authentication/login_with_xbox");
	private static final URI ENTITLEMENT_URL = HttpUtils
			.createURI("https://api.minecraftservices.com/entitlements/mcstore");
	private static final URI PROFILE_URL = HttpUtils.createURI("https://api.minecraftservices.com/minecraft/profile");
	private static final Integer port = 42069;

	public static HttpServer replyServer;
	public static boolean isLoggingInToMicrosoft = false;

	/**
	 * Logs in to an alt.
	 * 
	 * @param alt Alt to login to.
	 */
	public static void login(Alt alt, Boolean addAltIfSuccess) {
		try {
			LogUtil.infoLog("1");

			AuthToken token = renewAuthToken(alt.getAuthToken());
			DewCommon.altManager.status = "Renewed auth token";

			LogUtil.infoLog("2");

			XSTSToken xblToken = getXblToken(token);
			DewCommon.altManager.status = "Xbl token";

			LogUtil.infoLog("3");

			XSTSToken xstsToken = getXSTSToken(xblToken);
			DewCommon.altManager.status = "Xsts token";

			LogUtil.infoLog("4");

			MCAuthToken mcAuthToken = getMCAuthToken(xblToken, xstsToken);
			DewCommon.altManager.status = "Got MCAuth token";

			LogUtil.infoLog("5");

			EntitlementToken entitlement = getEntitlementToken(mcAuthToken);
			DewCommon.altManager.status = "Got entitlement token";

			LogUtil.infoLog("6");

			if (isLoggingInToMicrosoft) {
				LogUtil.infoLog("7");

				if (entitlement.hasGame()) {
					ProfileToken profileToken = getProfileToken(mcAuthToken);

					String uuid = profileToken.id;
					String username = profileToken.name;

					DewCommon.altManager.status = "Profile set";

					alt.setAuthToken(token);
					alt.setUsername(username);

					DewCommon.altManager.status = "Token set";

					Minecraft MC = IMinecraft.mc;
                    MC.session = new Session(username, uuid, mcAuthToken.accessToken, "legacy");

					DewCommon.altManager.status = "Session set";

					if (addAltIfSuccess) {
						boolean foundSame = false;
						for (Alt remainAlt : DewCommon.altManager.getAlts()) {
							if (Objects.equals(remainAlt.getUsername(), alt.getUsername())) {
								DewCommon.altManager.removeAlt(remainAlt);
								foundSame = true;
								break;
							}
						}

						DewCommon.altManager.addAlt(alt);
						if (foundSame)
							DewCommon.altManager.status = "Duplicated account detected";
						else DewCommon.altManager.status = "Added new account";
					}


					DewCommon.altManager.status = "Logged in as " + username + " (Microsoft)";
				} else DewCommon.altManager.status = "User does not have game";
			}
		} catch (Exception e) {
			DewCommon.altManager.status = "Could not log in to Microsoft account";
		}

		isLoggingInToMicrosoft = false;
		DewCommon.altManager.saveAlts();

		stopServer();
	}

	public static AuthToken renewAuthToken(AuthToken authToken) {
		String payload = "client_id=" + CLIENT_ID + "&refresh_token=" + authToken.refreshToken
				+ "&grant_type=refresh_token&redirect_uri=http://127.0.0.1:" + port;
		Optional<String> response = HttpUtils.builder(TOKEN_URL).acceptJson().form().post(payload);
		if (response.isPresent()) {
			JsonObject json = new Gson().fromJson(response.get(), JsonObject.class);
			return AuthToken.fromJson(json);
		} else
			throw new IllegalArgumentException("Auth token could not be fetched.");
	}

	/**
	 * Fetches the Xbl token from Microsoft.
	 * 
	 * @param authToken The AuthToken containing the access code and refresh code.
	 * @return Xbl token as a XSTSToken object.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static XSTSToken getXblToken(AuthToken authToken) {
		String payload = "{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"d="
				+ authToken.accessToken + "\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}";

		Optional<String> response = HttpUtils.builder(XBL_URL).acceptJson().json().post(payload);

		if (response.isPresent()) {
			JsonObject json = new Gson().fromJson(response.get(), JsonObject.class);
			return XSTSToken.fromJson(json);
		} else
			throw new IllegalArgumentException("Xbl token could not be fetched.");
	}

	/**
	 * Fetches the XSTS token from Microsoft.
	 * 
	 * @param xblToken The XSTS token previously fetched.
	 * @return XSTS token fetched
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static XSTSToken getXSTSToken(XSTSToken xblToken) {
		String payload = "{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\"" + xblToken.token
				+ "\"]},\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}";

		Optional<String> response = HttpUtils.builder(XBLAUTH_URL).acceptJson().json().post(payload);
		if (response.isPresent()) {
			JsonObject json = new Gson().fromJson(response.get(), JsonObject.class);
			return XSTSToken.fromJson(json);
		} else
			throw new IllegalArgumentException("XSTS token could not be fetched.");
	}

	public static MCAuthToken getMCAuthToken(XSTSToken xblToken, XSTSToken xstsToken) {
		String payload = "{\"identityToken\":\"XBL3.0 x=" + xblToken.displayClaims.xui[0].uhs + ";" + xstsToken.token
				+ "\", \"ensureLegacyEnabled\":true}";

		Optional<String> response = HttpUtils.builder(XBLOGIN_URL).acceptJson().json().post(payload);
		if (response.isPresent()) {
			JsonObject json = new Gson().fromJson(response.get(), JsonObject.class);
			return MCAuthToken.fromJson(json);
		} else
			throw new IllegalArgumentException("MC Auth token could not be fetched.");
	}

	public static EntitlementToken getEntitlementToken(MCAuthToken mcAuthToken) {
		Optional<String> response = HttpUtils.builder(ENTITLEMENT_URL).acceptJson().json()
				.bearer(mcAuthToken.accessToken).get();
		if (response.isPresent()) {
			JsonObject json = new Gson().fromJson(response.get(), JsonObject.class);
			return EntitlementToken.fromJson(json);
		} else
			throw new IllegalArgumentException("Entitlement token could not be fetched.");
	}

	public static ProfileToken getProfileToken(MCAuthToken mcAuthToken) throws IOException, InterruptedException {
		Optional<String> response = HttpUtils.builder(PROFILE_URL).acceptJson().json().bearer(mcAuthToken.accessToken)
				.get();
		if (response.isPresent()) {
			JsonObject json = new Gson().fromJson(response.get(), JsonObject.class);
			return ProfileToken.fromJson(json);
		} else
			throw new IllegalArgumentException("Entitlement token could not be fetched.");
	}

	public static void requestAuthToken(Consumer<AuthToken> onDataReceived) {
		boolean success = startServer(onDataReceived);
		if (success) {
			MiscUtil.showURL("https://login.live.com/oauth20_authorize.srf?client_id=" + CLIENT_ID
					+ "&response_type=code&redirect_uri=http://127.0.0.1:" + port + "&scope=XboxLive.signin%20offline_access%20openid%20profile&prompt=select_account");
		}
	}

	public static boolean startServer(Consumer<AuthToken> onDataReceived) {
		try {
			replyServer = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
			RefreshTokenHandler handler = new RefreshTokenHandler();
			handler.setConsumer(onDataReceived);
			replyServer.createContext("/", handler);
			replyServer.setExecutor(null);
			replyServer.start();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static void stopServer() {
		if (replyServer == null)
			return;

		replyServer.stop(0);
		replyServer = null;
	}

	static class RefreshTokenHandler implements HttpHandler {
		private Consumer<AuthToken> onDataReceived;

		@Override
		public void handle(HttpExchange t) throws IOException {
			URI requestURI = t.getRequestURI();
			String query = requestURI.getQuery();
			String[] parameterPairs = query.split("&");

			boolean found = false;
			for (String params : parameterPairs) {
				String[] paramSplit = params.split("=");
				if (paramSplit.length == 2) {
					if (paramSplit[0].equalsIgnoreCase("code")) {
						found = true;
						try {
							AuthToken token = getAccessToken(paramSplit[1]);
							onDataReceived.accept(token);
						} catch (Exception e) {
							LogUtil.infoLog("Error occured while fetching Access Tokens from Microsoft");
							onDataReceived.accept(null);
						}

						break;
					}
				}
			}

			if (!found) {
				LogUtil.infoLog("Unable to get Access Tokens from Microsoft");
				onDataReceived.accept(null);
			}

			if (replyServer == null)
				return;

			replyServer.stop(0);
			replyServer = null;
		}

		private AuthToken getAccessToken(String code) {
			String payload = "client_id=" + CLIENT_ID + "&code=" + code
					+ "&grant_type=authorization_code&redirect_uri=http://127.0.0.1:" + port;
			Optional<String> response = HttpUtils.builder(TOKEN_URL).acceptJson().form().post(payload);
			if (response.isPresent()) {
				JsonObject json = new Gson().fromJson(response.get(), JsonObject.class);
				return AuthToken.fromJson(json);
			} else
				throw new IllegalArgumentException("Device token could not be fetched.");
		}

		public void setConsumer(Consumer<AuthToken> consumer) {
			onDataReceived = consumer;
		}
	}
}
