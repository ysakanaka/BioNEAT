package reactionnetwork;

import common.Static;

public class Library {
	public static final ReactionNetwork startingMath;
	public static final ReactionNetwork squareFunction;

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
		squareFunction = Static.gson.fromJson("{\r\n" + 
				"  \"nodes\": [\r\n" + 
				"    {\r\n" + 
				"      \"name\": \"a\",\r\n" + 
				"      \"parameter\": 11.04,\r\n" + 
				"      \"DNAString\": \"ACGAGTCGGGA\",\r\n" + 
				"      \"initialConcentration\": 0.0,\r\n" + 
				"      \"type\": 1,\r\n" + 
				"      \"protectedSequence\": true,\r\n" + 
				"      \"reporter\": false\r\n" + 
				"    },    {\r\n" + 
				"      \"name\": \"b\",\r\n" + 
				"      \"parameter\": 54.05,\r\n" + 
				"      \"DNAString\": \"GTGAGTCTGGC\",\r\n" + 
				"      \"initialConcentration\": 10.0,\r\n" + 
				"      \"type\": 1,\r\n" + 
				"      \"protectedSequence\": false,\r\n" + 
				"      \"reporter\": true\r\n" + 
				"    },    {\r\n" + 
				"      \"name\": \"d\",\r\n" + 
				"      \"parameter\": 150.4,\r\n" + 
				"      \"DNAString\": \"GTGAGTCCACA\",\r\n" + 
				"      \"initialConcentration\": 10.0,\r\n" + 
				"      \"type\": 1,\r\n" + 
				"      \"protectedSequence\": false,\r\n" + 
				"      \"reporter\": false\r\n" + 
				"    },    {\r\n" + 
				"      \"name\": \"Iab\",\r\n" + 
				"      \"parameter\": 0.0816,\r\n" + 
				"      \"DNAString\": \"null\",\r\n" + 
				"      \"initialConcentration\": 10.0,\r\n" + 
				"      \"type\": 2,\r\n" + 
				"      \"protectedSequence\": false,\r\n" + 
				"      \"reporter\": false\r\n" + 
				"    }  ],\r\n" + 
				"  \"connections\": [\r\n" + 
				"    {\r\n" + 
				"      \"innovation\": 1,\r\n" + 
				"      \"enabled\": true,\r\n" + 
				"      \"parameter\": 30.9855,\r\n" + 
				"      \"from\": \"a\",\r\n" + 
				"      \"to\": \"b\"\r\n" + 
				"    },    {\r\n" + 
				"      \"innovation\": 2,\r\n" + 
				"      \"enabled\": true,\r\n" + 
				"      \"parameter\": 60.0,\r\n" + 
				"      \"from\": \"d\",\r\n" + 
				"      \"to\": \"d\"\r\n" + 
				"    },    {\r\n" + 
				"      \"innovation\": 3,\r\n" + 
				"      \"enabled\": true,\r\n" + 
				"      \"parameter\": 8.7468,\r\n" + 
				"      \"from\": \"d\",\r\n" + 
				"      \"to\": \"Iab\"\r\n" + 
				"    },    {\r\n" + 
				"      \"innovation\": 4,\r\n" + 
				"      \"enabled\": true,\r\n" + 
				"      \"parameter\": 42.9979,\r\n" + 
				"      \"from\": \"a\",\r\n" + 
				"      \"to\": \"d\"\r\n" + 
				"    }  ],\r\n" + 
				"  \"parameters\": {\r\n" + 
				"    \"nick\": 3.0,\r\n" + 
				"    \"pol\": 17.0,\r\n" + 
				"    \"exo\": 0.6818\r\n" + 
				"  }\r\n" + 
				"}", ReactionNetwork.class);
	}
}
