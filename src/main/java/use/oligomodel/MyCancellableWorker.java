package use.oligomodel;

import graphical.animation.AnimatedSequences;

import java.awt.event.WindowEvent;
import java.util.concurrent.TimeoutException;

import javax.swing.JFrame;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import org.apache.commons.math3.ode.FirstOrderIntegrator;

import utils.MyWorker;
import utils.PluggableWorker;

import model.Constants;
import model.OligoGraph;
import model.OligoSystem;
import model.chemicals.SequenceVertex;

public class MyCancellableWorker extends MyWorker implements PluggableWorker {

	private OligoSystem syst;
	
	protected long startTime;
	protected int maxTime;
	
	public MyCancellableWorker(OligoSystem syst, int maxTime){
		super(syst,null);
		this.syst = syst;
		this.maxTime = maxTime;
	}
	
	@Override
	protected Object doInBackground() throws Exception {
		setProgress(0);
		
		this.startTime = System.currentTimeMillis();
		
		return syst.calculateTimeSeries(this);
	}
	
	@Override
	protected void done(){
		
	}
	
	public void setCustomProgress (int time){
		//System.out.println(time);
		
		if(System.currentTimeMillis()-startTime > maxTime * 1000){
			System.err.println("How my god this is so long");
			int a = 1/0; //There should be a better way, but it works
		}
		//setProgress(time);
		//cop.setProgress(time);
	}

	@Override
	public void cancel() {
		super.cancel(true);
	}

}
