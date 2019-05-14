package com.nuclearfarts.simpleoldcombat.installer;

import java.io.File;

public class MCLocator {

	public static File getMinecraftDirectory() {
		String userHome = System.getProperty("user.home", ".");
		
		File minecraftDir;
		switch (OS.getCurrent()) {
		case LINUX:
			minecraftDir = new File(userHome, ".minecraft/");
			break;
		case WINDOWS:
			String applicationData = System.getenv("APPDATA");
			String folder = applicationData != null ? applicationData : userHome;
			minecraftDir = new File(folder, ".minecraft/");
			break;
		case MAC:
			minecraftDir = new File(userHome, "Library/Application Support/minecraft");
			break;
		default:
			minecraftDir = new File(userHome, "minecraft/");
		}
		return minecraftDir;
	}
	
	public enum OS {
		LINUX("linux", "unix"), WINDOWS("win"), MAC("mac"), OTHER;
		
		private String[] tests;
		
		private OS(String... tests) {
			this.tests = tests;
		}
		
		private boolean containedIn(String str) {
			str = str.toLowerCase();
			for (String test : tests) {
				if (str.contains(test)) {
					return true;
				}
			}
			return false;
		}
		
		public static final OS getCurrent() {
			String osName = System.getProperty("os.name");
			for (OS os : OS.values()) {
				if (os.containedIn(osName)) {
					return os;
				}
			}
			return OTHER;
		}
	}
	
}
