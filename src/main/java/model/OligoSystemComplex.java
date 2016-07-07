package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import model.Constants;
import model.PseudoTemplateGraph;
import model.PseudoTemplateOligoSystem;
import model.SaturationEvaluator;
import model.chemicals.PseudoExtendedSequence;
import model.chemicals.SequenceVertex;
import reactionnetwork.Connection;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;
import utils.EdgeFactory;
import utils.VertexFactory;

public class OligoSystemComplex {
	
	protected PseudoTemplateGraph<SequenceVertex,String> graph;
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
			if(n.type == Node.INHIBITING_SEQUENCE){
				Node from ;
				Node to;
				// Hard to sanitize at the mutator stage, so just to be sure: we ignore inhibitors that are pointing on nothing
				//check that everything is alright before going ahead
				if(n.name.contains("T")){
					String[] names = n.name.substring(1).split("T");
					from = network.getNodeByName(names[0]); // TODO: warning, very implementation dependent
					to = network.getNodeByName(names[1]);
				} else {
				from = network.getNodeByName(""+n.name.charAt(1)); // TODO: warning, very implementation dependent
				to = network.getNodeByName(""+n.name.charAt(2));
				}
				if (from == null || to == null) continue;
				Connection inhibited = network.getConnectionByEnds(from, to);
				if (!inhibited.enabled) continue; // we ignore the species
			}
			SequenceVertex s = graph.getVertexFactory().create();
			s.initialConcentration = n.initialConcentration;
			if(n.protectedSequence){
				s = new ProtectedSequenceVertex(s.ID,s.initialConcentration);
			}
			if(n.reporter){
				graph.addActivation("r"+s.ID,s,ReporterIndicator.indicator,ReporterIndicator.reporterConcentration);
			}
			equiv.put(n.name, s);
			s.setInhib(n.type == Node.INHIBITING_SEQUENCE);
			
			graph.addSpecies(s,n.parameter,n.initialConcentration);
			
			//For pseudoTemplates
			if(n.hasPseudoTemplate){
				PseudoExtendedSequence newi = new PseudoExtendedSequence(s,0.0);
				graph.setExtendedSpecies(s, newi);
				graph.setTemplateConcentration(graph.getEdgeFactory().createEdge(s, newi), n.pseudoTemplateConcentration);
			}
			
		}
		
		//Third step: add connections
		for(Connection c : network.connections){
			if (c.enabled) {
				String name = c.from.name+c.to.name;
				if(equiv.get(c.from.name)== null || equiv.get(c.to.name)== null){
					//Somehow one of the ends was deleted, and this connection was reactivated afterwards.
					continue;
				}
				
				graph.addActivation(name, equiv.get(c.from.name), equiv.get(c.to.name), c.parameter);
				if(!c.from.DNAString.equals("")&!c.to.DNAString.equals("")){
					String atTheNick = ""+c.from.DNAString.charAt(c.from.DNAString.length()-1);
					atTheNick += c.to.DNAString.charAt(0);
					//System.out.println(atTheNick);
					double[] slow = model.SlowdownConstants.getSlowdown(atTheNick);
					graph.setStacking(name,slow[0]);
					graph.setDangleL(name,slow[1]);
					graph.setDangleR(name,slow[2]);
				}	
			}
		}
		
		//Fourth step: add inhibitions
		for(Node n : network.nodes){
			if(n.type == Node.INHIBITING_SEQUENCE){
				String inhibited= n.name.substring(1);;
				if(n.name.contains("T")){
				 inhibited = inhibited.replace("T", "");
				} 
				SequenceVertex v = equiv.get(n.name);
				if (v != null) graph.addInhibition(inhibited, v);
			}
		}
		
		//Fifth step: other parameters? TODO
		//Specifically, we should change the kms above...
		graph.saturableExo = true;
		graph.saturableNick = true;
		graph.saturablePoly = true;
		graph.dangle = true;
		//if(this.graph.exoSaturationByFreeTemplates){
	    //	exoKm[SaturationEvaluator.TALONE] = Constants.exoKmTemplate;
	    //}
		
		//TODO: other params that could (should?) be evolved: missing bases.
		
/*		for(String param: network.parameters.keySet()){
			//only a few params are known:
			if(param.equals("nick")){
				Constants.nickVm = network.parameters.get(param)*Constants.nickKm;
			} else if(param.equals("pol")){
				Constants.polVm = network.parameters.get(param)*Constants.polKm;
			} else if(param.equals("exo")){
				Constants.exoVm = network.parameters.get(param)*Constants.exoKmSimple;
			}
		}*/ //Not sure we are evolving the enzyme conc...

	}
	
	public void toggleExoSaturationByAll(){
		if(exoKm[1]==0.0){
			for(int i=1; i<SaturationEvaluator.SIGNAL; i++ ){
				exoKm[i] = exoKm[0];
			}
		} else {
			for(int i=1; i<SaturationEvaluator.SIGNAL; i++ ){
				exoKm[i] = 0.0;
			}
		}
	}
	
	protected void initGraph(){
		final PseudoTemplateGraph<SequenceVertex, String> g = new PseudoTemplateGraph<SequenceVertex,String>();
		
		g.setSaturations(new SaturationEvaluatorProtected<String>(polKm,nickKm,exoKm));
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
	    },new EdgeFactory<SequenceVertex,String>(g){
	    	public String createEdge(SequenceVertex v1, SequenceVertex v2){
	    		if(PseudoExtendedSequence.class.isAssignableFrom(v2.getClass())){
	    			return "Pseudo"+v1.ID;
	    		}
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
		return calculateTimeSeries(-1); //for legacy/tests.
	}
	
	public Map<String, double[]> calculateTimeSeries(int timeOut) {
		Map<String, double[]> result = new HashMap<String, double[]>();
		OligoSystemWithProtectedSequences<String> myOligo = new OligoSystemWithProtectedSequences<String>(new PseudoTemplateOligoSystem(graph));
		myOligo.timeOut = timeOut;
		double[][] timeTrace = myOligo.calculateTimeSeries();
		for(Node n : this.network.nodes){
			SequenceVertex s = equiv.get(n.name);
			int index = getTrueIndex(myOligo.getSequences(),s);
			if (index == -1) continue; // This DNA strand does not exist in the system
			if(ProtectedSequenceVertex.class.isAssignableFrom(s.getClass())){
				result.put(n.name, arraySum(timeTrace[index],timeTrace[index+1]));
			} else {
				result.put(n.name, timeTrace[index]);
			}
			if(n.reporter){
				result.put("Reporter "+n.name,timeTrace[myOligo.total+myOligo.inhTotal+myOligo.getReporterIndex(s)]);
			}
		}
		return result;
	}
	
	public PseudoTemplateGraph<SequenceVertex,String> getGraph(){
		return graph;
	}
	
	public HashMap<String, SequenceVertex> getEquiv(){
		return equiv;
	}
}
