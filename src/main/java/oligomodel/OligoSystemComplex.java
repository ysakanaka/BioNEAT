package oligomodel;

import java.util.HashMap;
import java.util.Map;

import reactionnetwork.ReactionNetwork;

public class OligoSystemComplex {
	
	public OligoSystemComplex(ReactionNetwork network) {

	}

	public Map<String, double[]> calculateTimeSeries() {
		Map<String, double[]> result = new HashMap<String, double[]>();
		result.put("a", new double[] { 1, 2, 3, 4, 5 });
		result.put("b", new double[] { 5, 4, 3, 2, 1 });
		result.put("Iaa", new double[] { 6, 5, 4, 3, 2 });
		return result;
	}
}
