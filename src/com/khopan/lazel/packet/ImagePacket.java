package com.khopan.lazel.packet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

public class ImagePacket extends Packet {
	private final BufferedImage image;

	public ImagePacket(byte[] byteArray) {
		super(byteArray);

		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(this.byteArray);
			this.image = ImageIO.read(stream);
		} catch(Throwable Errors) {
			throw new InternalError("Error while reading the image", Errors);
		}
	}

	public ImagePacket(BufferedImage image) {
		super(null);

		if(image == null) {
			throw new NullPointerException("Image cannot be null");
		}

		this.image = image;

		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ImageIO.write(image, "png", stream);
			this.byteArray = stream.toByteArray();
		} catch(Throwable Errors) {
			throw new InternalError("Error while converting an image into byte array", Errors);
		}
	}

	public BufferedImage getImage() {
		return this.image;
	}
}
