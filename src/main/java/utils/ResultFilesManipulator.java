package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class ResultFilesManipulator {
	
	public static void writeResults(double[][] results, String suffix, File folder){
		StringBuilder sb = new StringBuilder("");
		for(int i= 0; i<results.length; i++){
			for(int j= 0; j<results[i].length; j++){
				sb.append(results[i][j]+"\t");
			}
			sb.append("\n");
		}
		PrintWriter fileOut;
			
		try {
			fileOut = new PrintWriter(folder.getAbsolutePath()+"_"+suffix+"_result.txt");
				fileOut.write(sb.toString());
				fileOut.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
	}
}

