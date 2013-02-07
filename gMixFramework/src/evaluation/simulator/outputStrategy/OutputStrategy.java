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
package evaluation.simulator.outputStrategy;

import evaluation.simulator.communicationBehaviour.ReplyReceiver;
import evaluation.simulator.core.Simulator;
import evaluation.simulator.message.MixMessage;
import evaluation.simulator.message.TransportMessage;
import evaluation.simulator.networkComponent.Mix;


public abstract class OutputStrategy implements ReplyReceiver {

	protected Simulator simulator;
	protected Mix mix;
	protected static boolean stopReplying = false;
	
	
	public static OutputStrategy getInstance(Mix mix, Simulator simulator) {
		
		String type = Simulator.settings.getProperty("OUTPUT_STRATEGY");
		
		if (type.equals("BASIC_SYNCHRONOUS_BATCH"))
			return new BasicSynchronousBatch(mix, simulator);
		else if (type.equals("BATCH"))
			return new Batch(mix, simulator);
		else if (type.equals("DISTINCT_USER_BATCH"))
			return new DistinctUserBatch(mix, simulator);
		else if (type.equals("NONE"))
			return new NoDelay(mix, simulator);
		else if (type.equals("COTTRELL_POOL"))
			return new CottrellPool(mix, simulator);
		else if (type.equals("COTTRELL_TIMED_POOL"))
			return new CottrellTimedPool(mix, simulator);
		else if (type.equals("BINOMIAL_POOL"))
			return new BinomialPool(mix, simulator);
		else if (type.equals("STOP_AND_GO"))
			return new StopAndGo(mix, simulator);
		else if (type.equals("BASIC_DLPA"))
			return new DLPABasic(mix, simulator);
		else if (type.equals("HEURISTIC_DLPA"))
			return new DLPAHeuristic(mix, simulator);
		else if (type.equals("LOSSY_SYNCHRONOUS_BATCH"))
			return new LossySynchronousBatch(mix, simulator);
		else
			throw new RuntimeException("ERROR: unknown OUTPUT_STRATEGY specified: " +type);
		
	}
	
	
	protected OutputStrategy(Mix mix, Simulator simulator) {
		
		this.mix = mix;
		this.simulator = simulator;
		
	}
	

	/**
	 * request from client or previous mix
	 * must call mix.putOutRequest(NetworkMessage networkMessage)
	 */
	public abstract void incomingRequest(MixMessage mixMessage);
	
	
	/**
	 * reply from next mix
	 * must call mix.putOutReply(MixMessage mixMessage)
	 */
	public abstract void incomingReply(MixMessage mixMessage);

	
	/**
	 * reply from distant proxy
	 * must call mix.putOutReply(MixMessage mixMessage)
	 */
	public abstract void incomingReply(TransportMessage noneMixMessage);
	
	
	// called when no more traffic will be generated
	public static void stopReplying() {
		stopReplying = true;
	}

	
	public static void reset() {
		stopReplying = false;
	}
	
	
	public void setOwner(Mix owner) {
		this.mix = owner;
	}


	public Mix getOwner() {
		return mix;
	}
	
}
