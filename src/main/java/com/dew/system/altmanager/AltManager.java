package com.dew.system.altmanager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.altmanager.login.MicrosoftAuth;
import com.dew.utils.LogUtil;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

public class AltManager {
	private final CopyOnWriteArrayList<Alt> alts = new CopyOnWriteArrayList<Alt>();
	private String encryptKey;
	public String status = "Waiting...";

	/**
	 * Constructor for the Alt Manager system.
	 */
	public AltManager() {
		this.encryptKey = generateEncryptionKey();
		readAlts();

		LogUtil.infoLog("init altManager");
	}

	/**
	 * Generates a unique encryption key based on HWID.
	 *
	 * @return A unique encryption key.
	 */
	private String generateEncryptionKey() {
		// Use the first 16 bytes of the HWID as the encryption key
		return "KrsAltEncryption";
	}

	/**
	 * Reads the Alts from a file.
	 */
	public void readAlts() {
		try {
			// Finds the file and opens it.
			File altFile = new File(DewCommon.BASE_CFG_DIR, "alts.json");
			if (!altFile.exists()) {
				LogUtil.infoLog("Alts file not found! Cannot load alts.");
				return;
			}

			alts.clear();

			// Read the JSON from the file
			FileReader reader = new FileReader(altFile);
			Gson gson = new Gson();
			Type altListType = new TypeToken<List<String>>() {
			}.getType();
			List<String> encryptedAltList = gson.fromJson(reader, altListType);
			reader.close();

			// Decrypt the alts and add them to the current alt list
			for (String encryptedAlt : encryptedAltList) {
				String decryptedAlt = decrypt(encryptedAlt);
				Alt alt = gson.fromJson(decryptedAlt, Alt.class);
				alts.add(alt);
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Saves the Alts to a file.
	 */
	public void saveAlts() {
		try {
			// Finds the file and opens it.
			File altFile = new File(DewCommon.BASE_CFG_DIR, "alts.json");

			// Encrypt the alts and convert the alt list to JSON
			Gson gson = new Gson();
			List<String> encryptedAltList = new ArrayList<>();
			for (Alt alt : alts) {
				String altJson = gson.toJson(alt);
				String encryptedAlt = encrypt(altJson);
				encryptedAltList.add(encryptedAlt);
			}
			String json = gson.toJson(encryptedAltList);

			// Write the JSON to the file
			FileWriter writer = new FileWriter(altFile);
			writer.write(json);
			writer.close();
		} catch (IOException exception) {
			LogUtil.infoLog("Failed to save alts");
			exception.printStackTrace();
		}
	}

	/**
	 * Encrypts a string using a defined encryption key.
	 * 
	 * @param strToEncrypt The string to by encrypted.
	 * @return The encrypted string.
	 */
	public String encrypt(String strToEncrypt) {
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptKey.getBytes(), "AES"));
			return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strToEncrypt;
	}

	/**
	 * Decrypts a string using a defined encryption key.
	 * 
	 * @return The decrypted string.
	 */
	public String decrypt(String strToDecrypt) {
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encryptKey.getBytes(), "AES"));
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strToDecrypt;
	}

	/**
	 * Adds an Alt account to the Alt list.
	 * 
	 * @param alt The Alt to be added.
	 */
	public void addAlt(Alt alt) {
		alts.add(alt);
	}

	/**
	 * Removes an Alt account from the Alt list.
	 * 
	 * @param alt The Alt to be removed.
	 */
	public void removeAlt(Alt alt) {
		alts.remove(alt);
	}

	/**
	 * Gets the Alt list.
	 * 
	 * @return The Alt list.
	 */
	public CopyOnWriteArrayList<Alt> getAlts() {
		return this.alts;
	}

	/**
	 * Logs in to an alt account.
	 * 
	 * @param alt Alt
	 * @return login success state
	 */
	public void login(Alt alt, Boolean addAltIfSuccess) {
		// Log in to the correct service depending on the Alt type.
		MicrosoftAuth.isLoggingInToMicrosoft = true;
		MicrosoftAuth.login(alt, addAltIfSuccess);
	}

	/**
	 * Logs in to a cracked alt account.
	 * 
	 * @param alt Alt
	 * @return login success state
	 */
	public void loginCracked(String alt) {
		try {
			String offlineAlt = UUID.fromString(alt).toString();
			Minecraft.getMinecraft().session = new Session(alt, offlineAlt, "", "legacy");
			LogUtil.infoLog("Logged in as " + alt + " (Cracked)");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
