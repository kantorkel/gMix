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
package userGeneratedContent.simulatorPlugIns.plugins.outputStrategy;

import java.security.SecureRandom;
import java.util.Vector;






import staticContent.evaluation.simulator.Simulator;
import staticContent.evaluation.simulator.annotations.plugin.Plugin;
import staticContent.evaluation.simulator.annotations.property.IntSimulationProperty;
import staticContent.evaluation.simulator.core.event.Event;
import staticContent.evaluation.simulator.core.event.EventExecutor;
import staticContent.evaluation.simulator.core.message.MixMessage;
import staticContent.evaluation.simulator.core.networkComponent.AbstractClient;
import staticContent.evaluation.simulator.core.networkComponent.Mix;
import userGeneratedContent.simulatorPlugIns.pluginRegistry.ClientSendStyle;
import userGeneratedContent.simulatorPlugIns.pluginRegistry.MixSendStyle;
import userGeneratedContent.simulatorPlugIns.plugins.clientSendStyle.ClientSendStyleImpl;
import userGeneratedContent.simulatorPlugIns.plugins.mixSendStyle.MixSendStyleImpl;


//Cottrell 1995 ("Mixmaster & Remailer Attacks")
//every "outputRate" ms, send x (= "numberOfMessagesInPool" - "minPoolSize") 
//randomly chosen messages (if x >= 1)
@Plugin(pluginKey = "COTTRELL_TIMED_POOL", pluginName = "Cottrell Timed Pool")
public class CottrellTimedPool extends OutputStrategyImpl {

	private SimplexCottrellTimedPool requestBatch;
	private SimplexCottrellTimedPool replyBatch;
	private static SecureRandom secureRandom = new SecureRandom();
	
	@IntSimulationProperty(
			name = "Sending Interval (ms)",
			key = "COTTRELL_TIMED_POOL_SENDING_INTERVAL_IN_MS",
			min = 0
	)
	private int sendingRate;
	
	@IntSimulationProperty(
			name = "Minimum pool size (requests)",
			key = "COTTRELL_TIMED_POOL_MIN_POOL_SIZE",
			min = 0
	)
	private int poolSize;
	
	public CottrellTimedPool(Mix mix, Simulator simulator) {
		super(mix, simulator);
		this.sendingRate = Simulator.settings.getPropertyAsInt("COTTRELL_TIMED_POOL_SENDING_INTERVAL_IN_MS");
		this.poolSize = Simulator.settings.getPropertyAsInt("COTTRELL_TIMED_POOL_MIN_POOL_SIZE");
		this.requestBatch = new SimplexCottrellTimedPool(true, sendingRate, poolSize);
		this.replyBatch = new SimplexCottrellTimedPool(false, sendingRate, poolSize);
	}
	
	@Override
	public void incomingRequest(MixMessage mixMessage) {
		requestBatch.addMessage(mixMessage);
	}

	@Override
	public void incomingReply(MixMessage mixMessage) {
		replyBatch.addMessage(mixMessage);
	}
	
	
	public class SimplexCottrellTimedPool implements EventExecutor {
		
		private boolean isRequestPool;
		private Vector<MixMessage> collectedMessages;
		private boolean isFirstMessage = true;
		private int sendingRate;
		private int poolSize;
		
		
		public SimplexCottrellTimedPool(boolean isRequestPool, int sendingRate, int poolSize) {
			
			this.collectedMessages = new Vector<MixMessage>(simulator.getClients().size()*2);	
			this.isRequestPool = isRequestPool;
			this.sendingRate = sendingRate;
			this.poolSize = poolSize;
			
		}
		
		
		public void addMessage(MixMessage mixMessage) {
			
			if (isFirstMessage) {
				isFirstMessage = false;
				scheduleNextOutput();
			}
			
			collectedMessages.add(mixMessage);

		}

		
		public void putOutMessages() {
			
			int numberOfMessagesToPutOut = collectedMessages.size() - poolSize;
			
			if (numberOfMessagesToPutOut > 0) {
				
				for (int i=0; i<numberOfMessagesToPutOut; i++) {
					
					int chosen = secureRandom.nextInt(collectedMessages.size());
					
					if (isRequestPool)
						mix.putOutRequest(collectedMessages.remove(chosen));
					else
						mix.putOutReply(collectedMessages.remove(chosen));
				}
			
			}
	
		}
		
		
		private void scheduleNextOutput() {
			
			simulator.scheduleEvent(new Event(this, Simulator.getNow() + sendingRate, OutputStrategyEvent.TIMEOUT), this);
		
		}

		
		@Override
		public void executeEvent(Event e) {
			
			if (e.getEventType() == OutputStrategyEvent.TIMEOUT) {
				putOutMessages();
				scheduleNextOutput();
			} else 
				throw new RuntimeException("ERROR: TimedBatch received unknown Event: " +e); 
			
		}
		
	}


	@Override
	public ClientSendStyleImpl getClientSendStyle(AbstractClient client) {
		return ClientSendStyle.getInstance(client);
	}


	@Override
	public MixSendStyleImpl getMixSendStyle() {
		return MixSendStyle.getInstance(mix, mix);
	}

}