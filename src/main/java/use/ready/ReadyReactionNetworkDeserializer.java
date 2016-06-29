package use.ready;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import reactionnetwork.Connection;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;
import reactionnetwork.ReactionNetworkDeserializer;

public class ReadyReactionNetworkDeserializer extends ReactionNetworkDeserializer {

	@Override
	public ReactionNetwork deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject jobject = (JsonObject) json;
		ReadyReactionNetwork network = new ReadyReactionNetwork();

		JsonArray nodes = jobject.getAsJsonArray("nodes");
		for (JsonElement jsonNode : nodes) {
			network.addNode((Node) context.deserialize(jsonNode, Node.class));
		}

		JsonArray connections = jobject.getAsJsonArray("connections");
		for (JsonElement jsonConnetion : connections) {
			JsonObject jsonConnectionObject = jsonConnetion.getAsJsonObject();
			int innovation = jsonConnectionObject.get("innovation").getAsInt();
			Connection connection = ((ReactionNetwork) network).addConnection(innovation,
					jsonConnectionObject.get("from").getAsString(),
					jsonConnectionObject.get("to").getAsString());
			connection.enabled = jsonConnectionObject.get("enabled")
					.getAsBoolean();
			connection.parameter = jsonConnectionObject.get("parameter")
					.getAsDouble();
		}

		JsonElement parameters = jobject.get("parameters");
		network.parameters = context.deserialize(parameters, Map.class);
		
		network.templateOnBeads = new ArrayList<Set<Connection>>();
		JsonArray tempOnBeads = jobject.getAsJsonArray("templateOnBeads");
		for (JsonElement hashSet : tempOnBeads){
			 if (hashSet.isJsonArray()){
				 HashSet<Connection> set = new HashSet<Connection>();
				 for (JsonElement jsonConnection : hashSet.getAsJsonArray()){
					 int innovation = jsonConnection.getAsJsonObject().get("innovation").getAsInt();
					 set.add(network.getConnectionByIN(innovation));
				 }
				 network.templateOnBeads.add(set);
			 }
		}
		
		return network;
	}
}
