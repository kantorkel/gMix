/*
 * gMix open source project - https://svs.informatik.uni-hamburg.de/gmix/
 * Copyright (C) 2011  Karl-Peter Fuchs
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package plugIns.layer1network.cascade_TCP_v0_001;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import framework.core.controller.SubImplementation;
import framework.core.message.MixMessage;
import framework.core.message.Reply;
import framework.core.message.Request;
import framework.core.userDatabase.User;
import framework.core.util.Util;


public class ClientHandler_TCP_RR_sync extends SubImplementation {
	//TODO: add timeout for inactive users
	
	private int port; 
	private InetAddress bindAddress;
	private int backlog;
	private ServerSocket serverSocket;
	private int maxRequestLength;
	//private int maxReplyLength; // TODO
	private Vector<ChannelData> channels;
	private Vector<ChannelData> newConnections;
	private int expectedConnections;
	private int requestBufferSize;
	private int replyBufferSize;
	private AcceptorThread acceptorThread;
	private RequestThread requestThread;
	private ReplyThread replyThread;
	//private int queueBlockSize;
	
	
	@Override
	public void constructor() {
		this.bindAddress = settings.getPropertyAsInetAddress("GLOBAL_MIX_BIND_ADDRESS");
		this.port = settings.getPropertyAsInt("GLOBAL_MIX_BIND_PORT");
		this.backlog = settings.getPropertyAsInt("BACKLOG");
		this.maxRequestLength = settings.getPropertyAsInt("MAX_REQUEST_LENGTH");
		//this.maxReplyLength = settings.getPropertyAsInt("MAX_REPLY_LENGTH");
		this.requestBufferSize = settings.getPropertyAsInt("REQUEST_BUFFER_SIZE");
		this.replyBufferSize = settings.getPropertyAsInt("REPLY_BUFFER_SIZE");
		this.expectedConnections = anonNode.EXPECTED_NUMBER_OF_USERS;
		//this.queueBlockSize = settings.getPropertyAsInt("QUEUE_BLOCK_SIZE");
		this.channels = new Vector<ChannelData>(expectedConnections);
		this.newConnections = new Vector<ChannelData>(100);
	}
	

	@Override
	public void initialize() {
		
	}
	

	@Override
	public void begin() {
		try {
			this.serverSocket = new ServerSocket(port, backlog, bindAddress);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("could not open ServerSocket"); 
		}
		System.out.println(anonNode +" listening on " +bindAddress +":" +port);
		this.acceptorThread = new AcceptorThread();
		this.requestThread = new RequestThread();
		if (anonNode.IS_DUPLEX)
			this.replyThread = new ReplyThread();
		this.acceptorThread.start();
		this.requestThread.start();
		if (anonNode.IS_DUPLEX)
			this.replyThread.start();
	}

	
	private class AcceptorThread extends Thread {
		
		@Override
		public void run() {
			new Thread(// TODO: remove
					new Runnable() {
						public void run() {
							while (true) {
								System.out.println("free in ioh-queue: " +anonNode.getRequestInputQueue().size()); 
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
				).start(); 
			int counter = 0;
			while (true) {
				try {
					Socket client = serverSocket.accept();
					if (++counter%100 == 0)
						System.out.println(counter +" connections"); 
					ChannelData channelData = new ChannelData();
					channelData.user = userDatabase.generateUser();
					channelData.user.channeldata = channelData;
					userDatabase.addUser(channelData.user);
					channelData.socket = client;
					channelData.inputStream = new BufferedInputStream(client.getInputStream(), requestBufferSize);
					if (anonNode.IS_DUPLEX)
						channelData.outputStream = new BufferedOutputStream(client.getOutputStream(), replyBufferSize);
					synchronized (acceptorThread) {
						newConnections.add(channelData);
					}
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
			}
		}
		
	}
	
	
	private synchronized void dropChannel(ChannelData ch) {
		channels.remove(ch);
		try {
			ch.inputStream.close();
			ch.outputStream.close();
			ch.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private class RequestThread extends Thread {
		
		@Override
		public void run() {
			int maxReadsPerChannelInARow = settings.getPropertyAsInt("MAX_READS_IN_A_ROW");
			int maxMessageBlockSize = settings.getPropertyAsInt("MAX_MESSAGE_BLOCK_SIZE");
			
			while (true) {
				synchronized (acceptorThread) { // handle new connections
					if (newConnections.size() != 0) {
						channels.addAll(newConnections);
						newConnections = new Vector<ChannelData>(50); // TODO 50?
					}	
				}
				Vector<Vector<Request>> newRequests = null;
				Vector<Request> newRequestsForCurrentChannel = null;
				for (ChannelData ch:channels) {// try to read data from existing channels
					try {
						for (int i=0; i<maxReadsPerChannelInARow; i++) {
							if (ch.requestLength == ChannelData.NOT_SET) {
								if (ch.inputStream.available() >= 4) {
									byte[] len = new byte[4];
									int read = ch.inputStream.read(len);
									assert read == 4; // should not be different due to buffered stream; check anyways...
									ch.requestLength = Util.byteArrayToInt(len);
									if (ch.requestLength > maxRequestLength) {
										System.err.println("warning: user " +ch.user +" sent a too large message");
									}
								} else {
									break;
								}
							}
							if (ch.requestLength != ChannelData.NOT_SET) { // length header already read
								if (ch.inputStream.available() >= ch.requestLength) {
									byte[] msg = new byte[ch.requestLength];
									int read = ch.inputStream.read(msg);
									assert read == ch.requestLength; // should not be different due to buffered stream; check anyways...
									ch.requestLength = ChannelData.NOT_SET;
									Request r = MixMessage.getInstanceRequest(msg, ch.user);
									if (newRequestsForCurrentChannel == null)
										newRequestsForCurrentChannel = new Vector<Request>(maxReadsPerChannelInARow);
									newRequestsForCurrentChannel.add(r);
								} else {
									break;
								}
							}
						}
						if (newRequestsForCurrentChannel != null) {
							if (newRequests == null)
								newRequests = new Vector<Vector<Request>>(maxMessageBlockSize);
							newRequests.add(newRequestsForCurrentChannel);
							newRequestsForCurrentChannel = null;
						}	
					} catch (IOException e) {
						e.printStackTrace();
						dropChannel(ch);
						continue;
					}
					
					if (newRequests != null && newRequests.size() >= maxMessageBlockSize) {
						for (Vector<Request> requests:newRequests)
							anonNode.putInRequestInputQueue(requests.toArray(new Request[0])); // might block
						newRequests = new Vector<Vector<Request>>(maxMessageBlockSize);
					}
				}
				if (newRequests == null) {
					try {Thread.sleep(1);} catch (InterruptedException e) {continue;} // TODO: 1 ms adequate?
					continue;
				}
				
				for (Vector<Request> requests:newRequests)
					anonNode.putInRequestInputQueue(requests.toArray(new Request[0])); // might block
			}
			
			/*try {
				
				
			} catch
							
							if (ch.inputStream.available() >= 4) {
								
								
								
							if (userData.receivedData.position() == 0)
								userData.receivedData.limit(4);
							
							if (userData.socketChannel.read(userData.receivedData) == -1) // read data
								throw new IOException("warning: lost connection to user " +userData.getOwner());
							
							if (userData.receivedData.hasRemaining()) {
								return null;
							} else {
								userData.receivedData.flip();
								byte[] lengthHeader = new byte[4];
								userData.receivedData.get(lengthHeader);
								int messageLength = Util.byteArrayToInt(lengthHeader);
								if (messageLength > maxRequestLength)
									throw new IOException("warning: user " +userData.getOwner() +" sent a too large message");
								userData.receivedData.clear();
								userData.receivedData = ByteBuffer.allocate(messageLength);
								userData.requestLengthHeaderRead = true;
							}
							
						} 
						
						
						if (ch.inputStream.available()>)
					}
					
					
					int messageLength = Util.forceReadInt(inputStream);
					if (messageLength > maxRequestLength)
						throw new IOException("warning: user " +user +" sent a too large message");
					Request request = MixMessage.getInstanceRequest(Util.forceRead(inputStream, messageLength), user, settings);
					inputOutputHandlerInternal.addUnprocessedRequests(new Request[]{request}); // might block
				}
			} catch (IOException e) {
				System.err.println("warning: connection to " +user +" lost");
				e.printStackTrace();
			}*/
		}
	}
	
	
	private class ReplyThread extends Thread {

		@Override
		public void run() {
			while (true) {
				Reply[] replies = anonNode.getFromReplyOutputQueue();
				for (Reply reply: replies) {
					try {
						//if (reply.getOwner().channeldata == null)
						//	reply.getOwner().channeldata = 
						assert reply != null;
						assert reply.getOwner() != null;
						assert reply.getOwner().channeldata != null;
						assert reply.getOwner().channeldata.outputStream != null;
						
						reply.getOwner().channeldata.outputStream.write(Util.intToByteArray(reply.getByteMessage().length));
						reply.getOwner().channeldata.outputStream.write(reply.getByteMessage().length);
						reply.getOwner().channeldata.outputStream.flush();
					} catch (IOException e) {
						System.err.println("warning: connection to " +reply.getOwner() +" lost");
						e.printStackTrace();
						// TODO: disconeect etc
					}
					
				}
			}
		}
	}
	
	
	public class ChannelData {
		final static int NOT_SET = -2;
		BufferedInputStream inputStream;
		BufferedOutputStream outputStream;
		Socket socket;
		User user;
		int requestLength = NOT_SET;
		int replyLength = NOT_SET;
	}
}