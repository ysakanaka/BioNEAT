package common;

import java.text.DecimalFormat;

import reactionnetwork.Connection;
import reactionnetwork.ConnectionSerializer;
import reactionnetwork.ReactionNetwork;
import reactionnetwork.ReactionNetworkDeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Static {
	public static DecimalFormat df2 = new DecimalFormat("#.##");
	public static DecimalFormat df4 = new DecimalFormat("#.####");
	public static DecimalFormat df8 = new DecimalFormat("#.########");
	//Use following code to get list of fitness functions
	//for (Iterator<Class<? extends FitnessFunction>> it = fitnessFunctions
	//		.iterator(); it.hasNext();) {
	//	Class<? extends FitnessFunction> f = it.next();
	//	f.newInstance().evaluate(null);
	//}
	
	public static Gson gson = new GsonBuilder().setPrettyPrinting()
			.registerTypeAdapter(ReactionNetwork.class, new ReactionNetworkDeserializer())
			.registerTypeAdapter(Connection.class, new ConnectionSerializer()).create();
	
	// list of fitness functions
	//public static Set<Class<? extends AbstractFitnessFunction>> fitnessFunctions;
	//static {
	//	Reflections reflections = new Reflections("");
	//	fitnessFunctions = reflections.getSubTypesOf(AbstractFitnessFunction.class);
	//}
}
