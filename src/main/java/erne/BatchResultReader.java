package erne;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class BatchResultReader {

	public static int maxGen = 200;
	public static boolean getAllVals = true;
	
	public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException, ExecutionException {
		File folder = new File(args[0]);
		double[][] allVals;
		if(folder.isDirectory()){
		File[] files = folder.listFiles();
		//count valid directories
		int count = 0;
		for(int i=0; i<files.length; i++){
			if(files[i].isDirectory()) count++;
		}
		allVals = new double[count][];
		for(int i=0; i<files.length; i++){
			if(files[i].isDirectory()){
				
				try{
					allVals[i] = Evolver.getBestFitnessOverTime(files[i].getAbsolutePath());
				} catch(Exception e){
					System.err.println("Warning: could not read");
					allVals[i] = new double[maxGen];
				}
			}
		}
		} else {
			System.err.println("Not a folder");
			return;
		}
		
		System.out.println("Done with loading");
		double[][] sortedList = new double[maxGen][allVals.length];
		
		for(int i = 0; i < sortedList.length; i++){
			for (int j = 0; j<allVals.length; j++){
				sortedList[i][j] = (i>=allVals[j].length?sortedList[i-1][j]:allVals[j][i]);
			}
			Arrays.sort(sortedList[i]);
			System.out.println("Done with gen "+i);
		}
		
		StringBuilder sb = new StringBuilder();
		int indexWorst = 0;
		int indexQ1 = allVals.length/4;
		int indexMed = (2*allVals.length)/4;
		int indexQ3 = (3*allVals.length)/4;
		int indexBest = allVals.length-1;
		
		for(int i=0;i<sortedList.length; i++){
			if (getAllVals){
				for (int j = 0; j<sortedList[i].length; j++){
					sb.append(sortedList[i][j]+" ");
				}
				sb.append("\n");
			} else {
			sb.append(sortedList[i][indexWorst]+"\t"+sortedList[i][indexQ1]+"\t"+sortedList[i][indexMed]
					    +"\t"+sortedList[i][indexQ3]+"\t"+sortedList[i][indexBest]+"\n");
			}
		}
		
		PrintWriter fileOut = new PrintWriter(folder.getAbsolutePath() + "/result.dat");
		fileOut.write(sb.toString());
		fileOut.close();
	}
	
}
