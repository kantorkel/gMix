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


import java.util.Timer;
import java.util.TimerTask;

import networkClock.NetworkClockController;

import message.Message;
import message.Reply;
import message.Request;
import architectureInterface.OutputStrategyInterface;
import framework.Implementation;


// Kesdogan et. al. 1998: Stop-and-Go MIXes: Providing Probabilistic Anonymity in an Open System
public class StopAndGo extends Implementation implements OutputStrategyInterface {

	private boolean useTimeStamps;
	private NetworkClockController nwclock;
	private Timer timer = new Timer();
	
	@Override
	public void constructor() {
		this.useTimeStamps = true; // TODO: property file!
	}
	

	@Override
	public void initialize() {
		nwclock = mix.getNetworkClock();
	}
	

	@Override
	public void begin() {
		// no need to do anything
	}

	
	@Override
	public void addRequest(Request request) {
		
		synchronized (timer) {
			
			long now = nwclock.getTime();
			
			if (useTimeStamps)
				if (now < request.getTsMin() || now > request.getTsMax())
					return;
			
			timer.schedule(new SendMessageTask((Message)request, true), request.getDelay());
			
		}
	}

	
	@Override
	public void addReply(Reply reply) {
		
		synchronized (timer) {
			
			long now = nwclock.getTime();
			
			if (useTimeStamps)
				if (now < reply.getTsMin() || now > reply.getTsMax())
					return;
			
			timer.schedule(new SendMessageTask((Message)reply, false), reply.getDelay());
		}

	}

	
	private final class SendMessageTask extends TimerTask {

		private Message relatedMessage;
		private boolean isRequest;
		
		protected SendMessageTask(Message message, boolean isRequest) {
			this.relatedMessage = message;
			this.isRequest = isRequest;
		}
		
		@Override 
		public void run() {
			
			synchronized (timer) {
				
				if (isRequest)
					controller.getInputOutputHandler().addRequest((Request)relatedMessage);
				else
					controller.getInputOutputHandler().addReply((Reply)relatedMessage);
				
			}
			
		}
			
	}

	
	@Override
	public String[] getCompatibleImplementations() {
		return (new String[] {	"outputStrategy.BasicBatch",
								"outputStrategy.BasicPool",
								"inputOutputHandler.BasicInputOutputHandler",
								"keyGenerator.BasicKeyGenerator",
								"messageProcessor.BasicMessageProcessor",
								"externalInformationPort.BasicExternalInformationPort",
								"networkClock.BasicSystemTimeClock",
								"userDatabase.BasicUserDatabase",
								"message.BasicMessage"	
			});
	}


	@Override
	public boolean usesPropertyFile() {
		return false;
	}
	
}