package utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import edu.uci.ics.jung.graph.util.Pair;

public class StatAnalysis {

	public static void main(String[] args){
     BufferedReader in;
     
     //Read data, separated by white spaces
	 ArrayList<double[]> allVals = new ArrayList<double[]>();
		try {
			in = new BufferedReader(new FileReader(args[0]));
			String line;
			while((line = in.readLine()) != null){
				String[] partial = line.split("\t");
				double[] partialVal = new double[partial.length];
				for(int i=0;i<partial.length;i++) partialVal[i] = Double.parseDouble(partial[i]);
				
				allVals.add(partialVal);
				
				//System.out.println("Done with "+allVals.size()+" "+Arrays.toString(partialVal)+" "+Arrays.toString(partial));
				
			}
		}catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Organize the data better
		double[][] arrayVals = new double[allVals.get(0).length][allVals.size()];
		for(int i=0; i<allVals.get(0).length;i++){
			for(int j=0;j<allVals.size(); j++){
				arrayVals[i][j] = allVals.get(j)[i];
			}
		}
		
		for(int i=0; i<arrayVals.length; i++) System.out.println(getMeanAndSD(arrayVals[i]));
		
		System.out.println("U before versus damage: "+getU(arrayVals[0],arrayVals[1]));
		System.out.println("U before versus after repair: "+getU(arrayVals[0],arrayVals[2]));
		System.out.println("U damage versus after repair: "+getU(arrayVals[1],arrayVals[2]));
	}
	
	public static Pair<Double> getMeanAndSD(double[] vals){
		double mean = 0.0;
		double sd = 0.0;
		
		for(int i=0; i<vals.length; i++){
			mean += vals[i];
		}
		
		mean /= vals.length;
		
		for(int i=0; i<vals.length; i++){
			sd += (vals[i]-mean)*(vals[i]-mean);
		}
		
		sd = Math.sqrt(sd/((double)(vals.length-1)));
		
		
		return new Pair<Double>(mean,sd);
	}
	
	public static double getStandardError(int sampleSize, double sd) {
		if (sampleSize <1) {
			System.err.println("getStandardErrod: Incorrect sample size "+sampleSize);
			return -1.0;
		}
		return sd/Math.sqrt((double)sampleSize);
	}
	
	public static double getU(double[] vals1, double[] vals2){
		double u = 0.0;
		
		for(int i=0;i<vals1.length;i++){
			for(int j=0;j<vals2.length;j++){
				if(vals1[i]> vals2[i]) u+=1.0;
				if(vals1[i]== vals2[i]) u+=0.5;
			}
		}
		
		return u;
	}
	
}
