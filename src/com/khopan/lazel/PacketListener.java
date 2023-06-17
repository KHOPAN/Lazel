package com.khopan.lazel;

import com.khopan.lazel.packet.Packet;

@FunctionalInterface
public interface PacketListener {
	public void onPacketReceived(Packet packet);
}
