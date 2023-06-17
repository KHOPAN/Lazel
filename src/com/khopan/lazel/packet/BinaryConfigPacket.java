package com.khopan.lazel.packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import com.khopan.lazel.config.BinaryConfig;
import com.khopan.lazel.config.BinaryConfig.ExceptionHandler;
import com.khopan.lazel.config.BinaryConfigArray;
import com.khopan.lazel.config.BinaryConfigElement;
import com.khopan.lazel.config.BinaryConfigObject;

public class BinaryConfigPacket extends Packet {
	private final BinaryConfigElement element;

	public BinaryConfigPacket(byte[] byteArray) {
		super(byteArray);
		ByteArrayInputStream stream = new ByteArrayInputStream(this.byteArray);
		ExceptionHandler handler = BinaryConfig.exceptionHandler().get();
		AtomicBoolean error = new AtomicBoolean();
		error.set(false);
		BinaryConfig.exceptionHandler().set(throwable -> {
			error.set(true);
		});

		this.element = BinaryConfig.read(stream);
		BinaryConfig.exceptionHandler().set(handler);

		if(error.get()) {
			throw new IllegalArgumentException("Failed reading the byte array data");
		}
	}

	public BinaryConfigPacket(BinaryConfigElement element) {
		super(null);

		if(element == null) {
			throw new NullPointerException("Element cannot be null");
		}

		this.element = element;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		BinaryConfig.write(this.element, stream);
		this.byteArray = stream.toByteArray();
	}

	public BinaryConfigElement getElement() {
		return this.element;
	}

	public boolean isArray() {
		return this.element instanceof BinaryConfigArray;
	}

	public boolean isObject() {
		return this.element instanceof BinaryConfigObject;
	}

	public BinaryConfigArray getArray() {
		if(this.isArray()) {
			return (BinaryConfigArray) this.element;
		} else {
			throw new IllegalStateException("Not a BinaryConfigArray");
		}
	}

	public BinaryConfigObject getObject() {
		if(this.isObject()) {
			return (BinaryConfigObject) this.element;
		} else {
			throw new IllegalStateException("Not a BinaryConfigObject");
		}
	}
}
