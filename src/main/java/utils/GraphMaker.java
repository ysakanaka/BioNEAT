package utils;
import java.util.HashMap;

import model.*;
import model.chemicals.*;
import reactionnetwork.Connection;
import reactionnetwork.Node;
import reactionnetwork.ReactionNetwork;
import use.processing.rd.RDConstants;

public class GraphMaker {
  
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

  public static OligoGraph<SequenceVertex,String> makeAutocatalyst(){
    OligoGraph<SequenceVertex,String> g = initGraph();
    SequenceVertex s1 = g.getVertexFactory().create();
     g.addSpecies(s1, 10.0, 5.0);
    SequenceVertex s2 = g.getVertexFactory().create();
     g.addSpecies(s2, 10.0, 5.0);
     SequenceVertex glue = g.getVertexFactory().create();
     g.addSpecies(glue, 10.0, 5.0);
    String edge1 = g.getEdgeFactory().createEdge(glue, glue);
    g.addActivation(edge1, glue, glue);
    //String edge2 = g.getEdgeFactory().createEdge(s2, s1);
    //g.addActivation(edge2, s2, s1);
    return g;
  }
  
  public static OligoGraph<SequenceVertex,String> makeOligator(){
		OligoGraph<SequenceVertex,String> g = initGraph();
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
  
  public static OligoGraph<SequenceVertex,String> makeOligatorWithGradient(){
		OligoGraph<SequenceVertex,String> g = initGraph();
		SequenceVertex grad1 = g.getVertexFactory().create();
		g.addSpecies(grad1, 10.0, 5.0);
		SequenceVertex grad2 = g.getVertexFactory().create();
		g.addSpecies(grad2, 10.0, 5.0);
		SequenceVertex s1 = g.getVertexFactory().create();
		g.addSpecies(s1, 10.0, 5.0);
		String trig = g.getEdgeFactory().createEdge(grad1, s1);
		g.addActivation(trig, grad1, s1);
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
  
  public static OligoGraph<SequenceVertex,String> lineWithGradient(){
	  OligoGraph<SequenceVertex,String> g = initGraph();
		SequenceVertex grad1 = g.getVertexFactory().create();
		g.addSpecies(grad1, 10.0, 5.0);
		SequenceVertex grad2 = g.getVertexFactory().create();
		g.addSpecies(grad2, 10.0, 5.0);
		SequenceVertex glue = g.getVertexFactory().create();
		g.addSpecies(glue, 10.0, 5.0);
		String autoGlue = g.getEdgeFactory().createEdge(glue, glue);
		g.addActivation(autoGlue, glue, glue, 1.0);
		SequenceVertex antiGlue = g.getVertexFactory().create();
		antiGlue.setInhib(true);
		g.addSpecies(antiGlue, 1.0, 5.0);
		g.addInhibition(autoGlue, antiGlue);
		String edge1 = g.getEdgeFactory().createEdge(grad1, antiGlue);
		g.addActivation(edge1, grad1, antiGlue);
		String edge2 = g.getEdgeFactory().createEdge(grad2, antiGlue);
		g.addActivation(edge2, grad2, antiGlue);
		return g;
  }
  
  public static OligoGraph<SequenceVertex,String> repressilator(){
	  OligoGraph<SequenceVertex,String> g = initGraph();
		SequenceVertex grad1 = g.getVertexFactory().create();
		g.addSpecies(grad1, 10.0, 5.0);
		SequenceVertex grad2 = g.getVertexFactory().create();
		g.addSpecies(grad2, 10.0, 5.0);
		SequenceVertex glue = g.getVertexFactory().create();
		g.addSpecies(glue, 10.0, 5.0);
		String autoGlue = g.getEdgeFactory().createEdge(glue, glue);
		g.addActivation(autoGlue, glue, glue, 1.0);
		SequenceVertex s2 = g.getVertexFactory().create();
		g.addSpecies(s2, 10.0, 5.0);
		String autos2 = g.getEdgeFactory().createEdge(s2, s2);
		g.addActivation(autos2, s2, s2, 1.0);
		SequenceVertex s3 = g.getVertexFactory().create();
		g.addSpecies(s3, 10.0, 5.0);
		String autos3 = g.getEdgeFactory().createEdge(s3, s3);
		g.addActivation(autos3, s3, s3, 1.0);
		SequenceVertex antiGlue = g.getVertexFactory().create();
		antiGlue.setInhib(true);
		g.addSpecies(antiGlue, 1.0, 5.0);
		g.addInhibition(autoGlue, antiGlue);
		SequenceVertex antis2 = g.getVertexFactory().create();
		antis2.setInhib(true);
		g.addSpecies(antis2, 1.0, 5.0);
		g.addInhibition(autos2, antis2);
		SequenceVertex antis3 = g.getVertexFactory().create();
		antis3.setInhib(true);
		g.addSpecies(antis3, 1.0, 5.0);
		g.addInhibition(autos3, antis3);
		String edge1 = g.getEdgeFactory().createEdge(grad1, antiGlue);
		g.addActivation(edge1, grad1, antiGlue,0.1);
		String edge2 = g.getEdgeFactory().createEdge(grad2, antis2);
		g.addActivation(edge2, grad2, antis2,0.1);
		String inhib1 = g.getEdgeFactory().createEdge(glue, antis2);
		g.addActivation(inhib1, glue, antis2,20.0);
		String inhib2 = g.getEdgeFactory().createEdge(s2, antis3);
		g.addActivation(inhib2, s2, antis3,20.0);
		String inhib3 = g.getEdgeFactory().createEdge(s3, antiGlue);
		g.addActivation(inhib3, s3, antiGlue,20.0);
		return g;
  }
  
  public static OligoGraph<SequenceVertex,String> line(){
	  OligoGraph<SequenceVertex,String> g = initGraph();
		SequenceVertex grad1 = g.getVertexFactory().create();
		g.addSpecies(grad1, 10.0, 5.0);
		SequenceVertex grad2 = g.getVertexFactory().create();
		g.addSpecies(grad2, 10.0, 5.0);
		SequenceVertex glue = g.getVertexFactory().create();
		g.addSpecies(glue, 100.0, 5.0);
		String autoGlue = g.getEdgeFactory().createEdge(glue, glue);
		g.addActivation(autoGlue, glue, glue, 30.0);
		
		SequenceVertex s1 = g.getVertexFactory().create();
		g.addSpecies(s1, 10.0, 5.0);
		SequenceVertex s2 = g.getVertexFactory().create();
		g.addSpecies(s2, 10.0, 5.0);
		
		SequenceVertex antiGlue = g.getVertexFactory().create();
		antiGlue.setInhib(true);
		g.addSpecies(antiGlue, 1.0, 5.0);
		g.addInhibition(autoGlue, antiGlue);
		
		String edge1 = g.getEdgeFactory().createEdge(grad1, s1);
		g.addActivation(edge1, grad1, s1,1.2);
		String edge2 = g.getEdgeFactory().createEdge(grad2, s2);
		g.addActivation(edge2, grad2, s2,1.2);
		
		SequenceVertex as1 = g.getVertexFactory().create();
		as1.setInhib(true);
		g.addSpecies(as1, 1.0, 5.0);
		g.addInhibition(edge1, as1);
		SequenceVertex as2 = g.getVertexFactory().create();
		as2.setInhib(true);
		g.addSpecies(as2, 1.0, 5.0);
		g.addInhibition(edge2, as2);
		
		String edge3 = g.getEdgeFactory().createEdge(grad1, as2);
		g.addActivation(edge3, grad1, as2,30.0);
		String edge4 = g.getEdgeFactory().createEdge(grad2, as1);
		g.addActivation(edge4, grad2, as1,30.0);
		String edge5 = g.getEdgeFactory().createEdge(s1, antiGlue);
		g.addActivation(edge5, s1, antiGlue,9.0);
		String edge6 = g.getEdgeFactory().createEdge(s2, antiGlue);
		g.addActivation(edge6, s2, antiGlue,9.0);
		String edge7 = g.getEdgeFactory().createEdge(grad1, glue);
		g.addActivation(edge7, grad1, glue,0.01);
		String edge8 = g.getEdgeFactory().createEdge(grad2, glue);
		g.addActivation(edge8, grad2, glue,0.01);
		
		return g;
  }
  
  //Addapted from the original bioneat code
  public static OligoGraph<SequenceVertex,String> fromReactionNetwork(ReactionNetwork network){
	  OligoGraph<SequenceVertex,String> graph = initGraph();
	  HashMap<String,SequenceVertex> equiv = new HashMap<String,SequenceVertex>();
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
			equiv.put(n.name, s);
			s.setInhib(n.type == Node.INHIBITING_SEQUENCE);
			
			graph.addSpecies(s,n.parameter,n.initialConcentration);
			if(RDConstants.debug) System.out.println("DEBUG: "+n.name+" added to graph as "+s);
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
		return graph;
  }
  
}