package use.ready.eqwriter;

import model.OligoGraph;
import model.chemicals.SequenceVertex;

public class TemplateWithShortInputDomainEqWriter<E> extends DefaultTemplateEqWriter<E> {
	
	String nameinBase;
	String missingBaseSlowdown = "mbSlowdown"; //how slow is it to attach
	
	public TemplateWithShortInputDomainEqWriter(OligoGraph<SequenceVertex, E> g, E t) {
		this(g, t,true);
	}

	public TemplateWithShortInputDomainEqWriter(OligoGraph<SequenceVertex, E> g, E t, boolean numerical) {
		super(g, t, numerical);
		nameinBase = namein; //in case the trick is not valid
		namein += " * "+missingBaseSlowdown+t.toString().replace("->", "to"); //trick, valid since namein is only used for hybridization.
	}

}
