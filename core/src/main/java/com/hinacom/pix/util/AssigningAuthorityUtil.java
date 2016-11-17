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
package com.hinacom.pix.util;

import java.util.List;

import com.misyshealthcare.connect.net.IConnectionDescription;
import com.misyshealthcare.connect.net.Identifier;


/**
 * This class contains the utility methods for 
 * assigning authority.
 *
 * @author Wenzhi Li
 * @version 1.0, Nov 27, 2008
 */
public class AssigningAuthorityUtil {
    /**
     * Reconciles authority with the ConnectionDescritpion configuration. An authority
     * can have NameSpace and/or UniversalId/UniversalIdType. For example, in the data source such as
     * database, if an authority is represented by NameSpace only, while in the xml configuration, the authority is configured
     * with both NameSpace and UnviersalId/UniversalIdType. The authority in the datasource has to be mapped
     * to the authority configured in the XML files.
     *
     * @param authority The authority
     * @param connection
     * @return The authority according the configuration
     */
    public static Identifier reconcileIdentifier(Identifier authority, IConnectionDescription connection) {
        List<Identifier> identifiers = connection.getAllIdentifiersByType("domain");
        for (Identifier identifier : identifiers) {
            if ( identifier.equals(authority) ) {
                return identifier;
            }
        }
        //no identifier is found, just return the original authority
        return authority;
    }
    

    /**
     * Validates whether an ID domain is valid against the connection configuration.
     *
     * @param id the feed or request ID domain to be validated
     * @param description
     * @return <code>true</code> if the idDomain is valid.
     */
    public static boolean validateDomain(Identifier id, IConnectionDescription description) {
         if (id == null) return  false;

         List<Identifier> identifiers = description.getAllIdentifiersByType("domain");
         for (Identifier identifier : identifiers) {
             if (identifier.equals(id))
                return true;
         }
         return false;
    }
    

}
