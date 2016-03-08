package br.com.linesolutions.photoorganizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * <p>This implementation is responsible for extracting the modified date of the file and organizing it in a defined template folder.</p>
 * 
 * <p>The final result of this process will organize the media as follows:</p>
 * 
 * <blockquote><pre>
 *   YEAR
 *     MONTH_01
 *     MONTH_02
 *     ...
 *     MONTH_12
 * </pre></blockquote>
 * 
 * <p>Future releases will provide the ability to set a folder template beforehand.</p>
 * 
 * @author alexandre.oliveira
 * @version 1.0
 */
public class MediaOrganizer {

	/**
	 * <p>The main method of the application. In order to use it, two parameter will need to be provided:</p>
	 * 
	 * <blockquote><pre>
	 *   ROOT_FOLDER - Folder in which the implementation will scan for files in order to extract the date information
	 *   DEST_FOLER - Final destination of the files processed.
	 * </pre><blockquote>
	 * 
	 * @param args
	 *        parameters to be provided by the client
	 *        
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		
		if (args.length < 2) {
			System.out.println("Usage: MediaOrganizer <ROOT_FOLDER> <DEST_FOLDER>");
			System.exit(0);
		}

		String rootFolder = args[0];
		String outputFolder = args[1];

		deleteRecursively(new File(outputFolder));
		processFilesRecursively(rootFolder, outputFolder);
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("\n-------------------------------------------------------");
		System.out.println("Media organization took " + (endTime - startTime) + "ms");
		System.out.println("-------------------------------------------------------");
	}

	/**
	 * <p>Delete all content from a specific folder recursively</p>
	 * 
	 * @param  path 
	 * 		   Path folder in order to start the deletion
	 * 
	 * @return boolean
	 * 		   true if path already exists, false otherwise
	 */
	private static boolean deleteRecursively(File path) {
		boolean ret = true;
		if (path.isDirectory()) {
			for (File f : path.listFiles()) {
				ret = ret && deleteRecursively(f);
			}
		}
		return ret && path.delete();
	}

	/**
	 * <p>Process all file recursively separating each one into the specific folder</p>
	 * 
	 * @param rootFolderStr
	 *        Source folder location in order to scan for files
	 *        
	 * @param outputFolderStr
	 *        Output folder location where the files will be copied
	 *        
	 * @throws IOException
	 */
	private static void processFilesRecursively(String rootFolderStr, String outputFolderStr) throws IOException {
		File sourceFolderPath = new File(rootFolderStr);
		File[] sourceFilePathArray = sourceFolderPath.listFiles();

		// criteria in order to stop navigating into the folders recursively
		if (sourceFilePathArray == null)
			return;

		for (File sourceFile : sourceFilePathArray) {
			// if it is a directory, walk into its subfolder recursively until
			// it finds a file
			if (sourceFile.isDirectory()) {
				System.out.println(sourceFile.getAbsolutePath());
				processFilesRecursively(sourceFile.getAbsolutePath(), outputFolderStr);
			} else {
				processFile(sourceFile, outputFolderStr);
			}
		}
	}

	/**
	 * <p>Extract the date in order to copy it to its final location</p>
	 * 
	 * @param sourceFile
	 *        Source file in order to extract the date
	 * 
	 * @param outputFolderPath
	 *        Final destination where the files will be copied
	 *        
	 * @throws IOException
	 */
	private static void processFile(File sourceFile, String outputFolderPath) throws IOException {
		String filename = sourceFile.getAbsolutePath();

		File mediaFile = new File(filename);
		Date date = new Date(mediaFile.lastModified());

		DateFormat df = DateFormat.getDateInstance();
		df.format(date);
		int year = df.getCalendar().get(Calendar.YEAR);
		String month = String.format("%02d", df.getCalendar().get(Calendar.MONTH) + 1);

		File outputFolder = new File(outputFolderPath + File.separator + year + File.separator + month);
		outputFolder.mkdirs();

		File destFile = new File(outputFolder.getAbsolutePath() + File.separator + sourceFile.getName());

		try {
			Files.copy(sourceFile.toPath(), destFile.toPath());
			System.out.println("\t" + sourceFile.getName() + " -> " + destFile.getAbsolutePath());
		} catch (java.nio.file.FileAlreadyExistsException e) {
			try {
				File duplicatedOutputFolder = new File(outputFolder.getAbsolutePath() + File.separator + "duplicated");
				duplicatedOutputFolder.mkdirs();

				File duplicatedFile = new File(
						duplicatedOutputFolder.getAbsolutePath() + File.separator + sourceFile.getName());
				Files.copy(sourceFile.toPath(), duplicatedFile.toPath());
				System.out.println("\tDuplicated file found\n\t\t" + sourceFile.getName() + " -> " + duplicatedFile.getAbsolutePath());	
			} catch (Exception ex) {
				//silent exception in order to not abort the entire process. no need to log the exception at this point
			}
		}
	}

}
