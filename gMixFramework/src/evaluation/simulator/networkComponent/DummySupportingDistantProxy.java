/*
 * gMix open source project - https://svs.informatik.uni-hamburg.de/gmix/
 * Copyright (C) 2012  Karl-Peter Fuchs
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
package evaluation.simulator.networkComponent;


import evaluation.simulator.communicationBehaviour.LastMixCommunicationBehaviour;
import evaluation.simulator.communicationBehaviour.ReplyReceiver;
import evaluation.simulator.core.EventExecutor;
import evaluation.simulator.core.MixEvent;
import evaluation.simulator.core.Simulator;
import evaluation.simulator.delayBox.DelayBox;
import evaluation.simulator.message.MessageFragment;
import evaluation.simulator.message.MixMessage;
import evaluation.simulator.message.TransportMessage;
import evaluation.simulator.message.PayloadObject;
import evaluation.simulator.statistics.StatisticsType;


public class DummySupportingDistantProxy extends BasicDistantProxy implements EventExecutor, ReplyReceiver {
	
	private LastMixCommunicationBehaviour lastMixCommunicationBehaviour = null;
	
	
	protected DummySupportingDistantProxy(String identifier, Simulator simulator, DelayBox delayBox) {
		super(identifier, simulator, delayBox, true);
		this.lastMixCommunicationBehaviour = LastMixCommunicationBehaviour.getInstance(this, simulator, this);
	}


	protected void incomingRequest(MixMessage mixMessage) {
		statistics.addValue(1, StatisticsType.DISTANTPROXY_MIXMESSAGES_RECEIVED);
		if (!mixMessage.isDummy())
			for (PayloadObject payloadObject:mixMessage.getPayloadObjectsContained())
				if (payloadObject instanceof TransportMessage)
					incomingRequest((TransportMessage)payloadObject);
				else if (payloadObject instanceof MessageFragment) {
					if (((MessageFragment)payloadObject).isLastFragment())
						incomingRequest(((MessageFragment)payloadObject).getAssociatedTransportMessage());
				} else {
					throw new RuntimeException("ERROR: unknown PayloadObject type! " +payloadObject); 
				}
	}
	
	
	@Override
	protected void requestAnswered(TransportMessage transportMessage) {
		lastMixCommunicationBehaviour.incomingDataFromDistantProxy(transportMessage);
	}


	@Override
	public void incomingReply(MixMessage mixMessage) {
		sendToPreviousHop(mixMessage, 0, MixEvent.INCOMING_MIX_MESSAGE_OF_TYPE_REPLY);
	}
	
}
