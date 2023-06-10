package com.khopan.lazel;

@FunctionalInterface
public interface PacketReceiver {
	public void receivePacket(Packet packet);
}
