/*
 *
 * Copyright 2000 CareScience, Inc. All Rights Reserved.
 *
 */
package org.openempi.ils.adapters;

/**
 * CdrAdapterException is an exception class used by CdrAdapterFactory
 *
 * @version 1.0
 * @author  Arlan Pope
 */
public class CdrAdapterException extends Exception {

    public CdrAdapterException() {
        super();
    }

    public CdrAdapterException(Throwable throwable) {
        super();
    }

    public CdrAdapterException(String message) {
        super(message);
    }
}
