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
package userGeneratedContent.testbedPlugIns.layerPlugIns.layer2recodingScheme.nameOfPluginA;

import staticContent.framework.controller.Implementation;
import staticContent.framework.interfaces.Layer2RecodingSchemeMix;
import staticContent.framework.message.Reply;
import staticContent.framework.message.Request;
import staticContent.framework.userDatabase.User;
import userGeneratedContent.testbedPlugIns.staticFunctionPlugIns.capabilities.DummyGenerator;


public class MixPlugIn extends Implementation implements Layer2RecodingSchemeMix, DummyGenerator {

	
	@Override
	public void constructor() {
		// TODO Auto-generated method stub
		System.out.println("loaded " +this.getClass().getCanonicalName()); 
		
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void begin() {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void generateDummy() {
		// TODO Auto-generated method stub
		System.out.println("generating dummy..."); 
	}

	@Override
	public int getMaxSizeOfNextReply() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxSizeOfNextRequest() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Request generateDummy(int[] route, User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Request generateDummy(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reply generateDummyReply(int[] route, User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reply generateDummyReply(User user) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
