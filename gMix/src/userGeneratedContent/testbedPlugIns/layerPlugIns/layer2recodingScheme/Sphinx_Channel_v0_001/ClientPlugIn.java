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
package userGeneratedContent.testbedPlugIns.layerPlugIns.layer2recodingScheme.Sphinx_Channel_v0_001;

import staticContent.framework.controller.Implementation;
import staticContent.framework.interfaces.Layer1NetworkClient;
import staticContent.framework.interfaces.Layer2RecodingSchemeClient;
import staticContent.framework.interfaces.Layer3OutputStrategyClient;
import staticContent.framework.interfaces.Layer4TransportClient;
import staticContent.framework.message.Reply;
import staticContent.framework.message.Request;
import staticContent.framework.routing.RoutingMode;


public class ClientPlugIn extends Implementation implements Layer2RecodingSchemeClient {

	private Sphinx_Channel_Config config;
	private Sphinx_Channel messageCreator;
	private Sphinx_Config configSphinx;
	
	
	@Override
	public void constructor() {
		if (anonNode.ROUTING_MODE == RoutingMode.DYNAMIC_ROUTING) 
			throw new RuntimeException("RoutingMode DYNAMIC_ROUTING not supported, only GLOBAL_ROUTING and SOURCE_ROUTING"); 
		this.config = new Sphinx_Channel_Config(anonNode, true);
		this.configSphinx = new Sphinx_Config(anonNode, true);
		this.messageCreator = new Sphinx_Channel(anonNode, config, configSphinx);
		this.messageCreator.initAsClient();
	}
	

	@Override
	public void initialize() {

	}

	@Override
	public void begin() {
		
	}
	
	
	@Override
	public void setReferences(
			Layer1NetworkClient layer1,
			Layer2RecodingSchemeClient layer2, 
			Layer3OutputStrategyClient layer3,
			Layer4TransportClient layer4) {
		assert layer2 == this;
	}
	

	@Override
	public Request applyLayeredEncryption(Request request) {
		return messageCreator.applyLayeredEncryption(request);
	}

	
	@Override
	public int getMaxPayloadForNextMessage() {
		return messageCreator.getMaxPayloadForNextMessage();
	}

	
	@Override
	public int getMaxPayloadForNextReply() {
		return messageCreator.getMaxPayloadForNextReply();
	}

	
	@Override
	public Reply extractPayload(Reply reply) {
		return messageCreator.extractPayload(reply);
	}

}