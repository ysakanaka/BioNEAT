package use.ready.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import use.ready.beads.Bead;
import model.OligoGraph;
import model.chemicals.SequenceVertex;
import use.ready.test.GraphMaker;
import utils.MyPair;

public class ReadyRunner {

	public static String execFile = "/home/naubertkato/Workspace-codeblocks/ready/build/daccadRD";
	public static String baseName = "base";
	public static String simu = "simu";
	public static boolean debug = false;
	
	public static void doReadySimulation(String path, String system) throws IOException{
		
		//first, make the folder, if it does not exist yet.
		new File(path).mkdirs();
		FileWriter fw = new FileWriter(path+baseName+".config");
		fw.write(system);
		fw.flush();
		fw.close();
		
		ProcessBuilder p ;
		if (debug){
			p = new ProcessBuilder(execFile, path+"base.config","-f",path+simu,"-t").inheritIO();
			p.redirectErrorStream(true);
		} else {
			p = new ProcessBuilder(execFile, path+"base.config","-f",path+simu,"-t");
		}
			
			try {
				int ret = p.start().waitFor();
				if (ret != 0) System.err.println("Evaluation failed! "+path);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public static double[][] concForSpecies(String path, String name, String species, int iter) throws IOException{
		BufferedReader fr = new BufferedReader(new FileReader(path+name+"_"+species+String.format("%03d",iter)+".txt"));
		String line;
		double[][] ret;
		ArrayList<double[]> forLine = new ArrayList<double[]>();
		while((line = fr.readLine())!=null){
			String[] l = line.split(",\\s+");
			double[] vals = new double[l.length];
			for (int i = 0; i< vals.length; i++){
				vals[i] = Double.parseDouble(l[i]);
			}
			forLine.add(vals);
		}
		fr.close();
		ret = new double[forLine.size()][forLine.get(0).length];
		return forLine.toArray(ret);
	}
	
	public static void main(String[] args) throws IOException{
		MyPair<String,Double>[] sp1 = (MyPair<String,Double>[]) new MyPair[3];
		MyPair<String,Double>[] sp2 = (MyPair<String,Double>[]) new MyPair[3];
		int totalBeads = 50;
		ReadyExporter.maxSteps = 1000000;
		ReadyExporter.interval = 100000;
		Random rand = new Random();
		sp1[0] = new MyPair<String,Double>("a",5.0);
		sp1[1] = new MyPair<String,Double>("b",0.0);
		sp1[2] = new MyPair<String,Double>("c",0.0);
		sp2[0] = new MyPair<String,Double>("d",3.0);
		sp2[1] = new MyPair<String,Double>("i",50.0);
		sp2[2] = new MyPair<String,Double>("n",20.0);
		Bead b = new Bead(0.1,0.1,0.3,sp1); // signal species triggers
		ArrayList<Bead> beads = new ArrayList<Bead>();
		beads.add(b);
		HashMap<String,Boolean> diffusing = new HashMap<String,Boolean>();
		diffusing.put("a", true);
		diffusing.put("b", true);
		diffusing.put("c", true);
		for (int i=0; i<totalBeads; i++){
			beads.add(new Bead(rand.nextDouble(),rand.nextDouble(),0.03,sp2));
		}
		String[] enzymes = {"pol", "poldispl", "nick", "exo", "exoinhib"};
		OligoGraph<SequenceVertex,String> g = GraphMaker.makeOligator();
		doReadySimulation("/home/naubertkato/Documents/Simulation/TestCode/", ReadyExporter.allInOneReadyExport(g,beads,enzymes,diffusing));
	    double[][] test = concForSpecies("/home/naubertkato/Documents/Simulation/TestCode/","simu","a",0);
	    System.out.println(Arrays.toString(test[0]));
	}
	
}
