package model;

public class Constants {

	// System parameters
	public static  double Kduplex = 0.2; // unit is "per nMolar per min"
	public static  double simpleKmin = 3e-4 / Kduplex;
	public static  double simpleKmax = 2e3 / Kduplex;
	public static  double inhibKmin = 1e-7 / Kduplex;
	public static  double inhibKmax = 1e-1 / Kduplex;

	// Simple model constants
	public static  double n = 3; // unit is "per minute"
	public static  double p = 17; // unit is "per minute"
	public static  double alpha = n * p / (n + p);
	public static  double lembda = 1.7; // no unit

	// Complex model

	public static  double exo = 0.35; // unit is "per minute".
	public static  double exoInhib = (double) 190 / 135;
	public static  double pol = 17;
	public static  double displ = 0.2;
	public static  double polDispl = pol * displ; // If an inhibiting
														// sequence is the
														// output, and
														// hybridized to the end
	public static  double nick = 3;

	// With saturation, Padirac's parameters
	public static  double exoVm = 300;
	public static  double exoKmSimple = 440;
	public static  double exoKmInhib = 150;
	public static double exoKmTemplate = 10;	// changed from 40 by Y

	public static  double polVm = 2100;
	public static  double polKm = 80;
	public static  double polKmBoth = 80;

	public static  double nickVm = 80;
	public static  double nickKm = 30;
	public static double nickKmProducts = 30;

	public static  double leakRatio = 1e-8;
	
	// Multiplication factor to approximate the stability of an inhibitor
	// attached to its inhibited template,
	// relatively to its generating template.
	public static  double ratioInhibition = 3;
	
	public static double ratioToeholdLeft = 0.002;
	public static double ratioToeholdRight = 0.01;
	//public static final int gMaxEvaluationTime = 60000;

	
	//Saturation and Coupling
	public static boolean saturableExonuclease = true;
	public static boolean saturablePoly = true;
	public static boolean saturableNick = true;
	public static boolean exoSaturationByFreeTemplates = true;
	public static boolean exoSaturationByTemplatesAll = true; //By default, only free templates saturate the exonuclease. Both should be true for saturation by everything
	public static boolean coupledExonuclease = true; // this is only relevant if saturated
	public static boolean coupledPoly = true;
	public static boolean coupledNick = true;
	
	//Solver parameters
	public static int numberOfPoints = 2500; // Number of points return when calculating the time series.
	public static double maxTime = 2500;
	public static int maxTimeSimple = 2500;
	public static  double integrationAbsoluteError = 1e-6;
	public static  double integrationRelativeError = 1e-6;
	

}
