package com.dew.system.altmanager.alt;

import java.util.UUID;

import com.dew.IMinecraft;
import com.dew.system.altmanager.alt.openauth.microsoft.MicrosoftAuthResult;
import com.dew.system.altmanager.alt.openauth.microsoft.MicrosoftAuthenticationException;
import com.dew.system.altmanager.alt.openauth.microsoft.MicrosoftAuthenticator;
import com.mojang.authlib.Agent;
import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

public class SessionChanger {

	private static SessionChanger instance;
	private final UserAuthentication auth;

	public static SessionChanger getInstance() {
		if (instance == null) {
			instance = new SessionChanger();
		}

		return instance;
	}
	
	//Creates a new Authentication Service. 
	private SessionChanger() {
		UUID notSureWhyINeedThis = UUID.randomUUID(); //Idk, needs a UUID. Seems to be fine making it random
		AuthenticationService authService = new YggdrasilAuthenticationService(Minecraft.getMinecraft().getProxy(), notSureWhyINeedThis.toString());
		auth = authService.createUserAuthentication(Agent.MINECRAFT);
		authService.createMinecraftSessionService();
	}

	
	//Online mode
	//Checks if your already loggin in to the account.
	public void setUser(String email, String password) {
		if(!Minecraft.getMinecraft().getSession().getUsername().equals(email) || Minecraft.getMinecraft().getSession().getToken().equals("0")){

			this.auth.logOut();
			this.auth.setUsername(email);
			this.auth.setPassword(password);
			try {
				this.auth.logIn();
				IMinecraft.mc.session = new Session(this.auth.getSelectedProfile().getName(), UUIDTypeAdapter.fromUUID(auth.getSelectedProfile().getId()), this.auth.getAuthenticatedToken(), this.auth.getUserType().getName());
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void setUserMicrosoft(String email, String password) {

		MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
		try {
			MicrosoftAuthResult acc = authenticator.loginWithCredentials(email, password);
			Minecraft.getMinecraft().session = new Session(acc.getProfile().getName(), acc.getProfile().getId(), acc.getAccessToken(), "legacy");

		} catch (MicrosoftAuthenticationException e) {

		}
	}

	//Login offline mode
	//Just like MCP does
	public void setUserOffline(String username) {
		this.auth.logOut();
		IMinecraft.mc.session = new Session(username, username, "0", "legacy");
	}

}