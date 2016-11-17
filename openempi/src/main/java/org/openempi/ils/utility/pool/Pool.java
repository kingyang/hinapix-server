/*
 * Title:       Pool
 * Description:
 * Copyright:   (c) 2002
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              7th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ils.utility.pool;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

/**
   A general-purpose object pooling class.  This allows you to create a
   pool of any other class of objects that implements the Poolable
   interface.
 */
public class Pool
{
  /**
     Static vector of all created pools.  Useful for monitoring
     and general fiddling.
  */
  private static Vector pools = new Vector();
  
  /**
     A friendly name for the Pool.  If not explicitly set it will
     be generated from the classname.
  */
  private String name;
  
  /**
     The class of objects being pooled.  Must implement Poolable
  */
  private Class cls;
  
  /**
     Optional parameter object passed to Poolable poolInit() to 
     help with creation of pooled objects.
  */
  private Object param = null;
  
  /**
     A list of available PoolObjects objects.  This is the list from
     which Poolable objects are allocated.
  */
  private LinkedList avail;
  
  /**
     A Map of currently allocated PoolObjects.  These are all of
     the objects currently in use.
  */
  private HashMap alloc;
  
  /**
     The minimum size of the Pool.  The pool will never shrink
     below this size
  */
  private int minSize = 0;
  
  /**
     The maximum size of the Pool.  When curSize hits maxSize,
     the pool is at its upper limit and will grow no more
  */
  private int maxSize = 0;
  
  /**
     The current size of the pool.  This is the total number of
     poolable objects in the pool. It will always be between
     minSize and maxSize
  */
  private int curSize = 0;
  
  /**
     The increment by which the pool will grow when it is below
     maxSize and more pooled objects are needed.  Regardless
     of this size, it will never grow beyond maxSize
  */
  private int growInc = 1;
  
  /**
     Number of milliseconds of idle time for a Poolable object
     before it is disposed of.  This allows the pool to shrink
     and grow as needed based on the system load.
     Setting this to 0 means the pool will never shrink.
  */
  private long shrinkAfter = 0;
  
  /**
     The last time the pool was checked for shrinkage.  This is used
     to compare against the current time to see if enough time has
     gone by to even begin to check if the pool should shrink
  */
  private long lastCheck = 0;
  
  /**
     If true, a call to get() will block when the pool is completely
     allocated and at maxSize.  When a poolable object is released,
     get() will return with the released object.
     If false, get() will never block but will allocate non-pooled
     objects to return to the called
  */
  private boolean blockOnMax = true;
  
  /**
     Total number of threads currently waiting for a pooled object.
  */
  private int waiters = 0;
  
  /**
     Total number of times since startup that the Pool max has been
     hit.
  */
  private long maxHits = 0;


  /**
     Thread that runs the periodic check for pool shrinking
  */
  private Thread shrinkerThread = null;
  
  /**
     @param classname The name of the class of objects this pool will hold.
                      Must implement Poolable.
     @param minSize The initial size of the pool.  It will never shrink
                    below this point.
     @param maxSize The maximum number of object pooled
     
     @exception IllegalArgumentException If the specified class does not
                implement Poolable
  */
  public Pool(String classname, int minSize, int maxSize)
  {
    this(lookupClass(classname), null, minSize, maxSize);
  }
  
  /**
     @param classname The name of the class of objects this pool will hold.
                      Must implement Poolable.
     @param param Parameter object passed to poolInit(). 
     @param minSize The initial size of the pool.  It will never shrink
                    below this point.
     @param maxSize The maximum number of object pooled
     
     @exception IllegalArgumentException If the specified class does not
                implement Poolable
  */
  public Pool(String classname, Object param, int minSize, int maxSize)
  {
    this(lookupClass(classname), param, minSize, maxSize);
  }
  
  /**
     @param cls The class of objects to pool. Must implement Poolable.
     @param minSize The initial size of the pool.  It will never shrink
                    below this point.
     @param maxSize The maximum number of object pooled
     
     @exception IllegalArgumentException If the specified class does not
                implement Poolable
  */
  public Pool(Class cls, int minSize, int maxSize)
  {
    this(cls, null, minSize, maxSize);
  }

  /**
     @param cls The class of objects to pool. Must implement Poolable.
     @param param Parameter object passed to poolInit(). 
     @param minSize The initial size of the pool.  It will never shrink
                    below this point.
     @param maxSize The maximum number of object pooled
     
     @exception IllegalArgumentException If the specified class does not
                implement Poolable
  */
  public Pool(Class cls, Object param, int minSize, int maxSize)
  {
    if (! isPoolable(cls))
      throw new IllegalArgumentException("Class " + cls + " does not implement Poolable");
    
    avail = new LinkedList();
    alloc = new HashMap();
    
    this.cls = cls;
    this.minSize = minSize;
    this.maxSize = maxSize;
    growPool(minSize);
    this.param = param;
    
    int idx;
    this.name = cls.getName();
    this.name = name.substring(name.lastIndexOf(".")+1);
    if ((idx = name.lastIndexOf("$")) != -1)
      this.name = name.substring(idx + 1);
    
    pools.add(this);
  }
  
  /**
     Called to dispose of a pool when you no longer want to use it.  
     This will wait until all currently allocated objects are 
     returned before completing.  It is  the responsibility of the caller 
     to call close() when done with the pool and to make sure they don't 
     use any of the get/release methods after close().
  */
  public synchronized void close()
  {
    PooledObject pobj;
    
    pools.remove(this);
    while(alloc.size() > 0) {
      try {
        wait();
      } catch (InterruptedException e) {
      }
    }
    
    minSize = maxSize = 0;
    
    for(int i = 0; i < avail.size(); i++) {
      pobj = (PooledObject) avail.get(i);
      pobj.delete();
    }
  }
  
  /**
     Sets the friendly name of the pool.  
     
     @param name New name for this pool
  */
  public void setName(String name)
  {
    this.name = name;
  }
  
  /**
     Gets the friendly name of the pool.  If setName() is not called,
     the name returned will be extracted from the name of the class
     being pooled.
  */
  public String getName()
  {
    return this.name;
  }
  
  /**
     Sets the maximum size of the pool. The pool will not be immediately
     affected by this change but will eventually grow or shrink to fit
     the size as it is used.  If the new max is less than the current
     minimum size, the minimum size of the pool is set to the new max.
     
     @param max The new maximum pool size
  */
  public synchronized void setMaxSize(int max)
  {
    maxSize = max;
    if (minSize > maxSize)
      minSize = maxSize;
  }
  
  /**
     @return The maximum size of the pool
  */
  public synchronized int getMaxSize()
  {
    return maxSize;
  }
  
  /**
     Sets the minimum size of the pool. The pool will not be immediately
     affected by this change but will eventually grow or shrink to fit
     the size as it is used.  If the new min is greater than the current
     maximum size, the maximum size of the pool is set to the new min.
     
     @param max The new maximum pool size
  */
  public synchronized void setMinSize(int min)
  {
    minSize = min;
    if (minSize > maxSize)
      maxSize = minSize;
    growPool(minSize);
  }
  
  /**
     @return The maximum size of the pool
  */
  public synchronized int getMinSize()
  {
    return minSize;
  }
  
  /**
     Set the increment to be used when the pool grows.  Regardless of
     this setting, the pool will never grow beyond maxSize.  The
     default increment is 1.
     
     @param inc The increment to use
  */
  public synchronized void setGrowIncrement(int inc)
  {
    if (inc <= 0)
      throw new IllegalArgumentException("setGrowIncrement must be supplied positive value");
    growInc = inc;
  }
  
  /**
     @return  The current pool grow increment
  */
  public synchronized int getGrowIncrement()
  {
    return growInc;
  }
  
  /**
     Milliseconds of inactivity after which unused Pooled objects will be
     freed or 0 to diable shrinking.  The default is 0, shrinking is
     disabled.
     
     @param shrink Milliseconds of inactivity.
  */
  public synchronized void setShrinkAfter(long shrink)
  {
    if (shrink < 0)
      throw new IllegalArgumentException("setShrinkAfter must be supplied non-negative value");
    shrinkAfter = shrink;
  }

  /**
     @return The current shrink after milliseconds
  */
  public synchronized long getShrinkAfter()
  {
    return shrinkAfter;
  }

  /**
     @param block If true, this get() will block when maxSize has been hit.
                    If false, get() will always return an object even
                    if max is hit but the objects will not be part of
                    the pool.  Default is true.
    */
  public synchronized void setBlockOnMax(boolean block)
  {
    blockOnMax = block;
  }

  /**
     @return The current blockOnMax setting
  */
  public synchronized boolean getBlockOnMax()
  {
    return blockOnMax;
  }

  /**
     @return The class being pooled
  */
  public synchronized Class getPoolCls()
  {
    return cls;
  }

  /**
       @return The number of available pooled objects
    */
  public synchronized int getAvailable()
  {
    return avail.size();
  }

  /**
     @return The number of allocated pooled objects
  */
  public synchronized int getAllocated()
  {
    return alloc.size();
  }

  /**
     @return The number of threads currently waiting for a pooled object.
  */
  public synchronized int getWaiters()
  {
    return waiters;
  }

  /**
     @return The total number of times the Pool max has been hit
  */
  public synchronized long getMaxHits()
  {
    return maxHits;
  }

  /**
     Returns an object from the Pool.  The called must call
     the Pool release() method on the returned object when they
     are done with it.
     
     If the pool is at the max size and there are no poolable objects
     available, get() will do one of two things.  If blockOnMax is
     set to true, the get() will block and wait for a poolable
     object to become available.  When one does, get() will return it.
     If blockOnMax is false, get() will always return a valid object
     even if above the available maximum.  Such objects are not directly
     tracked by the Pool but you must still call the Pool release() method
     on any object returned by this method.
     
     @return The Poolable object
  */
  public Poolable get()
  {
    PooledObject pobj = null;
    boolean init = false;

    synchronized(this) {
      while(true) {
        if (avail.size() == 0)
          growPool(curSize + growInc);
        if (avail.size() > 0) {
          pobj = (PooledObject) avail.removeFirst();
          alloc.put(pobj.getObj(), pobj);
          break;
        }
        maxHits++;
        if (blockOnMax) {
          try {
            waiters++;
            wait();
            waiters--;
          } catch (InterruptedException e) {
          }
        } else {
          try {
            pobj = new PooledObject((Poolable) cls.newInstance());
          } catch (Exception e) {
            /* Shouldn't happen unless memory is low or such */
            e.printStackTrace();
            return null;
          }
          break;
        }
      }
    }

    pobj.init();
    return pobj.getObj();
  }

  /**
     Forces the pool to be shrunk immediately.
  */
  public void shrink()
  {
    PooledObject pobj;
    ArrayList deleted = new ArrayList();

    synchronized (this) {
      shrinkPool(deleted);
    }
    Iterator iter = deleted.iterator();
    while(iter.hasNext()) {
      pobj = (PooledObject) iter.next();
      pobj.delete();
    }
  }

  /**
     Called to return a Poolable object retrieved by Pool get()
     to the pool.  This must be called when the caller is done
     using the Poolable object.
     
     @param p The Poolable object to be released
  */
  public void release(Poolable p)
  {
    release(p, false);
  }

  /**
     Called to return a Poolable object retrieved by Pool get()
     to the pool.  This must be called when the caller is done
     using the Poolable object.
     
     @param p The Poolable object to be released
     @param delete Forces the release object to be deleted rather
     than re-added to the available list.
  */
  public void release(Poolable p, boolean delete)
  {
    PooledObject pobj;
    ArrayList deleted = new ArrayList();

    synchronized(this) {
      pobj = (PooledObject) alloc.remove(p);
      if (pobj != null) {
        if (! delete && avail.size() < maxSize) {
          pobj.setLastAccess(System.currentTimeMillis());
          avail.addFirst(pobj);
        } else {
          deleted.add(pobj);
        }
      } else {
        deleted.add(new PooledObject(p, true));
      }
      notify();
      if (shrinkerThread == null) {
        shrinkerThread = shrinkerThread = new Thread(new Shrinker());
        shrinkerThread.setContextClassLoader(Thread.currentThread().getContextClassLoader());
        shrinkerThread.start();
      }
    }
    Iterator iter = deleted.iterator();
    while(iter.hasNext()) {
      pobj = (PooledObject) iter.next();
      pobj.delete();
    }
  }

  /**
     Returns an iterator of all Pool objects currently in use. 
     Useful for debugging and fiddling around with pool properties
     
     @return Iterator over all currently allocated Pool objects
  */
  public static Iterator poolsIterator()
  {
    return pools.iterator();
  }

  /**
     Prints out debug information about the currently allocated
     objects for this pool.  This includes the allocated object,
     time it was allocated and a stack trace indicating from whence
     it was allocated.
  */
  public void printAllocated()
  {
    printAllocated(System.out);
  }

  /**
     Prints out debug information about the currently allocated
     objects for this pool.  This includes the allocated object,
     time it was allocated and a stack trace indicating from whence
     it was allocated.
     
     @param out PrintStream to write to
  */
  public void printAllocated(PrintStream out)
  {
    printAllocated(new PrintWriter(out, true));
  }

  /**
     Prints out debug information about the currently allocated
     objects for this pool.  This includes the allocated object,
     time it was allocated and a stack trace indicating from whence
     it was allocated.
     
     @param out Writer to write to
  */
  public void printAllocated(Writer out)
  {
    printAllocated(new PrintWriter(out, true));
  }

  /**
     Prints out debug information about the currently allocated
     objects for this pool.  This includes the allocated object,
     time it was allocated and a stack trace indicating from whence
     it was allocated.
     
     @param out PrintWriter to write to
  */
  public synchronized void printAllocated(PrintWriter out)
  {
    PooledObject pobj;
    Iterator iter = alloc.values().iterator();
    SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss aaa");

    while(iter.hasNext()) {
      StringBuffer buf = new StringBuffer();
      pobj = (PooledObject) iter.next();
      out.println("Object: " + pobj.getObj());
      out.print("Time: ");
      df.format(new Date(pobj.getAllocTime()), buf, new FieldPosition(0));
      out.println(buf.toString());
      out.println("Stack trace:");
      pobj.printStackTrace(out);
    }
  }

  /**
     Called to check for and shrink the pool.  This will check the
     lastCheck time of the pool and will only examine the pool if
     at least shrinkAfter milliseconds have passes since last time
     we checked.
     This will examine each member of the avail list and will
     remove any element from that list that hasn't been used in
     at least shrinkAfter millis.  Elements removed from the avail
     List are added to the delete ArrayList passed in for cleanup
     later.  This is done because the shrinkPool method is called
     within other sychronized methods and we don't want to call
     the poolDelete() methods within pool synchronization.
     
     @param deleted A list of Poolable objects removed from the
     pool to be deleted later.
  */
  private void shrinkPool(ArrayList deleted)
  {
    PooledObject pobj;
    long now = System.currentTimeMillis();

    for(int i = avail.size() - 1; i >= 0 && curSize > minSize; i--) {
      pobj = (PooledObject) avail.get(i);
      if (now > pobj.getLastAccess() + shrinkAfter) {
        avail.remove(i);
        deleted.add(pobj);
        curSize--;
      }
    }
  }

  /**
     Called to cause the number of currently allocated pool elements
     to be changed to the given newSize.  If newSize is greater than
     maxSize then newSize is set to maxSize.  If newSize is less than
     curSize, the pool does not immediately shrink but will shrink
     as it is used.  If newSize is less than minSize then newSize
     is set to minSize;
     
     @param newSize The new size for the pool
  */
  private void growPool(int newSize)
  {
    if (newSize > maxSize)
      newSize = maxSize;
    if (newSize < minSize)
      newSize = minSize;
    for (; curSize < newSize; curSize++) {
      try {
        avail.addLast(new PooledObject((Poolable) cls.newInstance()));
      } catch (InstantiationException e) {
        System.err.println("Error instantiating new Poolable object of class: " + cls);
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        System.err.println("Error instantiating new Poolable object of class: " + cls);
        e.printStackTrace();
      }
    }
  }

  /**
     Utility method to verify that the given class is Poolable and
     may be used.
     
     @param cls The class to check
     @return true if cls implements Poolable, false if not
  */
  private static boolean isPoolable(Class cls)
  {
    try {
      Object o = cls.newInstance();
      return o instanceof Poolable;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
     Utility method to perform class name to Class lookup.  This is
     a method instead of in-lining it in the constructor because
     forName can throw exceptions.  If you in-line it, you cannot
     call the this() superclass constructor as it must be the first
     line of code in a constructor
     
     @param className The name of the class to look up
     @return The Class object
     @exception IllegalArgumentException if class lookup fails
  */
  private static Class lookupClass(String className)
  {
    try {
      return Class.forName(className);
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalArgumentException("Bad classname for new pool: " + className);
    }
  }

  /**
     A PooledObject encapsulates each Poolable object in the Pool.
     It tracks current state of each Poolable object.
  */
  private class PooledObject
  {
    /**
       The Poolable object we're tracking
    */
    private Poolable obj;

    /**
       The time in milliseconds when the poolable object was
       last allocated.
    */
    private long allocTime;

    /**
       Last time this Poolable object was used.  It it set
       to the current time in millis when a Poolable object
       is released
    */
    private long lastAccess;
    
    /**
       If true, the poolInit() method has been called on the
       Poolable object.  If false, poolInit() has yet to be called
    */
    private boolean initted;
    
    /**
       Keeps the stack trace for currently allocated pooled
       objects so we can debug them when they aren't released
    */
    private Throwable stack;

    public PooledObject(Poolable obj)
    {
      this.obj = obj;
      lastAccess = 0;
      initted = false;
    }

    public PooledObject(Poolable obj, boolean initted)
    {
      this.obj = obj;
      lastAccess = 0;
      this.initted = initted;
    }

    /**
       @return The Poolable object tracked herein
    */
    public Poolable getObj()
    {
      return obj;
    }

    /**
       Initializes the Poolable object by callng the poolInit()
       method if it has not yet been called
    */
    public void init()
    {
      stack = new Throwable();
      stack.fillInStackTrace();
      allocTime = System.currentTimeMillis();
      if (!initted) {
        obj.poolInit(param);
        initted = true;
      }
    }

    /**
       Deleted the poolable object by calling the poolDelete
       method on the object.
    */
    public void delete()
    {
      if (initted) 
        obj.poolDelete();
    }

    /**
       Sets the last access time for this object.
       
       @param t the last access time in milliseconds
    */
    public void setLastAccess(long t)
    {
      lastAccess = t;
    }

    /**
       @return the last access time for this object in milliseconds
    */
    public long getLastAccess()
    {
      return lastAccess;
    }

    /**
       Returns the time in milliseconds when this PooledObject 
       was last allocated.
       
       @return Time in milliseconds since the epoch
    */
    public long getAllocTime()
    {
      return allocTime;
    }

    /**
       Print the stack trace for the point at which this PooledObject
       was allocated.  Useful to debug why an object isn't being
       returned to the pool
       
       @param out PrintWriter to print to
    */
    public void printStackTrace(PrintWriter out)
    {
      stack.printStackTrace(out);
    }
  }

  /**
     Runnable class to wake up periodically and shrink the pool
     if need be.
  */
  public class Shrinker
    implements Runnable
  {
    public void run()
    {
      while(true) {
        try {
          Thread.currentThread().sleep(shrinkAfter);
        } catch (Exception e) {
          e.printStackTrace();
        }

        ArrayList deleted = new ArrayList();
        
        synchronized (this) {
          shrinkPool(deleted);
        }

        Iterator iter = deleted.iterator();
        while(iter.hasNext()) {
          PooledObject pobj = (PooledObject) iter.next();
          pobj.delete();
        }
        
        synchronized (this) {
          if (avail.size() <= minSize) {
            shrinkerThread = null;
            break;
          }
        }
        
      }
    }
  }

}
