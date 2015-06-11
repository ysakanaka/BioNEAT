package use.oligomodel;

import model.OligoSystem;
import model.SaturationEvaluator;
import model.SaturationEvaluator.ENZYME;
import model.chemicals.SequenceVertex;
import model.chemicals.Template;

public class SaturationEvaluatorProtected<E> extends SaturationEvaluator<E> {

	public SaturationEvaluatorProtected(double[] polKms, double[] nickKms,
			double[] exoKms) {
		super(polKms, nickKms, exoKms);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public double getCurrentEnzymeSaturation(ENZYME type, OligoSystem<E> os){
		double value = 1.0; //no sat
		double[] kms = enzymeKms[type.value];
		if(kms[TALONE]> 0.0 ||kms[TIN]> 0.0 ||kms[TOUT]> 0.0 ||kms[TBOTH]> 0.0 ||kms[TEXT]> 0.0 ||kms[TINHIB]> 0.0 ){
			for(Template<E> t : os.templates.values()){ //we take all the current templates
				if (kms[TALONE] > 0.0){
					if(Reporter.class.isAssignableFrom(t.getClass()) && kms[SIGNAL] >0.0){
						value += t.concentrationAlone/kms[SIGNAL];
					} else {
						value += t.concentrationAlone/kms[TALONE];
					}
				}
				if (kms[TIN] > 0.0 && !Reporter.class.isAssignableFrom(t.getClass())){
					value += t.concentrationWithInput/kms[TIN];
					if(TemplateWithProtected.class.isAssignableFrom(t.getClass())) value+= ((TemplateWithProtected<E>)t).concentrationWithProtecteInput/kms[TIN];
				}
				if (kms[TOUT] > 0.0){
					value += t.concentrationWithOutput/kms[TOUT];
					if(TemplateWithProtected.class.isAssignableFrom(t.getClass())) value+= ((TemplateWithProtected<E>)t).concentrationWithProtectedOutput/kms[TOUT];
				}
				if (kms[TBOTH] > 0.0){
					value += t.concentrationWithBoth/kms[TBOTH];
					if(TemplateWithProtected.class.isAssignableFrom(t.getClass())) {
						value+= ((TemplateWithProtected<E>)t).concentrationWithBothProtectedInput/kms[TBOTH];
						value+= ((TemplateWithProtected<E>)t).concentrationWithBothProtectedOutput/kms[TBOTH];
						value+= ((TemplateWithProtected<E>)t).concentrationWithBothProtectedBoth/kms[TBOTH];
					}
				}	
				if (kms[TEXT] > 0.0){
					value += t.concentrationExtended/kms[TEXT];
					if(TemplateWithProtected.class.isAssignableFrom(t.getClass())) value+= ((TemplateWithProtected<E>)t).concentrationExtendedProtectedInput/kms[TEXT];
				}
				if (kms[TINHIB] > 0.0){
					value += t.concentrationInhibited/kms[TINHIB];
				}
			}
		}
		if(kms[SIGNAL] > 0.0 || kms[INHIB] > 0.0){
			for(SequenceVertex s : os.getSequences()){
				if(!s.isInhib() && kms[SIGNAL] >0.0){
					value+= s.getConcentration()/kms[SIGNAL];
					if(ProtectedSequenceVertex.class.isAssignableFrom(s.getClass())) value+= ((ProtectedSequenceVertex) s).getProtectedConcentration()/kms[SIGNAL];
				} else if (s.isInhib() && kms[INHIB] >0.0){
					value+= s.getConcentration()/kms[INHIB];
					if(ProtectedSequenceVertex.class.isAssignableFrom(s.getClass())) value+= ((ProtectedSequenceVertex) s).getProtectedConcentration()/kms[INHIB];
				}
			}
		}
		return value;
	}

}
