package use.oligomodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import model.Constants;
import model.OligoGraph;
import model.OligoSystemAllSats;
import model.chemicals.SequenceVertex;
import reactionnetwork.Connection;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;
import utils.EdgeFactory;
import utils.VertexFactory;

public class OligoSystemComplex {
	
	protected OligoGraph<SequenceVertex,String> graph;
	protected ReactionNetwork network; //redundant?
	protected HashMap<String,SequenceVertex> equiv = new HashMap<String,SequenceVertex>();
	protected double[] polKm = {0.0, Constants.polKm, 0.0, Constants.polKmBoth, 0.0,0.0,0.0,0.0};
    protected double[] nickKm = {0.0, 0.0, 0.0, 0.0, Constants.nickKm,0.0,0.0,0.0};
    protected double[] exoKm = {0.0, 0.0, 0.0, 0.0, 0.0,0.0,Constants.exoKmSimple,Constants.exoKmInhib};
	
	public OligoSystemComplex(ReactionNetwork network) {
		
		this.network = network;
		
		//First step: create an empty oligograph:
		initGraph();
		
		//Second step: populate with all sequences ("nodes") from the network
		for(Node n : network.nodes){
			SequenceVertex s = graph.getVertexFactory().create();
			s.initialConcentration = n.initialConcentration;
			if(n.protectedSequence){
				s = new ProtectedSequenceVertex(s.ID,s.initialConcentration);
			}
			equiv.put(n.name, s);
			s.setInhib(n.type == Node.INHIBITING_SEQUENCE);
			graph.addSpecies(s,n.parameter,n.initialConcentration);
			//custom exonuclease inhibition
			graph.getCustomExoKm().put(s.toString(), (s.isInhib()?Constants.exoKmInhib:Constants.exoKmSimple));
		}
		
		//Third step: add connections
		for(Connection c : network.connections){
			String name = c.from.name+c.to.name;
			//custom exonuclease inhibition. Reference all the sub species of a template
			String[] subspecies = new String[]{"alone", "in", "out", "both", "ext", "inhib"};
			
			graph.addActivation(name, equiv.get(c.from.name), equiv.get(c.to.name), c.parameter, subspecies);
			
			//custom exonuclease inhibition. Defaults are: alone, in, out and inhib inhibits the exo.
			//We remove out for now
			graph.getCustomExoKm().put(name+" out", -1.0);
		}
		
		//Fourth step: add inhibitions
		for(Node n : network.nodes){
			if(n.type == Node.INHIBITING_SEQUENCE){
				String inhibited = n.name.substring(1); //We remove the starting I
				graph.addInhibition(inhibited, equiv.get(n.name));
			}
		}
		
		//Fifth step: other parameters? TODO
		//Specifically, we should change the kms above...

	}
	
	@Deprecated
	public void setUsageOfCustomExoKm(boolean value){
		//TODO: we will have to add getter/setter for the Kms
		graph.exoUseCustomKm = value;
	}
	
	protected void initGraph(){
		final OligoGraph<SequenceVertex, String> g = new OligoGraph<SequenceVertex,String>();
	    g.initFactories(new VertexFactory<SequenceVertex>(g){
	    	
			public SequenceVertex create() {
				SequenceVertex newvertex = associatedGraph.popAvailableVertex();
				if (newvertex == null){
					newvertex = new SequenceVertex(associatedGraph.getVertexCount() + 1);
				} else {
					newvertex = new SequenceVertex(newvertex.ID);
				}
				return newvertex;
			}

			@Override
			public SequenceVertex copy(SequenceVertex original) {
				 SequenceVertex ret = new SequenceVertex(original.ID);
				 ret.inputs = original.inputs;
				 return ret;
			} 	
	    }, new EdgeFactory<SequenceVertex,String>(g){
	    	public String createEdge(SequenceVertex v1, SequenceVertex v2){
	    		return v1.ID+"->"+v2.ID;
	    	}
	    	public String inhibitorName(String s){
	    		return "Inhib"+s;
	    	}
	    });
	    this.graph = g;
	}
	
	protected int getTrueIndex(ArrayList<SequenceVertex> list,SequenceVertex s){
		 int where = 0;
		if (s == null) {
            return -1;
        } else {
            for (int i = 0; i < list.size(); i++){
                if (s.equals(list.get(i))){
                    return where;
                }
            where++;
            if(ProtectedSequenceVertex.class.isAssignableFrom(list.get(i).getClass())) where++;
            }
           
        }
		return -1;
	}
	
	protected double[] arraySum(double[] a1, double[] a2){
		double[] a = new double[Math.min(a1.length, a2.length)];
		for (int i=0; i<a.length;i++){
			a[i] = a1[i] + a2[i];
		}
		return a;
	}

	public Map<String, double[]> calculateTimeSeries() {
		Map<String, double[]> result = new HashMap<String, double[]>();
		OligoSystemWithProtectedSequences<String> myOligo = new OligoSystemWithProtectedSequences<String>(graph,new SaturationEvaluatorProtected<String>(polKm,nickKm,exoKm));
		double[][] timeTrace = myOligo.calculateTimeSeries(null);
		for(Node n : this.network.nodes){
			SequenceVertex s = equiv.get(n.name);
			int index = getTrueIndex(myOligo.getSequences(),s);
			if(ProtectedSequenceVertex.class.isAssignableFrom(s.getClass())){
				result.put(n.name, arraySum(timeTrace[index],timeTrace[index+1]));
			} else {
				result.put(n.name, timeTrace[index]);
			}
		}
		return result;
	}
}
