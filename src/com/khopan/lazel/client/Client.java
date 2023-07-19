package com.khopan.lazel.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import com.khopan.lazel.ConnectionListener;
import com.khopan.lazel.ConnectionMessage;
import com.khopan.lazel.DisconnectionListener;
import com.khopan.lazel.PacketListener;
import com.khopan.lazel.config.Converter;
import com.khopan.lazel.packet.Packet;
import com.khopan.lazel.property.Property;
import com.khopan.lazel.property.SimpleProperty;

public class Client {
	private static int Connector;
	private static int Receiver;
	private static int Processor;

	private final List<Packet> packetQueue;

	private Thread reconnectionThread;
	private ConnectionListener connectionListener;
	private DisconnectionListener disconnectionListener;
	private PacketListener packetListener;
	private volatile boolean started;
	private volatile boolean connected;
	private volatile boolean established;
	private volatile boolean packetAvailable;
	private boolean portSet;
	private int port;
	private String host;
	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;

	public Client() {
		this.reconnectionThread = new Thread(this :: reconnectionThread);
		this.packetQueue = new ArrayList<>();
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
		while(this.connected) {
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
						this.receivePacket(stream.readAllBytes());
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
				if(Errors instanceof SocketException socket) {
					String message = socket.getMessage();

					if("Connection reset".equals(message) || "Socket closed".equals(message)) {
						this.connected = false;

						if(this.disconnectionListener != null) {
							this.disconnectionListener.disconnected();
						}

						try {
							this.inputStream.close();
							this.outputStream.close();
							this.socket.close();
						} catch(Throwable closingErrors) {
							throw new InternalError("Error while closing streams", closingErrors);
						}
					} else {
						throw new InternalError("Error while receiving packets", Errors);
					}
				} else {
					throw new InternalError("Error while receiving packets", Errors);
				}
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
				byte[] className = packet.getClass().getName().getBytes();
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				stream.write(Converter.intToByte(byteArray.length + className.length + 5));
				stream.write(ConnectionMessage.TYPE_MESSAGE_NORMAL);
				stream.write(Converter.intToByte(className.length));
				stream.write(className);
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

	private void receivePacket(byte[] data) {
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(data);
			int length = Converter.byteToInt(stream.readNBytes(4));
			String className = new String(stream.readNBytes(length));
			data = stream.readAllBytes();
			Class<?> packetClass = Class.forName(className);
			Constructor<?> constructor = packetClass.getConstructor(byte[].class);
			this.packetListener.onPacketReceived((Packet) constructor.newInstance(data));
		} catch(Throwable Errors) {
			throw new InternalError("Error while processing packet", Errors);
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

	public boolean isConnected() {
		return this.connected;
	}

	public void reconnect() {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			stream.write(Converter.intToByte(3));
			stream.write(ConnectionMessage.TYPE_MESSAGE_SPECIAL);
			stream.write(ConnectionMessage.TYPE_CLIENT_TO_SERVER);
			stream.write(ConnectionMessage.MESSAGE_DISCONNECT);
			this.outputStream.write(stream.toByteArray());
			this.started = false;
			this.connected = false;
			this.established = false;
			this.packetAvailable = false;
			this.reconnectionThread = new Thread(this :: reconnectionThread);
			this.inputStream.close();
			this.outputStream.close();
			this.socket.close();
			this.start();
		} catch(Throwable closingErrors) {
			throw new InternalError("Error while reconnecting to the server", closingErrors);
		}

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

	public Property<DisconnectionListener, Client> disconnectionListener() {
		return new SimpleProperty<DisconnectionListener, Client>(() -> this.disconnectionListener, disconnectionListener -> this.disconnectionListener = disconnectionListener, this).nullable();
	}

	public Property<PacketListener, Client> packetListener() {
		return new SimpleProperty<PacketListener, Client>(() -> this.packetListener, packetListener -> this.packetListener = packetListener, this).nullable();
	}
}
