package model;

import java.util.ArrayList;
import java.io.Serializable;
import java.text.DecimalFormat;

import model.util.CalcErrEventHandler;
import model.util.MyEventHandler;
import model.util.MyStepHandler;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegrator;
import org.apache.commons.math3.ode.sampling.StepHandler;

public class OligoSystemComplex extends OligoSystem implements Serializable,
		Cloneable, FirstOrderDifferentialEquations {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3626332588448633729L;

	// public ArrayList<ArrayList<Sequence>> sequences;
	// public ArrayList<ArrayList<ArrayList<Template>>> templates;
	public final ArrayList<Template> flatTemplates;

	public final ArrayList<Reporter> reporters = new ArrayList<Reporter>();

	public boolean saturableExonuclease = Constants.saturableExonuclease;
	public boolean saturablePoly = Constants.saturablePoly;
	public boolean saturableNick = Constants.saturableNick;
	public boolean exoSaturationByFreeTemplates = Constants.exoSaturationByFreeTemplates;
	public boolean coupledExonuclease = Constants.coupledExonuclease; // this is
																		// only
																		// relevant
																		// if
																		// saturated
	public boolean coupledPoly = Constants.coupledPoly;
	public boolean coupledNick = Constants.coupledNick;
	private double observedExoKm = 0;

	public int total = 0;
	public int inhTotal = 0;

	public Input[] inputs;

	int nbOfProtec;

	public void setInput(Input[] inputs) {
		this.inputs = inputs;
	}

	public OligoSystemComplex(int noOfSimpleSeq, double[][][] template,
			double[][] seqK, double[][] inhK, double[][] seqConcentration,
			Enzyme exo, Enzyme poly, Enzyme nick, boolean[] sequenceProtected,
			double[] reporterConcentration, String[] seqString) {
		this(noOfSimpleSeq, template, seqK, inhK, seqConcentration,
				sequenceProtected, reporterConcentration, seqString);
		this.exo = (exo.activity > 0.001) ? exo : new Enzyme("exo",
				Constants.exoVm / Constants.exoKmSimple, Constants.exoKmSimple,
				Constants.exoKmInhib);
		this.poly = poly;
		this.nick = nick;
		for (int i = 0; i < this.flatTemplates.size(); i++) {
			this.flatTemplates.get(i).setAllocatedPoly(this.poly);
			this.flatTemplates.get(i).setAllocatedNick(this.nick);
		}
	}

	public OligoSystemComplex(int noOfSimpleSeq, double[][][] template,
			double[][] seqK, double[][] inhK, double[][] seqConcentration,
			boolean[] sequenceProtected, double[] reporterConcentration,
			String[] seqString) {

		try {
			this.template = template;
			// First initialize sequence
			// List of all sequences (simple + inhibiting )
			this.sequences = new ArrayList<ArrayList<Sequence>>(
					noOfSimpleSeq + 1);
			// First row for simple sequences. So initialize the row for simple
			// sequences
			this.sequences.add(new ArrayList<Sequence>(noOfSimpleSeq));
			// initialize all simple sequences
			for (int i = 0; i < noOfSimpleSeq; ++i) {
				SimpleSequence sequence = null;
				if (sequenceProtected != null) {
					sequence = new SimpleSequence(i, seqConcentration[0][i],
							seqK[0][i], sequenceProtected[i]);
				} else {
					sequence = new SimpleSequence(i, seqConcentration[0][i],
							seqK[0][i]);
				}
				if (seqString != null) {
					sequence.setSeqString(seqString[i]);
				}
				this.sequences.get(0).add(sequence);
				total++;
			}

			// inhibiting sequences
			for (int i = 1; i <= noOfSimpleSeq; ++i) {
				this.sequences.add(new ArrayList<Sequence>(noOfSimpleSeq));
				for (int j = 0; j < noOfSimpleSeq; ++j) {
					if (seqK[i][j] == 0) { // A zero in K means that there is no
						// inhibiting sequence for that
						// position
						this.sequences.get(i).add(null);
					} else {
						this.sequences.get(i).add(
								new InhibitingSequence(
										seqConcentration[i][j], // From Nat: A
										// posteriori
										// InhibSeqs
										// don't need
										// IDs
										seqK[i][j], inhK[i][j],
										(SimpleSequence) this.sequences.get(0)
												.get(i - 1),
										(SimpleSequence) this.sequences.get(0)
												.get(j)));
						inhTotal++;
					}
				}
			}
			// All the sequences are done.

			// Now initialize Templates
			// Create Templates
			this.templates = new ArrayList<ArrayList<ArrayList<Template>>>(
					noOfSimpleSeq + 1);

			// Initialize Templates for Activation
			this.templates
					.add(new ArrayList<ArrayList<Template>>(noOfSimpleSeq));
			for (int i = 0; i < noOfSimpleSeq; ++i) {
				this.templates.get(0).add(
						new ArrayList<Template>(noOfSimpleSeq));
				for (int j = 0; j < noOfSimpleSeq; ++j) {
					if (template[0][i][j] != 0) {
						this.templates
								.get(0)
								.get(i)
								.add(new Template(template[0][i][j],
										(SimpleSequence) this.sequences.get(0)
												.get(i), this.sequences.get(0)
												.get(j),
										(InhibitingSequence) this.sequences
												.get(i + 1).get(j)));
					} else {
						this.templates.get(0).get(i).add(null);
					}
				}
			}

			// Initialize Templates for inhibition
			for (int i = 1; i <= noOfSimpleSeq; ++i) {
				this.templates.add(new ArrayList<ArrayList<Template>>(
						noOfSimpleSeq));
				for (int j = 0; j < noOfSimpleSeq; ++j) {
					this.templates.get(i).add(
							new ArrayList<Template>(noOfSimpleSeq));
					for (int k = 0; k < noOfSimpleSeq; ++k) {
						if (template[i][j][k] != 0) {
							this.templates
									.get(i)
									.get(j)
									.add(new Template(template[i][j][k],
											(SimpleSequence) this.sequences
													.get(0).get(i - 1),
											this.sequences.get(j + 1).get(k),
											null));
						} else {
							this.templates.get(i).get(j).add(null);
						}

					}
				}
			}

			this.exo = new Enzyme("exo", Constants.exoVm
					/ Constants.exoKmSimple, Constants.exoKmSimple,
					Constants.exoKmInhib);
			this.poly = new Enzyme("poly", Constants.polVm / Constants.polKm,
					Constants.polKm, Constants.polKmBoth);
			this.nick = new Enzyme("nick", Constants.nickVm / Constants.nickKm,
					Constants.nickKm);

		} catch (Exception e) {
			e.printStackTrace();
		}

		this.flatTemplates = flattenTemplates(templates);

		if (reporterConcentration != null) {
			for (int i = 0; i < reporterConcentration.length; i++) {
				if (reporterConcentration[i] > 0) {
					addReporter((SimpleSequence) this.sequences.get(0).get(i),
							reporterConcentration[i]);
				}
			}
		}
		this.nbOfProtec = 0;
		for (int i = 0; i < this.total; i++) {
			if (this.sequences.get(0).get(i).isProtected()) {
				nbOfProtec++;
			}
		}
	}

	public void setTotals(int total, int inhTotal) {
		this.total = total;
		this.inhTotal = inhTotal;
	}

	private ArrayList<Template> flattenTemplates(
			ArrayList<ArrayList<ArrayList<Template>>> templates) {
		ArrayList<Template> temps = new ArrayList<Template>();
		for (int i = 0; i < total + 1; i++) {
			for (int j = 0; j < total; j++) {
				for (int k = 0; k < total; k++) {
					try {
						if (templates.get(i).get(j).get(k) != null) {

							temps.add(templates.get(i).get(j).get(k));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return temps;
	}

	public double getTotalCurrentFlux(Sequence s) {
		// System.out.println("Current concentretation for "+s+": "+s.getConcentration());
		if (s.getClass() == SimpleSequence.class) {
			return getTotalCurrentFlux((SimpleSequence) s);
		} else {
			return getTotalCurrentFlux((InhibitingSequence) s);
		}
	}

	private double getTotalCurrentFlux(SimpleSequence s) {
		// As Input
		double flux = 0;
		Template temp;
		for (int j = 0; j < total; j++) {
			temp = templates.get(0).get(s.ID).get(j);
			if (temp != null) {
				flux += temp.inputSequenceFlux();
				// System.out.println("SimpleSequence"+s.ID+
				// " partial flux:"+temp.inputSequenceFlux()+" and flux "+flux);
			}
		}
		for (int i = 0; i < total; i++) {
			for (int j = 0; j < total; j++) {
				temp = templates.get(s.ID + 1).get(i).get(j);
				if (temp != null) {
					flux += temp.inputSequenceFlux();
					// System.out.println("SimpleSequence"+s.ID+
					// " partial flux:"+temp.inputSequenceFlux()+" for inhib "+i+"->"+j);
				}
			}
		}
		// As Output
		for (ArrayList<Template> list : templates.get(0)) {
			temp = list.get(s.ID);
			if (temp != null) {
				flux += temp.outputSequenceFlux();
				// System.out.println("SimpleSequence"+s.ID+" partial output flux:"+temp.outputSequenceFlux()+" from "+temp.getFrom().ID);
			}
		}
		// Interaction with reporter
		for (Reporter rep : reporters) {
			if (rep.getFrom() == s) {
				flux += rep.inputSequenceFlux();
			}
		}
		if (!this.saturableExonuclease) {

			flux -= s.getConcentration() * this.exo.activity;
		} else {
			if (this.coupledExonuclease) {
				flux -= s.getConcentration()
						* this.exo.activity
						* (this.exo.basicKm / (this.exo.basicKm * this.observedExoKm));
			} else {
				flux -= s.getConcentration() * this.exo.activity
						* this.exo.basicKm
						/ (this.exo.basicKm + s.getConcentration());
			}
			// Sequence seq;
			// double obsExoKm, value = 0;
			// for (int i=0; i <= total; i++ ) {
			// for(int j=0; j<total; j++){
			// seq = sequences.get(i).get(j);
			// if(seq != null)
			// value += seq.getConcentration()
			// /
			// (seq.getClass()==InhibitingSequence.class?Constants.exoKmInhib:Constants.exoKmSimple);
			// }
			// }
			// obsExoKm = 1 + value;
			// flux -= s.getConcentration() *Constants.exoVm
			// / (Constants.exoKmSimple * obsExoKm);
		}
		return flux;
	}

	private double getTotalCurrentProtectedFlux(SimpleSequence s) {
		if (s.protectedSequence) {
			// As Input
			double flux = 0;
			Template temp;
			for (int j = 0; j < this.total; j++) {
				temp = templates.get(0).get(s.ID).get(j);
				if (temp != null) {
					flux += temp.inputProtectedSequenceFlux();
					// System.out.println("SimpleSequence"+s.ID+
					// " partial flux:"+temp.inputSequenceFlux()+" and flux "+flux);
				}
			}
			for (int i = 0; i < this.total; i++) {
				for (int j = 0; j < this.total; j++) {
					temp = templates.get(s.ID + 1).get(i).get(j);
					if (temp != null) {
						flux += temp.inputProtectedSequenceFlux();
						// System.out.println("SimpleSequence"+s.ID+
						// " partial flux:"+temp.inputSequenceFlux()+" for inhib "+i+"->"+j);
					}
				}
			}
			// As Output
			for (ArrayList<Template> list : templates.get(0)) {
				temp = list.get(s.ID);
				if (temp != null) {
					flux += temp.outputProtectedSequenceFlux();
					// System.out.println("SimpleSequence"+s.ID+" partial output flux:"+temp.outputSequenceFlux()+" from "+temp.getFrom().ID);
				}
			}
			return flux;
		}
		return 0;
	}

	private double getTotalCurrentFlux(InhibitingSequence s) {
		double flux = 0;
		Template temp;
		// As Inhib
		temp = templates.get(0).get(s.getFrom().getID()).get(s.getTo().getID());
		if (temp != null) {
			flux += temp.inhibSequenceFlux();
			// System.out.println("InhibitingSequence "+s.ID+" partial flux = "+temp.inhibSequenceFlux());
		}
		// As Output
		for (int i = 0; i < total; i++) {
			if (templates.get(i + 1).get(s.getFrom().getID())
					.get(s.getTo().getID()) != null) {
				flux += templates.get(i + 1).get(s.getFrom().getID())
						.get(s.getTo().getID()).outputSequenceFlux();
				// System.out.println(s+" partial output flux = "+templates.get(i+1).get(s.getFrom().getID()).get(s.getTo().getID()).outputSequenceFlux()+" from "+i);
			}
		}
		if (!this.saturableExonuclease) {

			flux -= s.getConcentration() * this.exo.activity
					* (this.exo.basicKm / this.exo.optionalValue);

		} else {
			if (this.coupledExonuclease) {
				flux -= s.getConcentration()
						* this.exo.activity
						* (this.exo.basicKm / (this.exo.optionalValue * this.observedExoKm));
			} else {
				flux -= s.getConcentration() * this.exo.activity
						* this.exo.basicKm
						/ (this.exo.optionalValue + s.getConcentration());
			}
			// Sequence seq;
			// double obsExoKm, value = 0;
			// for (int i=0; i <= total; i++ ) {
			// for(int j=0; j<total; j++){
			// seq = sequences.get(i).get(j);
			// if(seq != null)
			// value += seq.getConcentration()
			// /
			// (seq.getClass()==InhibitingSequence.class?Constants.exoKmInhib:Constants.exoKmSimple);
			// }
			// }
			// obsExoKm = 1 + value;
			// System.out.println(""+this.observedExoKm+" and "+obsExoKm);
			// flux -= s.getConcentration() *Constants.exoVm
			// / (Constants.exoKmInhib * obsExoKm);
		}

		return flux;
	}

	private void setObservedExoKm() {
		double value = 0;
		for (int i = 0; i < this.total; i++) {
			value += this.sequences.get(0).get(i).getExoSaturation();
		}
		for (int i = 0; i < this.total; i++) {
			for (int j = 0; j < this.total; j++) {
				Sequence seq = this.sequences.get(i + 1).get(j);
				if (seq != null) {
					value += seq.getExoSaturation();
				}
			}
		}
		if (exoSaturationByFreeTemplates) {
			for (Template t : this.flatTemplates) {
				value += t.getExoSaturation();
			}
		}
		this.observedExoKm = 1 + value;
	}

	private void setObservedPolyKm() {
		double value = 1;
		Enzyme perceivedPoly;
		if (this.coupledPoly) {
			for (int i = 0; i < this.flatTemplates.size(); i++) {
				value += this.flatTemplates.get(i).getPolyUsage();
			}
			perceivedPoly = new Enzyme("poly", this.poly.activity / value,
					this.poly.basicKm, this.poly.optionalValue);
			for (int i = 0; i < this.flatTemplates.size(); i++) {
				this.flatTemplates.get(i).setAllocatedPoly(perceivedPoly);
			}
		} else {
			for (int i = 0; i < this.flatTemplates.size(); i++) {
				value = this.poly.basicKm
						+ this.flatTemplates.get(i).getPolyUsage();
				perceivedPoly = new Enzyme("poly", this.poly.activity
						* this.poly.basicKm / value, this.poly.basicKm,
						this.poly.optionalValue);
				this.flatTemplates.get(i).setAllocatedPoly(perceivedPoly);
			}
		}
	}

	private void setObservedNickKm() {
		double value = 1;
		Enzyme perceivedNick;
		if (this.coupledNick) {
			for (int i = 0; i < this.flatTemplates.size(); i++) {
				value += this.flatTemplates.get(i).getNickUsage();
			}
			perceivedNick = new Enzyme("nick", this.nick.activity / value,
					this.nick.basicKm);
			for (int i = 0; i < this.flatTemplates.size(); i++) {
				this.flatTemplates.get(i).setAllocatedNick(perceivedNick);
			}
		} else {
			for (int i = 0; i < this.flatTemplates.size(); i++) {
				value = this.nick.basicKm
						+ this.flatTemplates.get(i).getNickUsage();
				perceivedNick = new Enzyme("nick", this.nick.activity
						* this.nick.basicKm / value, this.nick.basicKm,
						this.nick.optionalValue);
				this.flatTemplates.get(i).setAllocatedNick(perceivedNick);
			}
		}
	}

	private double[] getCurrentConcentration() {
		double concentration[] = new double[total * (1 + total)];
		for (int i = 0; i < total + 1; ++i) {
			for (int j = 0; j < total; ++j) {
				if (this.sequences.get(i).get(j) != null) {
					concentration[total * (i) + j] = this.sequences.get(i)
							.get(j).getConcentration();
				} else {
					concentration[total * (i) + j] = 0.0;
				}
			}
		}
		return (concentration);
	}

	public void reinitializeOiligoSystem() {
		int noOfSimpleSeq = this.sequences.get(0).size();
		for (int i = 0; i <= noOfSimpleSeq; ++i) {
			for (int j = 0; j < noOfSimpleSeq; ++j) {
				if (this.sequences.get(i).get(j) != null) {
					this.sequences.get(i).get(j).resetConcentration();
				}
			}
		}
		for (int i = 0; i < noOfSimpleSeq; ++i) {
			for (int j = 0; j < noOfSimpleSeq; ++j) {
				if (this.templates.get(0).get(i).get(j) != null) {
					this.templates
							.get(0)
							.get(i)
							.get(j)
							.reset(template[0][i][j],
									(SimpleSequence) this.sequences.get(0).get(
											i),
									this.sequences.get(0).get(j),
									(InhibitingSequence) this.sequences.get(
											i + 1).get(j));
					this.templates.get(0).get(i).get(j).setAllocatedNick(nick);
					this.templates.get(0).get(i).get(j).setAllocatedPoly(poly);
				}
			}
		}
		for (int i = 1; i <= noOfSimpleSeq; ++i) {
			for (int j = 0; j < noOfSimpleSeq; ++j) {
				for (int k = 0; k < noOfSimpleSeq; ++k) {
					if (this.templates.get(i).get(j).get(k) != null) {
						this.templates
								.get(i)
								.get(j)
								.get(k)
								.reset(template[i][j][k],
										(SimpleSequence) this.sequences.get(0)
												.get(i - 1),
										this.sequences.get(j + 1).get(k), null);
						this.templates.get(i).get(j).get(k)
								.setAllocatedNick(nick);
						this.templates.get(i).get(j).get(k)
								.setAllocatedPoly(poly);
					}
				}
			}
		}
	}

	/******************************
	 * Print the current model in console
	 * 
	 * @param noOfSimpleSeq
	 * @param template
	 * @param sequence
	 */
	public String printModel(int noOfSimpSeq) {

		int noOfSimpleSeq = noOfSimpSeq;

		System.out.print("\n\nSimple Sequences: \n");
		for (int i = 0; i < noOfSimpleSeq; ++i) {
			System.out.print("C:"
					+ this.sequences.get(0).get(i).getInitialConcentration()
					+ " K:" + this.sequences.get(0).get(i).getK() + ", ");
		}

		System.out.print("\nInhibiting Sequences: \n");
		for (int i = 1; i <= noOfSimpleSeq; ++i) {
			for (int j = 0; j < noOfSimpleSeq; ++j) {
				// System.out.print(this.Sequence[i][j] + " ");
				if (this.sequences.get(i).get(j) != null) {
					System.out.print("C:"
							+ this.sequences.get(i).get(j)
									.getInitialConcentration() + " K:"
							+ this.sequences.get(i).get(j).getK() + ", ");
				} else {
					System.out.print("false, ");
				}
			}
			System.out.print("\n");
		}

		System.out.print("\nActivation: \n");
		for (int i = 0; i < noOfSimpleSeq; ++i) {
			for (int j = 0; j < noOfSimpleSeq; ++j) {
				if (this.templates.get(0).get(i).get(j) != null)
					System.out.print("C:"
							+ this.templates.get(0).get(i).get(j)
									.totalConcentration() + ", ");
				else
					System.out.print("false, ");
			}
			System.out.print("\n");
		}

		System.out.print("\nInhibition Pattern: \n");
		for (int i = 1; i <= noOfSimpleSeq; ++i) {
			System.out.print("\nSeq: " + i + " \n");
			for (int j = 0; j < noOfSimpleSeq; ++j) {
				for (int k = 0; k < noOfSimpleSeq; ++k) {
					if (this.templates.get(i).get(j).get(k) != null)
						System.out.print(" C:"
								+ this.templates.get(i).get(j).get(k)
										.totalConcentration() + ", ");
					else
						System.out.print("false, ");
				}
				System.out.print("\n");
			}
		}
		System.out.print("\nExo: " + this.exo + "\n");
		return "";
	}

	/**********************************************************
	 * Prints the model in the form of String that to be displayed in the
	 * display panel with network and time course
	 * 
	 * @param noOfSimpSeq
	 * @return
	 */

	public String printModeltoString(int noOfSimpSeq) {

		int noOfSimpleSeq = noOfSimpSeq;
		DecimalFormat df = new DecimalFormat("00E00");

		String text = "\n\nSimple Sequences: \n";

		for (int i = 0; i < noOfSimpleSeq; ++i) {
			text = text.concat("C:"
					+ df.format(this.sequences.get(0).get(i)
							.getInitialConcentration()) + " K:"
					+ df.format(this.sequences.get(0).get(i).getK()) + ", ");
		}

		text = text.concat("\nInhibiting Sequences: \n");
		for (int i = 1; i <= noOfSimpleSeq; ++i) {
			for (int j = 0; j < noOfSimpleSeq; ++j) {
				// System.out.print(this.Sequence[i][j] + " ");
				if (this.sequences.get(i).get(j) != null) {
					text = text.concat("C:"
							+ df.format(this.sequences.get(i).get(j)
									.getInitialConcentration()) + " K:"
							+ df.format(this.sequences.get(i).get(j).getK())
							+ ", ");
				} else {
					text = text.concat("false, ");
				}
			}
			text = text.concat("\n");
		}

		text = text.concat("\nActivation: \n");
		for (int i = 0; i < noOfSimpleSeq; ++i) {
			for (int j = 0; j < noOfSimpleSeq; ++j) {
				if (this.templates.get(0).get(i).get(j) != null)
					text = text.concat("C:"
							+ df.format(this.templates.get(0).get(i).get(j)
									.totalConcentration()) + ", ");
				else
					text = text.concat("false, ");
			}
			text = text.concat("\n");
		}

		text = text.concat("\nInhibitiion Pattern: \n");
		for (int i = 1; i <= noOfSimpleSeq; ++i) {
			text = text.concat("\nSeq: " + i + " \n");
			for (int j = 0; j < noOfSimpleSeq; ++j) {
				for (int k = 0; k < noOfSimpleSeq; ++k) {
					if (this.templates.get(i).get(j).get(k) != null)
						text = text.concat(" C:"
								+ df.format(this.templates.get(i).get(j).get(k)
										.totalConcentration()) + ", ");
					else
						text = text.concat("false, ");
				}
				text = text.concat("\n");
			}
		}
		text = text.concat("Exo: " + df.format(this.exo) + "\n");

		return (text);
	}

	public void updateInternalState(double[] y) {
		int where = 0;
		for (int i = 0; i < this.total + 1; i++) {
			for (int j = 0; j < this.total; j++) {
				if (this.sequences.get(i).get(j) != null) {
					try {
						this.sequences.get(i).get(j).setConcentration(y[where]);
					} catch (InvalidConcentrationException e) {

						// System.err.println("Invalid concentration set attempt at time "+t);
					}
					where++;

					if (!this.sequences.get(i).get(j).fakeProtected
							&& this.sequences.get(i).get(j).isProtected()) {
						this.sequences.get(i).get(j)
								.setProtectedConcentration(y[where]);
						where++;
					}
				}
			}
		}

		double[] internal = new double[12];
		for (int i = 0; i < this.flatTemplates.size(); i++) {
			internal[0] = y[where + 0]; // there must be a better way.
			internal[1] = y[where + 1];
			internal[2] = y[where + 2];
			internal[3] = y[where + 3];
			internal[4] = y[where + 4];
			internal[5] = y[where + 5];
			internal[6] = y[where + 6];
			internal[7] = y[where + 7];
			internal[8] = y[where + 8];
			internal[9] = y[where + 9];
			internal[10] = y[where + 10];
			internal[11] = y[where + 11];
			where = where + 12;
			try {
				this.flatTemplates.get(i).setStates(internal);
			} catch (InvalidConcentrationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}

		long time1, time2;
		time1 = System.nanoTime();
		if (this.saturableExonuclease) {
			this.setObservedExoKm();
		}
		time2 = System.nanoTime();
		// System.out.println("Time spent: "+(time2-time1));
		if (this.saturablePoly) {
			this.setObservedPolyKm();
		}
		if (this.saturableNick) {
			this.setObservedNickKm();
		}
	}

	public void updateInternalState(double[] y, int offset) {
		// TODO Auto-generated method stub
		int where = offset;
		for (int i = 0; i < this.total + 1; i++) {
			for (int j = 0; j < this.total; j++) {
				if (this.sequences.get(i).get(j) != null) {
					try {
						this.sequences.get(i).get(j).setConcentration(y[where]);
					} catch (InvalidConcentrationException e) {

						// System.err.println("Invalid concentration set attempt at time "+t);
					}
					where++;

					if (this.sequences.get(i).get(j).isProtected()) {
						this.sequences.get(i).get(j)
								.setProtectedConcentration(y[where]);
						where++;
					}
				}
			}
		}

		double[] internal = new double[12];
		for (int i = 0; i < this.flatTemplates.size(); i++) {
			internal[0] = y[where + 0]; // there must be a better way.
			internal[1] = y[where + 1];
			internal[2] = y[where + 2];
			internal[3] = y[where + 3];
			internal[4] = y[where + 4];
			internal[5] = y[where + 5];
			internal[6] = y[where + 6];
			internal[7] = y[where + 7];
			internal[8] = y[where + 8];
			internal[9] = y[where + 9];
			internal[10] = y[where + 10];
			internal[11] = y[where + 11];
			where = where + 12;
			try {
				this.flatTemplates.get(i).setStates(internal);
			} catch (InvalidConcentrationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}

		if (this.saturableExonuclease) {
			this.setObservedExoKm();
		}
		if (this.saturablePoly) {
			this.setObservedPolyKm();
		}
		if (this.saturableNick) {
			this.setObservedNickKm();
		}
	}

	public void computeDerivatives(double t, double[] y, double[] ydot) {
		// First all the activation sequences, then inibiting, then templates.
		// ydot is a placeholder, should be updated with the derivative of y at
		// time t.

		updateInternalState(y);
		// System.out.println("Stepping "+t);
		int where = 0;
		double[] internal = new double[12];
		for (int i = 0; i < this.total + 1; i++) {
			for (int j = 0; j < this.total; j++) {
				if (this.sequences.get(i).get(j) != null) {
					ydot[where] = (this.sequences.get(i).get(j).fakeProtected
							&& this.sequences.get(i).get(j).isProtected() ? this
							.getTotalCurrentProtectedFlux((SimpleSequence) this.sequences
									.get(i).get(j))
							: this.getTotalCurrentFlux(this.sequences.get(i)
									.get(j)));

					where++;

					if (!this.sequences.get(i).get(j).fakeProtected
							&& this.sequences.get(i).get(j).isProtected()) {
						ydot[where] = this
								.getTotalCurrentProtectedFlux((SimpleSequence) this.sequences
										.get(i).get(j));

						where++;
					}

				}
			}
		}

		// if (this.inputs != null) {
		// for (int i = 0; i < this.inputs.length; i++) {
		// ydot[inputs[i].receivingSeq] = ydot[inputs[i].receivingSeq]
		// + inputs[i].ammount
		// * Math.exp(-(t - inputs[i].timeStep)
		// * (t - inputs[i].timeStep) / 10)
		// / Math.sqrt(10) / Math.sqrt(Math.PI);
		// // System.out.println("ydot of input "+i+" :"+
		// // ydot[inputs[i].receivingSeq]);
		// }
		// }

		for (int i = 0; i < this.flatTemplates.size(); i++) {
			internal = this.flatTemplates.get(i).flux();
			ydot[where + 0] = internal[0];
			ydot[where + 1] = internal[1];
			ydot[where + 2] = internal[2];
			ydot[where + 3] = internal[3];
			ydot[where + 4] = internal[4];
			ydot[where + 5] = internal[5];
			ydot[where + 6] = internal[6];
			ydot[where + 7] = internal[7];
			ydot[where + 8] = internal[8];
			ydot[where + 9] = internal[9];
			ydot[where + 10] = internal[10];
			ydot[where + 11] = internal[11];
			where = where + 12;
		}
		// System.out.println("Derivative at time "+t+": "+ydot[1]+" value: "+y[1]);
	}

	public void computePartialDerivatives(double t, int offset, double[] y,
			double[] ydot) {
		// For multiple systems with leak
		updateInternalState(y, offset);

		int where = 0;
		double[] internal = new double[12];
		for (int i = 0; i < this.total + 1; i++) {
			for (int j = 0; j < this.total; j++) {
				if (this.sequences.get(i).get(j) != null) {
					ydot[where] = this.getTotalCurrentFlux(this.sequences
							.get(i).get(j));

					where++;

					if (this.sequences.get(i).get(j).isProtected()) {
						ydot[where] = this
								.getTotalCurrentProtectedFlux((SimpleSequence) this.sequences
										.get(i).get(j));

						where++;
					}

				}
			}
		}
		for (int i = 0; i < this.flatTemplates.size(); i++) {
			internal = this.flatTemplates.get(i).flux();
			ydot[where + 0] = internal[0];
			ydot[where + 1] = internal[1];
			ydot[where + 2] = internal[2];
			ydot[where + 3] = internal[3];
			ydot[where + 4] = internal[4];
			ydot[where + 5] = internal[5];
			ydot[where + 6] = internal[6];
			ydot[where + 7] = internal[7];
			ydot[where + 8] = internal[8];
			ydot[where + 9] = internal[9];
			ydot[where + 10] = internal[10];
			ydot[where + 11] = internal[11];
			where = where + 12;
		}

	}

	public int getDimension() {

		return this.total + this.nbOfProtec + this.inhTotal
				+ this.flatTemplates.size() * 12; // Most
		// of
		// the
		// variables
		// in
		// sequences
		// and
		// templates
		// are
		// 0
		// all
		// the
		// time
		// though.
	}

	public double[] initialConditions() {
		if (this.saturableExonuclease) {
			this.setObservedExoKm();
		}
		if (this.saturablePoly) {
			this.setObservedPolyKm();
		}
		if (this.saturableNick) {
			this.setObservedNickKm();
		}

		double[] ret = new double[this.total + nbOfProtec + this.inhTotal
				+ this.flatTemplates.size() * 12];
		int where = 0;
		for (int i = 0; i < this.total + 1; i++) {
			for (int j = 0; j < this.total; j++) {
				if (this.sequences.get(i).get(j) != null) {
					ret[where] = this.getCurrentConcentration()[i * this.total
							+ j];
					where++;
					if (!this.sequences.get(i).get(j).fakeProtected
							&& this.sequences.get(i).get(j).isProtected()) {
						ret[where] = this.sequences.get(i).get(j)
								.getProtectedConcentration();
						where++;
					}
				}
			}
		}
		double[] internal = new double[12];
		for (int i = 0; i < this.flatTemplates.size(); i++) {
			internal = this.flatTemplates.get(i).getStates();
			ret[where + 0] = internal[0];
			ret[where + 1] = internal[1];
			ret[where + 2] = internal[2];
			ret[where + 3] = internal[3];
			ret[where + 4] = internal[4];
			ret[where + 5] = internal[5];
			ret[where + 6] = internal[6];
			ret[where + 7] = internal[7];
			ret[where + 8] = internal[8];
			ret[where + 9] = internal[9];
			ret[where + 10] = internal[10];
			ret[where + 11] = internal[11];
			where = where + 12;
		}
		return ret;
	}

	public double[][] calculateTimeSeries(boolean stopAtStable) {

		GraggBulirschStoerIntegrator myIntegrator = new GraggBulirschStoerIntegrator(
				1e-18, 10000, Constants.integrationAbsoluteError,
				Constants.integrationRelativeError);
		// DormandPrince853Integrator myIntegrator = new
		// DormandPrince853Integrator(1e-11, 10000, 1e-10, 1e-10) ;
		if (inputs == null) {
			inputs = new Input[] { new Input(0, 0, 0), new Input(0, 0, 0) };
		}
		MyEventHandler event;
		if (stopAtStable) {
			event = new MyEventHandler(inputs, true);
		} else {
			event = new MyEventHandler(inputs);
		}
		MyStepHandler handler = new MyStepHandler(this.total + this.nbOfProtec
				+ this.inhTotal, event);
		for (int i = 0; i < this.flatTemplates.size(); i++) {
			if (this.flatTemplates.get(i).getClass() == Reporter.class) {
				handler.reporters.add(this.total + this.nbOfProtec
						+ this.inhTotal + i * 12 + 1); // Adds the offset of the
														// relevant
														// concentration
			}
		}
		myIntegrator.addStepHandler(handler);
		myIntegrator.addEventHandler(event, 100, 1e-6, 100);
		// myIntegrator.addEventHandler(new CalcErrEventHandler(),
		// GAConstants.maxTime, 1e-6, 100);
		this.reinitializeOiligoSystem();
		this.exo = (exo.activity > 0.1) ? exo : new Enzyme("exo",
				Constants.exoVm / Constants.exoKmSimple, Constants.exoKmSimple,
				Constants.exoKmInhib);
		double[] placeholder = this.initialConditions();
		try {
			myIntegrator.integrate(this, 0, placeholder,
					model.Constants.maxTime, placeholder);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// for (int t = 0; t < GAConstants.numberOfPoints; t++) {
		// where = 0;
		// outputModel.setInterpolatedTime(outputModel.getFinalTime()
		// * ((double) t / GAConstants.numberOfPoints));
		// //
		// System.out.println("value of the first sequence: "+outputModel.getInterpolatedState()[1]+" at time "+outputModel.getInterpolatedTime());
		// for (int i = 0; i < this.total; ++i) {
		// if (this.sequences.get(0).get(i) != null) {
		// series[where][t] = outputModel.getInterpolatedState()[i];
		// where++;
		// }
		// }
		// }
		// System.out.println("Done");
		return handler.getTimeSerie();
	}

	public double[][] calculateTimeSeries(int steps) {

		GraggBulirschStoerIntegrator myIntegrator = new GraggBulirschStoerIntegrator(
				1e-18, 10000, Constants.integrationAbsoluteError,
				Constants.integrationRelativeError);
		// DormandPrince853Integrator myIntegrator = new
		// DormandPrince853Integrator(1e-11, 10000, 1e-10, 1e-10) ;
		if (inputs == null) {
			inputs = new Input[] { new Input(0, 0, 0), new Input(0, 0, 0) };
		}
		MyEventHandler event = new MyEventHandler(inputs);
		MyStepHandler handler = new MyStepHandler(this.total + this.nbOfProtec
				+ this.inhTotal, event);
		for (int i = 0; i < this.flatTemplates.size(); i++) {
			if (this.flatTemplates.get(i).getClass() == Reporter.class) {
				handler.reporters.add(this.total + this.nbOfProtec
						+ this.inhTotal + i * 12 + 1); // Adds the offset of the
														// relevant
														// concentration
			}
		}
		myIntegrator.addStepHandler(handler);
		myIntegrator.addEventHandler(event, 100, 1e-6, 100);
		myIntegrator.addEventHandler(new CalcErrEventHandler(), steps, 1e-6,
				100);
		// this.reinitializeOiligoSystem();
		double[] placeholder = this.initialConditions();
		myIntegrator.integrate(this, 0, placeholder, steps, placeholder);

		// for (int t = 0; t < GAConstants.numberOfPoints; t++) {
		// where = 0;
		// outputModel.setInterpolatedTime(outputModel.getFinalTime()
		// * ((double) t / GAConstants.numberOfPoints));
		// //
		// System.out.println("value of the first sequence: "+outputModel.getInterpolatedState()[1]+" at time "+outputModel.getInterpolatedTime());
		// for (int i = 0; i < this.total; ++i) {
		// if (this.sequences.get(0).get(i) != null) {
		// series[where][t] = outputModel.getInterpolatedState()[i];
		// where++;
		// }
		// }
		// }
		// System.out.println("Done");
		return handler.getTimeSerie();
	}

	public double[][] calculateTimeSeries(EventHandler eventHandler) {
		GraggBulirschStoerIntegrator myIntegrator = new GraggBulirschStoerIntegrator(
				1e-18, 10000, Constants.integrationAbsoluteError,
				Constants.integrationRelativeError);
		// DormandPrince853Integrator myIntegrator = new
		// DormandPrince853Integrator(1e-11, 10000, 1e-10, 1e-10) ;
		if (inputs == null) {
			inputs = new Input[] { new Input(0, 0, 0), new Input(0, 0, 0) };
		}
		MyStepHandler handler = new MyStepHandler(this.total + this.nbOfProtec
				+ this.inhTotal, null);
		for (int i = 0; i < this.flatTemplates.size(); i++) {
			if (this.flatTemplates.get(i).getClass() == Reporter.class) {
				handler.reporters.add(this.total + this.nbOfProtec
						+ this.inhTotal + i * 12 + 1); // Adds the offset of the
														// relevant
														// concentration
			}
		}
		myIntegrator.addStepHandler(handler);
		myIntegrator.addEventHandler(eventHandler, 100, 1e-6, 100);
		// myIntegrator.addEventHandler(new CalcErrEventHandler(),
		// GAConstants.maxTime, 1e-6, 100);
		this.reinitializeOiligoSystem();
		this.exo = (exo.activity > 0.1) ? exo : new Enzyme("exo",
				Constants.exoVm / Constants.exoKmSimple, Constants.exoKmSimple,
				Constants.exoKmInhib);
		double[] placeholder = this.initialConditions();
		myIntegrator.integrate(this, 0, placeholder, model.Constants.maxTime,
				placeholder);

		// for (int t = 0; t < GAConstants.numberOfPoints; t++) {
		// where = 0;
		// outputModel.setInterpolatedTime(outputModel.getFinalTime()
		// * ((double) t / GAConstants.numberOfPoints));
		// //
		// System.out.println("value of the first sequence: "+outputModel.getInterpolatedState()[1]+" at time "+outputModel.getInterpolatedTime());
		// for (int i = 0; i < this.total; ++i) {
		// if (this.sequences.get(0).get(i) != null) {
		// series[where][t] = outputModel.getInterpolatedState()[i];
		// where++;
		// }
		// }
		// }
		// System.out.println("Done");
		return handler.getTimeSerie();
	}

	public double[][] calculateTimeSeries(MyStepHandler stepHandler,
			EventHandler eventHandler) {
		 GraggBulirschStoerIntegrator myIntegrator = new
		 GraggBulirschStoerIntegrator(
		 1e-18, 10000, Constants.integrationAbsoluteError,
		 Constants.integrationRelativeError);

		//DormandPrince853Integrator myIntegrator = new DormandPrince853Integrator(
		//		1e-13, 10000, Constants.integrationAbsoluteError,
		//		Constants.integrationRelativeError);
		if (inputs == null) {
			inputs = new Input[] { new Input(0, 0, 0), new Input(0, 0, 0) };
		}
		for (int i = 0; i < this.flatTemplates.size(); i++) {
			if (this.flatTemplates.get(i).getClass() == Reporter.class) {
				stepHandler.reporters.add(this.total + this.nbOfProtec
						+ this.inhTotal + i * 12 + 1); // Adds the offset of the
														// relevant
														// concentration
			}
		}
		// MyStepHandler handler = new MyStepHandler(this.total +
		// this.nbOfProtec
		// + this.inhTotal, null);
		myIntegrator.addStepHandler(stepHandler);
		myIntegrator.addEventHandler(eventHandler, 10, 1e-13, 1000);
		myIntegrator
				.addEventHandler(new CalcErrEventHandler(), 100, 2e-13, 100);
		this.reinitializeOiligoSystem();
		this.exo = (exo.activity > 0.1) ? exo : new Enzyme("exo",
				Constants.exoVm / Constants.exoKmSimple, Constants.exoKmSimple,
				Constants.exoKmInhib);
		double[] placeholder = this.initialConditions();
		myIntegrator.integrate(this, 0, placeholder, model.Constants.maxTime,
				placeholder);

		// for (int t = 0; t < GAConstants.numberOfPoints; t++) {
		// where = 0;
		// outputModel.setInterpolatedTime(outputModel.getFinalTime()
		// * ((double) t / GAConstants.numberOfPoints));
		// //
		// System.out.println("value of the first sequence: "+outputModel.getInterpolatedState()[1]+" at time "+outputModel.getInterpolatedTime());
		// for (int i = 0; i < this.total; ++i) {
		// if (this.sequences.get(0).get(i) != null) {
		// series[where][t] = outputModel.getInterpolatedState()[i];
		// where++;
		// }
		// }
		// }
		// System.out.println("Done");
		return stepHandler.getTimeSerie();
	}

	public ArrayList<Reporter> getReporters() {
		return reporters;
	}

	public void addReporter(SimpleSequence s, double concentration) {
		Reporter rep = new Reporter(concentration, s);
		this.reporters.add(rep);
		this.flatTemplates.add(rep);
	}

	public void removeReporter(Reporter rep) {
		this.reporters.remove(rep);
		this.flatTemplates.remove(rep);
	}

}
