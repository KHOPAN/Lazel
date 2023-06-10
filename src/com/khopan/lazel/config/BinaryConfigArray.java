package com.khopan.lazel.config;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.khopan.lazel.config.BinaryConfigObject.ObjectData;

public class BinaryConfigArray extends BinaryConfigElement {
	final List<ObjectData> list;

	public BinaryConfigArray() {
		this.list = new ArrayList<>();
	}

	public void addNull() {
		ObjectData object = new ObjectData();
		object.type = ObjectType.NULL;
		this.list.add(object);
	}

	public void addByte(byte data) {
		ObjectData object = new ObjectData();
		object.type = ObjectType.BYTE;
		object.data = data;
		this.list.add(object);
	}

	public void addShort(short data) {
		ObjectData object = new ObjectData();
		object.type = ObjectType.SHORT;
		object.data = data;
		this.list.add(object);
	}

	public void addInt(int data) {
		ObjectData object = new ObjectData();
		object.type = ObjectType.INTEGER;
		object.data = data;
		this.list.add(object);
	}

	public void addLong(long data) {
		ObjectData object = new ObjectData();
		object.type = ObjectType.LONG;
		object.data = data;
		this.list.add(object);
	}

	public void addFloat(float data) {
		ObjectData object = new ObjectData();
		object.type = ObjectType.FLOAT;
		object.data = data;
		this.list.add(object);
	}

	public void addDouble(double data) {
		ObjectData object = new ObjectData();
		object.type = ObjectType.DOUBLE;
		object.data = data;
		this.list.add(object);
	}

	public void addBoolean(boolean data) {
		ObjectData object = new ObjectData();
		object.type = ObjectType.BOOLEAN;
		object.data = data;
		this.list.add(object);
	}

	public void addChar(char data) {
		ObjectData object = new ObjectData();
		object.type = ObjectType.CHARACTER;
		object.data = data;
		this.list.add(object);
	}

	public void addString(String data) {
		if(data == null) {
			this.addNull();
			return;
		}

		ObjectData object = new ObjectData();
		object.type = ObjectType.STRING;
		object.data = data;
		this.list.add(object);
	}

	public void addObject(BinaryConfigObject data) {
		if(data == null) {
			this.addNull();
			return;
		}

		ObjectData object = new ObjectData();
		object.type = ObjectType.BINARY_CONFIG_OBJECT;
		object.data = data;
		this.list.add(object);
	}

	public void addArray(BinaryConfigArray data) {
		if(data == null) {
			this.addNull();
			return;
		}

		if(this.equals(data)) {
			return;
		}

		ObjectData object = new ObjectData();
		object.type = ObjectType.BINARY_CONFIG_ARRAY;
		object.data = data;
		this.list.add(object);
	}

	public List<Object> list() {
		List<Object> list = new ArrayList<>();

		for(int i = 0; i < this.list.size(); i++) {
			list.add(this.list.get(i).data);
		}

		return list;
	}

	public int size() {
		return this.list.size();
	}

	public byte getByte(int index) {
		ObjectData data = this.list.get(index);

		if(ObjectType.BYTE.equals(data.type)) {
			return (byte) data.data;
		} else {
			throw new IllegalArgumentException("Not a byte");
		}
	}

	public short getShort(int index) {
		ObjectData data = this.list.get(index);

		if(ObjectType.SHORT.equals(data.type)) {
			return (short) data.data;
		} else {
			throw new IllegalArgumentException("Not a short");
		}
	}

	public int getInt(int index) {
		ObjectData data = this.list.get(index);

		if(ObjectType.INTEGER.equals(data.type)) {
			return (int) data.data;
		} else {
			throw new IllegalArgumentException("Not an int");
		}
	}

	public long getLong(int index) {
		ObjectData data = this.list.get(index);

		if(ObjectType.LONG.equals(data.type)) {
			return (long) data.data;
		} else {
			throw new IllegalArgumentException("Not a long");
		}
	}

	public float getFloat(int index) {
		ObjectData data = this.list.get(index);

		if(ObjectType.FLOAT.equals(data.type)) {
			return (float) data.data;
		} else {
			throw new IllegalArgumentException("Not a float");
		}
	}

	public double getDouble(int index) {
		ObjectData data = this.list.get(index);

		if(ObjectType.DOUBLE.equals(data.type)) {
			return (double) data.data;
		} else {
			throw new IllegalArgumentException("Not a double");
		}
	}

	public boolean getBoolean(int index) {
		ObjectData data = this.list.get(index);

		if(ObjectType.BOOLEAN.equals(data.type)) {
			return (boolean) data.data;
		} else {
			throw new IllegalArgumentException("Not a boolean");
		}
	}

	public char getChar(int index) {
		ObjectData data = this.list.get(index);

		if(ObjectType.CHARACTER.equals(data.type)) {
			return (char) data.data;
		} else {
			throw new IllegalArgumentException("Not a char");
		}
	}

	public String getString(int index) {
		ObjectData data = this.list.get(index);

		if(data == null || ObjectType.NULL.equals(data.type)) {
			return null;
		}

		if(ObjectType.STRING.equals(data.type)) {
			return (String) data.data;
		} else {
			throw new IllegalArgumentException("Not a string");
		}
	}

	public BinaryConfigObject getObject(int index) {
		ObjectData data = this.list.get(index);

		if(data == null || ObjectType.NULL.equals(data.type)) {
			return null;
		}

		if(ObjectType.BINARY_CONFIG_OBJECT.equals(data.type)) {
			return (BinaryConfigObject) data.data;
		} else {
			throw new IllegalArgumentException("Not a BinaryConfigObject");
		}
	}

	public BinaryConfigArray getArray(int index) {
		ObjectData data = this.list.get(index);

		if(data == null || ObjectType.NULL.equals(data.type)) {
			return null;
		}

		if(ObjectType.BINARY_CONFIG_ARRAY.equals(data.type)) {
			return (BinaryConfigArray) data.data;
		} else {
			throw new IllegalArgumentException("Not a BinaryConfigArray");
		}
	}

	public Object get(int index) {
		return this.list.get(index).data;
	}

	public void print() {
		this.print(System.out);
	}

	public void print(PrintStream stream) {
		stream.print(this.printData(0));
	}

	String printData(int tab) {
		String totalLine = "";

		for(int i = 0; i < this.list.size(); i++) {
			ObjectData data = this.list.get(i);
			String line = BinaryConfigObject.repeatTab(tab);

			if(data.type.isPrimitive() || data.type.isString()) {
				line += data.data;
			} else if(data.type.isNull()) {
				line += "null";
			} else if(data.type.isBinaryConfigElement()) {
				BinaryConfigElement configElement = (BinaryConfigElement) data.data;

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

	public String toJson() {
		String result = "[";

		for(int i = 0; i < this.list.size(); i++) {
			result += (i != 0 ? "," : "") + BinaryConfigObject.toValue(this.list.get(i));
		}

		return result + "]";
	}
}
