package com.khopan.lazel.server;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.khopan.lazel.PacketGateway;
import com.khopan.lazel.property.Property;
import com.khopan.lazel.property.SimpleProperty;

public class Server {
	private static int Processor;

	public final List<PacketGateway> clientList;

	private final Thread requestThread;

	private ClientConnectionListener clientListener;
	private ServerSocket socket;
	private boolean started;
	private boolean portSet;
	private int port;
	private int backlog;
	private InetAddress address;

	public Server() {
		this.started = false;
		this.portSet = false;
		this.clientList = new ArrayList<>();
		this.requestThread = new Thread(this :: requestThreadRun);
		this.backlog = 50;
		this.address = null;
	}

	private void requestThreadRun() {
		while(true) {
			try {
				Socket socket = this.socket.accept();
				Thread thread = new Thread(() -> {
					PacketGateway gateway = new PacketGateway(socket);
					this.clientList.add(gateway);

					if(this.clientListener != null) {
						this.clientListener.clientConnected(gateway);
					}
				});

				thread.setPriority(7);
				Server.Processor++;
				thread.setName("Lazel Client Request Processor #" + Server.Processor);
				thread.start();
			} catch(Throwable Errors) {
				throw new InternalError("Error while processing client request", Errors);
			}
		}
	}

	public void start() {
		if(!this.started) {
			if(!this.portSet) {
				throw new IllegalStateException("Port is not set");
			}

			this.started = true;

			try {
				this.socket = new ServerSocket(this.port, this.backlog, this.address);
				this.requestThread.start();
			} catch(Throwable Errors) {
				throw new InternalError("Exception during the server initialization", Errors);
			}
		} else {
			throw new IllegalStateException("The server is already started");
		}
	}

	public void stop() {
		try {
			this.socket.close();
		} catch(Throwable Errors) {
			throw new InternalError("Error while closing the server", Errors);
		}
	}

	public Property<Integer, Server> port() {
		return new SimpleProperty<Integer, Server>(() -> this.port, port -> {
			this.port = port;
			this.portSet = true;
		}, this).nullable().whenNull(0);
	}

	public Property<Integer, Server> backlog() {
		return new SimpleProperty<Integer, Server>(() -> this.backlog, backlog -> this.backlog = backlog, this).nullable().whenNull(50);
	}

	public Property<InetAddress, Server> address() {
		return new SimpleProperty<InetAddress, Server>(() -> this.address, address -> this.address = address, this).nullable();
	}

	public Property<ClientConnectionListener, Server> clientConnectionListener() {
		return new SimpleProperty<ClientConnectionListener, Server>(() -> this.clientListener, clientListener -> this.clientListener = clientListener, this).nullable();
	}
}
