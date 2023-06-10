package com.khopan.lazel.client;

import java.net.Socket;

import com.khopan.lazel.PacketGateway;
import com.khopan.lazel.property.Property;
import com.khopan.lazel.property.SimpleProperty;

public class Client {
	private static int Processor;

	private final Thread reconnectionThread;

	private ServerConnectionListener serverListener;
	private volatile boolean started;
	private volatile boolean connected;
	private boolean portSet;
	private int port;
	private int reconnectDuration;
	private String host;
	private Socket socket;
	private PacketGateway packetGateway;

	public Client() {
		this.started = false;
		this.connected = false;
		this.portSet = false;
		this.reconnectDuration = 1000;
		this.reconnectionThread = new Thread(this :: reconnectionThreadRun);
		this.host = null;
	}

	private void reconnectionThreadRun() {
		while(this.started && !this.connected) {
			this.reconnect();

			try {
				Thread.sleep(this.reconnectDuration);
			} catch(Throwable Errors) {
				throw new InternalError("Error during the reconnection", Errors);
			}
		}

		if(this.connected) {
			this.packetGateway = new PacketGateway(this.socket);
			Thread thread = new Thread(() -> {
				if(this.serverListener != null) {
					this.serverListener.serverConnected(this.packetGateway);
				}
			});

			thread.setPriority(7);
			Client.Processor++;
			thread.setName("Lazel Server Processor #" + Client.Processor);
			thread.start();
		}
	}

	private void reconnect() {
		try {
			this.socket = new Socket(this.host, this.port);
			this.connected = true;
		} catch(Throwable Errors) {
			// Blank statement, server not found, reconnect again
		}
	}

	public void start() {
		if(!this.started) {
			if(!this.portSet) {
				throw new IllegalStateException("Port is not set");
			}

			this.started = true;
			this.reconnectionThread.start();
		} else {
			throw new IllegalStateException("The client is already started");
		}
	}

	public void stop() {
		this.started = false;
	}

	public Property<Integer, Client> port() {
		return new SimpleProperty<Integer, Client>(() -> this.port, port -> {
			this.port = port;
			this.portSet = true;
		}, this).nullable().whenNull(0);
	}

	public Property<String, Client> host() {
		return new SimpleProperty<String, Client>(() -> this.host, host -> this.host = host, this).nullable();
	}

	public Property<ServerConnectionListener, Client> serverConnectionListener() {
		return new SimpleProperty<ServerConnectionListener, Client>(() -> this.serverListener, serverListener -> this.serverListener = serverListener, this).nullable();
	}
}
