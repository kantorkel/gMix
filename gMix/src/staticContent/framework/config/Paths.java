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
package staticContent.framework.config;

import java.io.FileInputStream;
import java.util.Properties;


public class Paths {

	// global config file
	public final static String PATH_TO_PATH_CONFIG_FILE = "./inputOutput/global/paths.txt";
	private static Properties properties;
	static {
		properties = new Properties();
		try {
			properties.load(new FileInputStream(PATH_TO_PATH_CONFIG_FILE));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("property file " +PATH_TO_PATH_CONFIG_FILE +" could not be loaded!");
		}
	}
	
	// anon node config
	public final static String ANON_NODE_CONFIG_FILE_PATH = properties.getProperty("ANON_NODE_CONFIG_FILE_PATH");
	
	// simulator 
	public final static String SIM_PROPERTY_FILE_PATH = properties.getProperty("SIM_PROPERTY_FILE_PATH");
	public final static String SIM_OUTPUT_FOLDER_PATH = properties.getProperty("SIM_OUTPUT_FOLDER_PATH");
	public final static String SIM_PLOTSCRIPT_FOLDER_PATH = properties.getProperty("SIM_PLOTSCRIPT_FOLDER_PATH");
	public final static String SIM_EXPERIMENT_DEFINITION_FOLDER_PATH = properties.getProperty("SIM_EXPERIMENT_DEFINITION_FOLDER_PATH");
	public final static String SIM_ETC_FOLDER_PATH = properties.getProperty("SIM_ETC_FOLDER_PATH");

	// global logger
	public final static String GL_OUTPUT_PATH = properties.getProperty("GL_OUTPUT_PATH");
	
	// loadGenerator
	public final static String LG_PROPERTY_FILE_PATH = properties.getProperty("LG_PROPERTY_FILE_PATH");
	
	
	public static String getProperty(String key) {
		return properties.getProperty(key); 
	}
}
