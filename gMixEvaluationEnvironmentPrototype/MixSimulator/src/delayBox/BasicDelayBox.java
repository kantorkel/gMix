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

package delayBox;

import simulator.Simulator;
import simulator.Settings;


public class BasicDelayBox extends DelayBox {
	
	private SimplexDelayBox sendDelayBox;
	private SimplexDelayBox receiveDelayBox;
	
	
	// bandwith in MBit/Sec
	// latency in ms
	protected BasicDelayBox(int bandwidthSend, int bandwidthReceive, int latency) {

		super();
		
		sendDelayBox = new SimplexDelayBox(bandwidthSend, latency);
		receiveDelayBox = new SimplexDelayBox(bandwidthReceive, latency);
		
	}
	

	@Override
	public int getReceiveDelay(int numberOfBytesToReceive) {
		int delay = this.receiveDelayBox.getDelay(numberOfBytesToReceive);	
		//System.out.println("reveiceDelay: " +delay);
		if (delay > 1000*30)
			System.out.println("OVERLOAD"); // TODO nur ausgeben, wenn numberOfBytesToReceive "gro�" ist
		return delay;
	}


	@Override
	public int getSendDelay(int numberOfBytesToSend) {
		int delay = this.sendDelayBox.getDelay(numberOfBytesToSend);	
		//System.out.println("sendDelay: " +delay); 
		if (delay > 1000*30)
			System.out.println("OVERLOAD");  // TODO nur ausgeben, wenn numberOfBytesToSend "gro�" ist
		return delay;
	}
	
	
	private class SimplexDelayBox {
		
		private final static int NOT_SCHEDULED = -100;
		private final static int NOT_SET = -101;
		
		private double latency; // in ms
		private int packetSize = new Integer(Settings.getProperty("NETWORK_PACKET_PAYLOAD_SIZE"));
		private int freeSpaceInPacket = packetSize;
		private double pulse; // abstand in dem pakete geschickt werden k�nnen
		private int timeOfOutputForNextNotFullPacket;
		
		private int lastPulse = NOT_SET;
		private boolean isBusy;
		
		
		public SimplexDelayBox(int bandwidth, int latency) {
			
			this.latency = latency;
			double packetsPerSecond = Math.round(((bandwidth*(double)1000000) / (double)8) / (double)packetSize);
			this.pulse = (double)1000 / packetsPerSecond;
			
			if (pulse < 1) {
				pulse = 1;
				packetSize = (int) ((bandwidth*(double)1000000) / (double)8) / 1000;
			} 
			
		}

		
		public int getDelay(int numberOfBytesToSend) {
			
			int packetsTransmittedSinceLastCall;
			int transmitDuration; // when sending begins; see "delayTillTransmitBegins"
			int delayTillTransmitBegins;
			
			if (numberOfBytesToSend == 0)
				return 0;
			
			// Vergangene Zeit seit letztem Aufruf ber�cksichtigen
			packetsTransmittedSinceLastCall = getNumberOfPacketsTransmittedSinceLastCall();

			if (timeOfOutputForNextNotFullPacket < Simulator.getNow()) { // Leitung ist wieder frei
				timeOfOutputForNextNotFullPacket = NOT_SCHEDULED;
				freeSpaceInPacket = packetSize;
				lastPulse = NOT_SET;
				isBusy = false;
			} else if (packetsTransmittedSinceLastCall > 0) {
				lastPulse += Math.round((double)packetsTransmittedSinceLastCall * pulse);
			}

			if (!isBusy) { // Leitung frei -> neues Paket aufmachen
				
				if (numberOfBytesToSend < freeSpaceInPacket) { // kein neues Paket n�tig (Daten passen in ein Paket und f�llen dieses nicht komplett)
					
					transmitDuration = 0;
					delayTillTransmitBegins = (int)Math.round(pulse);
					freeSpaceInPacket -= numberOfBytesToSend;
					timeOfOutputForNextNotFullPacket = Simulator.getNow() + delayTillTransmitBegins;
					lastPulse = Simulator.getNow();
					isBusy = true;
					
				} else { // neues Paket n�tig					
					
					int packetsNeeded = (int)Math.floor((double)numberOfBytesToSend / (double)packetSize); // counts only full packets
					freeSpaceInPacket = packetSize - (numberOfBytesToSend % packetSize);
					delayTillTransmitBegins = (int)Math.round(pulse);
					timeOfOutputForNextNotFullPacket = Simulator.getNow() + (int)Math.round((double)packetsNeeded * pulse);
					if (freeSpaceInPacket == packetSize) // auf letztes Paket muss NICHT gewartet werden
						transmitDuration = (int)Math.round((double)packetsNeeded * pulse);
					else // auf letztes Paket muss gewartet werden
						transmitDuration = (int)Math.round((double)((double)packetsNeeded+(double)1) * pulse);
					
					lastPulse = Simulator.getNow();
					isBusy = true;
					
				}
				
			} else { // Leitung nicht frei

				delayTillTransmitBegins = timeOfOutputForNextNotFullPacket - Simulator.getNow();
				if (delayTillTransmitBegins < 0)
					throw new RuntimeException("ERROR: delayTillTransmitBegins < 0!");
				
				if (numberOfBytesToSend < freeSpaceInPacket) { // kein neues Paket n�tig (Daten passen in ein Paket und f�llen dieses nicht komplett)
					
					transmitDuration = 0;
					freeSpaceInPacket -= numberOfBytesToSend;
					
				} else { // neues Paket n�tig	
					
					numberOfBytesToSend -= freeSpaceInPacket;
					int packetsNeeded = 1 + (int)Math.floor((double)numberOfBytesToSend / (double)packetSize); // counts only full packets ("1 +" weil aktuelles nicht volles Paket auch gez�hlt werden muss)
					freeSpaceInPacket = packetSize - (numberOfBytesToSend % packetSize);
					int timeTillNextPulse = (int)Math.round((double)lastPulse + pulse) - Simulator.getNow();
					timeOfOutputForNextNotFullPacket += timeTillNextPulse + (int)Math.round((double)packetsNeeded * pulse);
					
					if (freeSpaceInPacket == packetSize) // auf letztes Paket muss NICHT gewartet werden
						transmitDuration = timeTillNextPulse + (int)Math.round((double)packetsNeeded * pulse);
					else // auf letztes Paket muss gewartet werden
						transmitDuration = (int)Math.round((double)((double)packetsNeeded+(double)1) * pulse);

				}

			}
			
			return delayTillTransmitBegins + transmitDuration + (int)latency;
			
		}
		
		
		private int getNumberOfPacketsTransmittedSinceLastCall() {
			
			int result;
			
			if (lastPulse == NOT_SET)
				result = 0;
			else
				result = (int) Math.floor((double)(Simulator.getNow() - lastPulse) / (double)pulse);

			if (result < 0) 
				throw new RuntimeException("ERROR: numberOfPacketsTransmittedSinceLastCall < 0!");
			
			return result;
			
		}
		
	}
	
	/**
	 * Comment
	 *
	 * @param args Not used.
	 */
	/*public static void main(String[] args) {
		
		Settings.initialize("properties.txt");
		Simulator s = new Simulator(null);
		BasicDelayBox sdb = new BasicDelayBox(s, 100, 100, 10);
		sdb.getSendDelay(100000000);
		s.setNow(10000);
		sdb.getSendDelay(100000000); 
		s.setNow(14000);
		sdb.getSendDelay(100000000); 
		s.setNow(100000);
		sdb.getSendDelay(100000000);
		sdb.getSendDelay(100000000);
		sdb.getSendDelay(100000000);
		sdb.getSendDelay(100000000);
		s.setNow(1000000);
		sdb.getSendDelay(100000000+100000000+100000000+100000000);
		s.setNow(1020000);
		sdb.getSendDelay(100000000+100000000+100000000+100000000);
		s.setNow(2000000);
		sdb.getSendDelay(1);
		sdb.getSendDelay(1);
		sdb.getSendDelay(1);
		sdb.getSendDelay(1);
		sdb.getSendDelay(1);
		sdb.getSendDelay(1);
		sdb.getSendDelay(1);
		sdb.getSendDelay(1);
		sdb.getSendDelay(1);
		sdb.getSendDelay(1);
		s.setNow(2000001);
		sdb.getSendDelay(1);
		sdb.getSendDelay(1);
		sdb.getSendDelay(1);
		sdb.getSendDelay(1);
		sdb.getSendDelay(1);
		s.setNow(2000002);
		sdb.getSendDelay(1);
		sdb.getSendDelay(1);
		sdb.getSendDelay(1);
		sdb.getSendDelay(1);
		sdb.getSendDelay(1);
	}*/

}