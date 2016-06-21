package use.ready.eqwriter;

import model.chemicals.SequenceVertex;

public interface TemplateEqWriter<E> {

	
	public String[] getEqs(int baseIndex);
	
	public String getInhibSequenceEq(int baseIndex);
	
	public String getInSequenceEq(int baseIndex);
	
	public String getOutSequenceEq(int baseIndex);
	
	public SequenceVertex getInhib();
	public SequenceVertex getFrom();
	public SequenceVertex getTo();
	
}
