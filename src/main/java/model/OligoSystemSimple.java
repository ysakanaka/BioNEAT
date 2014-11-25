package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;

import model.util.MyEventHandler;
import model.util.MyStepHandler;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegrator;

public class OligoSystemSimple extends OligoSystem implements Serializable,
		FirstOrderDifferentialEquations {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7198457965761900874L;
	public int total = 0;
	private int inhTotal = 0;
	private String mathematicaCode;
	private int NoOfSimpleSeq;
	public Input[] inputs;

	/**
	 * <b>templates<\b> is a list of matrixes representing the various
	 * concentration of templates in the solution.
	 * 
	 * The first matrix represents activation. Position (i,j) represents the
	 * concentration of the template generating the sequence j from the sequence
	 * i (note: generating a sequence doesn't destroy the primer).
	 * 
	 * After this, the remaining matrixes represent templates generating
	 * inhibiting sequences. Position (i,j) in the kth matrix represents the
	 * concentration of the template generating from (k-1) the sequence
	 * inhibiting the transcription from i to j.
	 * 
	 * Since the first matrix is a bit different in nature, it may be stored in
	 * a different variable.
	 */
	// public ArrayList<ArrayList<ArrayList<Template>>> templates;
	/**
	 * All species interacting in the present system. Store the kinetic
	 * parameter of each as well as their respective initial and current
	 * concentrations. First line is made of all SimpleSequences and position
	 * (i,j) represents the sequence inhibiting the production of j from (i-1).
	 */

	public OligoSystemSimple(int noOfSimpleSeq, double[][][] template,
			double[][] seqK, double[][] inhK, double[][] seqConcentration,
			double exo) {
		this.NoOfSimpleSeq = noOfSimpleSeq;
		this.seqConcentration = seqConcentration;
		this.seqK = seqK;
		this.inhK = inhK;
		this.exo = new Enzyme("exo", Constants.exoVm / Constants.exoKmSimple,
				Constants.exoKmSimple, Constants.exoKmInhib);
		this.template = template;

		// First initialize sequence
		// List of all sequences (simple + inhibiting )
		this.sequences = new ArrayList<ArrayList<Sequence>>(noOfSimpleSeq + 1);
		// First row for simple sequences. So initialize the row for simple
		// sequences
		this.sequences.add(new ArrayList<Sequence>(noOfSimpleSeq));
		// initialize all simple sequences
		for (int i = 0; i < noOfSimpleSeq; ++i) {

			try {
				this.sequences.get(0).add(
				// From Nat: it is more explicite this way
						new SimpleSequence(i, seqConcentration[0][i],
								seqK[0][i]));
			} catch (InvalidKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidConcentrationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // First row contains Kinetics and
				// concetration for simple
				// sequences
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
					try {
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
					} catch (InvalidKException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidConcentrationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
		this.templates.add(new ArrayList<ArrayList<Template>>(noOfSimpleSeq));
		for (int i = 0; i < noOfSimpleSeq; ++i) {
			this.templates.get(0).add(new ArrayList<Template>(noOfSimpleSeq));
			for (int j = 0; j < noOfSimpleSeq; ++j) {
				if (template[0][i][j] != 0) {
					this.templates
							.get(0)
							.get(i)
							.add(new Template(template[0][i][j],
									(SimpleSequence) this.sequences.get(0).get(
											i), this.sequences.get(0).get(j),
									(InhibitingSequence) this.sequences.get(
											i + 1).get(j)));
				} else {
					this.templates.get(0).get(i).add(null);
				}
			}
		}

		// Initialize Templates for inhibition
		for (int i = 1; i <= noOfSimpleSeq; ++i) {
			this.templates
					.add(new ArrayList<ArrayList<Template>>(noOfSimpleSeq));
			for (int j = 0; j < noOfSimpleSeq; ++j) {
				this.templates.get(i).add(
						new ArrayList<Template>(noOfSimpleSeq));
				for (int k = 0; k < noOfSimpleSeq; ++k) {
					if (template[i][j][k] != 0) {
						this.templates
								.get(i)
								.get(j)
								.add(new Template(template[i][j][k],
										(SimpleSequence) this.sequences.get(0)
												.get(i - 1), this.sequences
												.get(j + 1).get(k), null));
					} else {
						this.templates.get(i).get(j).add(null);
					}

				}
			}
		}

		// Initialize exo

		// calculateMathematicaCode();
	}

	double getExo() {
		return this.exo.activity;
	}

	void setExo(double exo) {
		this.exo.activity = exo;
	}

	@Override
	public void setTotals(int total, int inhTotal) {
		this.total = total;
		this.inhTotal = inhTotal;

	}

	/**
	 * Return the value of the current value of the derivative of the
	 * concentration of a given sequence s: d[s]/dt = flux.
	 * 
	 * @param s
	 * @return flux
	 */
	public double getTotalCurrentFlux(Sequence s) {
		double flux = 0;
		if (s != null) {
			ArrayList<Sequence> sequencesList = sequences.get(0);
			for (Sequence i : sequencesList) {
				flux += getCurrentFlux(i, s);
			}
			flux += getCurrentNegativeFlux(s);
		}
		return flux;
	}

	public double getCurrentFlux(Sequence i, Sequence j) {
		if (j.getClass() == SimpleSequence.class) {
			return getCurrentFlux(i, (SimpleSequence) j);
		}
		if (j.getClass() == InhibitingSequence.class) {
			return getCurrentFlux(i, (InhibitingSequence) j);
		}

		return 0;
	}

	/**
	 * Basic equation for a flux from i to j with an inhibitor inhib (which
	 * concentration may be 0) is Phyi->j = (alpha * [template i->j] *
	 * [i])/([i]+a0(1+a1)) with alpha constant, a0 = n/(Ki * (n + p)), a1 =
	 * Kinhib*[inhib] / (1 + lembda * Ki * [i]).
	 * 
	 * @param i
	 *            ,j
	 * @return the current flux from a given sequence to another
	 */
	public double getCurrentFlux(Sequence i, SimpleSequence j) {
		Template templateitoj = templates.get(0).get(i.getID()).get(j.ID);
		if (templateitoj == null || i.getConcentration() == 0 || i == null) {
			return 0;
		}
		Sequence inhib = sequences.get(i.getID() + 1).get(j.ID);
		double a1 = 0;
		if (inhib != null) {
			a1 = (Constants.Kduplex / inhib.getK())
					* inhib.getConcentration()
					/ (1 + Constants.lembda * (Constants.Kduplex / i.getK())
							* i.getConcentration());
			// System.out.println("a1: "+a1);
		}
		double a0 = Constants.n
				/ ((Constants.Kduplex / i.getK()) * (Constants.n + Constants.p));
		double debug = Constants.alpha * templateitoj.totalConcentration()
				* i.getConcentration() / (i.getConcentration() + a0 * (1 + a1));
		// System.out.println("Flux for "+j+": "+debug);
		return debug;
	}

	/**
	 * Almost the same as the equation from SimpleSequence to SimpleSequence,
	 * but since we don't model inhibitors of inhibitors, the equation is a bit
	 * simpler. Phyi->j = (alpha * [template i->j]*[i])/([i]+a0).
	 * 
	 * @param i
	 * @param j
	 * @return the current flux from a given sequence to an inhibiting sequence
	 */
	public double getCurrentFlux(Sequence i, InhibitingSequence j) {
		Template templateitoj = templates.get(i.getID() + 1)
				.get(j.getFrom().getID()).get(j.getTo().getID());
		if (templateitoj == null || i.getConcentration() == 0) {
			return 0;
		}
		double a0 = Constants.n
				/ ((Constants.Kduplex / i.getK()) * (Constants.n + Constants.p));
		double debug = Constants.alpha * templateitoj.totalConcentration()
				* i.getConcentration() / (i.getConcentration() + a0);
		// System.out.println("From "+i+" to "+j+": "+debug);
		return debug;
	}

	/**
	 * Action of the exonuclease enzyme.
	 * 
	 * @param j
	 * @return
	 */
	public double getCurrentNegativeFlux(Sequence j) {
		return -(j.getClass() == SimpleSequence.class ? Constants.exo
				: Constants.exoInhib) * j.getConcentration();
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

		String text = "\nSimple Sequences: \n";

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

		text = text.concat("Activation: \n");
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
			text = text.concat("Seq: " + i + " \n");
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

	/******************************************************
	 * Functions fore deriving the system of equations for using in LSA or
	 * somewhere else
	 * 
	 * @noman
	 */

	public ArrayList<String> getSystemEqn() {

		ArrayList<String> sysEqn = new ArrayList<String>(this.total); // initially
		// allocate
		// for
		// the
		// equations
		// for
		// simple
		// sequences
		// only
		// ...
		ArrayList<String> funVar = new ArrayList<String>();
		String[] seqNames = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j" }; // names
		// for
		// the
		// simple
		// sequences
		String[] equation;
		for (int i = 0; i < this.total; ++i) { // this.total = total number of
			// simple sequences
			equation = this.getEqn(this.sequences.get(0).get(i), seqNames[i]); // get
			// the
			// eqn
			// for
			// the
			// simple
			// sequence
			sysEqn.add(equation[0]);
			funVar.add(equation[1]);
			// System.out.println(seqNames[i]+" "+equation[1]+" "+equation[2]);
		}
		for (int i = 0; i < this.total; ++i) {
			for (int j = 0; j < this.total; ++j) {
				if (this.sequences.get(i + 1).get(j) != null) {
					equation = this.getEqn(this.sequences.get(i + 1).get(j),
							"I" + seqNames[i] + seqNames[j]); // get the eqn for
					// the
					// inhibiting
					// sequence
					sysEqn.add(equation[0]);
					// funVar.add(equation[1]);
				}
			}
		}
		// modify the exp notation for use in mathematica
		for (int i = 0; i < sysEqn.size(); ++i) {
			String temp = sysEqn.remove(i);
			temp = temp.replaceAll("E", "*10^");
			sysEqn.add(i, temp);
		}

		// list of function variables in the system of equations e.g. ta_, tb_
		// etc
		int i = 0;
		while (i < funVar.size() && funVar.get(i).length() == 0) {
			++i;
		}

		String funcVars = "";
		String funcVars_ = "";
		if (i < funVar.size()) {

			funcVars = funVar.get(i);
			funVar.set(i, funVar.get(i).replaceAll(",", "_,"));
			funcVars_ = funVar.get(i) + "_";
			for (++i; i < funVar.size(); ++i) {
				if (funVar.get(i).length() > 0) {
					funcVars = funcVars + ", " + funVar.get(i);
					funVar.set(i, funVar.get(i).replaceAll(",", "_,"));
					funcVars_ = funcVars_ + ", " + funVar.get(i) + "_";

				}
			}
		}

		sysEqn.add(0, funcVars_);
		sysEqn.add(1, funcVars);

		// create the list of variables for system of DE
		String varLst = "var = {" + seqNames[0];
		for (i = 1; i < this.total; ++i) { // this.total = total number of
			// simple sequences
			varLst = varLst + ", " + seqNames[i]; // get the eqn for the simple
			// sequence
		}
		varLst = varLst + "};";
		String varInhib = "{";
		for (i = 0; i < this.total; ++i) {
			for (int j = 0; j < this.total; ++j) {
				if (this.sequences.get(i + 1).get(j) != null) {
					varInhib = varInhib + ", " + "I" + seqNames[i]
							+ seqNames[j];
				}
			}
		}
		varInhib = varInhib.replace("{,", "{");
		varInhib = varInhib + "}";

		sysEqn.add(2, varLst);
		sysEqn.add(3, varInhib);

		return (sysEqn);
	}

	/***************************************************************************
	 * Function to create the equation for a simple sequence 's' with name
	 * 'sName
	 * 
	 * @noman
	 * @param s
	 *            : the simple sequence
	 * @param sName
	 *            : variable / letter to denote 's'
	 * @return : the equation
	 */

	private String[] getEqn(Sequence s, String sName) {
		String[] seqNames = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j" };
		;
		String eqn = "";
		String var = "";
		String varConcentration = "";
		String[] tmp;

		ArrayList<Sequence> sequencesList = sequences.get(0);
		// for(Sequence i : sequencesList){
		for (int i = 0; i < sequencesList.size(); ++i) {
			tmp = getEqnFraction(sequencesList.get(i), s, seqNames[i], sName); // get
			// the
			// part
			// of
			// the
			// complete
			// equation
			// for
			// 's'
			// and
			// 'i'
			if (tmp[0].length() > 0) { // check if the part is empty? then
				// ignore it
				if (eqn.length() > 0) { // if there is any existing part then
					// ADD with it otherwise commit as the
					// first component
					eqn = eqn + " + " + tmp[0];
				} else {
					eqn = tmp[0];
				}
			}
			if (tmp[1].length() > 0) {
				if (var.length() > 0) {
					var = var + ", " + tmp[1];
				} else {
					var = tmp[1];
				}
			}
			if (tmp.length > 2 && tmp[2].length() > 0) {
				if (varConcentration.length() > 0) {
					varConcentration = varConcentration + ", " + tmp[2];
				} else {
					varConcentration = tmp[2];
				}
			}
		}
		DecimalFormat df = new DecimalFormat("#.####");
		eqn += (" - " + df.format(this.exo) + " * " + sName); // add the
		// exo-nucleus
		// part

		String[] str = { "", "", "" };
		str[0] = eqn;
		str[1] = var;
		str[2] = varConcentration;
		// System.out.println(str[1] + " " + str[2]);
		return (str);
	}

	/********************************************************************************
	 * Function to create the part of sequence for 'i' and 'j'
	 * 
	 * @noman
	 * @param i
	 * @param j
	 * @param iName
	 * @param jName
	 * @return
	 */

	public String[] getEqnFraction(Sequence i, Sequence j, String iName,
			String jName) {
		if (j.getClass() == SimpleSequence.class) {
			return getEqnFraction(i, (SimpleSequence) j, iName, jName);
		}
		if (j.getClass() == InhibitingSequence.class) {
			return getEqnFraction(i, (InhibitingSequence) j, iName, jName);
		}
		String[] tmp = { "", "" };
		return (tmp);

	}

	public void computeDerivatives(double t, double[] y, double[] ydot) {
		int where = 0;
		for (int i = 0; i < this.total + 1; i++) {
			for (int j = 0; j < this.total; j++) {
				if (this.sequences.get(i).get(j) != null) {
					try {
						this.sequences.get(i).get(j).setConcentration(y[where]);
					} catch (InvalidConcentrationException e) {
						e.printStackTrace();
					}
					where++;
				}
			}
		}
		where = 0;
		for (int i = 0; i < this.total + 1; i++) {
			for (int j = 0; j < this.total; j++) {
				if (this.sequences.get(i).get(j) != null) {
					ydot[where] = this.getTotalCurrentFlux(this.sequences
							.get(i).get(j));
					where++;
				}
			}
		}
	}

	public void computePartialDerivatives(double t, int offset, double[] y,
			double[] partialDerivatives) {
		int where = offset;
		for (int i = 0; i < this.total + 1; i++) {
			for (int j = 0; j < this.total; j++) {
				if (this.sequences.get(i).get(j) != null) {
					try {
						this.sequences.get(i).get(j).setConcentration(y[where]);
					} catch (InvalidConcentrationException e) {
						y[where] = 0;
						try {
							this.sequences.get(i).get(j).setConcentration(0);
						} catch (InvalidConcentrationException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						// e.printStackTrace();
					}
					where++;
				}
			}
		}
		where = 0; // because the partialDerivatives array only takes those
					// sequences into account, not the global state
		for (int i = 0; i < this.total + 1; i++) {
			for (int j = 0; j < this.total; j++) {
				if (this.sequences.get(i).get(j) != null) {
					partialDerivatives[where] = this
							.getTotalCurrentFlux(this.sequences.get(i).get(j));
					where++;
				}
			}
		}

	}

	public int getDimension() {

		return this.total + this.inhTotal;
	}

	public double[][] calculateTimeSeries() {
		// GraggBulirschStoerIntegrator myIntegrator = new
		// GraggBulirschStoerIntegrator(
		// 1e-6, 10000, 1e-6, 1e-6);
		ClassicalRungeKuttaIntegrator myIntegrator = new ClassicalRungeKuttaIntegrator(
				1e-2);
		// DormandPrince853Integrator myIntegrator = new
		// DormandPrince853Integrator(1e-11, 10000, 1e-10, 1e-10) ;
		if (inputs == null) {
			inputs = new Input[] { new Input(0, 0, 0), new Input(0, 0, 0),
					new Input(0, 0, 0) };
		}
		MyEventHandler event = new MyEventHandler(inputs);
		MyStepHandler handler = new MyStepHandler(this.total + this.inhTotal,
				event);
		myIntegrator.addStepHandler(handler);
		myIntegrator.addEventHandler(event, 100, 1e-6, 100);
		this.reinitializeOiligoSystem();
		double[] placeholder = this.initialConditions();
		myIntegrator.integrate(this, 0, placeholder,
				model.Constants.maxTimeSimple, placeholder);
		return handler.getTimeSerie();
	}

	double[] initialConditions() {
		double[] y = new double[this.total + this.inhTotal];
		int where = 0;
		for (int i = 0; i < this.total + 1; i++) {
			for (int j = 0; j < this.total; j++) {
				if (this.sequences.get(i).get(j) != null) {
					y[where] = this.sequences.get(i).get(j)
							.getInitialConcentration();
					where++;
				}
			}
		}
		return y;
	}

	public void setInput(Input[] inputs) {
		this.inputs = inputs;
	}

}
