package org.openempi.ics.loader;

import java.io.File;

import org.apache.log4j.Logger;

public class FileLoaderManager
{
	private static Logger log = Logger.getLogger(FileLoaderManager.class);
	
	public FileLoaderManager() {
		
	}
	
	public void loadFile(String filename, String loaderAlias) {
		 File file = new File(filename);
		 if (!file.isFile() || !file.canRead()) {
			 log.error("Input file is not available.");
			 throw new RuntimeException("Input file " + filename + " is not readable.");
		 }
		 FileLoader loader = FileLoaderFactory.getFileLoader(loaderAlias);
		 loader.parseFile(file);
	}
	
	public static void main(String[] args) {
		if (args.length != 2) {
			usage();
			System.exit(-1);
		}
		String filename = args[0];
		String loaderAlias = args[1];
		log.info("Loading the data file " + filename + " using loader " + loaderAlias);
		FileLoaderManager fileLoaderManager = new FileLoaderManager();
		fileLoaderManager.setup();
		fileLoaderManager.loadFile(filename, loaderAlias);

	}

	private void setup() {
	}
	
	public static void usage() {
		System.out.println("Usage: " + FileLoaderManager.class.getName() + " <file-to-loader> <loader-alias>");
	}

}
