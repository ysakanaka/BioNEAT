package use.ready.export;

import java.util.ArrayList;
import java.util.HashMap;

import model.OligoGraph;
import model.PseudoTemplateGraph;
import model.chemicals.SequenceVertex;
import use.ready.beads.Bead;
import use.ready.test.GraphMaker;
import utils.MyPair;

public class TestBasicPT {

	public static void main(String[] args) {
		PseudoTemplateGraph<SequenceVertex,String> g = GraphMaker.makeAutocatalystWithPT();
		@SuppressWarnings("unchecked")
		MyPair<String,Double>[] sp1 = (MyPair<String,Double>[]) new MyPair[3];
		sp1[0] = new MyPair<String,Double>("a",0.1);
		sp1[1] = new MyPair<String,Double>("c",10.0);
		sp1[2] = new MyPair<String,Double>("m",10.0);
		Bead b = new Bead(0.1,0.3,0.03,sp1);
		System.out.println(ReadyExporter.beadToReady(b,0));
		System.out.println("===================");
		System.out.println("Test: Autocatalyst with PT");
		System.out.println("===================");
		ArrayList<Bead> beads = new ArrayList<Bead>();
		beads.add(b);
		HashMap<String,Boolean> diffusing = new HashMap<String,Boolean>();
		diffusing.put("a", true);
		diffusing.put("b", true);
		String[] enzymes = {"pol", "poldispl", "nick", "exo", "exoinhib"};
		
		
		System.out.println(ReadyExporter.allInOneReadyExport(g,beads,enzymes,diffusing));
		
		System.out.println("===================");
		System.out.println("Test: Autocatalyst without PT");
		System.out.println("===================");

		OligoGraph<SequenceVertex,String> g2 = GraphMaker.makeAutocatalyst();
		@SuppressWarnings("unchecked")
		MyPair<String,Double>[] sp2 = (MyPair<String,Double>[]) new MyPair[2];
		sp2[0] = new MyPair<String,Double>("a",0.1);
		sp2[1] = new MyPair<String,Double>("b",10.0);
		Bead b2 = new Bead(0.1,0.3,0.03,sp2);
		HashMap<String,Boolean> diffusing2 = new HashMap<String,Boolean>();
		diffusing2.put("a", true);
		ArrayList<Bead> beads2 = new ArrayList<Bead>();
		beads2.add(b2);
		System.out.println(ReadyExporter.allInOneReadyExport(g2,beads2,enzymes,diffusing2));
		
	}

}
