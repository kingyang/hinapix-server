package org.openempi.ics.ccs;

import org.openempi.ics.utility.IcsException;

/**
 * General exception thrown by the <code>CorrelationSystem</code>.
 *
 * @author CareScience
 * @version 1.3, 20010822
 * @see CorrelationSystem
 */
class CorrelationSystemException 
    extends IcsException 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 6669997036067282752L;

	CorrelationSystemException() {
        super();
    }
    
    CorrelationSystemException(String message) {
        super(message);
    }
    
    CorrelationSystemException(Throwable throwable) {
        super(throwable);
    }
}

