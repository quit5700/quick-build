package cn.quit5700.buildingwand.aux;
import java.util.*;
public final class AuxTransformSelection {
 private AuxTransformSelection(){}
 public static List<AuxRow> choose(List<AuxRow> rows,Set<String> selected,String active){
  var placed=rows.stream().filter(r->LegacyNbt.booleanOr(r.data,"Placed",false)).toList();
  if(!selected.isEmpty())return placed.stream().filter(r->selected.contains(r.id())).map(AuxRow::copy).toList();
  var current=placed.stream().filter(r->r.id().equals(active)).findFirst();
  if(current.isPresent())return List.of(current.get().copy());
  if(rows.stream().anyMatch(r->r.id().equals(active)))return List.of();
  return placed.size()==1?List.of(placed.getFirst().copy()):List.of();
 }
}
