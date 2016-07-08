package use.ready.eqwriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import model.OligoGraph;
import model.PseudoTemplateGraph;
import model.chemicals.SequenceVertex;

public class FullEqsWriter {
	
	
	public static String[] generateFullEqs(OligoGraph<SequenceVertex,String> g){
		return generateFullEqs(g,true);
	}
	
	public static String[] generateFullEqs(OligoGraph<SequenceVertex,String> g, boolean explicit){
		
		boolean withPT = PseudoTemplateGraph.class.isAssignableFrom(g.getClass());
		
		ArrayList<String> eqs = new ArrayList<String>();
		HashMap<String,Integer> baseIndexes = new HashMap<String,Integer>();
		HashMap<SequenceVertex,Integer> extSpeciesIndex = new HashMap<SequenceVertex,Integer>();
		Iterator<SequenceVertex> itseq = g.getVertices().iterator();
		//First match id and positions for seqs
		int curIndex = 0;
		while(itseq.hasNext()){
			SequenceVertex seq = itseq.next();
			
			seq.ID = curIndex+1; // we don't like 0. Note that it might create problems for hashmaps...
			//TODO: actually, it might just be that the hashmaps cannot find anything anymore.
			//In retrospect, it is a bad idea to change something that defines the hash function (and thus indexing)
			curIndex++;
			eqs.add("");
		}
		
		//If we have pseudo templates, next comes those sequences
		//Note that we don't use their index for naming, so we don't need to modify it (although we could to be safe)
		if (withPT){
			for (SequenceVertex s :((PseudoTemplateGraph<SequenceVertex,String>) g).getAllExtendedSpecies()){
				if (s != null){
					extSpeciesIndex.put(s, curIndex+1);
					curIndex++;
					eqs.add("");
				}
			}
		}
		
		Iterator<String> it = g.getEdges().iterator();
		while(it.hasNext()){
			String temp = it.next();
			baseIndexes.put(temp, curIndex);
			TemplateEqWriter<String> tew;
			
			if (withPT){
				tew = new TemplateWithExtendedSpecies<String>((PseudoTemplateGraph<SequenceVertex,String>) g, temp, explicit);
			}
			else {
				tew = new DefaultTemplateEqWriter<String>(g, temp,explicit);
			}
			String[] teqs = tew.getEqs(curIndex);
			eqs.addAll(Arrays.asList(teqs));
			//mod the sequencevertex eqs
			if (tew.getInhib() != null){
				eqs.set(tew.getInhib().ID-1,eqs.get(tew.getInhib().ID-1)+" + "+tew.getInhibSequenceEq(curIndex));
			}
			eqs.set(tew.getFrom().ID-1,eqs.get(tew.getFrom().ID-1)+" + "+tew.getInSequenceEq(curIndex));
			eqs.set(tew.getTo().ID-1,eqs.get(tew.getTo().ID-1)+" + "+tew.getOutSequenceEq(curIndex));
			if (withPT){
				TemplateWithExtendedSpecies<String> ttew = ((TemplateWithExtendedSpecies<String>) tew);
				if(ttew.fromext != null) eqs.set(extSpeciesIndex.get(ttew.fromext)-1,eqs.get(extSpeciesIndex.get(ttew.fromext)-1)+" + "+ttew.getInExtSequenceEq(curIndex));
				if(ttew.toext != null) eqs.set(extSpeciesIndex.get(ttew.toext)-1,eqs.get(extSpeciesIndex.get(ttew.toext)-1)+" + "+ttew.getOutExtSequenceEq(curIndex));
			}
			//done
			curIndex += teqs.length;
		}
		
		//Then add pseudotemplates, if any
		if (withPT){
			Iterator<SequenceVertex> it2 = ((PseudoTemplateGraph<SequenceVertex,String>) g).getAllSpeciesWithPseudoTemplate().iterator();
			while(it2.hasNext()){
				SequenceVertex seq = it2.next();
				SequenceVertex extSeq = ((PseudoTemplateGraph<SequenceVertex,String>) g).getExtendedSpecies(seq);
				String temp = ((PseudoTemplateGraph<SequenceVertex,String>) g).getPseudoTemplate(seq);
				
				TemplateEqWriter<String> tew = new PseudoTemplateEqWriter<String>((PseudoTemplateGraph<SequenceVertex,String>) g,temp);
				String[] teqs = tew.getEqs(curIndex);
				eqs.addAll(Arrays.asList(teqs));
				
				eqs.set(tew.getFrom().ID-1,eqs.get(tew.getFrom().ID-1)+" + "+tew.getInSequenceEq(curIndex));
				
				eqs.set(extSpeciesIndex.get(extSeq)-1,eqs.get(extSpeciesIndex.get(extSeq)-1)+" + "+tew.getOutSequenceEq(curIndex));
				//done
				curIndex += teqs.length;
			}
		}
		
		
		EnzymeSaturationEqWriter<String> eseqw = new EnzymeSaturationEqWriter<String>(g, Utils.getDefaultSE(),explicit);
		if (EnzymeSaturationEqWriter.enzymeDiffusion){
			for(int i = 0; i < EnzymeSaturationEqWriter.enzymeName.length; i++){
				eqs.add("D_"+EnzymeSaturationEqWriter.enzymeName[i]+"Free * laplacian_"+Utils.idToString(eqs.size())); //Enzymes only have diffustion settings
				eqs.add("D_"+EnzymeSaturationEqWriter.enzymeName[i]+"Attached * laplacian_"+Utils.idToString(eqs.size())); //Enzymes only have diffustion settings
			}
			eqs.addAll(Arrays.asList(eseqw.getAllSaturationEqs(baseIndexes,curIndex)));
		} else {
		    eqs.addAll(Arrays.asList(eseqw.getAllSaturationEqs(baseIndexes)));
		}
		for (SequenceVertex seq : g.getVertices()){
			if (eqs.get(seq.ID -1).length() >=3){
			eqs.set(seq.ID-1, eqs.get(seq.ID-1).substring(3)+" - exo"+(seq.isInhib()?"inhib":"")+" * "+Utils.idToString(seq.ID-1));
			} else {
				// the sequence has an empty equation?? So it is disconnected from the rest...
				eqs.set(seq.ID-1, " - exo"+(seq.isInhib()?"inhib":"")+" * "+Utils.idToString(seq.ID-1));
			}
		}
		
		if(withPT){
			for (SequenceVertex extSeq : ((PseudoTemplateGraph<SequenceVertex,String>) g).getAllExtendedSpecies()){
				if (eqs.get(extSpeciesIndex.get(extSeq) -1).length() >=3){
				eqs.set(extSpeciesIndex.get(extSeq)-1, eqs.get(extSpeciesIndex.get(extSeq)-1).substring(3)+" - exoinhib * "+Utils.idToString(extSpeciesIndex.get(extSeq)-1));
				} else {
					// the sequence has an empty equation?? So it is disconnected from the rest...
					eqs.set(extSpeciesIndex.get(extSeq)-1, " - exoinhib * "+Utils.idToString(extSpeciesIndex.get(extSeq)-1));
				}
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
