/*
 * FileSystemAdapter.java
 *
 * Created on February 8, 2005, 1:55 PM
 */

package org.openempi.ils.adapters;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openempi.ils.ExternalLocatorAdapter;
import org.openempi.ils.IlsException;
import org.openempi.ils.IlsMap;
import org.openempi.ils.utility.IlsProps;
import org.openempi.ils.utility.IlsSystemProps;

/**
 *
 * @author Jim Hazen
 */
public class FileSystemAdapter implements ExternalLocatorAdapter
{
    IlsMap _req = null;
    Logger _log = null;
    IlsSystemProps _props = null;
    
    /** Creates a new instance of FileSystemAdapter */
    public FileSystemAdapter()
    {
    }
    
    public IlsMap[] findLocators(IlsMap map) throws IlsException
    {
        _req = map;
        String domain = _req.getDomainId();
        String facility = _req.getFacilityId();
        String mrn = _req.getPatientId();
        String type = _req.getTypeCode();
        Date startDate = _req.getStartDate();
        Date endDate = _req.getEndDate();
        
        _props = (IlsSystemProps) IlsProps.getSystemProps(domain).get(0);
        String FSRoot = _props.getInterfaceProperty("FSRoot");
        String WebRoot = _props.getInterfaceProperty("WebRoot");
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
        _log = Logger.getLogger("ILS");
        _log.debug("FileSystemAdapter:\n" +
                "FSRoot: "+FSRoot+" \n" +
                "WebRoot: "+WebRoot+" \n" +
                "Domain: "+domain+" \n" +
                "Facility: "+facility+" \n" +
                "PatientId: "+mrn+" \n" +
                "Type: "+type+" \n"+
                "StartDate: "+df.format(startDate)+" \n" +
                "EndDate: "+df.format(endDate));
        if(FSRoot == null || WebRoot == null)
            throw new IlsException("Invalid configuration.  This adapter requires a FSRoot and WebRoot property" +
                    "to be set.");
        
        File fsRoot = new File(FSRoot);
        File webRoot = new File(WebRoot);
        IlsMap[] locators = null;
        List locatorList = new LinkedList();
        
        //search files based on information
        String searchLocString = fsRoot.getAbsolutePath();
        String relSearchPath = domain
                +  File.separatorChar + facility
                +  File.separatorChar + mrn
                +  File.separatorChar + type;
        searchLocString += File.separatorChar + relSearchPath;
        File searchLoc = new File(searchLocString);
        File[] files = searchLoc.listFiles();
        long startTime = (startDate != null)?startDate.getTime():0;
        long endTime = (endDate != null)?endDate.getTime():System.currentTimeMillis();
        if(files != null)
        {
            //iterate through and see which match the date range
            for(int i = 0; i < files.length; i++)
            {
                _log.debug("Found "+files.length+" possible matches.  Checking time...");
                long fileTime = files[i].lastModified();
                if(startTime <= fileTime && fileTime <= endTime)
                {
                    IlsMap returnMap = new IlsMap(_req);
                    String description = files[i].getName();
                    int dot = description.indexOf('.');
                    description = description.substring(0, (dot != -1)?dot:description.length());
                    returnMap.setResultDescription(description);
                    returnMap.setEncounterDate(new Date(fileTime));
                    String resultURL = WebRoot+"/"+relSearchPath+"/"+files[i].getName();
                    _log.debug("Found result.  Setting URL: "+resultURL);
                    returnMap.setResultUrl(resultURL);
                    locatorList.add(returnMap);
                }
                else
                {
                    _log.debug(files[i].getName()+": ("+startTime+" <= "+fileTime+" <= "+endTime+") == false");
                }
            }
        }
        else
        {
            _log.debug("Didn't find any candidate files at "+searchLoc.getAbsolutePath());
        }
        //return IlsMaps
        locators = (IlsMap[]) locatorList.toArray(new IlsMap[locatorList.size()]);
        return locators;
    }
}
