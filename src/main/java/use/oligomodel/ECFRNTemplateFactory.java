package use.oligomodel;

import model.Constants;
import model.OligoGraph;
import model.chemicals.SequenceVertex;
import model.chemicals.Template;
import utils.DefaultTemplateFactory;

public class ECFRNTemplateFactory<E> extends DefaultTemplateFactory<E> {

	public ECFRNTemplateFactory(OligoGraph<SequenceVertex, E> graph) {
		super(graph);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Template<E> create(E e) {
		SequenceVertex origin = graph.getSource(e);
		SequenceVertex dest = graph.getDest(e);
		if(dest == ReporterIndicator.indicator){
			//System.out.println("This is a reporter");
			return new Reporter<E>(graph,graph.getTemplateConcentration(e),origin);
		}
		double dangleL = graph.dangle?graph.dangleLSlowdown.get(e):Constants.baseDangleL;
		double dangleR = graph.dangle?graph.dangleRSlowdown.get(e):Constants.baseDangleR;
		if(ProtectedSequenceVertex.class.isAssignableFrom(origin.getClass())||ProtectedSequenceVertex.class.isAssignableFrom(dest.getClass())){
			return new TemplateWithProtected<E>(graph,graph.getTemplateConcentration(e),graph.stackSlowdown.get(e),dangleL,dangleR,graph.getSource(e),graph.getDest(e),(graph.getInhibition(e)==null?null:graph.getInhibition(e).getLeft()));	
		} else {
			return super.create(e);
		}
	}

}
