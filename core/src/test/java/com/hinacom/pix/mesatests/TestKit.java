package com.hinacom.pix.mesatests;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import com.hinacom.pix.ihe.configuration.ConfigurationLoader;
import com.hinacom.pix.ihe.log.IMesaLogger;

/**
 * @author Wenzhi Li
 * @version 1.0, Dec 23, 2008
 */
public class TestKit {
	
	public static void configActor(IMesaLogger log, String ... actor) {

		ConfigurationLoader loader = ConfigurationLoader.getInstance();

        try {
            URL url = TestKit.class.getResource("/mesatests/actors/IheActors.xml");
            File file = new File(url.toURI());
            loader.loadConfiguration(file, false, null, null, null, null, null, null);
            
            List actors = Arrays.asList(actor);
            //reset to add log file
            //loader.resetConfiguration(actors, "c:\\testlog.xml", log);
            loader.resetConfiguration(actors, null, log);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static String readTextFile(String file) throws IOException {
    	StringBuffer sb = new StringBuffer(1024);
    	BufferedReader reader = new BufferedReader(new FileReader(file));
    			
    	char[] chars = new char[1024];
    	int numRead = 0;
    	while( (numRead = reader.read(chars)) > -1){
    		sb.append(String.valueOf(chars));	
    	}

    	reader.close();

    	return sb.toString();
    }	
}
 

