/*
 * DeepImageJ
 * 
 * https://deepimagej.github.io/deepimagej/
 *
 * Conditions of use: You are free to use this software for research or educational purposes. 
 * In addition, we expect you to include adequate citations and acknowledgments whenever you 
 * present or publish results that are based on it.
 * 
 * Reference: DeepImageJ: A user-friendly plugin to run deep learning models in ImageJ
 * E. Gomez-de-Mariscal, C. Garcia-Lopez-de-Haro, L. Donati, M. Unser, A. Munoz-Barrutia, D. Sage. 
 * Submitted 2019.
 *
 * Bioengineering and Aerospace Engineering Department, Universidad Carlos III de Madrid, Spain
 * Biomedical Imaging Group, Ecole polytechnique federale de Lausanne (EPFL), Switzerland
 *
 * Corresponding authors: mamunozb@ing.uc3m.es, daniel.sage@epfl.ch
 *
 */

/*
 * Copyright 2019. Universidad Carlos III, Madrid, Spain and EPFL, Lausanne, Switzerland.
 * 
 * This file is part of DeepImageJ.
 * 
 * DeepImageJ is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * DeepImageJ is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DeepImageJ. 
 * If not, see <http://www.gnu.org/licenses/>.
 */

package deepimagej.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FileTools {

	static public String getFolderSizeKb(String dir) {
		return String.format("%3.2f Mb", (getFolderSize(dir) / (1024 * 1024.0)));
	}

	static public long getFolderSize(String dir) {
		File folder = new File(dir);
		long length = 0;
		File[] files = folder.listFiles();

		if (files == null)
			return 0;
		int count = files.length;
		for (int i = 0; i < count; i++) {
			if (files[i].isFile())
				length += files[i].length();
			else
				length += getFolderSize(files[i].getAbsolutePath());
		}

		return length;
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.getParentFile().exists()) {
			destFile.getParentFile().mkdir();
		}
		if (!sourceFile.exists()) {
			return;
		}
		// If we are trying to copy a file into itself, do nothing
		//i.e, the sorce file and the destination are the same
		if (sourceFile.getAbsolutePath().equals(destFile.getAbsolutePath())) {
			return;
		}
		
		if (!destFile.exists()) {
			destFile.createNewFile();
		}
		FileChannel source = null;
		FileChannel destination = null;
		source = new FileInputStream(sourceFile).getChannel();
		destination = new FileOutputStream(destFile).getChannel();
		if (destination != null && source != null) {
			destination.transferFrom(source, 0, source.size());
		}
		if (source != null) {
			source.close();
		}
		if (destination != null) {
			destination.close();
		}

	}
	
	public static void copyFolderFiles(File sourceFolder, File destFolder) throws IOException {
		// Copy only the files from a folder to the other one
		if (sourceFolder.isDirectory() == false) {
			return;
		}
		if (destFolder.isDirectory() == false) {
			return;
		}
		File[] listOfFiles = sourceFolder.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile() == true) {
				String sourceFile = file.getAbsolutePath();
				String fileName = file.getName();
				String destFile = destFolder.getAbsolutePath() + File.separator + fileName;
				copyFile(new File(sourceFile), new File(destFile));
			}
		}
	}
	
	public static void unzipJar(String destinationDir, String jarPath) throws IOException {
		File file = new File(jarPath);
		JarFile jar = new JarFile(file);
 
		// fist get all directories,
		// then make those directory on the destination Path
		for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
			JarEntry entry = (JarEntry) enums.nextElement();
 
			String fileName = destinationDir + File.separator + entry.getName();
			File f = new File(fileName);
 
			if (fileName.endsWith("/")) {
				f.mkdirs();
			}
 
		}
 
		//now create all files
		for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
			JarEntry entry = (JarEntry) enums.nextElement();
 
			String fileName = destinationDir + File.separator + entry.getName();
			File f = new File(fileName);
 
			if (!fileName.endsWith("/")) {
				InputStream src = jar.getInputStream(entry);
				FileOutputStream dst = new FileOutputStream(f);
				copy(src, dst);
			}
		}
		jar.close();
	}
	
	private static long copy(InputStream src, FileOutputStream dst) throws IOException {
	    try {
	      byte[] buffer = new byte[1 << 20]; // 1MB
	      long ret = 0;
	      int n = 0;
	      while ((n = src.read(buffer)) >= 0) {
	        dst.write(buffer, 0, n);
	        ret += n;
	      }
	      return ret;
	    } finally {
	      dst.close();
	      src.close();
	    }
	  }
	
	public static void deleteDir(File element) {
	    if (element.isDirectory()) {
	        for (File sub : element.listFiles()) {
	        	deleteDir(sub);
	        }
	    }
	    element.delete();
	}
}