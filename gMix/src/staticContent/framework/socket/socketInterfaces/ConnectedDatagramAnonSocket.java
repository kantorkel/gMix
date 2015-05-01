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
package staticContent.framework.socket.socketInterfaces;


public interface ConnectedDatagramAnonSocket extends AnonSocket {

	public void connect(int destinationPort); // only available if fixed route socket
	public void connect(int destinationPseudonym, int destinationPort); // only available if free route socket
	public void disconnect();
	public boolean isConnected();
	
	public void sendMessage(byte[] payload);
	public byte[] receiveMessage(); // may be not available if the implementing socket is simplex
	public int getMaxSizeForNextMessageSend();
	public int getMaxSizeForNextMessageReceive(); // may be not available if the implementing socket is simplex

	public AdaptiveAnonSocket getImplementation();
}