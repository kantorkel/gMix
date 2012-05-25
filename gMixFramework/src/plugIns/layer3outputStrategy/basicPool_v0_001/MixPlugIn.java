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
package plugIns.layer3outputStrategy.basicPool_v0_001;

import java.security.SecureRandom;

import framework.core.controller.Implementation;
import framework.core.interfaces.Layer3OutputStrategyMix;
import framework.core.message.MixMessage;
import framework.core.message.Reply;
import framework.core.message.Request;


public class MixPlugIn extends Implementation implements Layer3OutputStrategyMix {

	private static SecureRandom secureRandom = new SecureRandom();
	private SimplexBasicPool requestPool;
	private SimplexBasicPool replyPool;
	private int POOL_SIZE;
	
	
	@Override
	public void constructor() {
		this.POOL_SIZE = settings.getPropertyAsInt("BASIC_POOL_POOL_SIZE");
		this.requestPool = new SimplexBasicPool(true);
		this.replyPool = new SimplexBasicPool(false);
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
		requestPool.addMessage((MixMessage) request);
	}

	
	@Override
	public void addReply(Reply reply) {
		replyPool.addMessage((MixMessage) reply);
	}

	
	public class SimplexBasicPool {
		
		private boolean isRequestPool;
		private MixMessage[] collectedMessages;
		private int nextFreeSlot = 0;
		
		
		public SimplexBasicPool(boolean isRequestPool) {
			this.collectedMessages = new MixMessage[POOL_SIZE];
			this.isRequestPool = isRequestPool;
		}
		
		
		public synchronized void addMessage(MixMessage mixMessage) {
			if (nextFreeSlot < POOL_SIZE) {
				collectedMessages[nextFreeSlot++] = mixMessage;
			} else {
				int chosen = secureRandom.nextInt(POOL_SIZE+1);
				if (chosen == POOL_SIZE) {
					putOutMessage(mixMessage);
				} else {
					putOutMessage(collectedMessages[chosen]);
					collectedMessages[chosen] = mixMessage;
				}
			}
		}
		
		
		private void putOutMessage(MixMessage mixMessage) {
			if (isRequestPool)
				anonNode.putOutRequest((Request)mixMessage);
			else
				anonNode.putOutReply((Reply)mixMessage);
		}
	}

	
	@Override
	public int getMaxSizeOfNextReply() {
		return super.recodingLayerMix.getMaxSizeOfNextReply();
	}


	@Override
	public int getMaxSizeOfNextRequest() {
		return super.recodingLayerMix.getMaxSizeOfNextRequest();
	}
	
}