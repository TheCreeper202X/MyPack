package mypack;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class DataUtil {

	public static void setDataDir(Class<?> minecraft, File dir) {
		for (Field f : minecraft.getDeclaredFields()) {
			if (f.getType() == File.class) {
				if (Modifier.isPrivate(f.getModifiers())) {
					if (Modifier.isStatic(f.getModifiers())) {
						try {
							f.setAccessible(true);
							f.set(null, dir);
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
}
