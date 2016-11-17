package org.openempi.ics.pids;

import org.openempi.data.Person;
import org.openempi.data.PersonIdentifier;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.RemoveException;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by fyu on 2016/8/17.
 */
public class IdentityServiceImpl extends IdentityServiceBean implements IdentityService {

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
