package erne;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import erne.util.Serializer;

public class ResultReader {

	public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException, ExecutionException {
		Evolver evolver = (Evolver) Serializer.deserialize(args[0] + "/evolver");
		evolver.setReader(args[0]);
		evolver.evolve();
	}

}
