package org.openempi.ics.utility;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
  Simple performance profiling tools.  Profile allows you to mark
  code sections to be profiled arbitrarily.  It will track all profiled
  sections as well as the call map for other profiled sections called
  from within a profiled section.  Profile attempts to be minimally
  intrusive to the overall code performance by collecting the profile
  data as quickly as possible and deferring aggregration and analysis
  until later. Even so, there is some impact to using this tool so
  case should be used.

  Basic use is to use the begin() and end() methods to mark blocks of code
  you wish to profile.  Then use one of the dump() methods to display
  the profiled stats collected.  You can use enable() or disable() to
  enable/disable system-wide profiling, or set the system property
  Profile.enable=true

  NOTE: This code is NOT perfect!  It is designed to be quick and dirty!
  If you put a begin()/end() block in code and the end() doesn't get
  executed (maybe because of a throw) you will get unpredictable results
*/
public class Profile
{
    /**
       true if profiling is enabled, false otherwise
    */
    static private  boolean profile = Boolean.getBoolean("Profile.enable");

    /**
       Contains all completed Timers.  We're using A vector
       here because the sycnhronization is desired.
    */
    static private  Vector timers = new Vector();

    /**
       A Map containing Thread/List pairs.  Each thread in which
       Profiling is used will have an entry in this Map.  The List
       for each Thread is the stack of current timers which have
       started but not yet stopped
    */
    static private HashMap threads = new HashMap();

    /**
       No instances allowed
    */
    private Profile() {}

    /**
       In theory, prevents class garbage collection
    */
    static private Profile _instance = new Profile();

    /**
      Enables profiling.  Profiling is now turned on everywhere and
      data will be collected
      */
    static public void enable()
    {
        profile = true;
    }

    /**
      Disables profiling.  Profiling is now turned off and all collected
      data is lost
      */
    static public void disable()
    {
        profile = false;
        clearAll();
    }

    /**
      @return true if profiling is enabled, false otherwise
      */
    static public boolean isEnabled()
    {
        return profile;
    }

    /**
       Marks the start of a new profile timer for the name given.
       A matching end() call must be made with the same name.

       @param name The name of the profile timer
    */
    static public void begin(String name)
    {
        if (!profile)
            return;
        Timer t;
        LinkedList list;
        Thread thread = Thread.currentThread();

        synchronized (threads) {
            list = (LinkedList) threads.get(thread);
            if (list == null)
                threads.put(thread, list = new LinkedList());
        }
        t = new Timer(name);
        list.addLast(t);
    }

    /**
       Marks the end of a profile timer for the name given.  There
       must have been a corresponding begin() for the same name.

       @param name The name of the profile time
    */
    static public void end(String name)
    {
        if (!profile)
            return;
        LinkedList list;
        long total;
        Thread thread = Thread.currentThread();
        synchronized (threads) {
            list = (LinkedList) threads.get(thread);
        }
        
        Timer t = (Timer) list.removeLast();
        if (t != null) {
            total = t.end();
            
            if (list.size() > 0) {
                Timer p = (Timer) list.getLast();
                p.child += total;
                p.timers.add(t);
            } else
                timers.add(t);
        }
    }

    /**
      Dumps the current call map and totals to System.out.
      Each call to dump() also implicitly clears the call map
      and all statistics collected so far
      */
    static public void dump()
    {
        dump(System.out);
    }

    /**
      Dumps the current call map and totals to the PrintWriter given.
      Each call to dump() also implicitly clears the call map
      and all statistics collected so far

      @param out PrintStream to output to
      */
    static public void dump(PrintStream out)
    {
        dump(new PrintWriter(out, true));
    }

    /**
      Dumps the current call map and totals to the Writer given.
      Each call to dump() also implicitly clears the call map
      and all statistics collected so far

      @param out Writer to output to
      */
    static public void dump(Writer out)
    {
        dump(new PrintWriter(out, true));
    }

    /**
      Dumps the current call map and totals to the PrintStream given.
      Each call to dump() also implicitly clears the call map
      and all statistics collected so far

      @param out PrintWriter to output to
      */
    static public void dump(PrintWriter out)
    {
        if (!profile)
            return;

        HashMap totals = new HashMap();

        out.println("Call Map");
        dumpTimers(timers, out, "", totals);

        out.println("\nTotals");
        Iterator iter = totals.values().iterator();
        Timer t;
        while(iter.hasNext()) {
            t = (Timer) iter.next();
            out.println(t.name + ": " + t.total + "ms/" + t.cnt +
                        "c=" + (t.total/t.cnt) + "ms/c, Child: " +
                        t.child + "ms");
        }
        out.flush();
    }

    /**
      Clears the current call map and all statistics
    */
    static public void clearAll()
    {
        if (!profile)
            return;
        synchronized (threads) {
            threads.clear();
            timers.clear();
        }
    }

    /**
      Private utility method for aggregating call map and statistics
      and formatting them for display.  This method is called recursively
      during processing since each Timer can have a list of sub-timers
      which were invoked during the span of parent timers call.

      @param timers The list of Timer objects to process for display
      @param out The PrintWriter to send the output to
      @param prefix A String appended to each output line
      @param totals A Map to collect totals for all calls in the call map.
    */
    static private void dumpTimers(List timers, PrintWriter out, String prefix,
                                   Map totals)
    {
        HashMap map = new HashMap();
        Timer t, t2;
        int len = timers.size();
        for(int i =0; i<len; i++) {
            t = (Timer) timers.get(i);
            if ((t2 = (Timer) map.get(t.name)) == null) {
                map.put(t.name, t2 = new Timer(t));
                t2.cnt = 1;
            } else {
                t2.total += t.total;
                t2.child += t.child;
                t2.cnt++;
            }
            t2.timers.addAll(t.timers);
            if ((t2 = (Timer) totals.get(t.name)) == null) {
                totals.put(t.name, t2 = new Timer(t));
                t2.cnt = 1;
            } else {
                t2.total += t.total;
                t2.child += t.child;
                t2.cnt++;
            }
            t2.timers.addAll(t.timers);
        }
        Iterator iter = map.values().iterator();
        while(iter.hasNext()) {
            t = (Timer) iter.next();
            out.println(prefix + t.name + ": " + t.total + "ms/" + t.cnt +
                        "c=" + (t.total/t.cnt) + "ms/c, Child: " +
                        t.child + "ms");
            dumpTimers(t.timers, out, prefix + "  ", totals);
        }
    }

    /**
      Private class representing each individual timer
      */
    private static class Timer
    {
        /**
          The name of this timer
          */
        public String name;

        /**
          The time this timer was started in milliseconds
          */
        public long start;

        /**
          The total run time of this timer in milliseconds
          */
        public long total;

        /**
          The total time spent in sub-timers in milliseconds
          */
        public long child;

        /**
          Total number of timers this timer was invoked
          */
        int cnt;

        /**
          List of sub-Timers.  These are timers that were invoked
          during the span of this timer.
          */
        public ArrayList timers;

        public Timer(String name)
        {
            this.name = name;
            start = System.currentTimeMillis();
            timers = new ArrayList();
        }

        public Timer(Timer t)
        {
            name = t.name;
            start = t.start;
            total = t.total;
            child = t.child;
            cnt = t.cnt;
            timers = new ArrayList();
        }

        public long end()
        {
            return total = System.currentTimeMillis() - start;
        }

        public long total()
        {
            return total;
        }

    }
}
