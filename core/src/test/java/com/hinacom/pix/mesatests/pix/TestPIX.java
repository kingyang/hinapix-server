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

package com.hinacom.pix.mesatests.pix;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.hinacom.pix.ihe.PatientBroker;
import com.hinacom.pix.ihe.configuration.ConfigurationLoader;
import com.hinacom.pix.mesatests.MesaTestLogger;
import com.hinacom.pix.mesatests.TestKit;

import com.misyshealthcare.connect.net.IConnectionDescription;

/**
 * Test for PIX Manager. 
 * See http://ihewiki.wustl.edu/wiki/index.php/MESA/Patient_Cross_Reference_Manager
 * <p>
 * This Class can handle the following Mesa Test Cases:
 *   #10501 (PIX Feed and PIX Query Cases 1 and 2)
 *   #10502  PIX Query Case 3
 *   #10503  PIX Query Case 4
 *   #10506  PIX Query, PIX Update
 *   #10512  PIX Feed A04
 * 
 * @author Wenzhi Li
 * @version 1.0, Dec 23, 2008
 */
public class TestPIX {

	public static void main(String[] args) {
		String test = "PIXManager";
    	MesaTestLogger logger = new MesaTestLogger(System.out);
		logger.writeTestBegin(test);
		
		TestKit.configActor(logger, "pixman");
		ConfigurationLoader loader = ConfigurationLoader.getInstance();
        ConfigurationLoader.ActorDescription actor = loader.getDescriptionById("pixman");
        IConnectionDescription connection = actor.getConnection();

        try {
        	while (true) {
        		System.out.println("Enter \"q\" to quit>");
		        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		        String in = br.readLine();
		        if (in.equals("q")) {        
			        //gracefully exit and shut down the Pix Server
			        PatientBroker.getInstance().unregisterPixManagers(null);
			        break;
		        }
        	}
        }
        catch(Exception e) {
        	e.printStackTrace();
        }
		logger.writeTestEnd(test);
	}
}
