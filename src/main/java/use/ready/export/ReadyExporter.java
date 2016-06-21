package use.ready.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.pattern.Util;
import use.ready.beads.Bead;
import use.ready.eqwriter.EnzymeSaturationEqWriter;
import use.ready.eqwriter.FullEqsWriter;
import use.ready.eqwriter.Utils;
import model.Constants;
import model.OligoGraph;
import model.SaturationEvaluator;
import model.SaturationEvaluator.ENZYME;
import model.SlowdownConstants;
import model.chemicals.SequenceVertex;
import use.ready.test.GraphMaker;
import utils.MyPair;

public class ReadyExporter {
	
	public static int dimensions = 2;
	public static int maxSteps = 1000; // How many steps to simulate
    public static int interval = 100; // Take a snapshot every interval steps.
    public static int seed = 0; //Seed for random runs
    public static double temperature =  313.13;
    public static double scale = 5e-13; //scale for beads, in m
    public static double signalDiffusion = 16.0;
    public static double templateDiffusion = 10.7;
    
    public static double[] enzymeDiffusions = {10.7, 0.0, 10.7, 0.0, 10.7, 6.4}; //pol, nick, exo TODO; current values are BS
    
    public static SaturationEvaluator<String> se = Utils.getDefaultSE();
	
	public static String beadToReady(Bead bead, int index){
		StringBuilder st = new StringBuilder();
		for(int i = 0; i<bead.species.length; i++){
			st.append("bead_"+(index+i)+" = (\n"
		               +"x = "+ bead.x + "\n"
				       +"y = "+ bead.y + "\n"
				       +"radius = "+ bead.radius + "\n"
				       +"conc = "+ bead.conc[i] + "\n"
				       +"chem = "+ bead.species[i] + "\n)\n");
		}
		return st.toString();
	}
	
	public static Map<String,Double> getDefaultDiffusion(int totalSpecies, String[] eqs, String[] enzymes){
		HashMap<String,Double> diff = new HashMap<String,Double>();
		int numEnzEqs = enzymes.length;
		if (totalSpecies > eqs.length - numEnzEqs){
			System.err.println("ERROR: getDefaultDiffusion: incoherent number of signal species");
			return null;
		}
		for (int i= 0; i<totalSpecies; i++){
			diff.put(Utils.idToString(i), signalDiffusion);
		}
		for (int i=totalSpecies;i<eqs.length-numEnzEqs;i++){
			diff.put(Utils.idToString(i), templateDiffusion);
		}
		return diff;
	}
	
	public static String getReadyInit(OligoGraph<SequenceVertex,String> g, Map<String,Double> diffusions, String[] eqs, String[] enzymes){
		StringBuilder st = new StringBuilder();
		st.append("init = (\n");
		//TODO: I need a much more generic approach to enzymes. Has to go in DACCAD
		
		st.append("float4 PolConc = "+g.polConc+";\n");
		st.append("float4 NickConc = "+g.nickConc+";\n");
		st.append("float4 ExoConc = "+g.exoConc+";\n");
		
		st.append("float4 PolVm = "+Constants.polVm+";\n");
		st.append("float4 NickVm = "+Constants.nickVm+";\n");
		st.append("float4 ExoVm = "+Constants.exoVm+";\n");
		st.append("float4 PolKm = "+ se.enzymeKms[ENZYME.POL.value][SaturationEvaluator.TIN]+";\n");
		st.append("float4 PolKmBoth = "+ se.enzymeKms[ENZYME.POL.value][SaturationEvaluator.TBOTH]+";\n");
		st.append("float4 NickKm = "+ se.enzymeKms[ENZYME.NICK.value][SaturationEvaluator.TEXT]+";\n");
		st.append("float4 ExoKm = "+ se.enzymeKms[ENZYME.EXO.value][SaturationEvaluator.SIGNAL]+";\n");
		st.append("float4 ExoKmInhib = "+ se.enzymeKms[ENZYME.EXO.value][SaturationEvaluator.INHIB]+";\n");
		st.append(enzymeSaturationToReady(eqs,enzymes));
		
		st.append(allSimulationParams(g));
		//If I remember correctly, D_species should be already defined, we just change their value
		for(String s : diffusions.keySet()){
			st.append("D_"+s+" = "+diffusions.get(s)+";\n");
		}
		
		if(EnzymeSaturationEqWriter.enzymeDiffusion){
			for(int i = 0; i<EnzymeSaturationEqWriter.enzymeName.length; i++){
				st.append("float4 D_"+EnzymeSaturationEqWriter.enzymeName[i]+"Free = "+enzymeDiffusions[2*i]+";\n");
				st.append("float4 D_"+EnzymeSaturationEqWriter.enzymeName[i]+"Attached = "+enzymeDiffusions[2*i+1]+";\n");
			}
			
		}
		
		//TODO: add diffusion for enzymes
		
		st.append(")\n");
		return st.toString();
	}
	
	public static String enzymeSaturationToReady(String[] eqs, String[] enzymes){
		StringBuilder st = new StringBuilder();
		int offset = eqs.length-enzymes.length;
		
		
		
		for(int i=0;i < enzymes.length; i++){
			st.append("float4 "+enzymes[i]+" = \""+eqs[offset+i]+";\"\n");
		}
		
		if(EnzymeSaturationEqWriter.enzymeDiffusion){
			int realOffset = offset - 2*EnzymeSaturationEqWriter.enzymeName.length;
			
			for(int i = 0; i < EnzymeSaturationEqWriter.enzymeName.length; i++){
				st.append(Utils.idToString(2*i+realOffset)+" = "+EnzymeSaturationEqWriter.enzymeName[i]+"ConcFree;\n");
				st.append(Utils.idToString(2*i+1+realOffset)+" = "+EnzymeSaturationEqWriter.enzymeName[i]+"ConcAttached;\n");
				st.append(Utils.idToString(2*i+realOffset)+"_in[index_here] = "+EnzymeSaturationEqWriter.enzymeName[i]+"ConcFree;\n");
				st.append(Utils.idToString(2*i+1+realOffset)+"_in[index_here] = "+EnzymeSaturationEqWriter.enzymeName[i]+"ConcAttached;\n");
			}
		}
		
		return st.toString();
	}
	
	/**
	 * Generate the string for global parameters
	 * @param params
	 * @param beads
	 * @param eqs
	 * @param numEnzEqs
	 * @return
	 */
	public static String readyGlobalConfigs(Map<String,?> params, ArrayList<Bead> beads, String[] eqs, String[] enzymes, Map<String, Boolean> diffusing){
		StringBuilder st = new StringBuilder();
		int numEnzEqs = enzymes.length;
		
		st.append("nSpecies = "+(eqs.length-numEnzEqs)+"\n");
	    for (String p : params.keySet()){
		    st.append(p+" = "+params.get(p).toString()+"\n");
	    }
	    int index = 0;
	    for (Bead b : beads){
			st.append(beadToReady(b,index)+"\n");
			index += b.species.length;
	    }
	    st.append("formula = (\n");
	    for (int i = 0; i < eqs.length-numEnzEqs;i++){
	    	String name = Utils.idToString(i);
	        st.append("delta_"+name+" = \""+ eqs[i]+(diffusing.get(name)!=null && diffusing.get(name)?" + D_"+name+"* laplacian_"+name:"")+";\"\n");
	    }
	    st.append(")\n");
	    //st.append(getReadyInit(g, diffusion,eqs,enzymes));
			    //totalConfig += "init = (\n"+graphInitialReadyParams(penGraph,customSimuParams)+")\n"
	    return st.toString();
	}
	
	public static String allInOneReadyExport(OligoGraph<SequenceVertex,String> g, ArrayList<Bead> beads, String[] enzymes, Map<String, Boolean> diffusing){
		StringBuilder st = new StringBuilder();
		String[] eqs = FullEqsWriter.generateFullEqs(g,false);
		st.append(readyGlobalConfigs(basicParams(),beads,eqs,enzymes, diffusing));
		
		st.append(getReadyInit(g, getDefaultDiffusion(g.getVertexCount(),eqs,enzymes),eqs,enzymes));
		return st.toString();
	}
	
	public static Map<String,?> basicParams(){
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("dimensions", dimensions);
		params.put("maxSteps", maxSteps);
		params.put("interval", interval);
		params.put("seed", seed);
		params.put("temperature", temperature);
		params.put("scale", scale);
		return params;
	}
	
	public static String allSimulationParams(OligoGraph<SequenceVertex,String> g){
		StringBuilder st = new StringBuilder();
		//Basic params
		st.append("float4 kdup = "+Constants.Kduplex+";\n");
		st.append("float4 ratioleft = "+Double.toString(Constants.ratioToeholdLeft)+";\n");
		st.append("float4 ratioright = "+Double.toString(Constants.ratioToeholdRight)+";\n");
		st.append("float4 alpha = "+Double.toString(SlowdownConstants.inhibDangleSlowdown*Constants.alphaBase)+";\n");
		st.append("float4 displ = "+ Double.toString(Constants.displ)+";\n");
		
		for(SequenceVertex s : g.getVertices()){
			st.append("float4 k"+Utils.idToString(s.ID-1)+" = "+g.K.get(s)+";\n");
		}
		
		for(String edge : g.getEdges()){
			st.append("float4 dangleL"+edge.replace("->", "to")+" = "+g.getDangleL(edge)+";\n");
			st.append("float4 dangleR"+edge.replace("->", "to")+" = "+g.getDangleR(edge)+";\n");
			st.append("float4 stack"+edge.replace("->", "to")+" = "+g.getStacking(edge)+";\n");
		}
		
		return st.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args){
		System.out.println("===================");
		System.out.println("Test 1: Bead test");
		System.out.println("===================");
		MyPair<String,Double>[] sp1 = (MyPair<String,Double>[]) new MyPair[6];
		sp1[0] = new MyPair<String,Double>("a",5.0);
		sp1[1] = new MyPair<String,Double>("b",3.0);
		sp1[2] = new MyPair<String,Double>("c",50.0);
		sp1[3] = new MyPair<String,Double>("d",3.0);
		sp1[4] = new MyPair<String,Double>("i",50.0);
		sp1[5] = new MyPair<String,Double>("n",20.0);
		Bead b = new Bead(0.1,0.3,0.03,sp1);
		System.out.println(beadToReady(b,0));
		System.out.println("===================");
		System.out.println("Test 2: Autocatalyst system export");
		System.out.println("===================");
		ArrayList<Bead> beads = new ArrayList<Bead>();
		beads.add(b);
		HashMap<String,Boolean> diffusing = new HashMap<String,Boolean>();
		diffusing.put("a", true);
		diffusing.put("b", true);
		diffusing.put("c", true);
		String[] enzymes = {"pol", "poldispl", "nick", "exo", "exoinhib"};
		String[] enzymesDiff = {"pol", "poldispl", "nick", "exo", "exoinhib",
				"PolConcFree", "PolConcAttached", "NickConcFree", "NickConcAttached", "ExoConcFree", "ExoConcAttached"};
		OligoGraph<SequenceVertex,String> g = GraphMaker.makeAutocatalyst();
		System.out.println(allInOneReadyExport(g,beads,enzymes,diffusing));
		System.out.println("===================");
		System.out.println("Test 3: Oligator system export");
		System.out.println("===================");
		g =GraphMaker.makeOligator();
		System.out.println(allInOneReadyExport(g,beads,enzymes,diffusing));
		System.out.println("===================");
		System.out.println("Test 4: Oligator system export with diffusing enzymes");
		System.out.println("===================");
		EnzymeSaturationEqWriter.enzymeDiffusion = true;
		System.out.println(allInOneReadyExport(g,beads,enzymesDiff,diffusing));
	}

}
