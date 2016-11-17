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
package com.hinacom.pix.ihe.impl_v2;

import com.hinacom.pix.ihe.log.IMessageStoreLogger;
import com.hinacom.pix.ihe.log.MessageStore;

/**
 * An Mock up MessageStoreLogger
 * 
 * @author Wenzhi Li
 * @version 1.0, Dec 22, 2008
 */
public class MockMessageStoreLogger implements IMessageStoreLogger {

   public void saveLog(MessageStore store) {
		if (store == null) return;
		System.out.println("Inbound Message:");
		System.out.println(store.getInMessage());
		System.out.println("Outbound Message:");
		System.out.println(store.getOutMessage());
   }
  
}
