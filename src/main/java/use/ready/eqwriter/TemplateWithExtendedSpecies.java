package use.ready.eqwriter;

import java.util.ArrayList;

import model.PseudoTemplateGraph;
import model.chemicals.SequenceVertex;
import use.ready.test.GraphMaker;
import utils.SequenceVertexComparator;

/**
 * This class can write equations for templates with potentially extended input, output or both.
 * Technically, it can also be used when none are present (in which case it SHOULD behave like its
 * super class).
 * 
 * Note that the extension introduces a structure asymmetry. An extended input loses its dangle stability
 * bonus and prevents stacking. On the other hand, an extended output conserves both, and should also
 * have an extra stability bonus from an additional dangle. (EXTRA DANGLE NOT TAKEN INTO ACCOUNT YET).
 * @author naubertkato
 *
 * @param <E>
 */
public class TemplateWithExtendedSpecies<E> extends TemplateWithShortInputDomainEqWriter<E> {
	
	SequenceVertex fromext;
	SequenceVertex toext;
	String nameinext = "" ;
	String nameoutext = "";

	public TemplateWithExtendedSpecies(PseudoTemplateGraph<SequenceVertex, E> g, E t) {
		this(g, t,true);
	}
	
	public TemplateWithExtendedSpecies(PseudoTemplateGraph<SequenceVertex, E> g, E t, boolean numerical) {
		super(g, t, numerical);
		fromext = g.getExtendedSpecies(from);
		toext = g.getExtendedSpecies(to);
		//If both are null, behaves like a template with short input domain
		
		if(fromext != null || toext != null){
		
			//here is a trick: all the ext species will come after the normal species
			//otherwise, both species have the same id, so we would wrongly get the same name
			//NOTE: inefficient computation...
			ArrayList<SequenceVertex> extSpecs = new ArrayList<SequenceVertex>(g.getAllExtendedSpecies());
			extSpecs.sort(new SequenceVertexComparator());
			if (fromext != null) nameinext = Utils.idToString(g.getVertexCount()+extSpecs.indexOf(fromext)+1)+" * "+missingBaseSlowdown+t.toString().replace("->", "to");
			if (toext != null) nameoutext = Utils.idToString(g.getVertexCount()+extSpecs.indexOf(toext)+1);
			
			//Edit the index of the different species
			if(fromext != null) structPos.add("inext");
	    	if(toext != null) structPos.add("outext");
	    	if(fromext != null) structPos.add("bothinext");
	    	if(toext != null) structPos.add("bothoutext");
	    	if(fromext != null && toext != null) structPos.add("bothbothext");
			
		}
	}
	
	//OK
	public String getInhibSequenceEq(int baseIndex){
		String eq = "";
		if (inhib != null){
			eq = kdup+" * ("+alpha+" * "+kinhib+" * "+Utils.idToString(structPos.indexOf("inhib")+baseIndex)+" - "+nameinhib+" * ( "
					+Utils.idToString(structPos.indexOf("alone")+baseIndex)+" + "+Utils.idToString(structPos.indexOf("in")+baseIndex)
					+" + "+Utils.idToString(structPos.indexOf("out")+baseIndex);
			
			if (fromext != null) eq += " + "+Utils.idToString(structPos.indexOf("inext")+baseIndex);
			
			if (toext != null) eq += " + "+Utils.idToString(structPos.indexOf("outext")+baseIndex);
			
			eq += " ) + "+Utils.idToString(structPos.indexOf("inhib")+baseIndex)+" * ( "+ratioleft+" * "+namein+" + "+ratioright+" * "+nameout;
			
			if (fromext != null) eq += " + "+ratioleft+" * "+nameinext;
			
			if (toext != null) eq += " + "+ratioright+" * "+nameoutext;
			
			eq += " ) )";
			
			return eq;
		}
		
		return "";
	}
	
	//OK
	public String getInSequenceEq(int baseIndex){
		String ret =  kdup+" * ( "+kin+" * ("+dangleL+" * "+Utils.idToString(structPos.indexOf("in")+baseIndex)+" + "
	        +stack+" / "+dangleR+" * "+Utils.idToString(structPos.indexOf("both")+baseIndex);
		if (toext != null) ret += " + "+stack+" / "+dangleR+" * "+Utils.idToString(structPos.indexOf("bothoutext")+baseIndex);
	    	        
	    ret += " ) - "+namein+" * ( "+Utils.idToString(structPos.indexOf("alone")+baseIndex)+" + "
	    	+Utils.idToString(structPos.indexOf("out")+baseIndex);
	    if (toext != null) ret += " + "+Utils.idToString(structPos.indexOf("outext")+baseIndex);
	    ret += " ) ";
		if (inhib != null){
			ret += " + "+nameinhib+" * "+Utils.idToString(structPos.indexOf("in")+baseIndex)+" - "+ratioleft+" * "
		        +namein+" * "+Utils.idToString(structPos.indexOf("inhib")+baseIndex);
		}
		return ret+")";
	    
	}
	
	//OK
	public String getInExtSequenceEq(int baseIndex){
		if (fromext == null) return "";
		
		String ret =  kdup+" * ( "+kin+" * ("+Utils.idToString(structPos.indexOf("inext")+baseIndex)+" + defaultStack / "
		    +dangleR+" * "+Utils.idToString(structPos.indexOf("bothinext")+baseIndex);
			if (toext != null) ret += " + defaultStack / "+dangleR+" * "+Utils.idToString(structPos.indexOf("bothbothext")+baseIndex);
		    	        
		    ret += " ) - "+nameinext+" * ( "+Utils.idToString(structPos.indexOf("alone")+baseIndex)+" + "
		    	+Utils.idToString(structPos.indexOf("out")+baseIndex);
		    if (toext != null) ret += " + "+Utils.idToString(structPos.indexOf("outext")+baseIndex);
		    ret += " ) ";
			if (inhib != null){
				ret += " + "+nameinhib+" * "+Utils.idToString(structPos.indexOf("inext")+baseIndex)+" - "+ratioleft+" * "
			        +nameinext+" * "+Utils.idToString(structPos.indexOf("inhib")+baseIndex);
			}
			return ret+")";
	    
	}
	
	//OK
	public String getOutSequenceEq(int baseIndex){
        String eq = pol+" * "+Utils.idToString(structPos.indexOf("both")+baseIndex)+" + "+kdup+" * ( "+kout
            +" * ("+dangleR+" * "+Utils.idToString(structPos.indexOf("out")+baseIndex)+" + "+stack+" / "+dangleL+" * "
        	+Utils.idToString(structPos.indexOf("both")+baseIndex);
        
        if (fromext != null) eq += " + "+Utils.idToString(structPos.indexOf("bothinext")+baseIndex);	
        
        eq +=" ) - "+nameout+" * ( "
            +Utils.idToString(structPos.indexOf("alone")+baseIndex)+" + "+Utils.idToString(structPos.indexOf("in")+baseIndex);
        
        if (fromext != null) eq += " + "+Utils.idToString(structPos.indexOf("inext")+baseIndex);
        
        eq +=" ) ";
        String inhibeq = "";
        if (inhib!=null){
            inhibeq += " + "+nameinhib+" * "+Utils.idToString(structPos.indexOf("out")+baseIndex)+" - "+ratioright
            		+" * "+nameout+" * "+Utils.idToString(structPos.indexOf("inhib")+baseIndex);
        }
        return eq+inhibeq+" ) ";
    }
	
	//OK
	public String getOutExtSequenceEq(int baseIndex){
		if (toext == null) return "";
		
		String eq = pol+" * "+Utils.idToString(structPos.indexOf("bothoutext")+baseIndex)+" + "+kdup+" * ( "+kout
	            +" * ("+dangleR+" * "+Utils.idToString(structPos.indexOf("outext")+baseIndex)+" + "+stack+" / "+dangleL+" * "
	        	+Utils.idToString(structPos.indexOf("bothoutext")+baseIndex);
	        
	        if (fromext != null) eq += " + "+Utils.idToString(structPos.indexOf("bothbothext")+baseIndex);	
	        
	        eq +=" ) - "+nameoutext+" * ( "
	            +Utils.idToString(structPos.indexOf("alone")+baseIndex)+" + "+Utils.idToString(structPos.indexOf("in")+baseIndex);
	        
	        if (fromext != null) eq += " + "+Utils.idToString(structPos.indexOf("inext")+baseIndex);
	        
	        eq +=" ) ";
	        String inhibeq = "";
	        if (inhib!=null){
	            inhibeq += " + "+nameinhib+" * "+Utils.idToString(structPos.indexOf("outext")+baseIndex)+" - "+ratioright
	            		+" * "+nameoutext+" * "+Utils.idToString(structPos.indexOf("inhib")+baseIndex);
	        }
	        return eq+inhibeq+" ) ";
    }
        
	//OK
    public String getAloneTempEq(int baseIndex){
        String eq = kdup+" * ( "+dangleL+" * "+kin+" * "+Utils.idToString(structPos.indexOf("in")+baseIndex)+" + "+dangleR
        	+" * "+kout+" * "+Utils.idToString(structPos.indexOf("out")+baseIndex);
        
        if (fromext != null) eq += " + "+kin+" * "+Utils.idToString(structPos.indexOf("inext")+baseIndex);
        
        if (toext != null) eq += " + "+dangleR+" * "+kout+" * "+Utils.idToString(structPos.indexOf("outext")+baseIndex);
        
        eq += " - "+Utils.idToString(structPos.indexOf("alone")+baseIndex)+" * ( "+namein+" + "+nameout;
        
        if (fromext != null) eq += " + "+nameinext;
        
        if (toext != null) eq += " + "+nameoutext;
        
        eq+=" ) "; 
        if (inhib != null) eq += " + "+alpha+" * "+kinhib+" * "+Utils.idToString(structPos.indexOf("inhib")+baseIndex)+" - "
        	                     +nameinhib+" * "+Utils.idToString(structPos.indexOf("alone")+baseIndex);
        eq += " )";
        if (graph.selfStart) eq += " - "+selfstart+" * "+Utils.idToString(structPos.indexOf("alone")+baseIndex);
        return eq;
    }
    
    //OK
    public String getInTempEq(int baseIndex){
        String eq = kdup+" * ( "+namein+" * "+Utils.idToString(structPos.indexOf("alone")+baseIndex)+" + "+stack+" / "+dangleL
        	+" * "+kout+" * "+Utils.idToString(structPos.indexOf("both")+baseIndex);
        
        if (toext != null) eq += " + "+stack+" / "+dangleL+" * "+kout+" * "
        		+Utils.idToString(structPos.indexOf("bothoutext")+baseIndex);
        
        eq+=" - "+Utils.idToString(structPos.indexOf("in")+baseIndex)+" * ( "+dangleL+" * "+kin+" + "+nameout;
        
        if (toext != null) eq += " + "+nameoutext;
        
        eq+=" ) "; 
        
        if (inhib != null) eq += " + "+ratioleft+" * "+namein+" * "+Utils.idToString(structPos.indexOf("inhib")+baseIndex)
                                 +" - "+nameinhib+" * "+Utils.idToString(structPos.indexOf("in")+baseIndex);
        eq += " )"+" - "+pol+" * "+Utils.idToString(structPos.indexOf("in")+baseIndex);
        return eq;
    }
    
    //OK
    public String getInExtTempEq(int baseIndex){
    	if (fromext == null) return ""; 
    	
    	String eq = kdup+" * ( "+nameinext+" * "+Utils.idToString(structPos.indexOf("alone")+baseIndex)+" + "+stack+" / "+dangleL
            	+" * "+kout+" * "+Utils.idToString(structPos.indexOf("bothinext")+baseIndex);
            
        if (toext != null) eq += " + "+stack+" / "+dangleL+" * "+kout+" * "
            +Utils.idToString(structPos.indexOf("bothbothext")+baseIndex);
            
        eq+=" - "+Utils.idToString(structPos.indexOf("inext")+baseIndex)+" * ( "+kin+" + "+nameout;
            
        if (toext != null) eq += " + "+nameoutext;
            
        eq+=" ) "; 
            
        if (inhib != null) eq += " + "+ratioleft+" * "+nameinext+" * "+Utils.idToString(structPos.indexOf("inhib")+baseIndex)
                               +" - "+nameinhib+" * "+Utils.idToString(structPos.indexOf("inext")+baseIndex);
        eq += " )";
        return eq;
    }
    
    //OK
    public String getOutTempEq(int baseIndex){
        String eq = kdup+" * ( "+nameout+" * "+Utils.idToString(structPos.indexOf("alone")+baseIndex)+" + "+stack+" / "+dangleR
        	+" * "+kin+" * "+Utils.idToString(structPos.indexOf("both")+baseIndex);
        	
        if (fromext != null) eq+=" + defaultStack / "
		    +dangleR+" * "+kin+" * "+Utils.idToString(structPos.indexOf("bothinext")+baseIndex);
        
        eq += " - "+Utils.idToString(structPos.indexOf("out")+baseIndex)+" * ( "+dangleR+" * "+kout+" + "+namein;
        
        if (fromext != null) eq+=" + "+nameinext;
        
        eq+=" ) "; 
        
        if (inhib != null) eq += " + "+ratioright+" * "+nameout+" * "+Utils.idToString(structPos.indexOf("inhib")+baseIndex)
                                 +" - "+nameinhib+" * "+Utils.idToString(structPos.indexOf("out")+baseIndex);
        eq += " )";
        if (graph.selfStart) eq += " + "+selfstart+" * "+Utils.idToString(structPos.indexOf("alone")+baseIndex);
        return eq;
    }
    
    //OK
    public String getOutExtTempEq(int baseIndex){
    	if (toext == null) return "";
    	
    	String eq = kdup+" * ( "+nameoutext+" * "+Utils.idToString(structPos.indexOf("alone")+baseIndex)+" + "+stack+" / "+dangleR
            	+" * "+kin+" * "+Utils.idToString(structPos.indexOf("bothoutext")+baseIndex);
            	
        if (fromext != null) eq+=" + defaultStack / "
		    +dangleR+" * "+kin+" * "+Utils.idToString(structPos.indexOf("bothbothext")+baseIndex);
            
        eq += " - "+Utils.idToString(structPos.indexOf("outext")+baseIndex)+" * ( "+dangleR+" * "+kout+" + "+namein;
            
        if (fromext != null) eq+=" + "+nameinext;
            
        eq+=" ) "; 
            
        if (inhib != null) eq += " + "+ratioright+" * "+nameoutext+" * "+Utils.idToString(structPos.indexOf("inhib")+baseIndex)
                                     +" - "+nameinhib+" * "+Utils.idToString(structPos.indexOf("outext")+baseIndex);
        eq += " )";
        return eq;
    }
    
    //OK (no change)
    public String getBothTempEq(int baseIndex){
    	String eq = kdup+" * ( "+namein+" * "+Utils.idToString(structPos.indexOf("out")+baseIndex)+" + "+nameout+" * "
                +Utils.idToString(structPos.indexOf("in")+baseIndex)+" - "+Utils.idToString(structPos.indexOf("both")+baseIndex)
                +" * ( "+stack+" / "+dangleR+" * "+kin+" + "+stack+" / "+dangleL+" * "+kout+" ) ) + "+nick+" * "
                +Utils.idToString(structPos.indexOf("ext")+baseIndex)+" - "+poldispl+" * "
                +Utils.idToString(structPos.indexOf("both")+baseIndex);
        return eq;
    }
    
    //OK
    public String getBothInExtTempEq(int baseIndex){
    	if (fromext == null) return "";
    	String eq = kdup+" * ( "+nameinext+" * "+Utils.idToString(structPos.indexOf("out")+baseIndex)+" + "+nameout+" * "
                +Utils.idToString(structPos.indexOf("inext")+baseIndex)+" - "+Utils.idToString(structPos.indexOf("bothinext")+baseIndex)
                +" * ( defaultStack / "+dangleR+" * "+kin+" + "+kout+" ) )";
        return eq;
    }
    
    //OK
    public String getBothOutExtTempEq(int baseIndex){
    	if (toext == null) return "";
        return kdup+" * ( "+namein+" * "+Utils.idToString(structPos.indexOf("outext")+baseIndex)+" + "+nameoutext+" * "
               +Utils.idToString(structPos.indexOf("in")+baseIndex)+" - "+Utils.idToString(structPos.indexOf("bothoutext")+baseIndex)
               +" * ( "+stack+" / "+dangleR+" * "+kin+" + "+stack+" / "+dangleL+" * "+kout+" ) )"+" - "+poldispl+" * "
               +Utils.idToString(structPos.indexOf("bothoutext")+baseIndex);
    }
    
    //OK
    public String getBothBothExtTempEq(int baseIndex){
    	if (fromext == null || toext == null) return "";
        return kdup+" * ( "+nameinext+" * "+Utils.idToString(structPos.indexOf("outext")+baseIndex)+" + "+nameoutext+" * "
               +Utils.idToString(structPos.indexOf("inext")+baseIndex)+" - "+Utils.idToString(structPos.indexOf("bothbothext")+baseIndex)
               +" * ( defaultStack / "+dangleR+" * "+kin+" + "+kout+" ) )";
    }
    
    //OK
    public String getExtTempEq(int baseIndex){
        String eq = pol+" * "+Utils.idToString(structPos.indexOf("in")+baseIndex)+" + "+poldispl+" * "
               +Utils.idToString(structPos.indexOf("both")+baseIndex);
        
        if (toext != null) eq+= " + "+poldispl+" * "+Utils.idToString(structPos.indexOf("bothoutext")+baseIndex);
        
        eq+=" - "+nick+" * "+Utils.idToString(structPos.indexOf("ext")+baseIndex);
        return eq;
    }
    
    public String getInhibTempEq(int baseIndex){
        if (inhib == null){
            return "";
        }
        String eq =  " - "+kdup+" * ("+alpha+" * "+kinhib+" * "+Utils.idToString(structPos.indexOf("inhib")+baseIndex)+" - "
               +nameinhib+" * ( "+Utils.idToString(structPos.indexOf("alone")+baseIndex)+" + "
               +Utils.idToString(structPos.indexOf("in")+baseIndex)+" + "+Utils.idToString(structPos.indexOf("out")+baseIndex);
        
        if (fromext != null) eq += " + "+Utils.idToString(structPos.indexOf("inext")+baseIndex);
        if (toext != null) eq += " + "+Utils.idToString(structPos.indexOf("outext")+baseIndex);
        
        eq += " ) + "+Utils.idToString(structPos.indexOf("inhib")+baseIndex)+" * ( "+ratioleft+" * "+namein+" + "+ratioright
               +" * "+nameout;
        
        if (fromext != null) eq += " + "+ratioleft+" * "+nameinext;
        if (toext != null) eq += " + "+ratioright+" * "+nameoutext;
        
        eq += " ) )";
        return eq;
    }
    
    public String[] getEqs(int baseIndex){
    	ArrayList<String> eqs = new ArrayList<String>();
    	eqs.add(getAloneTempEq(baseIndex));
    	eqs.add(getInTempEq(baseIndex));
    	eqs.add(getOutTempEq(baseIndex));
    	eqs.add(getBothTempEq(baseIndex));
    	eqs.add(getExtTempEq(baseIndex));
    	if(inhib != null) eqs.add(getInhibTempEq(baseIndex));
    	if(fromext != null) eqs.add(getInExtTempEq(baseIndex));
    	if(toext != null) eqs.add(getOutExtTempEq(baseIndex));
    	if(fromext != null) eqs.add(getBothInExtTempEq(baseIndex));
    	if(toext != null) eqs.add(getBothOutExtTempEq(baseIndex));
    	if(fromext != null && toext != null) eqs.add(getBothBothExtTempEq(baseIndex));
    	return eqs.toArray(new String[eqs.size()]);
    }
    
    public static void main(String[] args){
		PseudoTemplateGraph<SequenceVertex,String> g = GraphMaker.makeAutocatalystWithPT();
		SequenceVertex s = g.getVertices().iterator().next();
		TemplateWithExtendedSpecies<String> ptew = new TemplateWithExtendedSpecies<String>(g, g.getEdgeFactory().createEdge(s, s));
		int tempIndex = g.getVertexCount()+g.getAllExtendedSpecies().size();
		System.out.println(ptew.getInExtSequenceEq(tempIndex));
		System.out.println(ptew.getOutExtSequenceEq(tempIndex));
		Utils.testTemplateEqWriter(ptew, tempIndex);
		
		//Adding a fake inhibitor
		ptew.inhib = new SequenceVertex(3);
		ptew.nameinhib = "c";
		ptew.structPos.add(5, "inhib");
		
		tempIndex ++;
		
		System.out.println("========================================================");
		System.out.println(ptew.getInExtSequenceEq(tempIndex));
		System.out.println(ptew.getOutExtSequenceEq(tempIndex));
		Utils.testTemplateEqWriter(ptew, tempIndex);
	}
}
