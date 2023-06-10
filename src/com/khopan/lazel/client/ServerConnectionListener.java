package com.khopan.lazel.client;

import com.khopan.lazel.PacketGateway;

@FunctionalInterface
public interface ServerConnectionListener {
	public void serverConnected(PacketGateway gateway);
}
