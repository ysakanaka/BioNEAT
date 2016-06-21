package use.ready.eqwriter;

import model.OligoGraph;
import model.chemicals.SequenceVertex;

public class TemplateWithShortInputDomainEqWriter<E> extends DefaultTemplateEqWriter<E> {
	
	public TemplateWithShortInputDomainEqWriter(OligoGraph<SequenceVertex, E> g, E t) {
		super(g, t);
		
	}

	public TemplateWithShortInputDomainEqWriter(OligoGraph<SequenceVertex, E> g, E t, boolean numerical) {
		super(g, t, numerical);
		
	}

}
