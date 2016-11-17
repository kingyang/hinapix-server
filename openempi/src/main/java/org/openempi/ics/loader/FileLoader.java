package org.openempi.ics.loader;

import java.io.File;

import org.openempi.data.Person;

public interface FileLoader
{
	public void parseFile(File file);
	
	public void loadPerson(Person person);
}
