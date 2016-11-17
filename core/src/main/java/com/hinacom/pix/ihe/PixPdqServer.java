/* Copyright 2009 Misys PLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License. 
 */
package com.hinacom.pix.ihe;

import com.hinacom.pix.ihe.configuration.ConfigurationLoader;
import com.hinacom.pix.ihe.configuration.IheConfigurationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * This class manages the stand alone PIXPDQ server startup and shutdown.
 * 
 * @author Wenzhi Li
 * @version 1.0, Mar 15, 2009
 */
public class PixPdqServer {

	/**
	 * The main method to start up or shut down PIX and PDQ servers.
	 * 
	 * @param args For server startup, it is expected to have 2 arguments.
	 *        The first is "startup"; the second one is the full file 
	 *        path to IheActors.xml.  
	 *        <p>
	 *        For server shutdown, provide just one argument "shutdown".  
	 */
	public static void main(String[] args) {
		if (args.length < 1 || args.length > 2 ||
		    (args.length == 1 && !args[0].equalsIgnoreCase("shutdown")) ||
		    (args.length == 2 && !args[0].equalsIgnoreCase("startup")) ) {
			printUsage();
			return ;
		}

		if (args.length == 2 && args[0].equalsIgnoreCase("startup") ) {
            String configPath = args[1];
            String applicationContextPath = String.format("file:%s/applicationContext.xml", configPath);

            ApplicationContext applicationContext = new ClassPathXmlApplicationContext(applicationContextPath);
            ConfigurationLoader loader = (ConfigurationLoader)applicationContext.getBean("configurationLoader");

            //Start up the servers
			// ConfigurationLoader loader = ConfigurationLoader.getInstance();
			String actorFile = String.format("%s/IheActors.xml", configPath);
	        try {
	            loader.loadConfiguration(actorFile, true);
	        } catch (IheConfigurationException e) {
	            e.printStackTrace();
	        }
		} 
		else if (args.length == 1 && args[0].equalsIgnoreCase("shutdown")) {
			//Shut down all the active servers
			ConfigurationLoader.getInstance().resetAllBrokers();
		}

	}
	
	/**
	 * Prints the usage of how to start up or shutdown this PIX/PDQ server.
	 */
	private static void printUsage() {
		System.out.println("*********************************************************");
		System.out.println("WRONG USAGE: PIXPDQ server expects 2 arguments.");
		System.out.println("To start up the server: ");
	    System.out.println("   java PixPdqServer startup <full path of IheActors.xml>");
		System.out.println("To shut down the server: ");
	    System.out.println("   java PixPdqServer shutdown");
		System.out.println("*********************************************************");		
	}
}
