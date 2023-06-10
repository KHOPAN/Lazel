package com.khopan.lazel.config;

enum ObjectType {
	NULL(0x01),
	BYTE(0x02),
	SHORT(0x03),
	INTEGER(0x04),
	LONG(0x05),
	FLOAT(0x06),
	DOUBLE(0x07),
	BOOLEAN(0x08),
	CHARACTER(0x09),
	STRING(0x0A),
	BINARY_CONFIG_OBJECT(0x0B),
	BINARY_CONFIG_ARRAY(0x0C);

	private final byte typeSpecifier;

	private ObjectType(int typeSpecifier) {
		this.typeSpecifier = (byte) typeSpecifier;
	}

	public byte getTypeSpecifier() {
		return this.typeSpecifier;
	}

	public boolean isPrimitive() {
		return !this.isString() && !this.isBinaryConfigElement() && !this.isNull();
	}

	public boolean isString() {
		return ObjectType.STRING.equals(this);
	}

	public boolean isBinaryConfigElement() {
		return ObjectType.BINARY_CONFIG_OBJECT.equals(this) || ObjectType.BINARY_CONFIG_ARRAY.equals(this);
	}

	public boolean isNull() {
		return ObjectType.NULL.equals(this);
	}

	public static ObjectType infer(byte typeSpecifier) {
		ObjectType[] list = ObjectType.values();

		for(int i = 0; i < list.length; i++) {
			if(list[i].typeSpecifier == typeSpecifier) {
				return list[i];
			}
		}

		return null;
	}
}
