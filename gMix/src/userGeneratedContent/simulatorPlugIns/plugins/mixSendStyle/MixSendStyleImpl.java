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
package userGeneratedContent.simulatorPlugIns.plugins.mixSendStyle;

import staticContent.evaluation.simulator.Simulator;
import staticContent.evaluation.simulator.annotations.plugin.PluginSuperclass;
import staticContent.evaluation.simulator.core.message.TransportMessage;
import staticContent.evaluation.simulator.core.networkComponent.NetworkNode;
import staticContent.evaluation.simulator.core.statistics.Statistics;

@PluginSuperclass( layerName = "Mix Proxy", layerKey = "MIX_SEND_STYLE", position = 3)
public abstract class MixSendStyleImpl {

	protected NetworkNode owner;
	protected Simulator simulator;
	protected Statistics statistics;
	protected ReplyReceiver replyReceiver;
	
	
	protected MixSendStyleImpl(	NetworkNode owner, 
								Simulator simulator, 
								ReplyReceiver replyReceiver
								) {
		
		this.simulator = simulator;
		this.owner = owner;
		this.statistics = owner.getStatistics();
		this.replyReceiver = replyReceiver;
		
	}
	
	
	// must call replyReceiver.addReply(MixMessage mixMessage)
	public abstract void incomingDataFromServer(TransportMessage transportMessage);

}
