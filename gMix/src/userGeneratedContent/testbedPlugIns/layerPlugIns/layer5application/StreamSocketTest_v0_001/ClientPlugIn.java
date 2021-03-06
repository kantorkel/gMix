/*******************************************************************************
 * gMix open source project - https://svs.informatik.uni-hamburg.de/gmix/
 * Copyright (C) 2014  SVS
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
 *******************************************************************************/
package userGeneratedContent.testbedPlugIns.layerPlugIns.layer5application.StreamSocketTest_v0_001;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import staticContent.framework.controller.Implementation;
import staticContent.framework.interfaces.Layer5ApplicationClient;
import staticContent.framework.socket.socketInterfaces.StreamAnonSocket;
import staticContent.framework.socket.socketInterfaces.AnonSocketOptions.CommunicationDirection;
import staticContent.framework.util.Util;


public class ClientPlugIn extends Implementation implements Layer5ApplicationClient {

	
	@Override
	public void constructor() {
	}


	@Override
	public void initialize() {
		// TODO Auto-generated method stub 
	}


	@Override
	public void begin() {
		int numberOfConnections = settings.getPropertyAsInt("NUMBER_OF_PARALLEL_CONNECTIONS");
		System.out.println("generating load for " +numberOfConnections +" users"); 
		for (int i=0; i<numberOfConnections; i++)
			new RequestThread().start();
	}

	
	public class RequestThread extends Thread {

		private Random random = new Random();
		
		@Override
		public void run() {
			
			CommunicationDirection cm = anonNode.IS_DUPLEX ? CommunicationDirection.DUPLEX : CommunicationDirection.SIMPLEX_SENDER;
			StreamAnonSocket socket = anonNode.createStreamSocket(cm, false);
			
			long ct = 0;
			try {
				socket.connect(settings.getPropertyAsInt("SERVICE_PORT1"));
				if (anonNode.IS_DUPLEX)
					new ReplyThread(socket).start();
				OutputStream outputStream = socket.getOutputStream();
				int mtu = socket.getMTU();
				byte[] data = new byte[mtu];
				//int ctr = 0;
				
				while(true) { 
					//outputStream.write(data);
					if (ct++ == 10000) {
						System.out.println("client sent: " +mtu * ct +" bytes");
						ct = 0;
					}
					//System.out.println("send " +ct++ +":" +data.length); 
					random.nextBytes(data);
					outputStream.write(data);
					/*if (ctr++ == 150) {
						ctr=0;
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}*/
				}
				
				/*
				InputStream inputStream = socket.getInputStream();
				String toSend = "1234567890";
				for (int i=0; i<1000; i++)
					toSend +="1234567890";
				System.out.println(client +" sending (pt1) " +toSend.length() +" bytes");
				outputStream.write(toSend.getBytes());
				outputStream.flush();
				
				if (InfoServiceServer.DUPLEX_MIX) {
					byte[] reply = new byte[toSend.length()];
					Util.forceRead(inputStream, reply);
					System.out.println(client +" all data read (pt1)");
				}
				
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(client +" sending (pt2) " +toSend.length() +" bytes");
				outputStream.write(toSend.getBytes());
				outputStream.flush();
				
				if (InfoServiceServer.DUPLEX_MIX) {
					byte[] reply = new byte[toSend.length()];
					Util.forceRead(inputStream, reply);
					System.out.println(client +" all data read (pt2)");
				}
				*/
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}
	
	
	private class ReplyThread extends Thread {
		
		private StreamAnonSocket socket;
		private InputStream inputStream;
		
		public ReplyThread(StreamAnonSocket socket) throws IOException {
			this.socket = socket;
			this.inputStream = socket.getInputStream();
		}
		
		int ct = 0;

		@Override
		public void run() {
			int mtu = socket.getMTU();
			while (true) {
				byte[] reply;
				try {
					reply = new byte[mtu];
					Util.forceRead(inputStream, reply);
					if (ct++ == 10000) {
						System.out.println("client received: " +mtu * ct +" bytes");
						ct = 0;
						}
					//System.out.println("read " +ct++ +": " +reply.length); 
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

}
