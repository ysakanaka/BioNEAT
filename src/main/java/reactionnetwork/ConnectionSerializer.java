package reactionnetwork;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ConnectionSerializer implements JsonSerializer<Connection> {

	public JsonElement serialize(Connection connection, Type type,
			JsonSerializationContext context) {
		JsonObject result = new JsonObject();
		result.add("innovation", new JsonPrimitive(connection.innovation));
		result.add("enabled", new JsonPrimitive(connection.enabled));
		result.add("parameter", new JsonPrimitive(connection.parameter));
		result.add("from", new JsonPrimitive(connection.from.name));
		result.add("to", new JsonPrimitive(connection.to.name));
		return result;
	}
}
