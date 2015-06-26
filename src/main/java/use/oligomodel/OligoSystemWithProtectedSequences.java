package use.oligomodel;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegrator;

import utils.MyStepHandler;
import utils.PluggableWorker;

import model.Constants;
import model.OligoGraph;
import model.OligoSystem;
import model.OligoSystemAllSats;
import model.SaturationEvaluator;
import model.chemicals.InvalidConcentrationException;
import model.chemicals.SequenceVertex;
import model.chemicals.Template;
import model.input.AbstractInput;

public class OligoSystemWithProtectedSequences<E> extends OligoSystemAllSats<E> {

	public OligoSystemWithProtectedSequences(OligoGraph<SequenceVertex, E> graph) {
		super(graph,new ECFRNTemplateFactory<E>(graph));
		for(SequenceVertex s : graph.getVertices()){
			if(ProtectedSequenceVertex.class.isAssignableFrom(s.getClass())){
				this.total++;//Those sequences need two slots
			}
		}
		
	}

	public OligoSystemWithProtectedSequences(
			OligoGraph<SequenceVertex, E> graph, SaturationEvaluator<E> se) {
		super(graph, new ECFRNTemplateFactory<E>(graph), se);
		for(SequenceVertex s : graph.getVertices()){
			if(ProtectedSequenceVertex.class.isAssignableFrom(s.getClass())){
				this.total++; //Those sequences need two slots
			}
		}
		// TODO Auto-generated constructor stub
	}

	@Override
	protected double getTotalCurrentFluxSimple(SequenceVertex s) {
		// As Input
		if(s == ReporterIndicator.indicator){
			return 0;
		}
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
	
	private double getTotalCurrentProtectedFlux(ProtectedSequenceVertex s) {
		if (s.protectedSequence) {
			// As Input
			double flux = 0;
			TemplateWithProtected temp;
			for(E e: graph.getOutEdges(s)){
				temp = (TemplateWithProtected) this.templates.get(e);
				flux += temp.inputProtectedSequenceFlux();
				}
			// As Output
			for (E e: graph.getInEdges(s)) {
				temp = (TemplateWithProtected) this.templates.get(e);
				flux += temp.outputProtectedSequenceFlux();
			}
			
			return flux;
		}
		return 0;
	}
	
	public int getReporterIndex(SequenceVertex s){
		ArrayList<Template<E>> arrs = new ArrayList<Template<E>>(templates.values());
		int soFar = 0;
		for(int i = 0; i<arrs.size(); i++){
			if(arrs.get(i).getFrom().equals(s) && arrs.get(i).getTo() == null){
				//System.out.println(soFar);
				return soFar;
			}
			soFar += arrs.get(i).getStates().length;
		}
		return -1;
	}
	
	@Override
	protected double[] getCurrentConcentration() {
		double concentration[] = new double[total + inhTotal];
		Iterator<SequenceVertex> it = this.sequences.iterator();
		int i=0;
		while (it.hasNext()) {
			SequenceVertex s = it.next();
					concentration[i] = s.getConcentration();
					i++;
					if(ProtectedSequenceVertex.class.isAssignableFrom(s.getClass())){
						concentration[i] = ((ProtectedSequenceVertex) s).getProtectedConcentration();
						i++;
					}
				}
		return (concentration);
	}
	
	@Override
	public void computeDerivatives(double t, double[] y, double[] ydot) {
		// First all the activation sequences, then inibiting, then templates.
		// ydot is a placeholder, should be updated with the derivative of y at
		// time t.
		
		int where = 0;
		boolean saveActivity = (t >= this.time +1);
		Iterator<SequenceVertex> it = this.sequences.iterator();
		while(it.hasNext()){
			SequenceVertex s = it.next();
						s.setConcentration(y[where]);
						where++;
						if(ProtectedSequenceVertex.class.isAssignableFrom(s.getClass())){
							((ProtectedSequenceVertex) s).setProtectedConcentration(y[where]);
							where++;
						}
			}

		double[] internal;
		Iterator<Template<E>> it2 = this.templates.values().iterator();
		while(it2.hasNext()) {
			Template<E> templ = it2.next();
			int length = templ.getStates().length;
			internal = new double[length];
			for(int i = 0; i< length; i++){
			internal[i] = y[where]; // there must be a better way. There is with arraycopy...
			where++;
			}
			try {
				templ.setStates(internal);
			} catch (InvalidConcentrationException e) {
				
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		if (graph.saturablePoly){
			this.setObservedPolyKm();
		}
		if (graph.saturableNick){
			this.setObservedNickKm();
		}
		
		if (saveActivity){
			//System.out.println("Saving stuff "+this.time);
			this.time++;
			//System.out.println("New pol activity: "+time+" "+this.templates.values().iterator().next().poly+" values:"+y[0]+" "+y[1]);
			this.savedActivity[0][time] = graph.saturableExo?Constants.exoKmSimple/ this.computeExoKm(Constants.exoKmSimple):1;
			//ret[where] = graph.saturablePoly?this.templates.values().iterator().next().poly:1;
			this.savedActivity[1][0] = (graph.saturablePoly && !this.templates.isEmpty())?this.templates.values().iterator().next().getCurrentPoly()/(Constants.polVm/Constants.polKm):1;
			//ret[where] = graph.saturablePoly?this.templates.values().iterator().next().nick:1;
			this.savedActivity[2][0] = (graph.saturableNick && !this.templates.isEmpty())?this.templates.values().iterator().next().getCurrentNick()/(Constants.nickVm/Constants.nickKm):1;
		}
		
		where = 0;
		it = this.sequences.iterator();
		SequenceVertex seq;
		while(it.hasNext()){
					seq = it.next();
					ydot[where] = this.getTotalCurrentFlux(seq);
					if(ProtectedSequenceVertex.class.isAssignableFrom(seq.getClass())){
						where++; //The inputs goes on the protected seq
						ydot[where] = this.getTotalCurrentProtectedFlux(((ProtectedSequenceVertex) seq));
						
					}
					for(AbstractInput inp : seq.inputs){
						ydot[where]+=inp.f(t);
					}
					where++;
				}

		it2 = this.templates.values().iterator();
		while(it2.hasNext()) {
			internal = it2.next().flux();
			for(int i=0; i<internal.length;i++){
			ydot[where] = internal[i];
			where++;
			}
			
		}
		
		where++;
	}
	
	public double[][] calculateTimeSeries() {
		final GraggBulirschStoerIntegrator myIntegrator = new GraggBulirschStoerIntegrator(
				1e-14, 1, Constants.absprec, Constants.relprec);
		
		this.reinitializeOiligoSystem();
		
		final double[] placeholder = this.initialConditions();
		final OligoSystem<E> syst = this;
		StopableEventHandler eventHandler = new StopableEventHandler();
		final StopableStepHandler handler = new StopableStepHandler(eventHandler); //TODO: put your own handler here
		
		
		myIntegrator.addStepHandler(handler);
		myIntegrator.addEventHandler(eventHandler, 100, 1e-6, 100);
		try{
		myIntegrator.integrate(syst, 0, placeholder, Constants.numberOfPoints,
				placeholder);
		} catch ( org.apache.commons.math3.exception.NumberIsTooSmallException e){
			System.out.println("Integration error: min possible value is "+e.getMin());
		}
		//syst.displayProfiling();
		
		return handler.getTimeSerie();
		
	}
}
