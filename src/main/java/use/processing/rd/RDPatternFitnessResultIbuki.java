package use.processing.rd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import com.google.common.collect.HashBasedTable;
import use.processing.bead.Bead;

public class RDPatternFitnessResultIbuki extends RDPatternFitnessResult{
 private static final long serialVersionUID=-4108046914347091513L;
 /**
  * This function actually calculate the fitness function. As there are many if-branches in the original code, I also try the same manner.
  */
 public RDPatternFitnessResultIbuki(float[][][] conc,boolean[][] pattern,HashBasedTable<Integer,Integer,ArrayList<Bead>> beads,double randomFit){
  // this.concGlue = conc[RDConstants.glueIndex];
  this.conc=new float[Math.min(conc.length,RDConstants.glueIndex+2)][][];
  for(int i=0;i<this.conc.length;i++)
   this.conc[i]=conc[i];
  this.pattern=pattern;
  // this.beads = beads;
  positions=(RDConstants.useGlueAsTarget ? PatternEvaluator.detectGlue(conc[RDConstants.glueIndex]) : PatternEvaluator.detectBeads(pattern.length,pattern[0].length,beads));
  fitness=distance(pattern,positions);
// if(RDConstants.useMatchFitness){
// fitness=((PatternEvaluator.matchOnPattern(pattern,positions))*RDConstants.spaceStep*RDConstants.spaceStep)/(RDConstants.hsize*RDConstants.wsize);
// }else if(RDConstants.useHellingerDistance){
// fitness=1.0-PatternEvaluator.hellingerDistance(conc[RDConstants.glueIndex],pattern);
// }else{
// fitness=RDConstants.hsize*RDConstants.wsize/((PatternEvaluator.distance(pattern,positions))*RDConstants.spaceStep*RDConstants.spaceStep);
// }
  fitness=Math.max(0.0,fitness-randomFit);
 }
 /**
  * From here, I implemented my original distance function.
  */
 public static int binX=5;// x
 public static int binY=5;// y
 public static int binA=5;// angle
 public static double ratioShiftX=0.8;
 public static double ratioShiftY=0.8;
 public static double distance(boolean[][] pattern,boolean[][] positions){
  double fitnessBestMatchNormalize=0.;
  double fitnessBestMatch=0.;
  for(int a=1;a<=binA;a++){
   double angle=a*2.*Math.PI/(binA+1)-Math.PI;
   boolean[][] rotate=rotate(pattern,angle);
   double angleWeight=getAbsoluteWeight(0,Math.PI,angle);
   for(int x=0;x<binX;x++){
    int dx=(int)((x*pattern.length/(binX-1)-pattern.length/2.)*ratioShiftX);
    boolean[][] shiftX=shiftX(rotate,dx);
    double xWeight=getAbsoluteWeight(0.,pattern.length/2.,dx);
    for(int y=0;y<binY;y++){
     int dy=(int)((y*pattern[0].length/(binY-1)-pattern[0].length/2.)*ratioShiftY);
     boolean[][] shiftY=shiftY(shiftX,dy);
     double yWeight=getAbsoluteWeight(0.,pattern[0].length/2.,dy);
     double fitness=((PatternEvaluator.matchOnPattern(shiftY,positions))*RDConstants.spaceStep*RDConstants.spaceStep)/(RDConstants.hsize*RDConstants.wsize);
     if(fitness>fitnessBestMatch){
      fitnessBestMatch=fitness;
      fitnessBestMatchNormalize=fitnessBestMatch*angleWeight*xWeight*yWeight;
     }
    }
   }
  }
  return fitnessBestMatchNormalize;
 }
 protected static double getGaussianDistribution(double mean,double sd,double x){
  return Math.exp(-(x-mean)*(x-mean)/2/sd/sd)/Math.sqrt(2*sd*sd*Math.PI);
 }
 protected static double getParabolicWeight(double mean,double width,double x){
  return 1.-Math.pow((x-mean)/width,2.);
 }
 protected static double getAbsoluteWeight(double mean,double width,double x){
  return 1.-Math.abs((x-mean)/width);
 }
 protected static boolean[][] rotate(boolean[][] pattern,double angle){
  boolean[][] rotate=new boolean[pattern.length][pattern[0].length];
  double centerX=pattern.length/2.;
  double centerY=pattern[0].length/2;
  for(int x=0;x<pattern.length;x++){
   for(int y=0;y<pattern[0].length;y++){
    double xShift=x-centerX;
    double yShift=y-centerY;
    int originalX=(int)(Math.cos(angle)*xShift+Math.sin(angle)*yShift+centerX);
    int originalY=(int)(-Math.sin(angle)*xShift+Math.cos(angle)*yShift+centerY);
    rotate[x][y]=getTheEntryOf(pattern,originalX,originalY);
   }
  }
  return rotate;
 }
 protected static boolean[][] shiftX(boolean[][] pattern,int shift){
  boolean[][] shiftX=new boolean[pattern.length][pattern[0].length];
  for(int x=0;x<pattern.length;x++){
   for(int y=0;y<pattern[0].length;y++){
    shiftX[x][y]=getTheEntryOf(pattern,x-shift,y);
   }
  }
  return shiftX;
 }
 protected static boolean[][] shiftY(boolean[][] pattern,int shift){
  boolean[][] shiftY=new boolean[pattern.length][pattern[0].length];
  for(int x=0;x<pattern.length;x++){
   for(int y=0;y<pattern[0].length;y++){
    shiftY[x][y]=getTheEntryOf(pattern,x,y-shift);
   }
  }
  return shiftY;
 }
 protected static boolean getTheEntryOf(boolean[][] pattern,int x,int y){
  if(x<0||x>=pattern.length) return false;
  if(y<0||y>=pattern[0].length) return false;
  else return pattern[x][y];
 }
 public static void main(String[] args){
  double width=0.2;
  boolean[][] pattern=new boolean[(int)(RDConstants.wsize/RDConstants.spaceStep)][(int)(RDConstants.hsize/RDConstants.spaceStep)];
  for(int i=0;i<pattern.length;i++){
   for(int j=0;j<pattern[i].length;j++){
    pattern[i][j]=(i>=pattern.length*(0.5f-width/2.0f)&&i<=pattern.length*(0.5f+width/2.0f));
   }
  }
  drawPattern(pattern,"images"+File.separator+"original.pbm");
  for(int a=1;a<=binA;a++){
   double angle=a*2.*Math.PI/(binA+1)-Math.PI;
   boolean[][] rotate=rotate(pattern,angle);
   double angleWeight=getAbsoluteWeight(0,Math.PI,angle);
//   drawPattern(rotate,"images"+File.separator+a+".pbm");
   for(int x=0;x<binX;x++){
    int dx=(int)((x*pattern.length/(binX-1)-pattern.length/2.)*ratioShiftX);
    boolean[][] shiftX=shiftX(rotate,dx);
    double xWeight=getAbsoluteWeight(0.,pattern.length/2,dx);
    for(int y=0;y<binY;y++){
     int dy=(int)((y*pattern[0].length/(binY-1)-pattern[0].length/2.)*ratioShiftY);
     boolean[][] shiftY=shiftY(shiftX,dy);
     double yWeight=getAbsoluteWeight(0.,pattern[0].length/2,dy);
     double weight=angleWeight*xWeight*yWeight;
     drawPattern(shiftY,"images"+File.separator+a+"-"+x+"-"+y+"-"+String.format("%.3f",weight)+".pbm");
    }
   }
  }
 }
 protected static void drawPattern(boolean[][] pattern,String fileName){
  try{
   PrintWriter pw=new PrintWriter(new BufferedWriter(new FileWriter(new File(fileName))));
   pw.println("P1");
   pw.println(pattern[0].length+" "+pattern.length);
   for(int j=0;j<pattern[0].length;j++){
    for(int i=0;i<pattern.length;i++){
     pw.print((pattern[i][j]?"1":"0"));
     if(i!=pattern.length-1) pw.print(" ");
    }
    pw.println();
   }
   pw.close();
  }catch(Exception e){
   e.printStackTrace();
  }
  
 }
}
