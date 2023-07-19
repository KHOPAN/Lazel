package com.khopan.lazel;

public interface ConnectionMessage {
	public static final byte TYPE_MESSAGE_NORMAL = 0x01;
	public static final byte TYPE_MESSAGE_SPECIAL = 0x02;

	public static final byte TYPE_CLIENT_TO_SERVER = 0x03;
	public static final byte TYPE_SERVER_TO_CLIENT = 0x04;

	public static final byte MESSAGE_REQUEST_CONNECTION = 0x05;
	public static final byte MESSAGE_CONNECTED = 0x06;
	public static final byte MESSAGE_DISCONNECT = 0x07;
}
