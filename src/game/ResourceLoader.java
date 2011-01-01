package game;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import json.JSNode;

public class ResourceLoader {
	static Image LoadImage(String name) {
		try {
			return ImageIO.read(ResourceLoader.class.getResource("/res/" + name));
		} catch (Exception e) {
			System.err.println("Failed load image `" + name + "` because: " + e.getMessage());
			return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		}
	}
	static InputStream LoadData(String name) {
		try {
			InputStream s = ResourceLoader.class.getResourceAsStream("/res/" + name);
			if (s == null) {
				throw new Exception("Resource not found");
			}
			return s;
		} catch (Exception e) {
			System.err.println("Failed to load data `" + name + "` because: " + e.getMessage());
			return null;
		}
	}
	static JSNode LoadJSON(String name) throws Exception {
		return JSNode.parse(LoadData(name));
	}
}
