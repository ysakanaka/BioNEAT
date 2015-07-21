package erne.mutation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;
import use.math.gaussian.GaussianFitnessFunction;
import common.Static;
import erne.Individual;
import erne.mutation.rules.AddActivation;
import erne.mutation.rules.AddInhibition;
import erne.mutation.rules.AddNode;
import erne.mutation.rules.DisableTemplate;
import erne.mutation.rules.MutateParameter;

public class Mutator implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<MutationRule> mutationRules;
	private transient Random rand = new Random();

	public Mutator(ArrayList<MutationRule> mutationRules) {
		this.mutationRules = mutationRules;
	}

	public Individual mutate(Individual indiv) {
		int totalMutationWeight = 0;
		for (MutationRule mutationRule : mutationRules) {
			totalMutationWeight += mutationRule.getWeight();
		}
		int randomizedInt = rand.nextInt(totalMutationWeight);
		for (MutationRule mutationRule : mutationRules) {
			if (mutationRule.getWeight() > randomizedInt) {
				try {
					return mutationRule.mutate(indiv);
				} catch (Exception e) {
					e.printStackTrace();
					return indiv;
				}
			} else {
				randomizedInt -= mutationRule.getWeight();
			}
		}
		return indiv;
	}

	public static void main(String[] args) {
		Mutator mutator = new Mutator(new ArrayList<MutationRule>(Arrays.asList(new MutationRule[] { new DisableTemplate(1),
				new MutateParameter(90), new AddNode(2), new AddActivation(2), new AddInhibition(5) })));
		Individual indiv = new Individual(Static.gson.fromJson("{\r\n" + "  \"nodes\": [\r\n" + "    {\r\n" + "      \"name\": \"a\",\r\n"
				+ "      \"parameter\": 29.54203610899496,\r\n" + "      \"initialConcentration\": 10.0,\r\n" + "      \"type\": 1,\r\n"
				+ "      \"protectedSequence\": true,\r\n" + "      \"DNAString\": \"\",\r\n" + "      \"reporter\": false\r\n"
				+ "    },\r\n" + "    {\r\n" + "      \"name\": \"b\",\r\n" + "      \"parameter\": 113.5863451763374,\r\n"
				+ "      \"initialConcentration\": 5.0,\r\n" + "      \"type\": 1,\r\n" + "      \"protectedSequence\": false,\r\n"
				+ "      \"DNAString\": \"\",\r\n" + "      \"reporter\": true\r\n" + "    },\r\n" + "    {\r\n"
				+ "      \"name\": \"c\",\r\n" + "      \"parameter\": 45.08905470291145,\r\n" + "      \"initialConcentration\": 0.0,\r\n"
				+ "      \"type\": 1,\r\n" + "      \"protectedSequence\": false,\r\n" + "      \"DNAString\": \"\",\r\n"
				+ "      \"reporter\": false\r\n" + "    },\r\n" + "    {\r\n" + "      \"name\": \"Icc\",\r\n"
				+ "      \"parameter\": 0.0,\r\n" + "      \"initialConcentration\": 0.0,\r\n" + "      \"type\": 2,\r\n"
				+ "      \"protectedSequence\": false,\r\n" + "      \"DNAString\": \"\",\r\n" + "      \"reporter\": false\r\n"
				+ "    },\r\n" + "    {\r\n" + "      \"name\": \"Ibb\",\r\n" + "      \"parameter\": 0.0,\r\n"
				+ "      \"initialConcentration\": 0.0,\r\n" + "      \"type\": 2,\r\n" + "      \"protectedSequence\": false,\r\n"
				+ "      \"DNAString\": \"\",\r\n" + "      \"reporter\": false\r\n" + "    }\r\n" + "  ],\r\n"
				+ "  \"connections\": [\r\n" + "    {\r\n" + "      \"innovation\": 0,\r\n" + "      \"enabled\": false,\r\n"
				+ "      \"parameter\": 1.0,\r\n" + "      \"from\": \"a\",\r\n" + "      \"to\": \"b\"\r\n" + "    },\r\n" + "    {\r\n"
				+ "      \"innovation\": 1,\r\n" + "      \"enabled\": true,\r\n" + "      \"parameter\": 1.4279487534549804,\r\n"
				+ "      \"from\": \"c\",\r\n" + "      \"to\": \"c\"\r\n" + "    },\r\n" + "    {\r\n" + "      \"innovation\": 2,\r\n"
				+ "      \"enabled\": true,\r\n" + "      \"parameter\": 1.0,\r\n" + "      \"from\": \"a\",\r\n"
				+ "      \"to\": \"Icc\"\r\n" + "    },\r\n" + "    {\r\n" + "      \"innovation\": 3,\r\n"
				+ "      \"enabled\": true,\r\n" + "      \"parameter\": 8.79339302669801,\r\n" + "      \"from\": \"b\",\r\n"
				+ "      \"to\": \"b\"\r\n" + "    },\r\n" + "    {\r\n" + "      \"innovation\": 4,\r\n" + "      \"enabled\": true,\r\n"
				+ "      \"parameter\": 10.073504045319158,\r\n" + "      \"from\": \"c\",\r\n" + "      \"to\": \"Ibb\"\r\n" + "    }\r\n"
				+ "  ],\r\n" + "  \"parameters\": {\r\n" + "    \"nick\": 10.0,\r\n" + "    \"pol\": 10.0,\r\n" + "    \"exo\": 10.0\r\n"
				+ "  }\r\n" + "}", ReactionNetwork.class));
		System.out.println(new GaussianFitnessFunction().evaluate(indiv.getNetwork()).getFitness());
		for (int i = 0; i < 10000; i++) {
			Individual indiv1 = indiv.clone();
			mutator.mutate(indiv1);
			if (indiv1.getNetwork().getConnectionByEnds(new Node("a"), new Node("Ibb")) != null) {
				System.out.println(indiv1.getNetwork());
				System.out.println(indiv1.getNetwork().getConnectionByEnds(new Node("a"), new Node("Ibb")).parameter + " "
						+ new GaussianFitnessFunction().evaluate(indiv.getNetwork()).getFitness());

			}

		}
	}
}
