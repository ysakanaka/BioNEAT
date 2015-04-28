package use.oligomodel;

import model.Constants;
import model.OligoGraph;
import model.OligoSystemAllSats;
import model.SaturationEvaluator;
import model.chemicals.SequenceVertex;
import model.chemicals.Template;

public class OligoSystemWithProtectedSequences<E> extends OligoSystemAllSats<E> {

	public OligoSystemWithProtectedSequences(OligoGraph<SequenceVertex, E> graph) {
		super(graph);
		// TODO Auto-generated constructor stub
	}

	public OligoSystemWithProtectedSequences(
			OligoGraph<SequenceVertex, E> graph, SaturationEvaluator<E> se) {
		super(graph, se);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected double getTotalCurrentFluxSimple(SequenceVertex s) {
		// As Input
		double flux = 0;
		Template<E> temp;

		for(E e: graph.getOutEdges(s)){
			temp = this.templates.get(e);
			flux += temp.inputSequenceFlux();
			}
		// As Output
		for (E e: graph.getInEdges(s)) {
			temp = this.templates.get(e);
			flux += temp.outputSequenceFlux();
		}
		if(s.getClass().isAssignableFrom(ProtectedSequenceVertex.class) && ((ProtectedSequenceVertex) s).protectedSequence){
			return flux;
		}
		if (!graph.saturableExo) {

			flux -= s.getConcentration() * graph.exoConc*(Constants.exoVm)/Constants.exoKmSimple;
		} else {
			double km = (graph.exoUseCustomKm?graph.getCustomExoKm().get(s.toString()):Constants.exoKmSimple);
			//flux -= s.getConcentration() * Constants.exoConc*Constants.exoVm
			//		/ (Constants.exoKmSimple * this.observedExoKm);
			if(graph.coupling){
			
			flux -= s.getConcentration() * graph.exoConc*Constants.exoVm/ this.computeExoKm(km);
			} else {
			flux -= s.getConcentration() * graph.exoConc*Constants.exoVm/(km + s.getConcentration());
			}
			
		}
		return flux;
	}

	@Override
	protected double getTotalCurrentFluxInhibitor(SequenceVertex s) {
		double flux = 0;
		Template<E> temp;
		// As Inhib
		temp = this.templates.get(graph.inhibitors.get(s));
		flux += temp.inhibSequenceFlux();
		
		// As Output
		for (E e: graph.getInEdges(s)) {
			temp = this.templates.get(e);
			flux += temp.outputSequenceFlux();
		}
		if(s.getClass().isAssignableFrom(ProtectedSequenceVertex.class) && ((ProtectedSequenceVertex) s).protectedSequence){
			return flux;
		}
		if (!graph.saturableExo) {

			flux -= s.getConcentration() * graph.exoConc*Constants.exoVm/Constants.exoKmInhib;

		} else {
			double km = (graph.exoUseCustomKm?graph.getCustomExoKm().get(s.toString()):Constants.exoKmInhib);
			//flux -= s.getConcentration() * Constants.exoConc*Constants.exoVm
			/// (Constants.exoKmInhib * this.observedExoKm);
			if(graph.coupling){
			flux -= s.getConcentration() * graph.exoConc*Constants.exoVm/ this.computeExoKm(km);//(Constants.exoKmInhib);
			} else {
				flux -= s.getConcentration() * graph.exoConc*Constants.exoVm/(km + s.getConcentration());
			}

		}

		return flux;
	}
	
}
