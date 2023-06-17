package com.khopan.lazel.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import com.khopan.lazel.ConnectionMessage;
import com.khopan.lazel.ConnectionResetListener;
import com.khopan.lazel.PacketListener;
import com.khopan.lazel.config.Converter;
import com.khopan.lazel.packet.Packet;
import com.khopan.lazel.property.Property;
import com.khopan.lazel.property.SimpleProperty;

public class ClientProcessor {
	private static int Receiver;
	private static int Processor;

	private final int identifier;
	private final Server server;
	private final Socket socket;
	private final InputStream inputStream;
	private final OutputStream outputStream;
	private final List<Packet> packetQueue;
	private boolean packetAvailable;
	private PacketListener packetListener;
	private ConnectionResetListener connectionResetListener;

	ClientProcessor(Socket socket, Server server, int processorIdentifier) {
		try {
			this.identifier = processorIdentifier;
			this.server = server;
			this.socket = socket;
			this.inputStream = this.socket.getInputStream();
			this.outputStream = this.socket.getOutputStream();
			this.packetQueue = new ArrayList<>();
			Thread receiveMessageThread = new Thread(this :: receiveMessageThread);
			receiveMessageThread.setPriority(6);
			ClientProcessor.Receiver++;
			receiveMessageThread.setName("Lazel Server Packet Receiver #" + ClientProcessor.Receiver);
			receiveMessageThread.start();
		} catch(Throwable Errors) {
			throw new InternalError("Error while initializing ClientProcessor", Errors);
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

					if(messageDirection == ConnectionMessage.TYPE_CLIENT_TO_SERVER) {
						byte message = (byte) stream.read();

						if(message == ConnectionMessage.MESSAGE_REQUEST_CONNECTION) {
							ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
							outputStream.write(Converter.intToByte(3));
							outputStream.write(ConnectionMessage.TYPE_MESSAGE_SPECIAL);
							outputStream.write(ConnectionMessage.TYPE_SERVER_TO_CLIENT);
							outputStream.write(ConnectionMessage.MESSAGE_CONNECTED);
							this.outputStream.write(outputStream.toByteArray());
						} else if(message == ConnectionMessage.MESSAGE_CONNECTED) {
							this.established();
						}
					} else if(messageDirection == ConnectionMessage.TYPE_SERVER_TO_CLIENT) {
						throw new InternalError("Server: Received a message from server");
					} else {
						throw new IllegalArgumentException("Invalid message direction 0x" + String.format("%02x", messageDirection).toUpperCase());
					}
				} else {
					throw new IllegalArgumentException("Invalid message type 0x" + String.format("%02x", messageType).toUpperCase());
				}
			} catch(Throwable Errors) {
				if(Errors instanceof SocketException socket && "Connection reset".equals(socket.getMessage()) && this.connectionResetListener != null) {
					this.connectionResetListener.connectionReset();
				}

				throw new InternalError("Error while receiving packets", Errors);
			}
		}
	}

	private void established() {
		Thread thread = new Thread(() -> {
			try {
				Thread.sleep(500);
				this.packetAvailable = true;

				if(this.server.connectionListener != null) {
					this.server.connectionListener.clientConnected(this);
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
		ClientProcessor.Processor++;
		thread.setName("Lazel Server Connection Processor #" + ClientProcessor.Processor);
		thread.start();
	}

	public int getIdentifier() {
		return this.identifier;
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

	public Property<PacketListener, ClientProcessor> packetListener() {
		return new SimpleProperty<PacketListener, ClientProcessor>(() -> this.packetListener, packetListener -> this.packetListener = packetListener, this).nullable();
	}

	public Property<ConnectionResetListener, ClientProcessor> connectionResetListener() {
		return new SimpleProperty<ConnectionResetListener, ClientProcessor>(() -> this.connectionResetListener, connectionResetListener -> this.connectionResetListener = connectionResetListener, this).nullable();
	}
}
