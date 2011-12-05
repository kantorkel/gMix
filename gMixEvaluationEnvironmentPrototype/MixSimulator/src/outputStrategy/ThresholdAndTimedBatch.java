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

package outputStrategy;

import java.util.Vector;

import message.MixMessage;
import message.NoneMixMessage;
import networkComponent.Mix;
import simulator.Event;
import simulator.EventExecutor;
import simulator.OutputStrategyEvent;
import simulator.Settings;
import simulator.Simulator;

import communicationBehaviour.LastMixCommunicationBehaviour;


//Dingledine 2002: Timed Mix
// "fires (flushes all messages) every t seconds but only
// when at least n messages have accumulated in the mix.
public class ThresholdAndTimedBatch extends OutputStrategy {

	private SimplexThresholdAndTimedBatch requestBatch;
	private SimplexThresholdAndTimedBatch replyBatch;
	private LastMixCommunicationBehaviour lastMixCommunicationBehaviour = null;
	
	
	protected ThresholdAndTimedBatch(Mix mix, Simulator simulator) {

		super(mix, simulator);
		int sendingRate = Settings.getPropertyAsInt("THRESHOLD_AND_TIMED_BATCH_SENDING_RATE");
		int batchSize = Settings.getPropertyAsInt("THRESHOLD_AND_TIMED_BATCH_BATCH_SIZE");
		this.requestBatch = new SimplexThresholdAndTimedBatch(true, sendingRate,batchSize );
		this.replyBatch = new SimplexThresholdAndTimedBatch(false, sendingRate, batchSize);
		this.lastMixCommunicationBehaviour = LastMixCommunicationBehaviour.getInstance(mix, simulator, this);

	}
	
	
	@Override
	public void incomingRequest(MixMessage mixMessage) {
		requestBatch.addMessage(mixMessage);
	}


	@Override
	public void incomingReply(MixMessage mixMessage) {
		replyBatch.addMessage(mixMessage);
	}


	@Override
	public void incomingReply(NoneMixMessage noneMixMessage) {
		lastMixCommunicationBehaviour.incomingDataFromDistantProxy(noneMixMessage);
	}
	
	
	public class SimplexThresholdAndTimedBatch implements EventExecutor {
		
		private boolean isRequestBatch;
		private Vector<MixMessage> collectedMessages;
		private boolean isFirstMessage = true;
		private int sendingRate;
		private int batchSize;
		
		
		public SimplexThresholdAndTimedBatch(boolean isRequestBatch, int sendingRate, int batchSize) {
			
			this.collectedMessages = new Vector<MixMessage>(batchSize);	
			this.isRequestBatch = isRequestBatch;
			this.sendingRate = sendingRate;
			this.batchSize = batchSize;
			
		}
		
		
		public void addMessage(MixMessage mixMessage) {
			
			if (isFirstMessage) {
				isFirstMessage = false;
				scheduleNextOutput();
			}
			
			collectedMessages.add(mixMessage);

		}

		
		public void putOutMessages() {
			
			if (collectedMessages.size() != batchSize)
				return;
				
			if (isRequestBatch)
				for (MixMessage m:collectedMessages)
					mix.putOutRequest(m);
			else
				for (MixMessage m:collectedMessages)
					mix.putOutReply(m);
			
			this.collectedMessages = new Vector<MixMessage>(collectedMessages.size()*2);	
				
		}
		
		
		private void scheduleNextOutput() {
			simulator.scheduleEvent(new Event(this, Simulator.getNow() + sendingRate, OutputStrategyEvent.TIMEOUT), this);
		}

		
		@Override
		public void executeEvent(Event e) {
			
			if (e.getEventType() == OutputStrategyEvent.TIMEOUT) {
				putOutMessages();
				if(!stopReplying)
					scheduleNextOutput();	
			} else 
				throw new RuntimeException("ERROR: TimedBatch received unknown Event: " +e); 
			
		}
		
	}

}