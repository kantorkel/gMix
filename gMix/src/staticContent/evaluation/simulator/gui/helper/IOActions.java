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
package staticContent.evaluation.simulator.gui.helper;

import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;

import staticContent.evaluation.simulator.gui.results.GnuplotPanel;

/**
 * Cleans the output folder
 * 
 * @author nachkonvention
 * 
 */
public class IOActions {
	
	/**
	 * Cleans up the output folder
	 * 
	 * @throws IOException
	 */
	public static void cleanOutputFolder() throws IOException {
		FileUtils.cleanDirectory(GnuplotPanel.outputFolder);
	}
}
