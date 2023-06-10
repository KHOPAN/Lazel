package com.khopan.lazel.config;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class BinaryConfigObject extends BinaryConfigElement {
	final Map<String, ObjectData> map;

	public BinaryConfigObject() {
		this.map = new LinkedHashMap<>();
	}

	public void putNull(String key) {
		ObjectData object = new ObjectData();
		object.type = ObjectType.NULL;
		object.data = null;
		this.map.put(key, object);
	}

	public void putByte(String key, byte data) {
		ObjectData object = new ObjectData();
		object.type = ObjectType.BYTE;
		object.data = data;
		this.map.put(key, object);
	}

	public void putShort(String key, short data) {
		ObjectData object = new ObjectData();
		object.type = ObjectType.SHORT;
		object.data = data;
		this.map.put(key, object);
	}

	public void putInt(String key, int data) {
		ObjectData object = new ObjectData();
		object.type = ObjectType.INTEGER;
		object.data = data;
		this.map.put(key, object);
	}

	public void putLong(String key, long data) {
		ObjectData object = new ObjectData();
		object.type = ObjectType.LONG;
		object.data = data;
		this.map.put(key, object);
	}

	public void putFloat(String key, float data) {
		ObjectData object = new ObjectData();
		object.type = ObjectType.FLOAT;
		object.data = data;
		this.map.put(key, object);
	}

	public void putDouble(String key, double data) {
		ObjectData object = new ObjectData();
		object.type = ObjectType.DOUBLE;
		object.data = data;
		this.map.put(key, object);
	}

	public void putBoolean(String key, boolean data) {
		ObjectData object = new ObjectData();
		object.type = ObjectType.BOOLEAN;
		object.data = data;
		this.map.put(key, object);
	}

	public void putChar(String key, char data) {
		ObjectData object = new ObjectData();
		object.type = ObjectType.CHARACTER;
		object.data = data;
		this.map.put(key, object);
	}

	public void putString(String key, String data) {
		if(data == null) {
			this.putNull(key);
			return;
		}

		ObjectData object = new ObjectData();
		object.type = ObjectType.STRING;
		object.data = data;
		this.map.put(key, object);
	}

	public void putObject(String key, BinaryConfigObject data) {
		if(data == null) {
			this.putNull(key);
			return;
		}

		if(this.equals(data)) {
			return;
		}

		ObjectData object = new ObjectData();
		object.type = ObjectType.BINARY_CONFIG_ARRAY;
		object.data = data;
		this.map.put(key, object);
	}

	public void putArray(String key, BinaryConfigArray data) {
		if(data == null) {
			this.putNull(key);
			return;
		}

		ObjectData object = new ObjectData();
		object.type = ObjectType.BINARY_CONFIG_ARRAY;
		object.data = data;
		this.map.put(key, object);
	}

	public Map<String, Object> map() {
		Map<String, Object> map = new LinkedHashMap<>();
		Iterator<Entry<String, ObjectData>> iterator = this.map.entrySet().iterator();

		while(iterator.hasNext()) {
			Entry<String, ObjectData> entry = iterator.next();
			map.put(entry.getKey(), entry.getValue());
		}

		return map;
	}

	public int size() {
		return this.map.size();
	}

	public byte getByte(String key) {
		ObjectData data = this.map.get(key);

		if(data == null || !ObjectType.BYTE.equals(data.type)) {
			throw new IllegalArgumentException("Not a byte");
		} else {
			return (byte) data.data;
		}
	}

	public short getShort(String key) {
		ObjectData data = this.map.get(key);

		if(data == null || !ObjectType.SHORT.equals(data.type)) {
			throw new IllegalArgumentException("Not a short");
		} else {
			return (short) data.data;
		}
	}

	public int getInt(String key) {
		ObjectData data = this.map.get(key);

		if(data == null || !ObjectType.INTEGER.equals(data.type)) {
			throw new IllegalArgumentException("Not an int");
		} else {
			return (int) data.data;
		}
	}

	public long getLong(String key) {
		ObjectData data = this.map.get(key);

		if(data == null || !ObjectType.LONG.equals(data.type)) {
			throw new IllegalArgumentException("Not a long");
		} else {
			return (long) data.data;
		}
	}

	public float getFloat(String key) {
		ObjectData data = this.map.get(key);

		if(data == null || !ObjectType.FLOAT.equals(data.type)) {
			throw new IllegalArgumentException("Not a float");
		} else {
			return (float) data.data;
		}
	}

	public double getDouble(String key) {
		ObjectData data = this.map.get(key);

		if(data == null || !ObjectType.DOUBLE.equals(data.type)) {
			throw new IllegalArgumentException("Not a double");
		} else {
			return (double) data.data;
		}
	}

	public boolean getBoolean(String key) {
		ObjectData data = this.map.get(key);

		if(data == null || !ObjectType.BOOLEAN.equals(data.type)) {
			throw new IllegalArgumentException("Not a double");
		} else {
			return (boolean) data.data;
		}
	}

	public char getChar(String key) {
		ObjectData data = this.map.get(key);

		if(data == null || !ObjectType.CHARACTER.equals(data.type)) {
			throw new IllegalArgumentException("Not a char");
		} else {
			return (char) data.data;
		}
	}

	public String getString(String key) {
		ObjectData data = this.map.get(key);

		if(data == null || ObjectType.NULL.equals(data.type)) {
			return null;
		}

		if(ObjectType.STRING.equals(data.type)) {
			return (String) data.data;
		}

		return null;
	}

	public BinaryConfigObject getObject(String key) {
		ObjectData data = this.map.get(key);

		if(data == null || ObjectType.NULL.equals(data.type)) {
			return null;
		}

		if(ObjectType.BINARY_CONFIG_OBJECT.equals(data.type)) {
			return (BinaryConfigObject) data.data;
		}

		return null;
	}

	public BinaryConfigArray getArray(String key) {
		ObjectData data = this.map.get(key);

		if(data == null || ObjectType.NULL.equals(data.type)) {
			return null;
		}

		if(ObjectType.BINARY_CONFIG_ARRAY.equals(data.type)) {
			return (BinaryConfigArray) data.data;
		}

		return null;
	}

	public Object get(String key) {
		return this.map.get(key).data;
	}

	public void print() {
		this.print(System.out);
	}

	public void print(PrintStream stream) {
		stream.print(this.printData(0));
	}

	String printData(int tab) {
		Iterator<Entry<String, ObjectData>> iterator = this.map.entrySet().iterator();
		String totalLine = "";

		while(iterator.hasNext()) {
			Entry<String, ObjectData> entry = iterator.next();
			String key = entry.getKey();
			ObjectData value = entry.getValue();
			String line = BinaryConfigObject.repeatTab(tab);
			line += '"' + key + "\": ";

			if(value == null || value.type.isNull()) {
				line += "null";
			} else if(value.type.isPrimitive() || value.type.isString()) {
				line += value.data;
			} else if(value.type.isBinaryConfigElement()) {
				BinaryConfigElement configElement = (BinaryConfigElement) value.data;

				if(configElement instanceof BinaryConfigObject object) {
					line += "[Object]:\n";
					line += object.printData(tab + 1);
				} else if(configElement instanceof BinaryConfigArray array) {
					line += "[Array]:\n";
					line += array.printData(tab + 1);
				}
			}

			totalLine += line + '\n';
		}

		return totalLine;
	}

	static String repeatTab(int tab) {
		String buffer = "";

		for(int i = 0; i < tab; i++) {
			buffer += '\t';
		}

		return buffer;
	}

	public String toJson() {
		Iterator<Entry<String, ObjectData>> iterator = this.map.entrySet().iterator();
		int index = 0;
		String result = "{";

		while(iterator.hasNext()) {
			Entry<String, ObjectData> entry = iterator.next();
			result += (index != 0 ? "," : "") + "\"" + entry.getKey() + "\":" + BinaryConfigObject.toValue(entry.getValue());
			index++;
		}

		return result + "}";
	}

	static String toValue(ObjectData data) {
		if(data == null || ObjectType.NULL.equals(data.type)) {
			return "null";
		} else if(ObjectType.BYTE.equals(data.type)) {
			return data.data + "";
		} else if(ObjectType.SHORT.equals(data.type)) {
			return data.data + "";
		} else if(ObjectType.INTEGER.equals(data.type)) {
			return data.data + "";
		} else if(ObjectType.LONG.equals(data.type)) {
			return data.data + "";
		} else if(ObjectType.FLOAT.equals(data.type)) {
			return data.data + "";
		} else if(ObjectType.DOUBLE.equals(data.type)) {
			return data.data + "";
		} else if(ObjectType.BOOLEAN.equals(data.type)) {
			return data.data + "";
		} else if(ObjectType.CHARACTER.equals(data.type)) {
			return "\"" + data.data + "\"";
		} else if(ObjectType.STRING.equals(data.type)) {
			return "\"" + data.data + "\"";
		} else if(ObjectType.BINARY_CONFIG_OBJECT.equals(data.type)) {
			return ((BinaryConfigObject) data.data).toJson();
		} else if(ObjectType.BINARY_CONFIG_ARRAY.equals(data.type)) {
			return ((BinaryConfigArray) data.data).toJson();
		}

		throw new InternalError("Cannot infer the type of 'data'");
	}

	static class ObjectData {
		ObjectType type;
		Object data;
	}
}
