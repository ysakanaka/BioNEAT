package use.ready.eqwriter;

import java.util.HashMap;
import java.util.Iterator;

import model.Constants;
import model.OligoGraph;
import model.SaturationEvaluator;
import model.chemicals.SequenceVertex;

public class EnzymeSaturationEqWriter<E> {

	public OligoGraph<SequenceVertex,E> graph;
	public SaturationEvaluator<E> se;
	
	public String polVm;
	public String displ ;
	public String nickVm;
	public String exoVm;
	public String polKm; // from the se. Based on the main target of each enzyme
	public String polKmBoth;
	public String nickKm;
	public String exoKm;
	public String exoKmInhib;
	
	public EnzymeSaturationEqWriter(OligoGraph<SequenceVertex,E> g, SaturationEvaluator<E> se){
		graph = g;
		this.se = se;
		init(g,true);
	}
	
	public EnzymeSaturationEqWriter(OligoGraph<SequenceVertex,E> g, SaturationEvaluator<E> se, boolean explicitValues){
		graph = g;
		this.se = se;
		init(g,explicitValues);
	}
	
	protected void init(OligoGraph<SequenceVertex,E> g, boolean explicitValues){
		if (explicitValues){
			displ =  Double.toString(Constants.displ);
			polVm = Double.toString(g.polConc*Constants.polVm);
		    nickVm = Double.toString(g.nickConc*Constants.nickVm);
		    exoVm = Double.toString(g.exoConc*Constants.exoVm);
		    polKm = Double.toString(se.enzymeKms[0][SaturationEvaluator.TIN]);
		    polKmBoth = Double.toString(se.enzymeKms[0][SaturationEvaluator.TBOTH]);
		    nickKm = Double.toString(se.enzymeKms[1][SaturationEvaluator.TEXT]);
		    exoKm = Double.toString(se.enzymeKms[2][SaturationEvaluator.SIGNAL]);
		    exoKmInhib = Double.toString(se.enzymeKms[2][SaturationEvaluator.INHIB]);
		} else {
			displ = "displ";
			polVm = "PolConc * PolVm";
		    nickVm = "NickConc * NickVm";
		    exoVm = "ExoConc * ExoVm";
		    polKm = "PolKm";
		    polKmBoth = "PolKmBoth";
		    nickKm = "NickKm";
		    exoKm = "ExoKm";
		    exoKmInhib = "ExoKmInhib";
		}
	}
	
	public String getEnzymeSaturationEq(SaturationEvaluator.ENZYME enz, HashMap<E,Integer> baseIndexes){
		String eq;
		int enzIndex = enz.value;
		switch (enz){
		case POL:
			if(!graph.saturablePoly) return polVm+"/"+polKm;
			eq = polVm+"/("+polKm+" * ( 1 ";
			break;
		case NICK:
			if(!graph.saturableNick) return nickVm+"/"+nickKm;
			eq = nickVm+"/("+nickKm+" * ( 1 ";
			break;
		case EXO:
			if(!graph.saturableExo) return exoVm+"/"+exoKm;
			eq = exoVm+"/("+exoKm+" * ( 1 ";
			break;
		default:
			System.err.println("WARNING: getEnzymeSaturationEq called on an unknown enzyme type.");
			return "";
		}
		for (int i = 0; i< SaturationEvaluator.SIGNAL;i++){
			if (se.enzymeKms[enzIndex][i] == 0.0) continue;
			for(E temp : graph.getEdges()){
				eq += "+ "+Utils.idToString(baseIndexes.get(temp)+i)+"/"+se.enzymeKms[enzIndex][i]+"f";
			}
		}
		Iterator<SequenceVertex> it = graph.getVertices().iterator();
		int curIndex = 0;
		while (it.hasNext()){
			SequenceVertex s = it.next();
			
			if (se.enzymeKms[enzIndex][SaturationEvaluator.SIGNAL] > 0.0 && !s.isInhib()){
				eq+="+ "+Utils.idToString(curIndex)+"/"+se.enzymeKms[enzIndex][SaturationEvaluator.SIGNAL]+"f";
			} else if (se.enzymeKms[enzIndex][SaturationEvaluator.INHIB] > 0.0 && s.isInhib()){
				eq+="+ "+Utils.idToString(curIndex)+"/"+se.enzymeKms[enzIndex][SaturationEvaluator.INHIB]+"f";
			}
			curIndex++;
		}
		return eq+") )";
	}
	
	public String[] getAllSaturationEqs(HashMap<E,Integer> baseIndexes){
		String polEq = getEnzymeSaturationEq(SaturationEvaluator.ENZYME.POL,baseIndexes);
		String polDisplEq = displ+" * "+polKm+"/"+polKmBoth+" * "+polEq;
		String nickEq = getEnzymeSaturationEq(SaturationEvaluator.ENZYME.NICK,baseIndexes);
		String exoEq = getEnzymeSaturationEq(SaturationEvaluator.ENZYME.EXO,baseIndexes);
		String exoInhibEq = exoKm+"/"+exoKmInhib+" * "+exoEq;
		return new String[] {polEq, polDisplEq, nickEq, exoEq, exoInhibEq};
	}
	
	public static void main(String[] args){
		OligoGraph<SequenceVertex,String> g = Utils.initGraph();
		SequenceVertex s = g.getVertexFactory().create();
		g.addSpecies(s, 10.0, 5.0);
		String edge = g.getEdgeFactory().createEdge(s, s);
		g.addActivation(edge, s, s);
		//graph init done
		
		HashMap<String,Integer> baseIndexes = new HashMap<String,Integer>();
		Iterator<String> it = g.getEdges().iterator();
		int curIndex = g.getVertexCount();
		while(it.hasNext()){
			String temp = it.next();
			baseIndexes.put(temp, curIndex);
			String[] eqs = new TemplateEqWriter<String>(g, temp).getEqs(curIndex);
			for(int i=0; i<eqs.length; i++){
				System.out.println(eqs[i]);
			}
			curIndex += eqs.length;
		}
		EnzymeSaturationEqWriter<String> eseqw = new EnzymeSaturationEqWriter<String>(g, Utils.getDefaultSE());
		String[] eqs = eseqw.getAllSaturationEqs(baseIndexes);
		for(int i=0; i<eqs.length; i++){
			System.out.println(eqs[i]);
		}
	}
	
}
