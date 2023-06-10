package com.khopan.lazel.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.khopan.lazel.property.Property;
import com.khopan.lazel.property.SimpleProperty;

public class BinaryConfig {
	public BinaryConfig() {}

	private static final byte MAGIC_NUMBER = 0x3F;

	private static final ExceptionHandler DEFAULT_HANDLER = Error -> {
		throw new RuntimeException(Error);
	};

	private static ExceptionHandler ExceptionHandler = BinaryConfig.DEFAULT_HANDLER;

	public static Property<ExceptionHandler, Void> exceptionHandler() {
		return new SimpleProperty<ExceptionHandler, Void>(() -> BinaryConfig.ExceptionHandler, handler -> BinaryConfig.ExceptionHandler = handler, null).whenNull(BinaryConfig.DEFAULT_HANDLER);
	}

	public static void write(BinaryConfigElement element, OutputStream stream) {
		if(element == null || stream == null) {
			return;
		}

		try {
			Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
			byte[] data = BinaryConfig.writeByte(element);
			deflater.setInput(data);
			deflater.finish();
			byte[] output = new byte[data.length * 2];
			int size = deflater.deflate(output);
			byte[] result = new byte[size];

			for(int i = 0; i < size; i++) {
				result[i] = output[i];
			}

			stream.write(BinaryConfig.MAGIC_NUMBER);
			stream.write(result);
			deflater.end();
			stream.close();
		} catch(Throwable Errors) {
			BinaryConfig.ExceptionHandler.onThrowableThrows(Errors);
		}
	}

	public static void writeFile(BinaryConfigElement element, File file) {
		if(element == null || file == null) {
			return;
		}

		try {
			BinaryConfig.write(element, new FileOutputStream(file));
		} catch(Throwable Errors) {
			BinaryConfig.ExceptionHandler.onThrowableThrows(Errors);
		}
	}

	public static byte[] writeByte(BinaryConfigElement element) {
		return InternalBinaryConfigWriter.write(element);
	}

	public static void writeRaw(BinaryConfigElement element, OutputStream stream) {
		if(element == null || stream == null) {
			return;
		}

		try {
			stream.write(BinaryConfig.writeByte(element));
			stream.close();
		} catch(Throwable Errors) {
			BinaryConfig.ExceptionHandler.onThrowableThrows(Errors);
		}
	}

	public static BinaryConfigElement read(InputStream stream) {
		if(stream == null) {
			return null;
		}

		try {
			if(stream.read() != BinaryConfig.MAGIC_NUMBER) {
				BinaryConfig.ExceptionHandler.onThrowableThrows(new IllegalArgumentException("Not a binary config"));
				return null;
			}

			Inflater inflater = new Inflater();
			byte[] input = stream.readAllBytes();
			inflater.setInput(input);
			byte[] output = new byte[input.length * 10];
			int size = inflater.inflate(output);
			byte[] result = new byte[size];

			for(int i = 0; i < size; i++) {
				result[i] = output[i];
			}

			inflater.end();
			return BinaryConfig.readByte(result);
		} catch(Throwable Errors) {
			BinaryConfig.ExceptionHandler.onThrowableThrows(Errors);
		}

		return null;
	}

	public static BinaryConfigElement readFile(File file) {
		if(file == null) {
			return null;
		}

		try {
			return BinaryConfig.read(new FileInputStream(file));
		} catch(Throwable Errors) {
			BinaryConfig.ExceptionHandler.onThrowableThrows(Errors);
		}

		return null;
	}

	public static BinaryConfigElement readByte(byte[] byteArray) {
		return InternalBinaryConfigReader.read(byteArray);
	}

	public static BinaryConfigElement readRaw(InputStream stream) {
		if(stream == null) {
			return null;
		}

		try {
			byte[] data = stream.readAllBytes();
			stream.close();
			return BinaryConfig.readByte(data);
		} catch(Throwable Errors) {
			BinaryConfig.ExceptionHandler.onThrowableThrows(Errors);
		}

		return null;
	}

	@FunctionalInterface
	public static interface ExceptionHandler {
		public void onThrowableThrows(Throwable Error);
	}
}
