package mypack;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.minecraft.Launcher;

public class MyPackImgs {

	public static BufferedImage getMinecraftFavicon() {
		try {
			return ImageIO.read(Launcher.class.getResource("favicon.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static BufferedImage getMinecraftLogo() {
		try {
			return ImageIO.read(Launcher.class.getResource("logo.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
