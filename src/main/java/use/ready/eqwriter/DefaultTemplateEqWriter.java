package use.ready.eqwriter;

import java.util.Arrays;
import java.util.ArrayList;

import model.Constants;
import model.OligoGraph;
import model.SlowdownConstants;
import model.chemicals.SequenceVertex;
import use.ready.test.GraphMaker;

/**
 * This class defines the default functions to generate equations from a basic PEN toolbox template.
 * Other types of templates need to extend this class.
 * @author naubertkato
 *
 * @param <E>
 */
public class DefaultTemplateEqWriter<E> implements TemplateEqWriter<E>{

	public ArrayList<String> structPos = new ArrayList<String>(Arrays.asList(new String[] {"alone", "in", "out", "both", "ext", "inhib"}));
	public OligoGraph<SequenceVertex,E> graph;
	public E template;
	SequenceVertex inhib;
	SequenceVertex from;
	SequenceVertex to;
	String nameinhib ;
	String namein ;
	String nameout;
	String ratioleft;
	String ratioright;
	String kinhib;
	String alpha;
	String kdup;
	String kin;
	String kout;
	String dangleL;
	String dangleR;
	String stack;
	String selfstart;
	String pol="pol";
	String poldispl;
	String nick="nick";
	
	
	public DefaultTemplateEqWriter(OligoGraph<SequenceVertex,E> g, E t){
		this.graph = g;
		this.template = t;
		inhib = graph.getInhibition(t) != null ? graph.getInhibition(t).getLeft(): null;
		from = graph.getSource(t);
		to = graph.getDest(t);
		nameinhib = inhib!=null?Utils.idToString(inhib.ID-1):"0";
		namein = Utils.idToString(from.ID-1);
		nameout = Utils.idToString(to.ID-1);
		initValues(g,t,true);
		if (to.isInhib()){
		    poldispl = "poldispl";
		}else{
		    poldispl = "pol";
		}
	}
	
	public DefaultTemplateEqWriter(OligoGraph<SequenceVertex,E> g, E t, boolean numerical){
		this.graph = g;
		this.template = t;
		inhib = graph.getInhibition(t) != null ? graph.getInhibition(t).getLeft(): null;
		from = graph.getSource(t);
		to = graph.getDest(t);
		nameinhib = inhib!=null?Utils.idToString(inhib.ID-1):"0";
		namein = Utils.idToString(from.ID-1);
		nameout = Utils.idToString(to.ID-1);
		initValues(g,t,numerical);
		if (to.isInhib()){
		    poldispl = "poldispl";
		}else{
		    poldispl = "pol";
		}
	}
	
	protected void initValues(OligoGraph<SequenceVertex,E> g, E t,boolean numerical){
		if (numerical){
			ratioleft = Double.toString(Constants.ratioToeholdLeft);
			ratioright = Double.toString(Constants.ratioToeholdRight);
			kinhib = inhib!=null?Double.toString(graph.getK(inhib)):"0";
			alpha = inhib!=null?Double.toString(SlowdownConstants.inhibDangleSlowdown*Constants.alphaBase*graph.getDangleR(graph.getIncidentEdges(inhib).iterator().next())):"0";
			kdup = Double.toString(Constants.Kduplex);
			kin = Double.toString(graph.getK(from));
			kout = Double.toString(graph.getK(to));
			dangleL = Double.toString(graph.getDangleL(t));
			dangleR = Double.toString(graph.getDangleR(t));
			stack = Double.toString(graph.getStacking(t));
			selfstart = Double.toString(Constants.ratioSelfStart);
		} else {
			ratioleft = "ratioleft";
			ratioright = "ratioright";
			kinhib = inhib!=null?"k"+nameinhib:"0";
			if (inhib != null){
				if (graph.getIncidentEdges(inhib).iterator().hasNext() ){
					alpha = "alpha * dangleR"+graph.getIncidentEdges(inhib).iterator().next().toString().replace("->", "to");
				} else {
					alpha = "alpha * 0.5f"; // This case means that the inhibitor isn't created by anyone.
				}
			} else {
				alpha = "0";
			}
			kdup = "kdup";
			kin = "k"+namein;
			kout = "k"+nameout;
			dangleL = "dangleL"+(t.toString().replace("->", "to"));
			dangleR = "dangleR"+(t.toString().replace("->", "to"));
			stack = "stack"+(t.toString().replace("->", "to"));
			selfstart = "selfstart";
		}
		if (inhib == null) structPos.remove("inhib");
	}
	
	public String getInhibSequenceEq(int baseIndex){
		
		if (inhib != null){
			return kdup+" * ("+alpha+" * "+kinhib+" * "+Utils.idToString(structPos.indexOf("inhib")+baseIndex)+" - "+nameinhib+" * ( "
					+Utils.idToString(structPos.indexOf("alone")+baseIndex)+" + "+Utils.idToString(structPos.indexOf("in")+baseIndex)
					+" + "+Utils.idToString(structPos.indexOf("out")+baseIndex)+" ) + "+Utils.idToString(structPos.indexOf("inhib")+baseIndex)
					+" * ( "+ratioleft+" * "+namein+" + "+ratioright+" * "+nameout+" ) )";
		}
		
		return "0";
	}
	
	public String getInSequenceEq(int baseIndex){
		String ret =  kdup+" * ( "+kin+" * ("+dangleL+" * "+Utils.idToString(structPos.indexOf("in")+baseIndex)+" + "
	        +stack+" / "+dangleR+" * "+Utils.idToString(structPos.indexOf("both")+baseIndex)+" ) - "+namein+" * ( "
			+Utils.idToString(structPos.indexOf("alone")+baseIndex)+" + "+Utils.idToString(structPos.indexOf("out")+baseIndex)+" ) ";
		if (inhib != null){
			ret += " + "+nameinhib+" * "+Utils.idToString(structPos.indexOf("in")+baseIndex)+" - "+ratioleft+" * "
		        +namein+" * "+Utils.idToString(structPos.indexOf("inhib")+baseIndex);
		}
		return ret+")";
	    
	}
	
	public String getOutSequenceEq(int baseIndex){
        String eq = pol+" * "+Utils.idToString(structPos.indexOf("both")+baseIndex)+" + "+kdup+" * ( "+kout
            +" * ("+dangleR+" * "+Utils.idToString(structPos.indexOf("out")+baseIndex)+" + "+stack+" / "+dangleL+" * "
        	+Utils.idToString(structPos.indexOf("both")+baseIndex)+" ) - "+nameout+" * ( "
            +Utils.idToString(structPos.indexOf("alone")+baseIndex)+" + "+Utils.idToString(structPos.indexOf("in")+baseIndex)
            +" ) ";
        String inhibeq = "";
        if (inhib!=null){
            inhibeq += " + "+nameinhib+" * "+Utils.idToString(structPos.indexOf("out")+baseIndex)+" - "+ratioright
            		+" * "+nameout+" * "+Utils.idToString(structPos.indexOf("inhib")+baseIndex);
        }
        return eq+inhibeq+" ) ";
    }
        
    public String getAloneTempEq(int baseIndex){
        String eq = kdup+" * ( "+dangleL+" * "+kin+" * "+Utils.idToString(structPos.indexOf("in")+baseIndex)+" + "+dangleR
        	+" * "+kout+" * "+Utils.idToString(structPos.indexOf("out")+baseIndex)+" - "
        	+Utils.idToString(structPos.indexOf("alone")+baseIndex)+" * ( "+namein+" + "+nameout+" ) "; 
        if (inhib != null) eq += " + "+alpha+" * "+kinhib+" * "+Utils.idToString(structPos.indexOf("inhib")+baseIndex)+" - "
        	                     +nameinhib+" * "+Utils.idToString(structPos.indexOf("alone")+baseIndex);
        eq += " )";
        if (graph.selfStart) eq += " - "+selfstart+" * "+Utils.idToString(structPos.indexOf("alone")+baseIndex);
        return eq;
    }
    
    public String getInTempEq(int baseIndex){
        String eq = kdup+" * ( "+namein+" * "+Utils.idToString(structPos.indexOf("alone")+baseIndex)+" + "+stack+" / "+dangleL
        	+" * "+kout+" * "+Utils.idToString(structPos.indexOf("both")+baseIndex)+" - "
        	+Utils.idToString(structPos.indexOf("in")+baseIndex)+" * ( "+dangleL+" * "+kin+" + "+nameout+" ) "; 
        if (inhib != null) eq += " + "+ratioleft+" * "+namein+" * "+Utils.idToString(structPos.indexOf("inhib")+baseIndex)
                                 +" - "+nameinhib+" * "+Utils.idToString(structPos.indexOf("in")+baseIndex);
        eq += " )"+" - "+pol+" * "+Utils.idToString(structPos.indexOf("in")+baseIndex);
        return eq;
    }
    
    public String getOutTempEq(int baseIndex){
        String eq = kdup+" * ( "+nameout+" * "+Utils.idToString(structPos.indexOf("alone")+baseIndex)+" + "+stack+" / "+dangleR
        	+" * "+kin+" * "+Utils.idToString(structPos.indexOf("both")+baseIndex)+" - "
        	+Utils.idToString(structPos.indexOf("out")+baseIndex)+" * ( "+dangleR+" * "+kout+" + "+namein+" ) "; 
        if (inhib != null) eq += " + "+ratioright+" * "+nameout+" * "+Utils.idToString(structPos.indexOf("inhib")+baseIndex)
                                 +" - "+nameinhib+" * "+Utils.idToString(structPos.indexOf("out")+baseIndex);
        eq += " )";
        if (graph.selfStart) eq += " + "+selfstart+" * "+Utils.idToString(structPos.indexOf("alone")+baseIndex);
        return eq;
    }
    
    public String getBothTempEq(int baseIndex){
        return kdup+" * ( "+namein+" * "+Utils.idToString(structPos.indexOf("out")+baseIndex)+" + "+nameout+" * "
               +Utils.idToString(structPos.indexOf("in")+baseIndex)+" - "+Utils.idToString(structPos.indexOf("both")+baseIndex)
               +" * ( "+stack+" / "+dangleR+" * "+kin+" + "+stack+" / "+dangleL+" * "+kout+" ) ) + "+nick+" * "
               +Utils.idToString(structPos.indexOf("ext")+baseIndex)+" - "+poldispl+" * "
               +Utils.idToString(structPos.indexOf("both")+baseIndex);
    }
    
    public String getExtTempEq(int baseIndex){
        return pol+" * "+Utils.idToString(structPos.indexOf("in")+baseIndex)+" + "+poldispl+" * "
               +Utils.idToString(structPos.indexOf("both")+baseIndex)+" - "+nick+" * "
        	   +Utils.idToString(structPos.indexOf("ext")+baseIndex);
    }
    
    public String getInhibTempEq(int baseIndex){
        if (inhib == null){
            return "";
        }
        return " - "+kdup+" * ("+alpha+" * "+kinhib+" * "+Utils.idToString(structPos.indexOf("inhib")+baseIndex)+" - "
               +nameinhib+" * ( "+Utils.idToString(structPos.indexOf("alone")+baseIndex)+" + "
               +Utils.idToString(structPos.indexOf("in")+baseIndex)+" + "+Utils.idToString(structPos.indexOf("out")+baseIndex)
               +" ) + "+Utils.idToString(structPos.indexOf("inhib")+baseIndex)+" * ( "+ratioleft+" * "+namein+" + "+ratioright
               +" * "+nameout+" ) )";
    }
    
    public String[] getEqs(int baseIndex){
    	if( inhib == null) {
    	return new String[] {getAloneTempEq(baseIndex), getInTempEq(baseIndex), getOutTempEq(baseIndex), getBothTempEq(baseIndex),
    			getExtTempEq(baseIndex)};
    	}
    	return new String[] {getAloneTempEq(baseIndex), getInTempEq(baseIndex), getOutTempEq(baseIndex), getBothTempEq(baseIndex),
    			getExtTempEq(baseIndex), getInhibTempEq(baseIndex)};
    }
	
	public static void main(String[] args){
		OligoGraph<SequenceVertex,String> g = Utils.initGraph();
		SequenceVertex s = g.getVertexFactory().create();
		g.addSpecies(s, 10.0, 5.0);
		String edge = g.getEdgeFactory().createEdge(s, s);
		g.addActivation(edge, s, s);
		int baseIndex = 1;
		DefaultTemplateEqWriter<String> t = new DefaultTemplateEqWriter<String>(g,edge);
		System.out.println("Test 1\n===========================");
		System.out.println(t.getInSequenceEq(baseIndex));
		System.out.println();
		System.out.println("Test 2\n===========================");
		String[] eqs = t.getEqs(baseIndex);
		for (int i = 0; i< eqs.length; i++){
			System.out.println("delta_"+Utils.idToString(baseIndex+i)+" = "+eqs[i]+";");
		}
		System.out.println();
		System.out.println("Test 3\n===========================");
		eqs = (new DefaultTemplateEqWriter<String>(g,edge,false)).getEqs(baseIndex);
		for (int i = 0; i< eqs.length; i++){
			System.out.println("delta_"+Utils.idToString(baseIndex+i)+" = "+eqs[i]+";");
		}
		System.out.println();
		System.out.println("Test 4\n===========================");
		eqs = (new DefaultTemplateEqWriter<String>(GraphMaker.makeOligator(),edge,false)).getEqs(baseIndex);
		for (int i = 0; i< eqs.length; i++){
			System.out.println("delta_"+Utils.idToString(baseIndex+i)+" = "+eqs[i]+";");
		}
	}

	@Override
	public SequenceVertex getInhib() {
		
		return inhib;
	}

	@Override
	public SequenceVertex getFrom() {
		
		return from;
	}

	@Override
	public SequenceVertex getTo() {
		
		return to;
	}
}
