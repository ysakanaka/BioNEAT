package model;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegrator;

import model.Constants;
import model.OligoSystem;
import model.OligoSystemAllSats;
import model.SaturationEvaluator;
import model.chemicals.InvalidConcentrationException;
import model.chemicals.SequenceVertex;
import model.chemicals.Template;
import model.input.AbstractInput;

/**
 * This class is a decorator for oligosystems, adding the possibility of protected sequences (that is, sequences not degraded by exonuclease)
 * Everything else is delegated to the decorated system.
 * 
 * We also overwrite the template factory to suit our needs.
 * @author naubertkato
 *
 * @param <E>
 */
public class OligoSystemWithProtectedSequences<E> extends OligoSystemAllSats<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public int timeOut = -1;
	
	protected OligoSystemAllSats<E> decoratedOligoSystem;

	public OligoSystemWithProtectedSequences(OligoSystemAllSats<E> toDecorate) {
		//TODO: interface is incompatible. Should add a protected dummy constructor in the OligoSystem class
		super(toDecorate.getGraph(),new ECFRNTemplateFactory<E>(toDecorate.getGraph(), toDecorate.templateFactory));
		this.decoratedOligoSystem = toDecorate;
		for(SequenceVertex s : graph.getVertices()){
			if(ProtectedSequenceVertex.class.isAssignableFrom(s.getClass())){
				decoratedOligoSystem.total++;//Those sequences need two slots
			}
		}
		this.total = decoratedOligoSystem.total;
		sequences = decoratedOligoSystem.sequences;
		decoratedOligoSystem.templateFactory = templateFactory;
		templates = decoratedOligoSystem.templates;
		
		
	}

	public OligoSystemWithProtectedSequences(
			OligoSystemAllSats<E> toDecorate, SaturationEvaluator<E> se) {
		super(toDecorate.getGraph(),new ECFRNTemplateFactory<E>(toDecorate.getGraph(), toDecorate.templateFactory),se);
		for(SequenceVertex s : graph.getVertices()){
			if(ProtectedSequenceVertex.class.isAssignableFrom(s.getClass())){
				decoratedOligoSystem.total++; //Those sequences need two slots
			}
		}
		this.total = decoratedOligoSystem.total;
	}

	@Override
	public double getTotalCurrentFlux(SequenceVertex s) {
		// As Input
		if(s == ReporterIndicator.indicator){
			return 0;
		}
		
		return decoratedOligoSystem.getTotalCurrentFlux(s);
	}
	
	protected double getTotalCurrentProtectedFlux(ProtectedSequenceVertex s) {
		if (s.protectedSequence) {
			// As Input
			double flux = 0;
			TemplateWithProtected<E> temp;
			for(E e: graph.getOutEdges(s)){
				temp = (TemplateWithProtected<E>) decoratedOligoSystem.templates.get(e);
				flux += temp.inputProtectedSequenceFlux();
				}
			// As Output
			for (E e: graph.getInEdges(s)) {
				temp = (TemplateWithProtected<E>) decoratedOligoSystem.templates.get(e);
				flux += temp.outputProtectedSequenceFlux();
			}
			
			return flux;
		}
		return 0;
	}
	
	public int getReporterIndex(SequenceVertex s){
		ArrayList<Template<E>> arrs = new ArrayList<Template<E>>(decoratedOligoSystem.templates.values());
		int soFar = 0;
		for(int i = 0; i<arrs.size(); i++){
			if(arrs.get(i).getFrom().equals(s) && arrs.get(i).getTo() == null){
				return soFar;
			}
			soFar += arrs.get(i).getStates().length;
		}
		return -1;
	}
	
	@Override
	protected double[] getCurrentConcentration() {
		double concentration[] = new double[total + inhTotal];
		Iterator<SequenceVertex> it = decoratedOligoSystem.sequences.iterator();
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
		Iterator<SequenceVertex> it = decoratedOligoSystem.sequences.iterator();
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
		Iterator<Template<E>> it2 = decoratedOligoSystem.templates.values().iterator();
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
			decoratedOligoSystem.setObservedPolyKm();
		}
		if (graph.saturableNick){
			decoratedOligoSystem.setObservedNickKm();
		}
		
		if (saveActivity){
			//TODO: have to clean up
			this.time++;
			//System.out.println("New pol activity: "+time+" "+this.templates.values().iterator().next().poly+" values:"+y[0]+" "+y[1]);
			this.savedActivity[0][time] = graph.saturableExo?Constants.exoKmSimple/ this.computeExoKm(Constants.exoKmSimple):1;
			//ret[where] = graph.saturablePoly?this.templates.values().iterator().next().poly:1;
			this.savedActivity[1][time] = (graph.saturablePoly && !this.templates.isEmpty())?this.templates.values().iterator().next().getCurrentPoly()/(Constants.polVm/Constants.polKm):1;
			//ret[where] = graph.saturablePoly?this.templates.values().iterator().next().nick:1;
			this.savedActivity[2][time] = (graph.saturableNick && !this.templates.isEmpty())?this.templates.values().iterator().next().getCurrentNick()/(Constants.nickVm/Constants.nickKm):1;
		}
		
		where = 0;
		it = decoratedOligoSystem.sequences.iterator();
		SequenceVertex seq;
		while(it.hasNext()){
					seq = it.next();
					ydot[where] = this.getTotalCurrentFlux(seq); //HERE: trick
					if(ProtectedSequenceVertex.class.isAssignableFrom(seq.getClass())){
						where++; //The inputs goes on the protected seq
						ydot[where] = this.getTotalCurrentProtectedFlux(((ProtectedSequenceVertex) seq));
						
					}
					for(AbstractInput inp : seq.inputs){
						ydot[where]+=inp.f(t);
					}
					where++;
				}

		it2 = decoratedOligoSystem.templates.values().iterator();
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
		//decoratedOligoSystem.reinitializeOiligoSystem();
		
		final double[] placeholder = this.initialConditions();
		final OligoSystem<E> syst = this;
		StopableEventHandler eventHandler = new StopableEventHandler();
		TimeOutEventHandler timeOutHandler = new TimeOutEventHandler();
		timeOutHandler.setFire(timeOut);
		final StopableStepHandler handler = new StopableStepHandler(eventHandler);
		
		
		myIntegrator.addStepHandler(handler);
		myIntegrator.addEventHandler(eventHandler, 100, 1e-6, 100);
		myIntegrator.addEventHandler(timeOutHandler, 100, 1e-6, 100);
		try{
		myIntegrator.integrate(syst, 0, placeholder, erne.Constants.maxEvalTime,
				placeholder);
		} catch ( org.apache.commons.math3.exception.NumberIsTooSmallException e){
			System.out.println("Integration error: min possible value is "+e.getMin());
		}
		//syst.displayProfiling(); //For debug
		
		return handler.getTimeSerie();
		
	}
}
