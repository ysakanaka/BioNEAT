package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class OligoSystemGeneral implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int nSimpleSequences;
	public double[][][] templates;
	public double[][] seqK;
	public double[][] inhK;
	public double[][] seqConcentration;
	public boolean[] sequenceProtected;
	public Enzyme exo;
	public Enzyme nick;
	public Enzyme poly;
	public double[] reporterConcentration;
	public String[] seqString;

	// /////DYNAMIC PRINTING//////////
	public double[][] TimeSerie;
	public int t = 0;
	public int t2 = 0;
	public float Fmax;
	// ///////////////////////////////

	public Map<Integer, String> intToName;
	public Map<String, Integer> nameToInt;

	public OligoSystemGeneral(int nSimpleSequences, double[][][] templates,
			double[][] seqK, double[][] inhK, double[][] seqConcentration,
			double exo, Map<Integer, String> intToName,
			Map<String, Integer> nameToInt) {
		this.nSimpleSequences = nSimpleSequences;
		this.templates = templates;
		this.seqK = seqK;
		this.inhK = inhK;
		this.seqConcentration = seqConcentration;
		this.exo = new Enzyme("exo", exo, model.Constants.exoKmSimple,
				model.Constants.exoKmInhib);
		this.nick = new Enzyme("nick", model.Constants.nickVm
				/ Constants.nickKm, Constants.nickKm);
		this.poly = new Enzyme("poly", Constants.polVm / Constants.polKm,
				Constants.polKm, Constants.polKmBoth);
		this.nameToInt = nameToInt;
		this.intToName = intToName;
	}

	public OligoSystemGeneral(int nSimpleSequences, double[][][] templates,
			double[][] seqK, double[][] inhK, double[][] seqConcentration,
			Map<Integer, String> intToName, Map<String, Integer> nameToInt) {
		this.nSimpleSequences = nSimpleSequences;
		this.templates = templates;
		this.seqK = seqK;
		this.inhK = inhK;
		this.seqConcentration = seqConcentration;
		this.exo = new Enzyme("exo", Constants.exoVm / Constants.exoKmSimple,
				model.Constants.exoKmSimple, model.Constants.exoKmInhib);
		this.nick = new Enzyme("nick", model.Constants.nickVm
				/ Constants.nickKm, Constants.nickKm);
		this.poly = new Enzyme("poly", Constants.polVm / Constants.polKm,
				Constants.polKm, Constants.polKmBoth);
		this.nameToInt = nameToInt;
		this.intToName = intToName;
	}

	public OligoSystemGeneral(int nSimpleSequences, double[][][] templates,
			double[][] seqK, double[][] inhK, double[][] seqConcentration,
			Enzyme exo, Enzyme nick, Enzyme poly,
			Map<Integer, String> intToName, Map<String, Integer> nameToInt,
			String[] seqStr) {
		this.nSimpleSequences = nSimpleSequences;
		this.templates = templates;
		this.seqK = seqK;
		this.inhK = inhK;
		this.seqConcentration = seqConcentration;
		this.exo = exo;
		this.nick = nick;
		this.poly = poly;
		this.nameToInt = nameToInt;
		this.intToName = intToName;
		this.seqString = seqStr;
	}

	public OligoSystemGeneral(int nSimpleSequences, double[][][] templates,
			double[][] seqK, double[][] inhK, double[][] seqConcentration,
			Enzyme exo, Enzyme nick, Enzyme poly,
			Map<Integer, String> intToName, Map<String, Integer> nameToInt,
			boolean[] sequenceProtected, double[] reporterConcentration,
			String[] seqString) {
		this(nSimpleSequences, templates, seqK, inhK, seqConcentration, exo,
				nick, poly, intToName, nameToInt, seqString);
		this.sequenceProtected = sequenceProtected;
		this.reporterConcentration = reporterConcentration;
	}

	public ArrayList<Double> getSeqK() {
		ArrayList<Double> result = new ArrayList<Double>();
		for (int i = 0; i < nSimpleSequences + 1; i++) {
			for (int j = 0; j < nSimpleSequences; j++) {
				if (seqK[i][j] != 0) {
					result.add(seqK[i][j]);
				}
			}
		}
		return result;
	}

	public OligoSystemComplex getOligoSystemComples() {
		return new OligoSystemComplex(nSimpleSequences, templates, seqK, inhK,
				seqConcentration, exo, poly, nick, sequenceProtected,
				reporterConcentration, seqString);
	}

	public String getName(int i) {
		return intToName.get(i);
	}

	public int getId(String name) {
		return nameToInt.get(name);
	}

}
