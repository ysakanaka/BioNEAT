package cluster;

import edu.uci.ics.jung.graph.util.Pair;
import erne.MultiobjectiveAbstractFitnessResult;

public class ClusterCompareResults extends AbstractTask<Pair<MultiobjectiveAbstractFitnessResult>,Integer> {

	
	public ClusterCompareResults(Pair<MultiobjectiveAbstractFitnessResult> results){
		super(results);
		data = null;
	}
	
	public ClusterCompareResults(){
		super();
	}
	
	@Override
	public Integer call() throws Exception {
		
		return MultiobjectiveAbstractFitnessResult.defaultComparator.compare(origin.getFirst(), origin.getSecond());
	}
	
}
