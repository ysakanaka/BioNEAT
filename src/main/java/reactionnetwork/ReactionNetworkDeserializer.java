package reactionnetwork;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class ReactionNetworkDeserializer implements
		JsonDeserializer<ReactionNetwork> {

	public ReactionNetwork deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject jobject = (JsonObject) json;
		ReactionNetwork network = new ReactionNetwork();

		JsonArray nodes = jobject.getAsJsonArray("nodes");
		for (JsonElement jsonNode : nodes) {
			network.addNode((Node) context.deserialize(jsonNode, Node.class));
		}

		JsonArray connections = jobject.getAsJsonArray("connections");
		for (JsonElement jsonConnetion : connections) {
			JsonObject jsonConnectionObject = jsonConnetion.getAsJsonObject();
			int innovation = jsonConnectionObject.get("innovation").getAsInt();
			Connection connection = network.addConnection(innovation,
					jsonConnectionObject.get("from").getAsString(),
					jsonConnectionObject.get("to").getAsString());
			connection.enabled = jsonConnectionObject.get("enabled")
					.getAsBoolean();
			connection.parameter = jsonConnectionObject.get("parameter")
					.getAsDouble();
		}

		JsonElement parameters = jobject.get("parameters");
		network.parameters = context.deserialize(parameters, Map.class);
		return network;
	}
}
