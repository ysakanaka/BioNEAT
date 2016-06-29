package use.math.gaussian;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.ExecutionException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import differentialevolution.DE;
import reactionnetwork.Connection;
import reactionnetwork.ConnectionSerializer;
import reactionnetwork.ReactionNetwork;
import reactionnetwork.ReactionNetworkDeserializer;
import use.math.AbstractMathFitnessFunction;
import use.math.FitnessResult;
import use.math.TestGUI;

public class RunGaussianDE {
	
	public static void main(String[] args) throws FileNotFoundException, InterruptedException, ExecutionException{
		Gson gson = new GsonBuilder().registerTypeAdapter(ReactionNetwork.class, new ReactionNetworkDeserializer())
				.registerTypeAdapter(Connection.class, new ConnectionSerializer()).create();
		BufferedReader in = new BufferedReader(new FileReader(args[0]));
		ReactionNetwork rn = gson.fromJson(in, ReactionNetwork.class);
		//System.out.println(rn);  //Check: ok
		DE de = new DE(rn,new GaussianFitnessFunction());
		ReactionNetwork fin = de.optimize();
		System.out.println(fin); //Instead, show a detailed
		TestGUI window = new TestGUI();
		window.frame.setVisible(true);
		AbstractMathFitnessFunction.setSaveSimulation(true);
		window.displayEvaluationResults(fin, (FitnessResult) new GaussianFitnessFunction().evaluate(fin));
	}

}
