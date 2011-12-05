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

package framework;

import inputOutputHandler.InputOutputHandlerController;
import java.util.logging.Logger;
import keyGenerator.KeyGeneratorController;
import messageProcessor.MessageProcessorController;
import networkClock.NetworkClockController;
import outputStrategy.OutputStrategyController;
import userDatabase.UserDatabaseController;
import externalInformationPort.ExternalInformationPortController;


public abstract class Implementation implements ComponentReferences {
	
	protected InputOutputHandlerController inputOutputHandler;
	protected OutputStrategyController outputStrategy;
	protected KeyGeneratorController keyGenerator;
	protected MessageProcessorController messageProcessor;
	protected ExternalInformationPortController externalInformationPort;
	protected NetworkClockController networkClock;
	protected UserDatabaseController userDatabase;
	protected Mix mix;
	protected Logger logger;
	protected Settings settings;
	protected Controller controller;
	
	//showing the actual implementation time of all the components...
	static {
		System.out.println(Implementation.class.getSimpleName());
	}
	
	public Implementation() {
		System.out.println(this.getBinaryName());
	}
	
	//overrides aus der Schnittstelle ComponentReferences
	@Override
	public InputOutputHandlerController getInputOutputHandler() {
		return inputOutputHandler;
	}


	@Override
	public OutputStrategyController getOutputStrategy() {
		return outputStrategy;
	}


	@Override
	public KeyGeneratorController getKeyGenerator() {
		return keyGenerator;
	}


	@Override
	public MessageProcessorController getMessageProcessor() {
		return messageProcessor;
	}


	@Override
	public ExternalInformationPortController getExternalInformationPort() {
		return externalInformationPort;
	}


	@Override
	public NetworkClockController getNetworkClock() {
		return networkClock;
	}


	@Override
	public UserDatabaseController getUserDatabase() {
		return userDatabase;
	}
	
	
	@Override
	public Mix getMix() {
		return mix;
	}
	
	@Override
	public Logger getLogger() {
		return logger;
	}
	
	
	@Override
	public Settings getSettings() {
		return settings;
	}
	
	
	public void setController(Controller controller) {
		
		this.controller = controller;
		this.inputOutputHandler = controller.getInputOutputHandler();
		this.outputStrategy = controller.getOutputStrategy();
		this.keyGenerator = controller.getKeyGenerator();
		this.messageProcessor = controller.getMessageProcessor();
		this.externalInformationPort = controller.getExternalInformationPort();
		this.networkClock = controller.getNetworkClock();
		this.userDatabase = controller.getUserDatabase();
		this.logger = controller.getLogger();
		this.settings = controller.getSettings();
		this.mix = controller.getMix();
		this.mix.registerImplementation(this);
		this.constructor();
		controller.setImplementation(this);
		
	}
	
	// Pseudokonstruktor - wird aufgerufen sobald Klasse von ClassLoader instanziiert wurde
	public abstract void constructor();
	
	// referenzen auf andere Komponenten sind gesetzt; ggf. Daten an andere Komponenten schicken/
	// von anderen holen
	// bei "return" muss Komponente bereit sein ihren Betreib aufzunehmen
	public abstract void initialize();
	
	// bei Aufruf muss Komponente mit ihrer Nutzfunktion beginnen
	public abstract void begin();
	
	// TODO: laden des strings mit den unterst�tzten implementierungen �ber instanzmethoden (so wie hier umgesetzt) nicht optimal,
	// weil so eine konkrete implementierung erst instanziiert werden muss, um ihre abh�ngigkeiten zu erfahren...
	// dies sollte vor dem instanziieren m�glich sein! -> l�sung �ber code-annotations o.�.
	// (anforderung: nutzer muss sp�testetns beim compilieren eine fehlermeldung erhalten,
	// wenn er keine abh�ngigkeiten f�r seine implementierung angegeben hat! -> nicht erst zur laufzeit)
	public abstract String[] getCompatibleImplementations();
	
	// name of property file must be className.properties (e.g. BasicBatch.properties for the implementation BasicBatch.java)
	// TODO: wo sollen die property-dateien liegen m�ssen? (bei class files: -> werden dann normal nicht in eclipse angezeigt;
	// bei source-files: schlecht f�r deployment...)
	public abstract boolean usesPropertyFile();
	
		
	public String getBinaryName() {
		return this.getClass().getName();
	}
	
}