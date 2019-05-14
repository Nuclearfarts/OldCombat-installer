package com.nuclearfarts.simpleoldcombat.installer;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.nuclearfarts.simpleoldcombat.installer.util.DownloadData;

public class OldCombatVersion {
	
	public static final OldCombatVersion LOADING_VERSION = new LoadingVersion();
	
	public final String mcVersion;
	public final String oldCombatVersion;
	public final DownloadData mcServer;
	public final DownloadData clientJson;
	public final DownloadData serverJar;
	
	public OldCombatVersion(String mcVer, String oldCombatVer, DownloadData mcServer, DownloadData clientJson, DownloadData serverJar) {
		mcVersion = mcVer;
		oldCombatVersion = oldCombatVer;
		this.mcServer = mcServer;
		this.clientJson = clientJson;
		this.serverJar = serverJar;
	}
	
	public String toString() {
		return "OldCombat version " + oldCombatVersion + " for Minecraft " + mcVersion;
	}
	
	private static class LoadingVersion extends OldCombatVersion {

		public LoadingVersion() {
			super(null, null, null, null, null);
		}
		
		public String toString() {
			return "Loading...";
		}
		
	}
	
	public static final class Deserializer implements JsonDeserializer<OldCombatVersion> {

		@Override
		public OldCombatVersion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			return new OldCombatVersion(obj.get("mcVersion").getAsString(), 
					obj.get("oldcombatVersion").getAsString(),
					context.deserialize(obj.get("mcServerJar"), DownloadData.class),
					context.deserialize(obj.get("clientJson"), DownloadData.class),
					context.deserialize(obj.get("serverJar"), DownloadData.class));
		}
		
	}
}
