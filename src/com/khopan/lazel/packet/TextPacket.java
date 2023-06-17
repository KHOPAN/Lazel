package com.khopan.lazel.packet;

import java.nio.charset.Charset;

public class TextPacket extends Packet {
	private final String text;

	public TextPacket(byte[] byteArray) {
		super(byteArray);
		this.text = new String(this.byteArray);
	}

	public TextPacket(String text) {
		super(null);

		if(text == null) {
			throw new NullPointerException("Text cannot be null");
		}

		this.text = text;
		this.byteArray = this.text.getBytes(Charset.defaultCharset());
	}

	public String getText() {
		return this.text;
	}
}
