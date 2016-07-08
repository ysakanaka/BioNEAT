package use.ready.eqwriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.PseudoTemplateGraph;
import model.chemicals.SequenceVertex;
import use.ready.test.GraphMaker;
import utils.SequenceVertexComparator;

public class PseudoTemplateEqWriter<E> implements TemplateEqWriter<E> {
	
	public static List<String> structPos = Arrays.asList(new String[] {"alone", "in", "ext"});
	public PseudoTemplateGraph<SequenceVertex,E> graph;
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

	public PseudoTemplateEqWriter(PseudoTemplateGraph<SequenceVertex,E> g, E t){
		graph = g;
		template = t;
		from = graph.getPseudoTemplateInput(t);
		to = graph.getExtendedSpecies(from);
		namein = Utils.idToString(from.ID-1);
		//here is a trick: all the ext species will come after the normal species
		//otherwise, both species have the same id, so we would wrongly get the same name
		//NOTE: inefficient computation...
		ArrayList<SequenceVertex> extSpecs = new ArrayList<SequenceVertex>(g.getAllExtendedSpecies());
		extSpecs.sort(new SequenceVertexComparator());
		nameout = Utils.idToString(g.getVertexCount()+extSpecs.indexOf(to)+1);
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
			      " - "+ Utils.idToString(structPos.indexOf("in")+baseIndex) +" * ( "+dangleL+" * "+kin+" * "+kdup+" + "+pol+" + "+
			     outputInvasionRate+" * "+kdup+" * "+nameout+" )";
		return ret;
	}

	private String getExtTempEq(int baseIndex) {
		String ret = "( "+pol+" + "+ outputInvasionRate+" * "+kdup+" * "+nameout+" ) * "+Utils.idToString(structPos.indexOf("in")+baseIndex)
			      +" - "+outputExtraStab+" * "+kin+" * "+kdup+" * "+Utils.idToString(structPos.indexOf("ext")+baseIndex);
		return ret;
	}

	private String getAloneTempEq(int baseIndex) {
		String ret = kdup+" * "+dangleL+" * "+kin+" * "+Utils.idToString(structPos.indexOf("in")+baseIndex)+" + "+
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
		PseudoTemplateGraph<SequenceVertex,String> g = GraphMaker.makeAutocatalystWithPT();
		SequenceVertex s = g.getVertices().iterator().next();
		PseudoTemplateEqWriter<String> ptew = new PseudoTemplateEqWriter<String>(g, g.getEdgeFactory().createEdge(s, g.getExtendedSpecies(s)));
		int tempIndex = g.getVertexCount()+g.getAllExtendedSpecies().size() + 5; //5 cannot be determined statically, I need to implement the previous template. Oh well, fine for now.
		Utils.testTemplateEqWriter(ptew, tempIndex);
	}
	
}
