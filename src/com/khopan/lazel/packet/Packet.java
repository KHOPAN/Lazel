package com.khopan.lazel.packet;

public class Packet {
	protected byte[] byteArray;

	public Packet(byte[] byteArray) {
		this.byteArray = byteArray;
	}

	public byte[] getByteArray() {
		return this.byteArray;
	}

	@SuppressWarnings("unchecked")
	public <T extends Packet> T getPacket(Class<T> packetType) {
		if(packetType == Packet.class) {
			return (T) this;
		}

		try {
			return (T) packetType.getConstructor(byte[].class).newInstance(this.byteArray);
		} catch(Throwable Errors) {
			throw new InternalError("Error while casting packet type", Errors);
		}
	}
}
