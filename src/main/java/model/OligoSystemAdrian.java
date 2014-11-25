package model;

import java.util.ArrayList;

import model.util.MyEventHandler;
import model.util.MyStepHandler;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;

public class OligoSystemAdrian extends OligoSystem implements
		FirstOrderDifferentialEquations {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int total;
	protected int inhTotal;
	protected int NoOfSimpleSeq;
	public Input[] inputs;

	public OligoSystemAdrian(int noOfSimpleSeq, double[][][] template,
			double[][] seqK, double[][] inhK, double[][] seqConcentration,
			Enzyme exo) {
		this.NoOfSimpleSeq = noOfSimpleSeq;
		this.seqConcentration = seqConcentration;
		this.seqK = seqK;
		this.inhK = inhK;
		this.exo = exo;
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
	}

	@Override
	public void setTotals(int total, int inhTotal) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reinitializeOiligoSystem() {
		// TODO Auto-generated method stub

	}

	public void computeDerivatives(double t, double[] y, double[] ydot) {
		this.setCurrentConcentration(y);

		Sequence seq;
		int where = 0;
		for (int i = 0; i < sequences.size(); i++) {
			for (int j = 0; j < sequences.get(0).size(); j++) {
				seq = sequences.get(i).get(j);
				if (seq != null) {
					ydot[where] = getTotalCurrentFlux(seq);
					where++;
				}
			}
		}

	}

	public int getDimension() {
		return this.total + this.inhTotal;
	}

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
	 * concentration may be 0) is Phyi->j = ([template i->j] *
	 * [i])/((1+[i]+lembda*[Iij]) with lembda Ki/KIij.
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
		double denom = 1 + i.getConcentration();
		Sequence inhib = sequences.get(i.getID() + 1).get(j.ID);

		if (inhib != null) {
			denom += inhib.getConcentration() * i.getK() / inhib.getK();
		}
		double debug = templateitoj.totalConcentration() * i.getConcentration()
				/ denom;
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
		double denom = 1 + i.getConcentration();
		double debug = templateitoj.totalConcentration() * i.getConcentration()
				/ denom;
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
		return -j.getConcentration();
	}

	public double[] initialConditions() {
		double concentration[] = new double[this.total + this.inhTotal];

		int where = 0;
		for (int i = 0; i < this.total; ++i) {
			if (this.sequences.get(0).get(i) != null) {
				concentration[where] = this.sequences.get(0).get(i)
						.getConcentration();
				where++;
			}
		}
		for (int i = 0; i < this.total; ++i) {
			for (int j = 0; j < this.total; ++j) {
				if (this.sequences.get(i + 1).get(j) != null) {
					concentration[where] = this.sequences.get(i + 1).get(j)
							.getConcentration();
					where++;
				}
			}
		}
		return (concentration);
	}

	// Corrected
	private void setCurrentConcentration(double concentration[]) {
		try {
			int where = 0;
			for (int i = 0; i < this.total; ++i) {
				if (this.sequences.get(0).get(i) != null) {
					this.sequences.get(0).get(i)
							.setConcentration(concentration[where]);
					where++;
				}
			}
			for (int i = 0; i < this.total; ++i) {
				for (int j = 0; j < this.total; ++j) {
					if (this.sequences.get(i + 1).get(j) != null) {
						// System.out.println(this.sequences.get(i+1).get(j)+" with concentration "+concentration[(i+1)*this.total+j]);
						this.sequences.get(i + 1).get(j)
								.setConcentration(concentration[where]);
						where++;
						// System.out.println(this.sequences.get(i+1).get(j).getConcentration());
					}
				}
			}
		} catch (Exception e) {
			System.err.print("Invalid expression  " + e.toString());
			System.exit(1);
		}
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

}
