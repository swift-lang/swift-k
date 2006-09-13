/*
 * Created on May 10, 2005
 */
package org.cogkit.repository.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.rmi.RemoteException;


/**
 * @author dkodeboy
 * Manages all the calls to import/export Text files to/from the ATcT userspace
 * 
 */
public class FileHandler {
	private String userName;
	private String userNotesDirectory;
  private String usersDirectory;
  
	public FileHandler(){
	
	}
  
	public String setFile(byte[] fileData, String fileName) throws RemoteException{
		String internalErrorMesg = "Error occured while saving the file:" + fileName;
		String status = "File not saved";
		//check if this file already exists
		//if (!usrMgr.checkFileExists(fileName, userName)){
		 	//save the file to the userNotes folder
				try {
					FileOutputStream textFile = new FileOutputStream(fileName);
					textFile.write(fileData);
					textFile.close();
					status = "File saved";
				} catch (IOException ioe) {
					ioe.printStackTrace();
					throw new RemoteException(internalErrorMesg);
				} 	
		//}
	    //else{
		//			status = "File Already Exists";
		//		}		
		return status;
	}
	public byte[] getFile(String fileName) throws RemoteException{
		
	
		//get the file using InputStream and path from usernotes and filename
		try {
			File exportFile = new File(fileName);
			long fileSize = exportFile.length();
			byte[] b = new byte[(int)fileSize];
			FileInputStream fis = new FileInputStream(exportFile);
			int bytesRead = fis.read(b);
			fis.close();
			if (fileSize == bytesRead) {
				return b;
			}
		} catch (FileNotFoundException fnfe) {
			System.err.println("File required was not found");
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}
	
	public boolean appendFile(String fileName, String lineToAppend){
		boolean status = false;
    FileOutputStream textFile;
    
		try {
			
         textFile = new FileOutputStream(fileName, true);
		 textFile.write('\n');
		 textFile.write(lineToAppend.getBytes());
         textFile.close();
	     status = true;
		} catch (IOException e) {
			status = false;
			e.printStackTrace();
		}
		return status;	
	}

	
	public static void main(String[] args) {
		
		FileHandler fHdlr = new FileHandler();
       
		//Testing SaveFile 
		
		File f = new File("c:\\INSTALL.LOG");
		long fileSize = f.length();
		byte[] b = new byte[(int)fileSize];
		try{
		FileInputStream fis = new FileInputStream(f);
		int bytesRead = fis.read(b);
		fis.close();
		if (fileSize == bytesRead) {
			String status = fHdlr.setFile(b, f.getName());
			System.out.println(status);
			} 
		}
		catch (FileNotFoundException fnfe) {
			System.err.println("File required was not found");
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
        //	Testing getting a file from userspace
		try {
			b = fHdlr.getFile("Network");
			String fileData = new String(b);
			System.out.println(fileData);
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
		
	}

	
}
