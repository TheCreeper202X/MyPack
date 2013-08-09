package mypack;

import java.applet.Applet;
import java.util.HashMap;

import net.minecraft.Launcher;
import net.minecraft.Util;

public class MyPackLauncher {

	public static void updateSystemProperties() {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.net.preferIPv6Addresses", "false");
	}
	
	public static String[] getPlayerData(String name, String pw, ErrorHandler handle) {
		HashMap<String, Object> localHashMap = new HashMap<String, Object>();
		localHashMap.put("user", name);
		localHashMap.put("password", pw);
		localHashMap.put("version", Integer.valueOf(13));
		String str = Util.executePost("https://login.minecraft.net/", localHashMap);
		if (str == null) {
			handle.error(ErrorHandler.CANT_CONNECT, "Can't connect to minecraft.net");
			return new String[0];
		}
		if (!str.contains(":")) {
			if (str.trim().equals("Bad login")) {
				handle.error(ErrorHandler.LOGIN_FAILED, "Login failed");
			} else if (str.trim().equals("Old version")) {
				handle.error(ErrorHandler.OUTDATED_LAUNCHER, "Outdated launcher");
			} else if (str.trim().equals("User not premium")) {
				handle.error(ErrorHandler.USER_NOT_PREMIUM, str);
			} else {
				handle.error(ErrorHandler.OTHER, str);
			}
			return new String[0];
		}
		String[] arrayOfString = str.split(":");
		return arrayOfString;
	}
	
	public static Applet createLauncher(String[] arrayOfString) {
		Launcher launcher = new Launcher();
		launcher.customParameters.put("stand-alone", "true");
		launcher.customParameters.put("userName", arrayOfString[2].trim());
		launcher.customParameters.put("latestVersion", arrayOfString[0].trim());
		launcher.customParameters.put("downloadTicket", arrayOfString[1].trim());
		launcher.customParameters.put("sessionId", arrayOfString[3].trim());
		launcher.init();
		return launcher;
	}
	
	public static void launch(Applet launcher) {
		((Launcher) launcher).start();
	}
	
	public static void stop(Applet launcher) {
		((Launcher) launcher).stop();
		((Launcher) launcher).destroy();
	}
	
}
