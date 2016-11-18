package utils;

import model.OligoGraph;
import model.chemicals.PadiracTemplate;
import model.chemicals.SequenceVertex;
import model.chemicals.Template;

public class PadiracTemplateFactory extends TemplateFactory<String> {
	 protected OligoGraph<SequenceVertex,String> graph;
	  
	  public PadiracTemplateFactory(OligoGraph<SequenceVertex,String> graph){
	    this.graph = graph;
	  }
	  
	  @Override
	  public Template<String> create(String e) {
	    
	    return new PadiracTemplate(graph,graph.getTemplateConcentration(e), graph.getSource(e),graph.getDest(e),(graph.getInhibition(e)==null?null:graph.getInhibition(e).getLeft()));
	  } 
	}
