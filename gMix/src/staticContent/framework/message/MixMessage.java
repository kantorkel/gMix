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
package staticContent.framework.message;

import staticContent.framework.userDatabase.User;
import staticContent.framework.util.Util;


public abstract class MixMessage implements Message, ExternalMessage, Comparable<MixMessage> {
	
	public static final int NONE = -111;
	public static final int CLIENT = -343555;
	private int identifier;
	private User owner;
	private DummyStatus dummyStatus = DummyStatus.UNKNOWN;
	private byte[] byteMessage;
	private static int msgIdCounter = 0;
	
	public static boolean recordStatistics = false;
	public int nextHopAddress = Util.NOT_SET;

	public int destinationPseudonym = Util.NOT_SET;
	public int[] route;
	public byte[][] headers;

	public long statisticsCreateTime;
	public boolean isFirstReplyHop = false;
	
	
	public static Request getInstanceRequest(byte[] byteMesssage, User owner) {
		Request request = getInstanceRequest(byteMesssage);
		request.setOwner(owner);
		if (recordStatistics)
			request.statisticsCreateTime = System.nanoTime();
		return request;
	}
	
	
	public static Request getInstanceRequest(byte[] byteMessage) {
		Request request = new Request();
		request.setByteMessage(byteMessage);
		request.setIdentifier(msgIdCounter++);
		if (byteMessage == null)
			request.setByteMessage(new byte[0]);
		if (request.getByteMessage().length == 0) // no payload -> dummy
			request.setDummyStatus(DummyStatus.DUMMY);
		else
			request.setDummyStatus(DummyStatus.NO_DUMMY);
		return request;
	}
	
	
	public static Request getInstanceRequest() {
		return new Request();
	}
	
	
	public static Reply getInstanceReply(byte[] byteMessage, User owner) {
		Reply reply = getInstanceReply(byteMessage);
		reply.setOwner(owner);
		reply.nextHopAddress = owner.prevHopAddress;
		return reply;
	}
	
	
	public static Reply getInstanceReply(byte[] byteMessage) {
		Reply reply = new Reply();
		reply.setByteMessage(byteMessage);
		reply.setIdentifier(msgIdCounter++);
		if (byteMessage == null)
			reply.setByteMessage(new byte[0]);
		if (reply.getByteMessage().length == 0) // no payload -> dummy
			reply.setDummyStatus(DummyStatus.DUMMY);
		else
			reply.setDummyStatus(DummyStatus.NO_DUMMY);
		return reply;
	}

	
	public static Reply getInstanceReply() {
		return new Reply();
	}
	
	
	@Override
	public int getIdentifier() {
		return identifier;
	}	
	
	
	@Override
	public void setIdentifier(int identifier) {
		this.identifier = identifier;
	}
	
	
	@Override
	public User getOwner() {
		return this.owner;
	}
	

	@Override
	public byte[] getByteMessage() {
		return this.byteMessage;
	}
	
	
	@Override
	public void setByteMessage(byte[] byteMessage) {
		this.byteMessage = byteMessage;
		if (byteMessage == null)
			this.byteMessage = new byte[0];
		//if (byteMessage.length == 0) // no payload -> dummy
		//	this.dummyStatus = DummyStatus.DUMMY;
		//else
		//	this.dummyStatus = DummyStatus.NO_DUMMY;
	}
	
	
	@Override
	public void setOwner(User owner) {
		this.owner = owner;
	}
	
	
	@Override
	public DummyStatus getDummyStatus() {
		return dummyStatus;
	}

	
	@Override
	public void setDummyStatus(DummyStatus dummyStatus) {
		this.dummyStatus = dummyStatus;
	}
	
	public boolean wasCreatedOnThisMix() {
		return this.isFirstReplyHop;
	}
	
	
	/**
	 * Implements the <code>Comparable</code> interface's <code>compareTo()
	 * </code> method. Compares this <code>MixMessage</code> with the specified 
	 * <code>MixMessage</code> for order (criterion: alphabetic order of this 
	 * <code>MixMessage</code>'s payload. Returns a negative integer, zero, or a 
	 * positive integer as this <code>MixMessage</code> is less than, equal to, or 
	 * greater than the specified <code>MixMessage</code>.
	 * 
	 * @param mixMessage	The <code>MixMessage</code> to be compared.
	 * 
	 * @return			-1, 0, or 1 as this <code>MixMessage</code> is less than, 
	 * 					equal to, or greater than the specified <code>MixMessage
	 * 					</code>.
	 * 
	 * @see #setPayloadRange(int, int)
	 */
	@Override
	public int compareTo(MixMessage mixMessage) {

		if (this.byteMessage.length < mixMessage.byteMessage.length)
			return -1;
		else if (this.byteMessage.length > mixMessage.byteMessage.length)
			return 1;	
		else { // both payloads have the same length
			for (int i=0; i<this.byteMessage.length; i++)
				if (this.byteMessage[i] < mixMessage.byteMessage[i])
					return -1;
				else if (this.byteMessage[i] > mixMessage.byteMessage[i])
					return 1;
			}
				
			// both payloads contain the same message
			return 0;
	}
}
