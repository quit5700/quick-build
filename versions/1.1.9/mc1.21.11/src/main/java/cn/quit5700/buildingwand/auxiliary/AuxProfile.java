package cn.quit5700.buildingwand.aux;

/** Approved front-view profiles. Positive y points up; planar callers map north to up. */
public final class AuxProfile {
 private static final double INNER=Math.sin(Math.PI/10)/Math.sin(3*Math.PI/10);
 private static final double[] X=new double[10],Y=new double[10];
 static{for(int i=0;i<10;i++){double angle=Math.PI/2+i*Math.PI/5,r=i%2==0?1:INNER;X[i]=Math.cos(angle)*r;Y[i]=Math.sin(angle)*r;}}
 private AuxProfile(){}
 public static boolean star(double x,double y,double radius){
  boolean inside=false;double px=X[9]*radius,py=Y[9]*radius;
  for(int i=0;i<10;i++){double nx=X[i]*radius,ny=Y[i]*radius;if((py>y)!=(ny>y)&&x<(nx-px)*(y-py)/(ny-py)+px)inside=!inside;px=nx;py=ny;}
  return inside;
 }
 public static boolean crescent(double x,double y,double radius){return x*x+y*y<=radius*radius&&(x-radius*.46)*(x-radius*.46)+y*y>(radius*.88)*(radius*.88);}
}
