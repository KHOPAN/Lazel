package com.khopan.lazel.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.khopan.lazel.ConnectionListener;
import com.khopan.lazel.ConnectionMessage;
import com.khopan.lazel.PacketListener;
import com.khopan.lazel.config.Converter;
import com.khopan.lazel.packet.Packet;
import com.khopan.lazel.property.Property;
import com.khopan.lazel.property.SimpleProperty;

public class Client {
	private static int Connector;
	private static int Receiver;
	private static int Processor;

	private final Thread reconnectionThread;
	private final List<Packet> packetQueue;

	private ConnectionListener connectionListener;
	private PacketListener packetListener;
	private volatile boolean started;
	private volatile boolean connected;
	private volatile boolean established;
	private boolean portSet;
	private int port;
	private String host;
	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	private boolean packetAvailable;

	public Client() {
		this.reconnectionThread = new Thread(this :: reconnectionThread);
		this.packetQueue = new ArrayList<>();
		this.started = false;
		this.connected = false;
		this.portSet = false;
		this.host = null;
	}

	private void reconnectionThread() {
		while(this.started && !this.connected) {
			try {
				this.socket = new Socket(this.host, this.port);
				this.connected = true;
				this.inputStream = this.socket.getInputStream();
				this.outputStream = this.socket.getOutputStream();
			} catch(Throwable Errors) {
				// Blank statement, server not found, reconnect again
			}
		}

		if(this.connected) {
			Thread receiveMessageThread = new Thread(this :: receiveMessageThread);
			receiveMessageThread.setPriority(6);
			Client.Receiver++;
			receiveMessageThread.setName("Lazel Client Packet Receiver #" + Client.Receiver);
			receiveMessageThread.start();
			Thread serverConnectorThread = new Thread(this :: serverConnectorThread);
			serverConnectorThread.setPriority(5);
			Client.Connector++;
			serverConnectorThread.setName("Lazel Client Server Connector #" + Client.Connector);
			serverConnectorThread.start();
		}
	}

	private void receiveMessageThread() {
		while(true) {
			try {
				byte[] lengthByte = this.inputStream.readNBytes(4);

				if(lengthByte.length != 4) {
					continue;
				}

				int length = Converter.byteToInt(lengthByte);
				byte[] data = this.inputStream.readNBytes(length);
				ByteArrayInputStream stream = new ByteArrayInputStream(data);
				byte messageType = (byte) stream.read();

				if(messageType == ConnectionMessage.TYPE_MESSAGE_NORMAL) {
					if(this.packetListener != null) {
						this.packetListener.onPacketReceived(new Packet(stream.readAllBytes()));
					}
				} else if(messageType == ConnectionMessage.TYPE_MESSAGE_SPECIAL) {
					byte messageDirection = (byte) stream.read();

					if(messageDirection == ConnectionMessage.TYPE_SERVER_TO_CLIENT) {
						byte message = (byte) stream.read();

						if(message == ConnectionMessage.MESSAGE_CONNECTED) {
							ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
							outputStream.write(Converter.intToByte(3));
							outputStream.write(ConnectionMessage.TYPE_MESSAGE_SPECIAL);
							outputStream.write(ConnectionMessage.TYPE_CLIENT_TO_SERVER);
							outputStream.write(ConnectionMessage.MESSAGE_CONNECTED);
							this.outputStream.write(outputStream.toByteArray());
							this.established();
						}
					} else if(messageDirection == ConnectionMessage.TYPE_CLIENT_TO_SERVER) {
						throw new InternalError("Client: Received a message from client");
					} else {
						throw new IllegalArgumentException("Invalid message direction 0x" + String.format("%02x", messageDirection).toUpperCase());
					}
				} else {
					throw new IllegalArgumentException("Invalid message type 0x" + String.format("%02x", messageType).toUpperCase());
				}
			} catch(Throwable Errors) {
				throw new InternalError("Error while receiving packets", Errors);
			}
		}
	}

	private void established() {
		this.established = true;

		Thread thread = new Thread(() -> {
			try {
				Thread.sleep(500);
				this.packetAvailable = true;

				if(this.connectionListener != null) {
					this.connectionListener.connected();
				}

				Thread.sleep(100);

				for(int i = 0; i < this.packetQueue.size(); i++) {
					this.sendPacket(this.packetQueue.get(i));
				}

				this.packetQueue.clear();
			} catch(Throwable Errors) {
				throw new InternalError("Error while establishing the connection", Errors);
			}
		});

		thread.setPriority(6);
		Client.Processor++;
		thread.setName("Lazel Client Connection Processor #" + Client.Processor);
		thread.start();
	}

	private void serverConnectorThread() {
		while(!this.established) {
			try {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				stream.write(Converter.intToByte(3));
				stream.write(ConnectionMessage.TYPE_MESSAGE_SPECIAL);
				stream.write(ConnectionMessage.TYPE_CLIENT_TO_SERVER);
				stream.write(ConnectionMessage.MESSAGE_REQUEST_CONNECTION);
				this.outputStream.write(stream.toByteArray());
				Thread.sleep(1000);
			} catch(Throwable Errors) {
				throw new InternalError("Error while trying to establish a connection", Errors);
			}
		}
	}

	public void sendPacket(Packet packet) {
		if(packet == null) {
			throw new NullPointerException("Packet cannot be null");
		}

		try {
			if(this.packetAvailable) {
				byte[] byteArray = packet.getByteArray();
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				stream.write(Converter.intToByte(byteArray.length + 1));
				stream.write(ConnectionMessage.TYPE_MESSAGE_NORMAL);
				stream.write(byteArray);
				byte[] data = stream.toByteArray();
				this.outputStream.write(data);
			} else {
				this.packetQueue.add(packet);
			}
		} catch(Throwable Errors) {
			throw new InternalError("Error while sending a packet", Errors);
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

	public Property<ConnectionListener, Client> connectionListener() {
		return new SimpleProperty<ConnectionListener, Client>(() -> this.connectionListener, connectionListener -> this.connectionListener = connectionListener, this).nullable();
	}

	public Property<PacketListener, Client> packetListener() {
		return new SimpleProperty<PacketListener, Client>(() -> this.packetListener, packetListener -> this.packetListener = packetListener, this).nullable();
	}
}
