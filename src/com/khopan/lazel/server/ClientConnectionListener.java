package com.khopan.lazel.server;

import com.khopan.lazel.PacketGateway;

@FunctionalInterface
public interface ClientConnectionListener {
	public void clientConnected(PacketGateway gateway);
}
