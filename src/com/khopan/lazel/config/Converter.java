package com.khopan.lazel.config;

public class Converter {
	private Converter() {}

	private static final byte BOOLEAN_TRUE = 0xB6 - 128;
	private static final byte BOOLEAN_FALSE = 0xF2 - 128;

	// Everything in here is Little-Endian

	public static byte[] shortToByte(short data) {
		return new byte[] {
				(byte) ((data >> 8) & 0xFF),
				(byte) (data & 0xFF)
		};
	}

	public static short byteToShort(byte[] data) {
		if(data == null || data.length != 2) {
			return 0;
		}

		return (short) (
				((data[0] & 0xFF) << 8) |
				(data[1] & 0xFF)
				);
	}

	public static byte[] intToByte(int data) {
		return new byte[] {
				(byte) ((data >> 24) & 0xFF),
				(byte) ((data >> 16) & 0xFF),
				(byte) ((data >> 8) & 0xFF),
				(byte) (data & 0xFF)
		};
	}

	public static int byteToInt(byte[] data) {
		if(data == null || data.length != 4) {
			return 0;
		}

		return ((data[0] & 0xFF) << 24) |
				((data[1] & 0xFF) << 16) |
				((data[2] & 0xFF) << 8) |
				(data[3] & 0xFF);
	}

	public static byte[] longToByte(long data) {
		return new byte[] {
				(byte) ((data >> 56) & 0xFF),
				(byte) ((data >> 48) & 0xFF),
				(byte) ((data >> 40) & 0xFF),
				(byte) ((data >> 32) & 0xFF),
				(byte) ((data >> 24) & 0xFF),
				(byte) ((data >> 16) & 0xFF),
				(byte) ((data >> 8) & 0xFF),
				(byte) (data & 0xFF)
		};
	}

	public static long byteToLong(byte[] data) {
		if(data == null || data.length != 8) {
			return 0;
		}

		return (((long) data[0] & 0xFF) << 56) |
				(((long) data[1] & 0xFF) << 48) |
				(((long) data[2] & 0xFF) << 40) |
				(((long) data[3] & 0xFF) << 32) |
				(((long) data[4] & 0xFF) << 24) |
				(((long) data[5] & 0xFF) << 16) |
				(((long) data[6] & 0xFF) << 8) |
				((long) data[7] & 0xFF);
	}

	public static byte[] floatToByte(float data) {
		return Converter.intToByte(Float.floatToIntBits(data));
	}

	public static float byteToFloat(byte[] data) {
		if(data == null || data.length != 4) {
			return 0;
		}

		return Float.intBitsToFloat(Converter.byteToInt(data));
	}

	public static byte[] doubleToByte(double data) {
		return Converter.longToByte(Double.doubleToLongBits(data));
	}

	public static double byteToDouble(byte[] data) {
		if(data == null || data.length != 8) {
			return 0;
		}

		return Double.longBitsToDouble(Converter.byteToLong(data));
	}

	public static byte booleanToByte(boolean data) {
		return data ? Converter.BOOLEAN_TRUE : Converter.BOOLEAN_FALSE;
	}

	public static boolean byteToBoolean(byte data) {
		return data == Converter.BOOLEAN_TRUE;
	}

	public static byte[] charToByte(char data) {
		return Converter.shortToByte((short) data);
	}

	public static char byteToChar(byte[] data) {
		return (char) Converter.byteToShort(data);
	}
}
