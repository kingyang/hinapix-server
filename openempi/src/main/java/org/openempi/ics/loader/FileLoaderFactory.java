package org.openempi.ics.loader;

public class FileLoaderFactory
{
	// TODO: Need to fix this factory; at the moment it can only create CDCFileLoader
	// entries regardless of the alias that was passed in.
	// 
	public static FileLoader getFileLoader(String alias) {
		if (alias.equals("CDC")) {
			return new CDCFileLoader();
		} else {
			return new NominalSetFileLoader();
		}
	}
}
