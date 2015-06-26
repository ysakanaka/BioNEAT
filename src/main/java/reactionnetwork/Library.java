package reactionnetwork;

import common.Static;

public class Library {
	public static final ReactionNetwork startingMath;

	static {
		startingMath = Static.gson.fromJson("{\n" + 
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
				"      \"reporter\": true\n" + 
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
				"    \"nick\": 10.0,\n" + 
				"    \"pol\": 10.0,\n" + 
				"    \"exo\": 10.0\n" + 
				"  }\n" + 
				"}", ReactionNetwork.class);
	}
}
