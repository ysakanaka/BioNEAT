package utils;

import common.Static;
import reactionnetwork.ReactionNetwork;

public class RDLibrary {
  public static final ReactionNetwork rdstart;
  
  static {
	  rdstart = Static.gson.fromJson("{\n" + 
				"  \"nodes\": [\n" + 
				"    {\n" + 
				"      \"name\": \"a\",\n" + 
				"      \"parameter\": 10.0,\n" + 
				"      \"initialConcentration\": 10.0,\n" + 
				"      \"type\": 1,\n" + 
				"      \"protectedSequence\": true,\n" + 
				"      \"DNAString\": \"\",\n" + 
				"      \"reporter\": false\n" + 
				"    },\n" + 
				"    {\n" + 
				"      \"name\": \"b\",\n" + 
				"      \"parameter\": 10.0,\n" + 
				"      \"initialConcentration\": 5.0,\n" + 
				"      \"type\": 1,\n" + 
				"      \"protectedSequence\": true,\n" + 
				"      \"DNAString\": \"\",\n" + 
				"      \"reporter\": false\n" + 
				"    },\n" + 
				"    {\n" + 
				"      \"name\": \"c\",\n" + 
				"      \"parameter\": 100.0,\n" + 
				"      \"initialConcentration\": 5.0,\n" + 
				"      \"type\": 1,\n" + 
				"      \"protectedSequence\": true,\n" + 
				"      \"DNAString\": \"\",\n" + 
				"      \"reporter\": false\n" + 
				"    }\n" + 
				"  ],\n" + 
				"  \"connections\": [\n" + 
				"    {\n" + 
				"      \"innovation\": 0,\n" + 
				"      \"enabled\": true,\n" + 
				"      \"parameter\": 5.0,\n" + 
				"      \"from\": \"c\",\n" + 
				"      \"to\": \"c\"\n" + 
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
