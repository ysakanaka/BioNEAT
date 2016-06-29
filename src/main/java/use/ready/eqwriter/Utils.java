package use.ready.eqwriter;

import java.util.Iterator;

import cern.colt.Arrays;
import model.Constants;
import model.OligoGraph;
import model.SaturationEvaluator;
import model.chemicals.SequenceVertex;
import reactionnetwork.Connection;
import reactionnetwork.Node;
import use.oligomodel.OligoSystemComplex;
import utils.EdgeFactory;
import utils.VertexFactory;

public class Utils {
	
	public static final double[] polKm = {0.0, Constants.polKm, 0.0, Constants.polKmBoth, 0.0,0.0,0.0,0.0};
    public static final double[] nickKm = {0.0, 0.0, 0.0, 0.0, Constants.nickKm,0.0,0.0,0.0};
    public static final double[] exoKm = {0.0, 0.0, 0.0, 0.0, 0.0,0.0,Constants.exoKmSimple,Constants.exoKmInhib};
    public static final double[] exoKmTem = {Constants.exoKmTemplate, 0.0, 0.0, 0.0, 0.0,0.0,Constants.exoKmSimple,Constants.exoKmInhib};
	
	public static String idToString(int index){
		String name = "";
		if (index < 0) return "0"; // This variable does not exist
		int residual = index;
		char lsb;
		while (residual > 25) {
		    lsb = (char) (residual % 26);
		    name = (char)('a'+lsb)+name;
		    residual = ((residual -lsb)/26 -1);
		}
		lsb = (char) (residual % 26);
	    name = (char)('a'+lsb)+name;
		return name;
	}
	
	public static OligoGraph<SequenceVertex, String> initGraph(){
		OligoGraph<SequenceVertex, String> g = new OligoGraph<SequenceVertex,String>();
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
	    return g;
	}
	
	public static SaturationEvaluator<String> getDefaultSE(){
	    return new SaturationEvaluator<String>(polKm,nickKm,exoKm);
	}
	
	public static SaturationEvaluator<String> getTempSatSE(){
	    return new SaturationEvaluator<String>(polKm,nickKm,exoKmTem);
	}

	public static <E> void  packGraphSequences(OligoGraph<SequenceVertex,E> graph){
		Iterator<SequenceVertex> itseq = graph.getVertices().iterator();
		//First match id and positions for seqs
		int curIndex = 0;
		while(itseq.hasNext()){
			SequenceVertex seq = itseq.next();
			Double k = graph.K.remove(seq);
			E o = null;
			if (seq.isInhib()){
				o = graph.inhibitors.remove(seq);
			}
			seq.ID = curIndex;
			graph.K.put(seq, k);
			if (seq.isInhib()) graph.inhibitors.put(seq, o);
			curIndex++;
		}
	}
	
	public static int getEquivIndex(OligoSystemComplex oligo, Node n){
		return oligo.getEquiv().get(n.name).ID - 1 ; //Sequences are indexed from 1, instead of 0...
	}
	
	public static int getEquivIndex(OligoSystemComplex oligo, Connection c){
		OligoGraph<SequenceVertex,String> graph = oligo.getGraph();
		String temp = c.from.name+c.to.name;//graph.getEdgeFactory().createEdge(oligo.getEquiv().get(c.from.name),oligo.getEquiv().get(c.to.name) );
		if (graph.getEdges().contains(temp)){
			int index = graph.getVertexCount();
			Iterator<String> it = graph.getEdges().iterator();
			while(it.hasNext()){
				String otherTemp = it.next();
				if (otherTemp.equals(temp)){
					return index;
				}
				index += (graph.getInhibitableTemplates().contains(otherTemp)?5:6); //TODO: hard coded template size
			}
			
		}
		return -1;
	}
	
	public static void testTemplateEqWriter(TemplateEqWriter<?> tew, int index){
		System.out.println(tew.getInSequenceEq(index));
		System.out.println(tew.getOutSequenceEq(index));
		System.out.println(tew.getInhibSequenceEq(index));
		System.out.println(Arrays.toString(tew.getEqs(index)));
	}
	
	public static void main(String[] args){
		System.out.println(idToString(1));
		System.out.println(idToString(50));
		System.out.println(idToString(1000000));
		System.out.println(idToString(-1));
	}
	
}
