package common;

import java.text.DecimalFormat;
import java.util.Set;

import org.reflections.Reflections;

import erne.AbstractFitnessFunction;

public class Static {
	public static DecimalFormat df2 = new DecimalFormat("#.##");
	public static DecimalFormat df4 = new DecimalFormat("#.####");
	public static DecimalFormat df8 = new DecimalFormat("#.########");
	public static Set<Class<? extends AbstractFitnessFunction>> fitnessFunctions;
	//Use following code to get list of fitness functions
	//for (Iterator<Class<? extends FitnessFunction>> it = fitnessFunctions
	//		.iterator(); it.hasNext();) {
	//	Class<? extends FitnessFunction> f = it.next();
	//	f.newInstance().evaluate(null);
	//}
	
	static {
		Reflections reflections = new Reflections("");
		fitnessFunctions = reflections.getSubTypesOf(AbstractFitnessFunction.class);
	}
}
