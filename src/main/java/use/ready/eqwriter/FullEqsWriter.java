package use.ready.eqwriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import model.OligoGraph;
import model.chemicals.SequenceVertex;

public class FullEqsWriter {
	
	
	public static String[] generateFullEqs(OligoGraph<SequenceVertex,String> g){
		return generateFullEqs(g,true);
	}
	
	public static String[] generateFullEqs(OligoGraph<SequenceVertex,String> g, boolean explicit){
		ArrayList<String> eqs = new ArrayList<String>();
		HashMap<String,Integer> baseIndexes = new HashMap<String,Integer>();
		Iterator<SequenceVertex> itseq = g.getVertices().iterator();
		//First match id and positions for seqs
		int curIndex = 0;
		while(itseq.hasNext()){
			SequenceVertex seq = itseq.next();
			seq.ID = curIndex+1; // we don't like 0
			curIndex++;
			eqs.add("");
		}
		Iterator<String> it = g.getEdges().iterator();
		curIndex = g.getVertexCount();
		while(it.hasNext()){
			String temp = it.next();
			baseIndexes.put(temp, curIndex);
			TemplateEqWriter<String> tew = new TemplateEqWriter<String>(g, temp,explicit);
			String[] teqs = tew.getEqs(curIndex);
			eqs.addAll(Arrays.asList(teqs));
			//mod the sequencevertex eqs
			if (tew.inhib != null){
				eqs.set(tew.inhib.ID-1,eqs.get(tew.inhib.ID-1)+" + "+tew.getInhibSequenceEq(curIndex));
			}
			eqs.set(tew.from.ID-1,eqs.get(tew.from.ID-1)+" + "+tew.getInSequenceEq(curIndex));
			eqs.set(tew.to.ID-1,eqs.get(tew.to.ID-1)+" + "+tew.getOutSequenceEq(curIndex));
			//done
			curIndex += teqs.length;
		}
		EnzymeSaturationEqWriter<String> eseqw = new EnzymeSaturationEqWriter<String>(g, Utils.getDefaultSE(),explicit);
		eqs.addAll(Arrays.asList(eseqw.getAllSaturationEqs(baseIndexes)));
		for (SequenceVertex seq : g.getVertices()){
			if (eqs.get(seq.ID -1).length() >=3){
			eqs.set(seq.ID-1, eqs.get(seq.ID-1).substring(3)+" - exo"+(seq.isInhib()?"inhib":"")+" * "+Utils.idToString(seq.ID-1));
			} else {
				// the sequence has an empty equation?? So it is disconnected from the rest...
				eqs.set(seq.ID-1, " - exo"+(seq.isInhib()?"inhib":"")+" * "+Utils.idToString(seq.ID-1));
			}
		}
		return eqs.toArray(new String[eqs.size()]);
	}
	
	public static int eqSize(OligoGraph<SequenceVertex,String> g, String template){
		if (g.getInhibitableTemplates().contains(template)){
			return 5;
		}
		return 6;
	}
	
	public static void main(String[] args){
		OligoGraph<SequenceVertex,String> g = Utils.initGraph();
		SequenceVertex s1 = g.getVertexFactory().create();
		g.addSpecies(s1, 10.0, 5.0);
		String edge = g.getEdgeFactory().createEdge(s1, s1);
		g.addActivation(edge, s1, s1);
		
		System.out.println("=====================");
		System.out.println("Test 1a: autocatalyst");
		System.out.println("=====================");
		String[] eqs = generateFullEqs(g);
		for(int i = 0; i<eqs.length; i++){
			System.out.println(eqs[i]);
		}
		System.out.println("=====================");
		System.out.println("Test 1b: autocatalyst (selfstart)");
		System.out.println("=====================");
		g.selfStart = true;
		eqs = generateFullEqs(g);
		for(int i = 0; i<eqs.length; i++){
			System.out.println(eqs[i]);
		}
		
		g.selfStart = false;
		System.out.println("=====================");
		System.out.println("Test 2: autocat + activation");
		System.out.println("=====================");
		SequenceVertex s2 = g.getVertexFactory().create();
		g.addSpecies(s2, 50.0, 5.0);
		String edge2 = g.getEdgeFactory().createEdge(s1, s2);
		g.addActivation(edge2, s1, s2);
		
		eqs = generateFullEqs(g);
		for(int i = 0; i<eqs.length; i++){
			System.out.println(eqs[i]);
		}
		
		System.out.println("=====================");
		System.out.println("Test 3a: oligator");
		System.out.println("=====================");
		SequenceVertex i3 = g.getVertexFactory().create();
		i3.setInhib(true);
		g.addSpecies(i3, 1.0, 5.0);
		g.addInhibition(edge, i3);
		String edge3 = g.getEdgeFactory().createEdge(s2, i3);
		g.addActivation(edge3, s2, i3);
		
		eqs = generateFullEqs(g);
		for(int i = 0; i<eqs.length; i++){
			System.out.println(eqs[i]);
		}
		System.out.println("=====================");
		System.out.println("Test 3b: oligator (no sat)");
		System.out.println("=====================");
		
		g.saturableExo = false;
		g.saturableNick = false;
		g.saturablePoly = false;
		eqs = generateFullEqs(g);
		for(int i = 0; i<eqs.length; i++){
			System.out.println(eqs[i]);
		}
	}

}
