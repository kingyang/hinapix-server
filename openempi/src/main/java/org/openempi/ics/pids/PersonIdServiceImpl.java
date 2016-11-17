package org.openempi.ics.pids;

import org.apache.log4j.Logger;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.RemoveException;
import java.rmi.RemoteException;

/**
 * Created by fyu on 2016/8/17.
 */
public class PersonIdServiceImpl extends PersonIdServiceBean implements PersonIdService {

    @Override
    public EJBHome getEJBHome() throws RemoteException {
        return null;
    }

    @Override
    public Object getPrimaryKey() throws RemoteException {
        return null;
    }

    @Override
    public void remove() throws RemoteException, RemoveException {

    }

    @Override
    public Handle getHandle() throws RemoteException {
        return null;
    }

    @Override
    public boolean isIdentical(EJBObject ejbObject) throws RemoteException {
        return false;
    }
}
