package use.ready.export;

import java.util.ArrayList;
import java.util.HashMap;

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
		sp1[0] = new MyPair<String,Double>("a",5.0);
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

	}

}
