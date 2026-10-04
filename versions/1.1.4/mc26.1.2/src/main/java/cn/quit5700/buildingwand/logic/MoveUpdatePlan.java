package cn.quit5700.buildingwand.logic;
public final class MoveUpdatePlan {
 public static boolean boundary(DeleteBox source,DeleteBox target,int x,int y,int z){return boundary(source,x,y,z)||boundary(target,x,y,z);}
 private static boolean boundary(DeleteBox b,int x,int y,int z){return b.contains(x,y,z)&&(x==b.minX()||x==b.maxX()||y==b.minY()||y==b.maxY()||z==b.minZ()||z==b.maxZ());}
}
