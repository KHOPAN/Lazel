package com.khopan.lazel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.khopan.lazel.config.BinaryConfig;
import com.khopan.lazel.config.BinaryConfigElement;
import com.khopan.lazel.config.BinaryConfigObject;

public class Packet {
	private static final byte RAW = 0x1E;
	private static final byte CONFIG = 0x24;

	private byte[] data;
	private boolean rawData;
	private byte[] raw;
	private BinaryConfigObject config;

	public Packet(byte[] raw) {
		this.rawData = true;
		this.raw = raw;
		List<Byte> list = new ArrayList<>();
		list.add(Packet.RAW);

		for(int i = 0; i < this.raw.length; i++) {
			list.add(this.raw[i]);
		}

		int size = list.size();
		this.data = new byte[size];

		for(int i = 0; i < size; i++) {
			this.data[i] = list.get(i);
		}
	}

	public Packet(BinaryConfigObject config) {
		this.rawData = false;
		this.config = config;
		List<Byte> list = new ArrayList<>();
		list.add(Packet.CONFIG);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		BinaryConfig.write(this.config, stream);
		this.raw = stream.toByteArray();

		for(int i = 0; i < this.raw.length; i++) {
			list.add(this.raw[i]);
		}

		int size = list.size();
		this.data = new byte[size];

		for(int i = 0; i < size; i++) {
			this.data[i] = list.get(i);
		}
	}

	public byte[] send() {
		return this.data;
	}

	public boolean isRawData() {
		return this.rawData;
	}

	public byte[] getRawData() {
		return this.raw;
	}

	public BinaryConfigObject getBinaryConfigObject() {
		if(this.rawData) {
			throw new IllegalArgumentException("The data is not a BinaryConfigObject");
		}

		return this.config;
	}

	public static Packet receive(byte[] data) {
		byte header = data[0];

		if(header == Packet.RAW) {
			byte[] output = new byte[data.length - 1];

			for(int i = 0; i < output.length; i++) {
				output[i] = data[i + 1];
			}

			return new Packet(output);
		} else if(header == Packet.CONFIG) {
			byte[] output = new byte[data.length - 1];

			for(int i = 0; i < output.length; i++) {
				output[i] = data[i + 1];
			}

			ByteArrayInputStream stream = new ByteArrayInputStream(output);
			BinaryConfigElement element = BinaryConfig.read(stream);

			if(element instanceof BinaryConfigObject object) {
				return new Packet(object);
			} else {
				throw new IllegalArgumentException("Only BinaryConfigObject is allowed");
			}
		} else {
			return new Packet(data);
		}
	}
}
