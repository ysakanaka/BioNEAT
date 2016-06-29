package use.ready.test;

import use.ready.eqwriter.Utils;
import model.OligoGraph;
import model.chemicals.SequenceVertex;

public class GraphMaker {

	public static OligoGraph<SequenceVertex,String> makeAutocatalyst(){
		OligoGraph<SequenceVertex,String> g = Utils.initGraph();
		SequenceVertex s1 = g.getVertexFactory().create();
		g.addSpecies(s1, 10.0, 5.0);
		String edge = g.getEdgeFactory().createEdge(s1, s1);
		g.addActivation(edge, s1, s1);
		return g;
	}
	
	public static OligoGraph<SequenceVertex,String> makeAutocatWithActivation(){
		OligoGraph<SequenceVertex,String> g = Utils.initGraph();
		SequenceVertex s1 = g.getVertexFactory().create();
		g.addSpecies(s1, 10.0, 5.0);
		String edge = g.getEdgeFactory().createEdge(s1, s1);
		g.addActivation(edge, s1, s1);
		SequenceVertex s2 = g.getVertexFactory().create();
		g.addSpecies(s2, 50.0, 5.0);
		String edge2 = g.getEdgeFactory().createEdge(s1, s2);
		g.addActivation(edge2, s1, s2);
		return g;
	}
	
	public static OligoGraph<SequenceVertex,String> makeOligator(){
		OligoGraph<SequenceVertex,String> g = Utils.initGraph();
		SequenceVertex s1 = g.getVertexFactory().create();
		g.addSpecies(s1, 10.0, 5.0);
		String edge = g.getEdgeFactory().createEdge(s1, s1);
		g.addActivation(edge, s1, s1);
		SequenceVertex s2 = g.getVertexFactory().create();
		g.addSpecies(s2, 50.0, 5.0);
		String edge2 = g.getEdgeFactory().createEdge(s1, s2);
		g.addActivation(edge2, s1, s2);
		SequenceVertex i3 = g.getVertexFactory().create();
		i3.setInhib(true);
		g.addSpecies(i3, 1.0, 5.0);
		g.addInhibition(edge, i3);
		String edge3 = g.getEdgeFactory().createEdge(s2, i3);
		g.addActivation(edge3, s2, i3);
		return g;
	}
	
	
}
