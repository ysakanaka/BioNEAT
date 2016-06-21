package use.ready.eqwriter;

import java.util.Arrays;
import java.util.List;

import model.OligoGraph;
import model.chemicals.SequenceVertex;
import use.ready.test.GraphMaker;

public class PseudoTemplateEqWriter<E> implements TemplateEqWriter<E> {
	
	public static List<String> structPos = Arrays.asList(new String[] {"alone", "in", "ext"});
	public OligoGraph<SequenceVertex,E> graph;
	public E template;
	SequenceVertex from;
	SequenceVertex to;
	String namein ;
	String nameout;
	String kdup;
	String kin;

	String dangleL;
	String pol="pol";
	String nick="nick";
	String outputExtraStab = "1.0f/(alpha * 0.5f)"; //Similar to the default bonus an inhibitor gets on its producing template
	String missingBaseSlowdown = "mbSlowdown"; //should take into account the dangles, if relevant
	String outputInvasionRate = "outputInvasionRate";

	public PseudoTemplateEqWriter(OligoGraph<SequenceVertex,E> g, E t){
		graph = g;
		template = t;
		from = graph.getSource(t);
		to = graph.getDest(t);
		namein = Utils.idToString(from.ID-1);
		nameout = Utils.idToString(to.ID-1);
		kdup = "kdup";
		kin = "k"+namein;
		dangleL = "dangleL"+(t.toString().replace("->", "to"));
		missingBaseSlowdown += t.toString().replace("->", "to");
	}
	
	@Override
	public String[] getEqs(int baseIndex) {
		return new String[] {getAloneTempEq(baseIndex), getInTempEq(baseIndex), getExtTempEq(baseIndex)};
	}

	private String getInTempEq(int baseIndex) {
		String ret =  missingBaseSlowdown+" * "+kdup+" * "+namein+" * "+Utils.idToString(structPos.indexOf("alone")+baseIndex)+
			      " - "+ Utils.idToString(structPos.indexOf("in")+baseIndex) +" * ( "+kin+" * "+kdup+" + "+pol+" + "+
			     outputInvasionRate+" * "+kdup+" * "+nameout+" )";
		return ret;
	}

	private String getExtTempEq(int baseIndex) {
		String ret = "( "+pol+" + "+ outputInvasionRate+" * "+kdup+" * "+nameout+" ) * "+Utils.idToString(structPos.indexOf("in")+baseIndex)
			      +" - "+outputExtraStab+" * "+kin+" * "+kdup+" * "+Utils.idToString(structPos.indexOf("ext")+baseIndex);
		return ret;
	}

	private String getAloneTempEq(int baseIndex) {
		String ret = kdup+" * "+kin+" * "+Utils.idToString(structPos.indexOf("in")+baseIndex)+" + "+
			      outputExtraStab+" * "+kdup+" * "+kin+" * "+Utils.idToString(structPos.indexOf("ext")+baseIndex)+
			      " - "+kdup+" * "+ Utils.idToString(structPos.indexOf("alone")+baseIndex)+" * ( "
			      +missingBaseSlowdown+" * "+namein+" + "+nameout+" )";
		return ret;
	}

	@Override
	public String getInhibSequenceEq(int baseIndex) {
		return ""; //Cannot be inhibited
	}

	@Override
	public String getInSequenceEq(int baseIndex) {
		String ret =  kdup+" * ( "+kin+" * ("+dangleL+" * "+Utils.idToString(structPos.indexOf("in")+baseIndex)+" ) - "+missingBaseSlowdown
				+" * "+namein+" * "+Utils.idToString(structPos.indexOf("alone")+baseIndex)+" )";
		return ret;
	}

	@Override
	public String getOutSequenceEq(int baseIndex) {
		String ret = outputExtraStab+" * "+kin+" * "+kdup+" * "+Utils.idToString(structPos.indexOf("ext")+baseIndex)
			      +" - "+kdup+" * "+nameout+" * ("+Utils.idToString(structPos.indexOf("alone")+baseIndex)+" + "+
			      Utils.idToString(structPos.indexOf("in")+baseIndex)+" * "+outputInvasionRate+")";
		return ret;
	}

	@Override
	public SequenceVertex getInhib() {
		return null; //Cannot be inhibited
	}

	@Override
	public SequenceVertex getFrom() {
		return from;
	}

	@Override
	public SequenceVertex getTo() {
		return to;
	}

	public static void main(String[] args){
		OligoGraph<SequenceVertex,String> g = GraphMaker.makeAutocatalyst();
		PseudoTemplateEqWriter<String> ptew = new PseudoTemplateEqWriter<String>(g, g.getEdges().iterator().next());
		ptew.nameout = "b";
		Utils.testTemplateEqWriter(ptew, 2);
	}
	
}
