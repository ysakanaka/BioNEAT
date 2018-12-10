package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.uci.ics.jung.graph.util.Pair;

public class RunningStatsAnalysis {
	
	protected double mean=0.0;
	protected double sd = 0.0;
	protected double se = 0.0;
	
	protected ArrayList<Double> data = new ArrayList<Double>();
	
	public List<Double> getData(){
		return Collections.unmodifiableList(data);
	}
	
	public double getMean() {
		return mean;
	}
	
	public double getStandardDeviation() {
		return sd;
	}
	
	public double getStandardError() {
		return se;
	}
	
	public void addData(double val) {
		data.add(val);
		mean = 0.0;
		sd = 0.0;
		
		
		for(int i=0; i<data.size(); i++){
			mean += data.get(i);
		}
		
		mean /= data.size();
		
		for(int i=0; i<data.size(); i++){
			sd += (data.get(i)-mean)*(data.get(i)-mean);
		}
		
		sd = Math.sqrt(sd/((double)(data.size()-1)));
		se = sd/Math.sqrt(data.size());
	}

}
