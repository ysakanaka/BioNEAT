package use.ready.fakefitness;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import erne.Evolver;
import erne.mutation.MutationRule;
import erne.mutation.Mutator;
import erne.mutation.rules.AddActivation;
import erne.mutation.rules.AddInhibition;
import erne.mutation.rules.AddNode;
import erne.mutation.rules.DisableTemplate;
import erne.mutation.rules.MutateParameter;
import use.ready.AbstractReadyFitnessFunction;
import use.ready.ReadyReactionNetwork;
import use.ready.export.ReadyExporter;
import use.ready.mutation.rules.BeadMerge;
import use.ready.mutation.rules.BeadSplit;

public class RunReady {

	public static final Mutator mutator = new Mutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[] {
			new DisableTemplate(1), new MutateParameter(90), new AddNode(2), new AddActivation(2), new AddInhibition(5)
			, new BeadMerge(2), new BeadSplit(2000)})));
	
	public static final ReadyReactionNetwork startingReady;
	
	static {
		startingReady = ReadyReactionNetwork.gson.fromJson("{\n" + 
				"  \"templateOnBeads\": [\n"+
				"    [\n"+
				"    {\n" + 
				"      \"innovation\": 0,\n" + 
				"      \"enabled\": true,\n" + 
				"      \"parameter\": 1.0,\n" + 
				"      \"from\": \"a\",\n" + 
				"      \"to\": \"b\"\n" + 
				"    }\n" +
				"    ]\n"+
				"  ],\n"+
				"  \"nodes\": [\n" + 
				"    {\n" + 
				"      \"name\": \"a\",\n" + 
				"      \"parameter\": 40.0,\n" + 
				"      \"initialConcentration\": 10.0,\n" + 
				"      \"type\": 1,\n" + 
				"      \"protectedSequence\": true,\n" + 
				"      \"DNAString\": \"\",\n" + 
				"      \"reporter\": false\n" + 
				"    },\n" + 
				"    {\n" + 
				"      \"name\": \"b\",\n" + 
				"      \"parameter\": 50.0,\n" + 
				"      \"initialConcentration\": 5.0,\n" + 
				"      \"type\": 1,\n" + 
				"      \"protectedSequence\": false,\n" + 
				"      \"DNAString\": \"\",\n" + 
				"      \"reporter\": false\n" + 
				"    }\n" + 
				"  ],\n" + 
				"  \"connections\": [\n" + 
				"    {\n" + 
				"      \"innovation\": 0,\n" + 
				"      \"enabled\": true,\n" + 
				"      \"parameter\": 1.0,\n" + 
				"      \"from\": \"a\",\n" + 
				"      \"to\": \"b\"\n" + 
				"    }\n" + 
				"  ],\n" + 
				"  \"parameters\": {\n" + 
				"  }\n" + 
				"}", ReadyReactionNetwork.class);
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		AbstractReadyFitnessFunction.simulationPath = "/home/naubertkato/Documents/Simulation/TestCode/";
		//FakeReadyFitness fitness = new FakeReadyFitness(); // Test 1
		FakeReadyFitnessBeads fitness = new FakeReadyFitnessBeads();
		ReadyExporter.maxSteps = 10;
		ReadyExporter.interval = 10;
		
		Evolver evolver = new Evolver(Evolver.DEFAULT_POP_SIZE, Evolver.MAX_GENERATIONS,startingReady, fitness, mutator, Evolver.DEFAULT_FITNESS_DISPLAYER);
		evolver.evolve();
        System.out.println("Evolution completed.");
        System.exit(0);
	}

}
