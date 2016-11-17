/*
 * Title:       Poolable
 * Description:
 * Copyright:   (c) 2002
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              7th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ils.utility.pool;

/**
  The interface that is used by {@link com.carescience.utility.pool.Pool} 
  to define a class of object that may be pooled.  
 */
public interface Poolable
{
    /**
       Called to initialize a new Poolable object.  This will be called
       once for each instance before the first time it is used.  You
       may use this to set up anything specific that is need by the 
       object.

       @param param Optional parameter object passed in during Pool
                    construction.
    */
    public void poolInit(Object param);

    /**
       Called after this Poolable object is removed from the pool
       because of shrinkage or other reasons.  You can use this
       to close Connections, dispose of objects, etc.  It will
       only be called once per instance.
    */
    public void poolDelete();
}
