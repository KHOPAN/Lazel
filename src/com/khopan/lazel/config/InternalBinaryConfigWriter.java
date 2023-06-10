package com.khopan.lazel.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.khopan.lazel.config.BinaryConfigObject.ObjectData;

class InternalBinaryConfigWriter {
	private final byte[] byteData;

	private InternalBinaryConfigWriter(BinaryConfigElement element) {
		if(element == null) {
			throw new NullPointerException("'element' cannot be null");
		}

		ObjectData data = new ObjectData();

		if(element instanceof BinaryConfigObject) {
			data.type = ObjectType.BINARY_CONFIG_OBJECT;
		} else if(element instanceof BinaryConfigArray) {
			data.type = ObjectType.BINARY_CONFIG_ARRAY;
		}

		data.data = element;
		List<Byte> byteArray = this.recursiveByteBuilder(data);
		this.byteData = new byte[byteArray.size()];

		for(int i = 0; i < this.byteData.length; i++) {
			this.byteData[i] = byteArray.get(i);
		}
	}

	private List<Byte> recursiveByteBuilder(ObjectData data) {
		List<Byte> list = new ArrayList<>();

		if(data == null) {
			return list;
		}

		if(data.type.isNull()) {
			list.add(ObjectType.NULL.getTypeSpecifier());
		} else if(data.type.isPrimitive()) {
			list.add(data.type.getTypeSpecifier());

			if(ObjectType.BYTE.equals(data.type)) {
				list.add((byte) data.data);
			} else if(ObjectType.SHORT.equals(data.type)) {
				this.addByteArray(list, Converter.shortToByte((short) data.data));
			} else if(ObjectType.INTEGER.equals(data.type)) {
				this.addByteArray(list, Converter.intToByte((int) data.data));
			} else if(ObjectType.LONG.equals(data.type)) {
				this.addByteArray(list, Converter.longToByte((long) data.data));
			} else if(ObjectType.FLOAT.equals(data.type)) {
				this.addByteArray(list, Converter.floatToByte((float) data.data));
			} else if(ObjectType.DOUBLE.equals(data.type)) {
				this.addByteArray(list, Converter.doubleToByte((double) data.data));
			} else if(ObjectType.BOOLEAN.equals(data.type)) {
				list.add(Converter.booleanToByte((boolean) data.data));
			} else if(ObjectType.CHARACTER.equals(data.type)) {
				this.addByteArray(list, Converter.charToByte((char) data.data));
			}
		} else if(data.type.isString()) {
			try {
				list.add(ObjectType.STRING.getTypeSpecifier());
				byte[] text = ((String) data.data).getBytes("UTF-8");
				this.addLength(list, text.length);
				this.addByteArray(list, text);
			} catch(Throwable Errors) {
				throw new RuntimeException(Errors);
			}
		} else if(data.type.isBinaryConfigElement()) {
			BinaryConfigElement element = (BinaryConfigElement) data.data;

			if(element instanceof BinaryConfigObject object) {
				list.add(ObjectType.BINARY_CONFIG_OBJECT.getTypeSpecifier());
				List<Byte> buffer = new ArrayList<>();
				Iterator<Entry<String, ObjectData>> iterator = object.map.entrySet().iterator();

				while(iterator.hasNext()) {
					Entry<String, ObjectData> entry = iterator.next();
					byte[] key = entry.getKey().getBytes();
					this.addLength(buffer, key.length);
					this.addByteArray(buffer, key);
					buffer.addAll(this.recursiveByteBuilder(entry.getValue()));
				}

				this.addLength(list, buffer.size());
				list.addAll(buffer);
			} else if(element instanceof BinaryConfigArray array) {
				list.add(ObjectType.BINARY_CONFIG_ARRAY.getTypeSpecifier());
				List<Byte> buffer = new ArrayList<>();

				for(int i = 0; i < array.list.size(); i++) {
					buffer.addAll(this.recursiveByteBuilder(array.list.get(i)));
				}

				this.addLength(list, buffer.size());
				list.addAll(buffer);
			}
		}

		return list;
	}

	private void addLength(List<Byte> array, int length) {
		this.addByteArray(array, Converter.intToByte(length));
	}

	private void addByteArray(List<Byte> list, byte[] array) {
		for(int i = 0; i < array.length; i++) {
			list.add(array[i]);
		}
	}

	private byte[] getByte() {
		return this.byteData;
	}

	static byte[] write(BinaryConfigElement element) {
		return new InternalBinaryConfigWriter(element).getByte();
	}
}
