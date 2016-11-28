package erne;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import erne.util.Serializer;

public class BatchResultReader {

	public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException, ExecutionException {
		File folder = new File(args[0]);
		if(folder.isDirectory()){
		File[] files = folder.listFiles();
		for(int i=0; i<files.length; i++){
			if(files[i].isDirectory()){
		      Evolver evolver = (Evolver) Serializer.deserialize(files[i].getAbsolutePath() + "/evolver");
		      evolver.setReader(files[i].getAbsolutePath());
		      evolver.evolve();
			}
		}
		}
	}
	
}
