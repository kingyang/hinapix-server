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

/**
 * This interface defines a patient Identifier cross reference manager (PIX Manager)actor. 
 * PIX Manager is a server side actor specified by IHE PIX profile. 
 * See Section 3.8, 3.9 and 3.10 of <a href="http://www.ihe.net/Technical_Framework/index.cfm#IT">Vol. 2 (ITI TF-2): Transactions</a>, 
 * available on the IHE site.   
 *
 * <p>This class needs to register a PIX Manager adapter which implements
 * {@link IPixManagerAdapter} by invoking {@link #registerPixManagerAdapter(IPixManagerAdapter)}.
 *  The adapter provides a bridge between this PIXManager actor and the EMPI.
 *
 * @author Wenzhi Li
 * @version 1.0, Mar 1, 2007
 */
public interface IPixManager {
    /**
	 * Starts this patient ID cross reference manager.  Do any initialization and logging that
	 * might be needed.
	 */
	public void start();

	/**
	 * Stops this patient ID cross reference manager.  Do any de-initialization and logging that
	 * might be needed.
	 *
	 */
	public void stop();

    /**
	 * Gets an informative name for this patient ID cross reference manager for use in error
	 * and log messages.
	 *
	 * @return An informative name for this patient ID cross reference source
	 */
	public String getName();
	
    /**
     * Registers a PixManagerAdapter which handles patient creation, update, merge and
     *  and query by patient Id.
     *
     * @param pixManagerAdapter the adapter
     * @see IPixManagerAdapter
     */
    public void registerPixManagerAdapter(IPixManagerAdapter pixManagerAdapter);
}
