package use.processing.parallel;
import java.util.concurrent.Callable;

//--------------------------------------------------//
//                                                  //
//  Generic return objects from threads             //
//                                                  //
//--------------------------------------------------//
public class GenericThreadComputation <E> implements Callable<E> {
  
  protected final int totalThreads;
  protected final int myID;
  
  public GenericThreadComputation(int total, int id){
    myID = id;
    totalThreads = total;
  }
  
  public int getID(){
    return myID;
  }
  
  public E call(){
    return null;
  }
  
}