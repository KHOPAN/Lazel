package com.khopan.lazel.config;

import java.util.ArrayList;
import java.util.List;

import com.khopan.lazel.config.BinaryConfigObject.ObjectData;

public class InternalBinaryConfigReader {
	private final BinaryConfigElement element;

	private InternalBinaryConfigReader(byte[] byteArray) {
		if(byteArray == null) {
			throw new NullPointerException("'byteArray' cannot be null");
		}

		List<Byte> byteData = new ArrayList<>();

		for(int i = 0; i < byteArray.length; i++) {
			byteData.add(byteArray[i]);
		}

		ObjectData data = this.recursiveByteDataReader(byteData).data;

		if(data.type.isBinaryConfigElement()) {
			this.element = (BinaryConfigElement) data.data;
		} else {
			throw new IllegalArgumentException("Can decode, but the result is not a BinaryConfigElement");
		}
	}

	private Result recursiveByteDataReader(List<Byte> data) {
		Result result = new Result();
		int i = -1;
		byte typeSpecifier = data.get(++i);
		ObjectType type = ObjectType.infer(typeSpecifier);

		if(type == null) {
			throw new IllegalArgumentException(String.format("Invalid type specifier %02x", typeSpecifier));
		}

		boolean isPrimitive = type.isPrimitive();
		boolean isNull = type.isNull();
		int length = 0;

		if(isPrimitive || isNull) {
			result.decodedBytes = 1;
		} else {
			length = Converter.byteToInt(new byte[] {data.get(++i), data.get(++i), data.get(++i), data.get(++i)});
			result.decodedBytes = length + 5;
		}

		result.data.type = type;

		if(isNull) {
			result.data.data = null;
		} else if(type.isPrimitive()) {
			if(ObjectType.BYTE.equals(type)) {
				result.data.data = data.get(++i);
				result.decodedBytes += 1;
			} else if(ObjectType.SHORT.equals(type)) {
				result.data.data = Converter.byteToShort(new byte[] {data.get(++i), data.get(++i)});
				result.decodedBytes += 2;
			} else if(ObjectType.INTEGER.equals(type)) {
				result.data.data = Converter.byteToInt(new byte[] {data.get(++i), data.get(++i), data.get(++i), data.get(++i)});
				result.decodedBytes += 4;
			} else if(ObjectType.LONG.equals(type)) {
				result.data.data = Converter.byteToLong(new byte[] {data.get(++i), data.get(++i), data.get(++i), data.get(++i), data.get(++i), data.get(++i), data.get(++i), data.get(++i)});
				result.decodedBytes += 8;
			} else if(ObjectType.FLOAT.equals(type)) {
				result.data.data = Converter.byteToFloat(new byte[] {data.get(++i), data.get(++i), data.get(++i), data.get(++i)});
				result.decodedBytes += 4;
			} else if(ObjectType.DOUBLE.equals(type)) {
				result.data.data = Converter.byteToDouble(new byte[] {data.get(++i), data.get(++i), data.get(++i), data.get(++i), data.get(++i), data.get(++i), data.get(++i), data.get(++i)});
				result.decodedBytes += 8;
			} else if(ObjectType.BOOLEAN.equals(type)) {
				result.data.data = Converter.byteToBoolean(data.get(++i));
				result.decodedBytes += 1;
			} else if(ObjectType.CHARACTER.equals(type)) {
				result.data.data = Converter.byteToChar(new byte[] {data.get(++i), data.get(++i)});
				result.decodedBytes += 2;
			}
		} else if(type.isString()) {
			try {
				byte[] string = new byte[length];

				for(int x = 0; x < length; x++) {
					string[x] = data.get(++i);
				}

				String text = new String(string, "UTF-8");
				result.data.data = text;
			} catch(Throwable Errors) {
				throw new RuntimeException(Errors);
			}
		} else if(type.isBinaryConfigElement()) {
			if(ObjectType.BINARY_CONFIG_OBJECT.equals(type)) {
				List<Byte> array = new ArrayList<>();

				for(int x = 0; x < length; x++) {
					array.add(data.get(++i));
				}

				int decodedBytes = 0;
				BinaryConfigObject binaryConfigObject = new BinaryConfigObject();

				while(decodedBytes < length) {
					int size = Converter.byteToInt(new byte[] {array.get(decodedBytes++), array.get(decodedBytes++), array.get(decodedBytes++), array.get(decodedBytes++)});
					byte[] text = new byte[size];

					for(int x = 0; x < size; x++) {
						text[x] = array.get(decodedBytes++);
					}

					String key = new String(text);
					Result resultData = this.recursiveByteDataReader(array.subList(decodedBytes, length));
					decodedBytes += resultData.decodedBytes;
					binaryConfigObject.map.put(key, resultData.data);
				}

				result.data.data = binaryConfigObject;
			} else if(ObjectType.BINARY_CONFIG_ARRAY.equals(type)) {
				List<Byte> array = new ArrayList<>();

				for(int x = 0; x < length; x++) {
					array.add(data.get(++i));
				}

				int decodedBytes = 0;
				BinaryConfigArray binaryConfigArray = new BinaryConfigArray();

				while(decodedBytes < length) {
					Result resultData = this.recursiveByteDataReader(array.subList(decodedBytes, length));
					decodedBytes += resultData.decodedBytes;
					binaryConfigArray.list.add(resultData.data);
				}

				result.data.data = binaryConfigArray;
			}
		} else {
			throw new IllegalStateException("Unexpected state, type is not either null, primitive, string or BinaryConfigElement");
		}

		return result;
	}

	private BinaryConfigElement getElement() {
		return this.element;
	}

	static BinaryConfigElement read(byte[] byteArray) {
		return new InternalBinaryConfigReader(byteArray).getElement();
	}

	private class Result {
		private ObjectData data;
		private int decodedBytes;

		private Result() {
			this.data = new ObjectData();
		}
	}
}
