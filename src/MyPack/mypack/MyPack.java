package mypack;

import java.io.File;


public class MyPack {

	private static File dataDir = new File("./data");
	private static Class<?> theMinecraftClass;
	
	public static void setWorkingDir(File dir) {
		dataDir = dir;
	}

	public static void onClassPathLoaded(ClassLoader cl) {
		try {
			theMinecraftClass = cl.loadClass("net.minecraft.client.Minecraft");
		} catch (ClassNotFoundException e) {
			
		}
		DataUtil.setDataDir(theMinecraftClass, dataDir);
	}

	public static File getWorkingDirectory() {
		return dataDir;
	}

}
