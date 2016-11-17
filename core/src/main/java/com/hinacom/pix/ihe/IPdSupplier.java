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
 * This interface defines a patient demographics supplier (PDS) actor. PDS
 * is a server side actor specified by IHE patient demographics query (PDQ) 
 * profile. See Section 3.21 of <a href="http://www.ihe.net/Technical_Framework/index.cfm#IT">Vol. 2 (ITI TF-2): Transactions</a>, 
 * available on the IHE site.   
 *
 * <p>This class needs to register a PDS adapter which implements
 * {@link IPdSupplierAdapter} by invoking {@link #registerPdSupplierAdapter(IPdSupplierAdapter)}.
 *  The adapter provides a bridge between this PDSupplier actor and the EMPI.
 * 
 * @author Wenzhi Li
 * @version 1.0, Mar 27, 2007
 * @see IPdSupplierAdapter
 */
public interface IPdSupplier {
    /**
	 * Starts this patient demographics supplier actor.  Do any initialization and logging that
	 * might be needed.
	 */
	public void start();

	/**
	 * Stops this patient demographics supplier actor.  Do any de-initialization and logging that
	 * might be needed.
	 *
	 */
	public void stop();

    /**
	 * Gets an informative name for this patient demographics supplier actor for use in error
	 * and log messages.
	 *
	 * @return an informative name for this patient demographics supplier actor
	 */
	public String getName();

    /**
     * Registers a PdSupplier adapter which provides patient demographics data for 
     * patient demographics query (PDQ).
     *
     * @param pdSupplierAdapter
     * @see IPdSupplierAdapter
     */
    public void registerPdSupplierAdapter(IPdSupplierAdapter pdSupplierAdapter);
}
