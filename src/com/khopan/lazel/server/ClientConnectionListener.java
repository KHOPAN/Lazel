package com.khopan.lazel.server;

@FunctionalInterface
public interface ClientConnectionListener {
	public void clientConnected(ClientProcessor processor);
}
