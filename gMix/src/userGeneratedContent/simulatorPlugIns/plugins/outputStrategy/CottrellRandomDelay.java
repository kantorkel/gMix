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


// Cottrell 1995 ("Mixmaster & Remailer Attacks")
// delays incoming messages randomly
@Plugin(pluginKey = "COTTRELL_RANDOM_DELAY", pluginName = "Cottrell Random Delay")
public class CottrellRandomDelay extends OutputStrategyImpl implements EventExecutor {

	private static SecureRandom secureRandom = new SecureRandom();
	
	@IntSimulationProperty(
			name = "Maximum delay (ms)",
			key = "COTTRELL_MAX_RANDOM_DELAY_IN_MS",
			min = 0
	)
	private int maxDelay;
	
	
	public CottrellRandomDelay(Mix mix, Simulator simulator) {

		super(mix, simulator);
		this.maxDelay = Simulator.settings.getPropertyAsInt("COTTRELL_MAX_RANDOM_DELAY_IN_MS");
	}
	
	
	@Override
	public void incomingRequest(MixMessage mixMessage) {
		scheduleOutput(mixMessage, getRandomDelay());
	}


	@Override
	public void incomingReply(MixMessage mixMessage) {
		scheduleOutput(mixMessage, getRandomDelay());
	}

	
	private int getRandomDelay() {
		return secureRandom.nextInt(maxDelay+1);
	}

	
	private void scheduleOutput(MixMessage mixMessage, int delayTillOutput) {
		simulator.scheduleEvent(new Event(this, Simulator.getNow() + delayTillOutput, OutputStrategyEvent.TIMEOUT, mixMessage), this);
	}
	
	
	@Override
	public void executeEvent(Event e) {
		
		if (e.getEventType() == OutputStrategyEvent.TIMEOUT) {
			
			MixMessage mixMessage = (MixMessage)e.getAttachment();
			if (mixMessage.isRequest())
				mix.putOutRequest(mixMessage);
			else
				mix.putOutReply(mixMessage);
			
		} else 
			throw new RuntimeException("ERROR: CottrellRandomDelay received unknown Event: " +e); 
		
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
