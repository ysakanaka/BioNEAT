package model;

import model.Constants;
import model.OligoGraph;
import model.chemicals.SequenceVertex;
import model.chemicals.Template;
import utils.TemplateFactory;

public class ECFRNTemplateFactory<E> extends TemplateFactory<E> {

	protected TemplateFactory<E> decoratedFactory;
	protected OligoGraph<SequenceVertex,E> graph;
	
	public ECFRNTemplateFactory(OligoGraph<SequenceVertex,E> graph, TemplateFactory<E> factory) {
		this.graph = graph;
		this.decoratedFactory = factory;
		
	}

	@Override
	public Template<E> create(E e) {
		SequenceVertex origin = graph.getSource(e);
		SequenceVertex dest = graph.getDest(e);
		if(dest == ReporterIndicator.indicator){
			//System.out.println("This is a reporter");
			return new Reporter<E>(graph,graph.getTemplateConcentration(e),origin);
		}
		
		double dangleL = graph.dangle?graph.getDangleL(e):Constants.baseDangleL;
		double dangleR = graph.dangle?graph.getDangleR(e):Constants.baseDangleR;
		if(origin != null && dest!=null && (ProtectedSequenceVertex.class.isAssignableFrom(origin.getClass())||ProtectedSequenceVertex.class.isAssignableFrom(dest.getClass()))){
			return new TemplateWithProtected<E>(graph,graph.getTemplateConcentration(e),graph.getStacking(e),dangleL,dangleR,graph.getSource(e),graph.getDest(e),(graph.getInhibition(e)==null?null:graph.getInhibition(e).getLeft()));	
		} else {
			return decoratedFactory.create(e);
		}
	}

}
