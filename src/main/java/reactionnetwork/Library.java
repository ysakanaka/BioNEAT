package reactionnetwork;

import common.Static;

public class Library {
	public static final ReactionNetwork startingMath;
	public static final ReactionNetwork squareFunction;
	public static final ReactionNetwork oldGaussian;

	static {
		oldGaussian = Static.gson.fromJson("{\r\n" + 
				"\"nodes\": [\r\n" + 
				"    {\r\n" + 
				"\"name\": \"a\",\r\n" + 
				"\"parameter\": \"42.88\",\r\n" + 
				"\"initialConcentration\": \"0.0\",\r\n" + 
				"\"type\": \"1\",\r\n" + 
				"\"protectedSequence\": true,\r\n" + 
				"\"DNAString\": \"\",\r\n" + 
				"\"reporter\": false\r\n" + 
				"    },\r\n" + 
				"    {\r\n" + 
				"\"name\": \"b\",\r\n" + 
				"\"parameter\": \"195.4\",\r\n" + 
				"\"initialConcentration\": \"10.0\",\r\n" + 
				"\"type\": \"1\",\r\n" + 
				"\"protectedSequence\": false,\r\n" + 
				"\"DNAString\": \"\",\r\n" + 
				"\"reporter\": true\r\n" + 
				"    },\r\n" + 
				"    {\r\n" + 
				"\"name\": \"c\",\r\n" + 
				"\"parameter\": \"99.72\",\r\n" + 
				"\"initialConcentration\": \"10.0\",\r\n" + 
				"\"type\": \"1\",\r\n" + 
				"\"protectedSequence\": false,\r\n" + 
				"\"DNAString\": \"\",\r\n" + 
				"\"reporter\": false\r\n" + 
				"    },\r\n" + 
				"    {\r\n" + 
				"\"name\": \"Ibb\",\r\n" + 
				"\"parameter\": \"0.4896\",\r\n" + 
				"\"initialConcentration\": \"10.0\",\r\n" + 
				"\"type\": \"2\",\r\n" + 
				"\"protectedSequence\": false,\r\n" + 
				"\"DNAString\": \"\",\r\n" + 
				"\"reporter\": false\r\n" + 
				"    },\r\n" + 
				"    {\r\n" + 
				"\"name\": \"Icc\",\r\n" + 
				"\"parameter\": \"0.196\",\r\n" + 
				"\"initialConcentration\": \"10.0\",\r\n" + 
				"\"type\": \"2\",\r\n" + 
				"\"protectedSequence\": false,\r\n" + 
				"\"DNAString\": \"\",\r\n" + 
				"\"reporter\": false\r\n" + 
				"    }\r\n" + 
				"  ],\r\n" + 
				"  \"connections\": [\r\n" + 
				"    {\r\n" + 
				"      \"innovation\": 1,\r\n" + 
				"      \"enabled\": true,\r\n" + 
				"      \"parameter\": 46.333,\r\n" + 
				"      \"from\": \"b\",\r\n" + 
				"      \"to\": \"b\"\r\n" + 
				"    },\r\n" + 
				"    {\r\n" + 
				"      \"innovation\": 2,\r\n" + 
				"      \"enabled\": true,\r\n" + 
				"      \"parameter\": 0,\r\n" + 
				"      \"from\": \"c\",\r\n" + 
				"      \"to\": \"c\"\r\n" + 
				"    },\r\n" + 
				"    {\r\n" + 
				"      \"innovation\": 3,\r\n" + 
				"      \"enabled\": true,\r\n" + 
				"      \"parameter\": 43.9616,\r\n" + 
				"      \"from\": \"a\",\r\n" + 
				"      \"to\": \"Ibb\"\r\n" + 
				"    },\r\n" + 
				"    {\r\n" + 
				"      \"innovation\": 4,\r\n" + 
				"      \"enabled\": true,\r\n" + 
				"      \"parameter\": 20.8801,\r\n" + 
				"      \"from\": \"a\",\r\n" + 
				"      \"to\": \"Icc\"\r\n" + 
				"    },\r\n" + 
				"    {\r\n" + 
				"      \"innovation\": 5,\r\n" + 
				"      \"enabled\": true,\r\n" + 
				"      \"parameter\": 47.8194,\r\n" + 
				"      \"from\": \"c\",\r\n" + 
				"      \"to\": \"Ibb\"\r\n" + 
				"    }\r\n" + 
				"  ],\r\n" + 
				"  \"parameters\": {\r\n" + 
				"    \"nick\": 3.0,\r\n" + 
				"    \"pol\": 17.0,\r\n" + 
				"    \"exo\": 0.68182\r\n" + 
				"  }}", ReactionNetwork.class);
				
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
				"      \"parameter\": 42.88,\r\n" + 
				"      \"initialConcentration\": 100.00000000000004,\r\n" + 
				"      \"type\": 1,\r\n" + 
				"      \"protectedSequence\": true,\r\n" + 
				"      \"DNAString\": \"\",\r\n" + 
				"      \"reporter\": false\r\n" + 
				"    },\r\n" + 
				"    {\r\n" + 
				"      \"name\": \"b\",\r\n" + 
				"      \"parameter\": 195.4,\r\n" + 
				"      \"initialConcentration\": 1.3019581547960157E-9,\r\n" + 
				"      \"type\": 1,\r\n" + 
				"      \"protectedSequence\": false,\r\n" + 
				"      \"DNAString\": \"\",\r\n" + 
				"      \"reporter\": true\r\n" + 
				"    },\r\n" + 
				"    {\r\n" + 
				"      \"name\": \"c\",\r\n" + 
				"      \"parameter\": 99.72,\r\n" + 
				"      \"initialConcentration\": 1.4215589690850545E-39,\r\n" + 
				"      \"type\": 1,\r\n" + 
				"      \"protectedSequence\": false,\r\n" + 
				"      \"DNAString\": \"\",\r\n" + 
				"      \"reporter\": false\r\n" + 
				"    },\r\n" + 
				"    {\r\n" + 
				"      \"name\": \"Ibb\",\r\n" + 
				"      \"parameter\": 0.4896,\r\n" + 
				"      \"initialConcentration\": 16.618414580658637,\r\n" + 
				"      \"type\": 2,\r\n" + 
				"      \"protectedSequence\": false,\r\n" + 
				"      \"DNAString\": \"\",\r\n" + 
				"      \"reporter\": false\r\n" + 
				"    },\r\n" + 
				"    {\r\n" + 
				"      \"name\": \"Icc\",\r\n" + 
				"      \"parameter\": 0.196,\r\n" + 
				"      \"initialConcentration\": 7.899799172282717,\r\n" + 
				"      \"type\": 2,\r\n" + 
				"      \"protectedSequence\": false,\r\n" + 
				"      \"DNAString\": \"\",\r\n" + 
				"      \"reporter\": false\r\n" + 
				"    }\r\n" + 
				"  ],\r\n" + 
				"  \"connections\": [\r\n" + 
				"    {\r\n" + 
				"      \"innovation\": 1,\r\n" + 
				"      \"enabled\": true,\r\n" + 
				"      \"parameter\": 46.333,\r\n" + 
				"      \"from\": \"b\",\r\n" + 
				"      \"to\": \"b\"\r\n" + 
				"    },\r\n" + 
				"    {\r\n" + 
				"      \"innovation\": 2,\r\n" + 
				"      \"enabled\": true,\r\n" + 
				"      \"parameter\": 0.0,\r\n" + 
				"      \"from\": \"c\",\r\n" + 
				"      \"to\": \"c\"\r\n" + 
				"    },\r\n" + 
				"    {\r\n" + 
				"      \"innovation\": 3,\r\n" + 
				"      \"enabled\": true,\r\n" + 
				"      \"parameter\": 43.9616,\r\n" + 
				"      \"from\": \"a\",\r\n" + 
				"      \"to\": \"Ibb\"\r\n" + 
				"    },\r\n" + 
				"    {\r\n" + 
				"      \"innovation\": 4,\r\n" + 
				"      \"enabled\": true,\r\n" + 
				"      \"parameter\": 20.8801,\r\n" + 
				"      \"from\": \"a\",\r\n" + 
				"      \"to\": \"Icc\"\r\n" + 
				"    },\r\n" + 
				"    {\r\n" + 
				"      \"innovation\": 5,\r\n" + 
				"      \"enabled\": true,\r\n" + 
				"      \"parameter\": 47.8194,\r\n" + 
				"      \"from\": \"c\",\r\n" + 
				"      \"to\": \"Ibb\"\r\n" + 
				"    }\r\n" + 
				"  ],\r\n" + 
				"  \"parameters\": {\r\n" + 
				"    \"nick\": 3.0,\r\n" + 
				"    \"pol\": 17.0,\r\n" + 
				"    \"exo\": 0.68182\r\n" + 
				"  }\r\n" + 
				"}", ReactionNetwork.class);
	}
}
