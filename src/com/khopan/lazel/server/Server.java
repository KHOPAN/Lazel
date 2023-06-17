package com.khopan.lazel.server;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import com.khopan.lazel.property.Property;
import com.khopan.lazel.property.SimpleProperty;

public class Server {
	static int Processor;

	private final Thread requestThread;
	private final List<ClientProcessor> processors;

	ClientConnectionListener connectionListener;
	private ServerSocket socket;
	private boolean started;
	private boolean portSet;
	private int port;
	private int backlog;
	private InetAddress address;

	public Server() {
		this.requestThread = new Thread(this :: requestThreadRun);
		this.processors = new ArrayList<>();
		this.started = false;
		this.portSet = false;
		this.backlog = 50;
		this.address = null;
	}

	private void requestThreadRun() {
		while(true) {
			try {
				Socket socket = this.socket.accept();
				ClientProcessor processor = new ClientProcessor(socket, this, this.processors.size());
				this.processors.add(processor);
				/*PacketGateways gateway = new PacketGateways(socket, true, null);
				gateway.onEstablishedConnection = () -> {
					Thread thread = new Thread(() -> {
						this.clientList.add(gateway);

						if(this.clientListener != null) {
							this.clientListener.clientConnected(gateway);
						}
					});

					thread.setPriority(7);
					Server.Processor++;
					thread.setName("Lazel Client Request Processor #" + Server.Processor);
					thread.start();
				};*/
			} catch(SocketException socket) {
				if(this.started) {
					throw new InternalError("Error while processing client request", socket);
				} else {
					break;
				}
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
			this.requestThread.interrupt();
			this.started = false;
		} catch(Throwable Errors) {
			throw new InternalError("Error while closing the server", Errors);
		}
	}

	public ClientProcessor processor(int identifier) {
		return this.processors.get(identifier);
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

	public Property<ClientConnectionListener, Server> connectionListener() {
		return new SimpleProperty<ClientConnectionListener, Server>(() -> this.connectionListener, connectionListener -> this.connectionListener = connectionListener, this).nullable();
	}
}
