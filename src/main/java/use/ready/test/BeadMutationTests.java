package use.ready.test;

import java.util.ArrayList;
import java.util.Arrays;

import erne.Individual;
import erne.mutation.MutationRule;
import erne.mutation.Mutator;
import erne.mutation.rules.AddActivation;
import erne.mutation.rules.AddNode;
import use.ready.fakefitness.RunReady;
import use.ready.mutation.rules.BeadMerge;
import use.ready.mutation.rules.BeadSplit;

public class BeadMutationTests {
	
	public static Mutator addNodeOnly = new Mutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[] {new AddNode(2)})));
	public static Mutator addActivationOnly = new Mutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[] {new AddActivation(2)})));
	public static Mutator splitBeadOnly = new Mutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[] {new BeadSplit(2)})));
	public static Mutator mergeBeadOnly = new Mutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[] {new BeadMerge(2)})));
	
	public static void main(String[] args){
		Individual indiv = new Individual(RunReady.startingReady);
		System.out.println("First step: basic indiv");
		System.out.println(indiv);
		System.out.println("=============");
		for (int i = 0; i<10; i++){
			addNodeOnly.mutate(indiv);
		}
		System.out.println("Second step: + 10 nodes");
		System.out.println(indiv);
		System.out.println("=============");
		
		for (int i = 0; i<3; i++){
			splitBeadOnly.mutate(indiv);
		}
		System.out.println("Third step: split beads three times");
		System.out.println(indiv);
		System.out.println("=============");
		for (int i = 0; i<2; i++){
			mergeBeadOnly.mutate(indiv);
		}
		System.out.println("Fourth step: merge beads twice");
		System.out.println(indiv);
		System.out.println("=============");
	}

}
