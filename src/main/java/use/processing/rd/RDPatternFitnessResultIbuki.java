package use.processing.rd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
// I chose one of my distances
// fitness=distanceTopology(pattern,positions);
  fitness=distanceBlurExponential(pattern,positions);
  fitness=Math.max(0.0,fitness-randomFit);
 }
 /**
  * From here, I implemented my original distance function.
  */
 protected static double getFitnessBasic(boolean[][] target,boolean[][] positions){
  // This is the basic fitness function.
  // I copied the code of Nat.
  // My fitness function recursively calls this method.
  return ((PatternEvaluator.matchOnPattern(target,positions))*RDConstants.spaceStep*RDConstants.spaceStep)/(RDConstants.hsize*RDConstants.wsize);
 }
 public static int binX=5;// x
 public static int binY=5;// y
 public static int binA=5;// angle
 public static double ratioShiftX=0.8;
 public static double ratioShiftY=0.8;
 public static double weightExponential=2./3.;
 public static double distanceTopology(boolean[][] pattern,boolean[][] positions){
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
     double fitness=getFitnessBasic(shiftY,positions);
     if(fitness>fitnessBestMatch){
      fitnessBestMatch=fitness;
      fitnessBestMatchNormalize=fitnessBestMatch*angleWeight*xWeight*yWeight;
     }
    }
   }
  }
  return fitnessBestMatchNormalize;
 }
 protected static double getGaussianWeight(double mean,double sd,double x){
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
 public static double distanceBlurExponential(boolean[][] pattern,boolean[][] positions){
  List<boolean[][]> blurredPatterns=getBlurredPatters(pattern);
  double fitness=0.;
  int farthestDistance=blurredPatterns.size();
  double currentWeight=weightExponential;
  for(int distance=0;distance<farthestDistance;distance++){
   double fitnessTemp=getFitnessBasic(blurredPatterns.get(distance),positions);
   fitness+=fitnessTemp*currentWeight;
   currentWeight=getWeightExponential(currentWeight);
  }
  return fitness;
 }
 public static double distanceBlurLinear(boolean[][] pattern,boolean[][] positions){
  List<boolean[][]> blurredPatterns=getBlurredPatters(pattern);
  double fitness=0.;
  int farthestDistance=blurredPatterns.size();
  double currentWeight=farthestDistance;
  double totalWeight=0;
  for(int distance=0;distance<farthestDistance;distance++){
   double fitnessTemp=getFitnessBasic(blurredPatterns.get(distance),positions);
   fitness+=fitnessTemp*currentWeight;
   totalWeight+=currentWeight;
   currentWeight=getWeightLinear(currentWeight);
  }
  return fitness/totalWeight;// normalize
 }
 protected static class XY{
  @Override public int hashCode(){
   final int prime=31;
   int result=1;
   result=prime*result+x;
   result=prime*result+y;
   return result;
  }
  @Override public boolean equals(Object obj){
   if(this==obj) return true;
   if(obj==null) return false;
   if(getClass()!=obj.getClass()) return false;
   XY other=(XY)obj;
   if(x!=other.x) return false;
   if(y!=other.y) return false;
   return true;
  }
  protected int x;
  protected int y;
  XY(int x,int y){
   this.x=x;
   this.y=y;
  }
 }
 protected static List<boolean[][]> getBlurredPatters(boolean[][] pattern){
  List<boolean[][]> patternsByStep=new ArrayList<boolean[][]>();
  patternsByStep.add(pattern);// only link to the given pattern
  Set<XY> setOfNeighbor=new HashSet<XY>();
  for(int x=0;x<pattern.length;x++){
   for(int y=0;y<pattern[0].length;y++){
    if(pattern[x][y]){
     Set<XY> set=getUnlabeledSurrondingCoordinate(patternsByStep,0,x,y);
     setOfNeighbor.addAll(set);
    }
   }
  }
  Set<XY> nextSetOfNeighbor=new HashSet<XY>();
  for(int step=1;setOfNeighbor.size()>0;step++){
   boolean[][] newPattern=new boolean[pattern.length][pattern[0].length];
   for(XY coordinate:setOfNeighbor){
    newPattern[coordinate.x][coordinate.y]=true;
   }
   patternsByStep.add(newPattern);
   for(XY neighbor:setOfNeighbor){
    Set<XY> set=getUnlabeledSurrondingCoordinate(patternsByStep,step,neighbor.x,neighbor.y);
    nextSetOfNeighbor.addAll(set);
   }
   setOfNeighbor.clear();
   setOfNeighbor.addAll(nextSetOfNeighbor);
   nextSetOfNeighbor.clear();
  }
  return patternsByStep;
 }
 public static boolean isSurroundingExist(boolean[][] pattern,int x,int y){
  if(getTheEntryOf(pattern,x-1,y)) return true;
  if(getTheEntryOf(pattern,x,y-1)) return true;
  if(getTheEntryOf(pattern,x,y+1)) return true;
  if(getTheEntryOf(pattern,x+1,y)) return true;
  return false;
 }
 public static Set<XY> getUnlabeledSurrondingCoordinate(List<boolean[][]> patternsByStep,int steps,int x,int y){
  XY[] surroundings=new XY[]{new XY(x-1,y),new XY(x,y-1),new XY(x,y+1),new XY(x+1,y)};
  Set<XY> answer=new HashSet<XY>();
  for(XY surronding:surroundings){
   if(surronding.x<0||surronding.x>=patternsByStep.get(0).length||surronding.y<0||surronding.y>=patternsByStep.get(0)[0].length) continue;
   boolean isUnlabel=true;
   for(int step=0;step<steps;step++){
    if(patternsByStep.get(step)[surronding.x][surronding.y]) isUnlabel=false;
   }
   if(isUnlabel) answer.add(surronding);
  }
  return answer;
 }
 protected static double getWeightExponential(double currentWeight){
  return currentWeight*weightExponential;
 }
 protected static double getWeightLinear(double currentWeight){
  return currentWeight-1;
 }
 public static void main(String[] args){
// boolean[][] pattern=getCenterLine();
  boolean[][] pattern=getSmileyFace();
  drawPattern(pattern,"images"+File.separatorChar+"pattern.pbm");
// you can check the function by drawing the pictures
// drawAllPatterns(pattern);
// drawBlurredPattern(pattern);
 }
 public static boolean[][] getCenterLine(){
  double width=0.2;
  boolean[][] centerLine=new boolean[(int)(RDConstants.wsize/RDConstants.spaceStep)][(int)(RDConstants.hsize/RDConstants.spaceStep)];
  for(int i=0;i<centerLine.length;i++){
   for(int j=0;j<centerLine[i].length;j++){
    centerLine[i][j]=(i>=centerLine.length*(0.5f-width/2.0f)&&i<=centerLine.length*(0.5f+width/2.0f));
   }
  }
  return centerLine;
 }
 public static boolean[][] getSmileyFace(){
  boolean[][] smileyFace=new boolean[(int)(RDConstants.wsize/RDConstants.spaceStep)][(int)(RDConstants.hsize/RDConstants.spaceStep)];
  double centerX=smileyFace.length/2.;
  double centerY=smileyFace[0].length/2.;
  double min=Math.min(centerX,centerY);
  double radiusSquareMin=Math.pow(0.7*min,2);
  double radiusSquareMax=Math.pow(0.8*min,2);
  double radiusSquareMouthMin=Math.pow(0.45*min,2);
  double radiusSquareMouthMax=Math.pow(0.55*min,2);
  double mouth=0.6*smileyFace[0].length;
  int eyeLeft=(int)(smileyFace.length*0.4);
  int eyeRight=(int)(smileyFace.length*0.6);
  int eyeHeight=(int)(smileyFace[0].length*0.3);
  int eyeSizeW=2;
  int eyeSizeH=4;
  for(int eyeX=-eyeSizeW;eyeX<=eyeSizeW;eyeX++){
   for(int eyeY=-eyeSizeH;eyeY<=eyeSizeH;eyeY++){
    smileyFace[eyeLeft+eyeX][eyeHeight+eyeY]=true;
    smileyFace[eyeRight+eyeX][eyeHeight+eyeY]=true;
   }
  }
  for(int i=0;i<smileyFace.length;i++){
   for(int j=0;j<smileyFace[i].length;j++){
    double distanceSquare=Math.pow(i-centerX,2.)+Math.pow(j-centerY,2.);
    if(distanceSquare>radiusSquareMin&&distanceSquare<radiusSquareMax){
     smileyFace[i][j]=true;
    }
    if(distanceSquare>radiusSquareMouthMin&&distanceSquare<radiusSquareMouthMax&&j>mouth){
     smileyFace[i][j]=true;
    }
   }
  }
  return smileyFace;
 }
 protected static void drawAllPatterns(boolean[][] pattern){
  drawPattern(pattern,"images"+File.separator+"original.pbm");
  for(int a=1;a<=binA;a++){
   double angle=a*2.*Math.PI/(binA+1)-Math.PI;
   boolean[][] rotate=rotate(pattern,angle);
   double angleWeight=getAbsoluteWeight(0,Math.PI,angle);
// drawPattern(rotate,"images"+File.separator+a+".pbm");
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
     pw.print((pattern[i][j] ? "1" : "0"));
     if(i!=pattern.length-1) pw.print(" ");
    }
    pw.println();
   }
   pw.close();
  }catch(Exception e){
   e.printStackTrace();
  }
 }
 protected static void drawBlurredPattern(boolean[][] pattern){
  List<boolean[][]> blurredPatterns=getBlurredPatters(pattern);
  try{
   PrintWriter pw=new PrintWriter(new BufferedWriter(new FileWriter(new File("images"+File.separator+"blur.pgm"))));
   pw.println("P2");
   pw.println(pattern[0].length+" "+pattern.length);
   pw.println(blurredPatterns.size());
   for(int j=0;j<pattern[0].length;j++){
    for(int i=0;i<pattern.length;i++){
     int distance=blurredPatterns.size();
     for(int step=0;step<blurredPatterns.size();step++){
      if(blurredPatterns.get(step)[i][j]){
       distance=step;
      }
     }
     pw.print(distance);
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
