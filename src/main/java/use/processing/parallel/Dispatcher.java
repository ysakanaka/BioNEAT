package use.processing.parallel;
import java.util.ArrayList;
import java.util.concurrent.*;

import use.processing.rd.RDSystem;

import java.lang.Math;


//--------------------------------------------------//
//                                                  //
//  Generic dispatcher of threads             //
//                                                  //
//--------------------------------------------------//
public class Dispatcher <E> {
  
  protected int availableProc;
  
  protected static int defaultNiceness = 1; //how many procs we leave
  protected ArrayList<GenericThreadComputation<E>> threads;
  protected RDSystem parent;
  
  public static ExecutorService execserv = Executors.newCachedThreadPool(); //Or should I use a fixed thread pool?
  
  public Dispatcher(RDSystem parent){
	this.parent = parent;
    init(defaultNiceness);
  }
  
  public Dispatcher(int niceness, RDSystem parent){
	this.parent = parent;
    init(niceness);
  }
  
  protected void init(int niceness){
    availableProc = Math.max(1,getTotalProc()-niceness);
    threads = new ArrayList<GenericThreadComputation<E>>();
    makeThreads();
  }
  
  // This one should be extended to replace the generic threads with something usefull
  protected void makeThreads(){
    System.out.println("Warning: generic makeThreads");
    for (int i = 0; i<availableProc; i++){
      threads.add(new GenericThreadComputation<E>(availableProc,i));
    }
  }
  
  protected static int getTotalProc(){
    return Runtime.getRuntime().availableProcessors();
  }
  
  public void exec(){

    try {
      execserv.invokeAll(threads);
      
    } catch (InterruptedException ie) {
      System.err.println("Evaluation interrupted");
    }
  }
  
}