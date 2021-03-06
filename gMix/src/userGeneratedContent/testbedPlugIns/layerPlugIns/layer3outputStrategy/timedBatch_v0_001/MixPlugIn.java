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
package userGeneratedContent.testbedPlugIns.layerPlugIns.layer3outputStrategy.timedBatch_v0_001;

import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import staticContent.framework.controller.Implementation;
import staticContent.framework.interfaces.Layer3OutputStrategyMix;
import staticContent.framework.message.MixMessage;
import staticContent.framework.message.Reply;
import staticContent.framework.message.Request;
import staticContent.framework.userDatabase.User;


//Dingledine 2002: Timed Mix
//"The mix fires (flushes all messages) every t seconds"
public class MixPlugIn extends Implementation implements Layer3OutputStrategyMix {

	private SimplexTimedBatch requestBatch;
	private SimplexTimedBatch replyBatch;
	private int SENDING_RATE;
	private int DEFAULT_BATCH_SIZE;
	
	@Override
	public void constructor() {
		this.SENDING_RATE = settings.getPropertyAsInt("TIMED_BATCH_SENDING_RATE");
		this.DEFAULT_BATCH_SIZE = settings.getPropertyAsInt("TIMED_BATCH_DEFAULT_BATCH_SIZE");
		this.requestBatch = new SimplexTimedBatch(true);
		this.replyBatch = new SimplexTimedBatch(false);
	}

	
	@Override
	public void initialize() {
		// no need to do anything
	}

	
	@Override
	public void begin() {
		// no need to do anything
	}

	
	@Override
	public void addRequest(Request request) {
		requestBatch.addMessage((MixMessage)request);
	}


	@Override
	public void addReply(Reply reply) {
		replyBatch.addMessage((MixMessage)reply);
	}
	
	
	public class SimplexTimedBatch {

		private boolean isRequestPool;
		private Vector<MixMessage> collectedMessages;
		private boolean isFirstMessage = true;
		private Timer timer = new Timer();
		
		
		public SimplexTimedBatch(boolean isRequestPool) {
			this.collectedMessages = new Vector<MixMessage>(DEFAULT_BATCH_SIZE);
			this.isRequestPool = isRequestPool;
		}
		
		
		public void addMessage(MixMessage mixMessage) {
			synchronized (timer) {
				if (isFirstMessage) {
					isFirstMessage = false;
					timer.scheduleAtFixedRate(new TimeoutTask(this), SENDING_RATE, SENDING_RATE);
				}
				collectedMessages.add(mixMessage);
			}
		}

		
		public void putOutMessages() {
			synchronized (timer) {
				if (collectedMessages.size() > 0) {
					Collections.sort(collectedMessages);
					if (isRequestPool) {
						for (MixMessage m: collectedMessages)
							anonNode.putOutRequest((Request)m);
					} else {
						for (MixMessage m: collectedMessages)
							anonNode.putOutReply((Reply)m);
					}/*if (isRequestPool)
						anonNode.putOutRequests(collectedMessages.toArray(new Request[0]));
					else
						anonNode.putOutReplies(collectedMessages.toArray(new Reply[0]));*/
					this.collectedMessages = new Vector<MixMessage>(DEFAULT_BATCH_SIZE);
				}
			}	
		}
	
		
		private final class TimeoutTask extends TimerTask {

			private SimplexTimedBatch linkedBatch;
			
			protected TimeoutTask(SimplexTimedBatch linkedBatch) {
				this.linkedBatch = linkedBatch;
			}
			
			@Override 
			public void run() {
				linkedBatch.putOutMessages();
			}
			
		}
			
	}
	
	
	@Override
	public int getMaxSizeOfNextWrite() {
		return super.recodingLayerMix.getMaxSizeOfNextReply();
	}


	@Override
	public int getMaxSizeOfNextRead() {
		return super.recodingLayerMix.getMaxSizeOfNextRequest();
	}


	@Override
	public void write(User user, byte[] data) {
		Reply reply = MixMessage.getInstanceReply(data, user); 
		reply.isFirstReplyHop = true;
		transportLayerMix.addLayer4Header(reply);
		anonNode.forwardToLayer2(reply);
	}
	
}
