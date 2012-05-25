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

package plugIns.layer2recodingScheme.sphinx_v0_001;

import java.util.Arrays;

import framework.core.util.Util;


public class EncryptedMessage {
	
	public byte[] delta;
	public byte[][] alpBetaGam;
	
	
	public EncryptedMessage() {
		
	}
	
	
	public EncryptedMessage(byte[] message, Sphinx_Config config) {
		toObject(message, config);
	}
	
	
	public byte[] toByteArray(Sphinx_Config config) {
		assert delta.length == config.DELTA_SIZE;
		assert alpBetaGam[0].length == config.ALPHA_SIZE;
		assert alpBetaGam[1].length == config.BETA_SIZE;
		assert alpBetaGam[2].length == config.GAMMA_SIZE;
		return Util.concatArrays(new byte[][] {alpBetaGam[0], alpBetaGam[1], alpBetaGam[2], delta});
	}
	
	
	public void toObject(byte[] message, Sphinx_Config config) {
		assert message.length == config.getTotalMessageSize();
		this.alpBetaGam = new byte[3][];
		int pointer = 0;
		alpBetaGam[0] = Arrays.copyOfRange(message, pointer, pointer += config.ALPHA_SIZE);
		int lengthOfBeta = 16 + (config.ROUTE_LENGTH * 32);
		alpBetaGam[1] = Arrays.copyOfRange(message, pointer, pointer += lengthOfBeta);
		alpBetaGam[2] = Arrays.copyOfRange(message, pointer, pointer += 16);
		delta = Arrays.copyOfRange(message, pointer, message.length);
	}
	
}