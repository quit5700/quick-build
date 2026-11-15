package cn.quit5700.buildingwand.aux;
import cn.quit5700.buildingwand.blueprint.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import cn.quit5700.buildingwand.registry.ModItems;

public final class AuxScreens {
 private static boolean jobKind(CompoundTag job){return cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(job,"Kind","").equals("Build");}
 static net.minecraft.network.chat.MutableComponent t(String key,Object... args){return Component.translatable("aux.q5."+key,args);}
 static Component literal(String text){return Component.literal(text);}
 public abstract static class Base extends Screen{
  protected int x,y,w,h;protected Component status=Component.empty();
  protected float renderScale=1;
  Base(String key){this(t(key));}
  protected Base(Component title){super(title);}
  @Override protected void init(){
   AuxClient.ui=this;var window=minecraft.getWindow();int pixels=AuxUiLayout.pixelScale(window.getWidth(),window.getHeight());if((this instanceof Main||this instanceof Box)&&window.getHeight()<600)pixels=1;renderScale=AuxUiLayout.renderScale(pixels,(int)window.getGuiScale());
   width=Math.round(window.getGuiScaledWidth()/renderScale);height=Math.round(window.getGuiScaledHeight()/renderScale);
   int preferredW=this instanceof Main?620:this instanceof Box?330:this instanceof Materials?390:340;
   int preferredH=this instanceof Main?410:this instanceof Box?430:this instanceof Materials?320:this instanceof Choices?240:this instanceof FilesScreen files&&!files.read?150:280;
   h=AuxUiLayout.panelHeight(height,preferredH);w=this instanceof Main?AuxUiLayout.mainWidth(width,h):AuxUiLayout.panelWidth(width,this instanceof Box?Math.min(preferredW,(int)(h*.9)):preferredW);x=(width-w)/2;y=(height-h)/2;
  }
  protected PlainButton button(Component label,int bx,int by,int bw,Runnable run){var result=addRenderableWidget(new PlainButton(label,bx,by,Math.max(12,bw),run));result.hint=label;return result;}
  protected PlainButton toggle(Component label,int bx,int by,int bw,boolean checked,Runnable run){var result=button(label,bx,by,bw,run);result.check=checked;return result;}
  protected PlainButton dropdown(Component label,int bx,int by,int bw,Runnable run){var result=button(label,bx,by,bw,run);result.arrow=true;return result;}
  protected Base parent(){return null;}
  protected void drawBackdrop(GuiGraphics g){}
  protected void drawContent(GuiGraphics g,int mx,int my,float partial){}
  protected float labelScale(){return renderScale*minecraft.getWindow().getGuiScale()>=1.9f?1.5f:1;}
  protected void text(GuiGraphics g,Component label,int xx,int yy,int color){g.pose().pushPose();g.pose().translate(xx,yy,0);g.pose().scale(labelScale(),labelScale(),1);g.drawString(font,label,0,0,color,false);g.pose().popPose();}
  protected void centered(GuiGraphics g,Component label,int xx,int yy,int color){text(g,label,Math.round(xx-font.width(label)*labelScale()/2),yy,color);}
  protected void border(GuiGraphics g,int xx,int yy,int ww,int hh,int color){g.fill(xx,yy,xx+ww,yy+1,color);g.fill(xx,yy+hh-1,xx+ww,yy+hh,color);g.fill(xx,yy,xx+1,yy+hh,color);g.fill(xx+ww-1,yy,xx+ww,yy+hh,color);}
  public void refresh(){rebuildWidgets();}
  @Override public void removed(){if(AuxClient.ui==this)AuxClient.ui=null;super.removed();}
  @Override public boolean isPauseScreen(){return false;}
  @Override public void renderBackground(GuiGraphics g,int mx,int my,float partial){}
  @Override public final void render(GuiGraphics g,int mx,int my,float partial){
   Base behind=parent();if(behind!=null){behind.render(g,-10000,-10000,partial);g.flush();g.pose().pushPose();g.pose().translate(0,0,400);g.fill(0,0,g.guiWidth(),g.guiHeight(),0x88000000);}
   g.pose().pushPose();g.pose().scale(renderScale,renderScale,1);
   g.fill(x,y,x+w,y+h,0xFFC6C6C6);border(g,x,y,w,h,0xFF303030);g.fill(x+1,y+1,x+w-1,y+2,0xFFFFFFFF);g.fill(x+1,y+1,x+2,y+h-1,0xFFFFFFFF);g.fill(x+2,y+h-3,x+w-1,y+h-1,0xFF777777);g.fill(x+w-3,y+2,x+w-1,y+h-1,0xFF777777);
   float titleScale=this instanceof Main?2:labelScale();g.pose().pushPose();g.pose().translate(Math.round(width/2f-font.width(title)*titleScale/2),y+8,0);g.pose().scale(titleScale,titleScale,1);g.drawString(font,title,0,0,0xFF111111,false);g.pose().popPose();drawBackdrop(g);super.render(g,Math.round(mx/renderScale),Math.round(my/renderScale),partial);drawContent(g,Math.round(mx/renderScale),Math.round(my/renderScale),partial);
   if(!status.getString().isEmpty())text(g,literal(font.plainSubstrByWidth(status.getString(),(int)((w-24)/labelScale()))),x+12,y+h-38,0xFF803322);
   g.pose().popPose();
   for(var child:children())if(child instanceof PlainButton button&&button.isMouseOver(mx/renderScale,my/renderScale)&&!button.hint.getString().isEmpty()){g.renderTooltip(font,button.hint,mx,my);break;}
   if(behind!=null){g.flush();g.pose().popPose();}
  }
  @Override public boolean mouseClicked(double mx,double my,int button){return super.mouseClicked(mx/renderScale,my/renderScale,button);}
  @Override public boolean mouseReleased(double mx,double my,int button){return super.mouseReleased(mx/renderScale,my/renderScale,button);}
  @Override public boolean mouseDragged(double mx,double my,int button,double dx,double dy){return super.mouseDragged(mx/renderScale,my/renderScale,button,dx/renderScale,dy/renderScale);}
  @Override public void mouseMoved(double mx,double my){super.mouseMoved(mx/renderScale,my/renderScale);}
  @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){return scrollPage(vertical)||super.mouseScrolled(mx/renderScale,my/renderScale,horizontal,vertical);}
  protected boolean scrollPage(double amount){return false;}
 }
 public static final class PlainButton extends Button{
  Boolean check;boolean arrow;Integer swatch;ItemStack icon=ItemStack.EMPTY;Component hint=Component.empty();
  PlainButton(Component label,int x,int y,int width,Runnable action){super(x,y,width,20,label,b->action.run(),DEFAULT_NARRATION);}
  @Override protected void renderWidget(GuiGraphics g,int mx,int my,float partial){
   var mc=Minecraft.getInstance();var font=mc.font;float scale=AuxClient.ui==null?1:AuxClient.ui.labelScale();int xx=getX(),yy=getY(),ww=getWidth(),hh=getHeight();int shade=isHoveredOrFocused()?0xFFB9C9D0:active?0xFFA7A7A7:0xFFB8B8B8;
   if(check!=null){int box=16,top=yy+(hh-box)/2;frame(g,xx,top,box,box,check?0xFF34596F:0xFFAAAAAA);if(check){g.fill(xx+4,top+8,xx+7,top+11,0xFFFFFFFF);g.fill(xx+6,top+9,xx+9,top+12,0xFFFFFFFF);g.fill(xx+8,top+6,xx+11,top+10,0xFFFFFFFF);g.fill(xx+10,top+4,xx+13,top+8,0xFFFFFFFF);}if(ww>23)label(g,font,getMessage().getString(),xx+21,yy+(hh-Math.round(8*scale))/2,scale);return;}
   frame(g,xx,yy,ww,hh,shade);int left=xx+5,right=xx+ww-5;
   if(swatch!=null){g.fill(left,yy+4,left+12,yy+hh-4,0xFF303030);g.fill(left+1,yy+5,left+11,yy+hh-5,0xFF000000|swatch);left+=17;}
   if(!icon.isEmpty()){g.renderItem(icon,left,yy+(hh-16)/2);left+=20;}
   if(arrow){int center=right-3,top=yy+hh/2-1;g.fill(center-3,top,center+4,top+1,0xFF202020);g.fill(center-2,top+1,center+3,top+2,0xFF202020);g.fill(center-1,top+2,center+2,top+3,0xFF202020);g.fill(center,top+3,center+1,top+4,0xFF202020);right-=12;}
   String label=getMessage().getString();if(font.width(label)*scale>right-left&&font.width(label)<=right-left)scale=1;
   if(font.width(label)*scale<=right-left)label(g,font,label,Math.round(left+(right-left-font.width(label)*scale)/2),yy+(hh-Math.round(8*scale))/2,scale);
   else{double phase=(System.currentTimeMillis()%9000)/9000.0;int offset=(int)Math.round((1-Math.cos(phase*Math.PI*2))/2*(font.width(label)*scale-(right-left)));g.enableScissor(left,yy+1,right,yy+hh-1);label(g,font,label,left-offset,yy+(hh-Math.round(8*scale))/2,scale);g.disableScissor();}
  }
  private static void label(GuiGraphics g,net.minecraft.client.gui.Font font,String text,int x,int y,float scale){g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(scale,scale,1);g.drawString(font,text,0,0,0xFF111111,false);g.pose().popPose();}
  private static void frame(GuiGraphics g,int x,int y,int w,int h,int color){g.fill(x,y,x+w,y+h,0xFF303030);g.fill(x+1,y+1,x+w-1,y+h-1,color);g.fill(x+1,y+1,x+w-1,y+2,0xFFFFFFFF);g.fill(x+1,y+1,x+2,y+h-1,0xFFFFFFFF);g.fill(x+2,y+h-3,x+w-1,y+h-1,0xFF666666);g.fill(x+w-3,y+2,x+w-1,y+h-1,0xFF666666);}
 }
 public static final class Main extends Base{
  private int page;private int visibleRows=1;Main(){super("title");}
  @Override protected void init(){super.init();int top=y+28;boolean shown=cn.quit5700.buildingwand.aux.LegacyNbt.booleanOr(AuxClient.state,"Shown",true),micro=cn.quit5700.buildingwand.aux.LegacyNbt.booleanOr(AuxClient.state,"Micro",false);
   toggle(t("shown_label"),x+12,top,140,shown,()->flags(!shown,micro));toggle(t("micro",micro?t("on"):t("off")),x+w-132,top,120,micro,()->flags(shown,!micro));
   int bw=(w-24)/5;button(t("all"),x+12,top+26,bw-4,()->{if(AuxClient.selected.size()==AuxClient.rows().size())AuxClient.selected.clear();else{AuxClient.selected.clear();for(var row:AuxClient.rows())AuxClient.selected.add(row.id());}refresh();});
   button(t("transform"),x+12+bw,top+26,bw-4,AuxClient::beginTransform);button(t("generate"),x+12+bw*2,top+26,bw-4,()->AuxClient.send("generate",AuxClient.ids(AuxClient.selected)));button(t("save"),x+12+bw*3,top+26,bw-4,()->minecraft.setScreen(new FilesScreen(this,false)));button(t("read"),x+12+bw*4,top+26,bw-4,()->minecraft.setScreen(new FilesScreen(this,true)));
   var rows=AuxClient.rows();boolean compact=AuxUiLayout.compact(w);int step=compact?50:28;int per=AuxUiLayout.rows(h,108,75,step);visibleRows=per;page=AuxUiLayout.page(page,rows.size(),per);int rowY=y+108;
   int[] col={12,40,118,252,288,324,w-88,w-32};
   for(int i=page*per;i<Math.min(rows.size(),(page+1)*per);i++){
    var row=rows.get(i);int yy=rowY+(i%per)*step;boolean selected=AuxClient.selected.contains(row.id());
    toggle(Component.empty(),x+col[0],yy,20,selected,()->selectRow(row.id()));
    var color=dropdown(t("color_"+Math.floorMod(cn.quit5700.buildingwand.aux.LegacyNbt.intOr(row.data,"Color",0),16)),x+col[1],yy,74,()->minecraft.setScreen(new Choices(this,row.id(),false)));color.swatch=AuxRow.COLORS[Math.floorMod(cn.quit5700.buildingwand.aux.LegacyNbt.intOr(row.data,"Color",0),16)];
    dropdown(t("shape_"+AuxRow.SHAPES[Math.floorMod(cn.quit5700.buildingwand.aux.LegacyNbt.intOr(row.data,"Shape",0),AuxRow.SHAPES.length)]),x+col[2],yy,compact?w-col[2]-20:col[3]-col[2]-4,()->minecraft.setScreen(new Choices(this,row.id(),true)));
    int detailY=compact?yy+23:yy;int showX=compact?x+12:x+col[3],solidX=compact?x+64:x+col[4];
    toggle(compact?t("visible"):Component.empty(),showX,detailY,compact?44:24,cn.quit5700.buildingwand.aux.LegacyNbt.booleanOr(row.data,"Visible",true),()->edit(row.id(),"Visible",!cn.quit5700.buildingwand.aux.LegacyNbt.booleanOr(row.data,"Visible",true)));
    var solid=toggle(compact?t("solid"):Component.empty(),solidX,detailY,compact?44:24,row.beacon()||cn.quit5700.buildingwand.aux.LegacyNbt.booleanOr(row.data,"Solid",true),()->edit(row.id(),"Solid",!cn.quit5700.buildingwand.aux.LegacyNbt.booleanOr(row.data,"Solid",true)));solid.active=!row.beacon();
    String material=cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(row.data,"Material","");Component label=material.isEmpty()?t(row.data.contains("Blueprint")?"original_material":"pure"):BuiltInRegistries.BLOCK.get(new ResourceLocation(material)).getName();
    int materialX=compact?x+120:x+col[5];var materialButton=dropdown(label,materialX,detailY,x+col[6]-materialX-4,()->minecraft.setScreen(new Materials(this,row.id())));if(!material.isEmpty())materialButton.icon=new ItemStack(BuiltInRegistries.BLOCK.get(new ResourceLocation(material)));
    var generate=button(t("generate"),x+col[6],detailY,52,()->AuxClient.send("generate",AuxClient.ids(List.of(row.id()))));generate.active=cn.quit5700.buildingwand.aux.LegacyNbt.booleanOr(row.data,"Placed",false)&&(!material.isEmpty()||row.data.contains("Blueprint"));
    button(literal("×"),x+col[7],detailY,20,()->AuxClient.send("delete",AuxClient.ids(List.of(row.id()))));
   }
   button(t("add"),x+12,rowY+Math.min(per,Math.max(0,rows.size()-page*per))*step+8,w-24,()->{AuxClient.active="";AuxClient.send("add",new CompoundTag());});
   button(literal("<"),x+8,y+h-25,28,()->{page=Math.max(0,page-1);refresh();});button(literal(">"),x+40,y+h-25,28,()->{page++;refresh();});button(t("close"),x+w/2-48,y+h-25,96,this::onClose);
  }
  private void selectRow(String id){AuxClient.first=null;AuxClient.second=null;AuxClient.active=id;if(!AuxClient.selected.add(id))AuxClient.selected.remove(id);refresh();}
  @Override public boolean mouseClicked(double mouseX,double mouseY,int button){if(super.mouseClicked(mouseX,mouseY,button))return true;double mx=mouseX/renderScale,my=mouseY/renderScale;int step=AuxUiLayout.compact(w)?50:28;int index=(int)Math.floor((my-y-108)/step)+page*visibleRows;if(mx>=x+8&&mx<x+w-8&&my>=y+108&&index>=page*visibleRows&&index<Math.min(AuxClient.rows().size(),(page+1)*visibleRows)){selectRow(AuxClient.rows().get(index).id());return true;}return false;}
  @Override protected void drawBackdrop(GuiGraphics g){var rows=AuxClient.rows();int step=AuxUiLayout.compact(w)?50:28;for(int i=page*visibleRows;i<Math.min(rows.size(),(page+1)*visibleRows);i++){if(AuxClient.selected.contains(rows.get(i).id())){int yy=y+108+(i%visibleRows)*step;g.fill(x+8,yy-2,x+w-8,yy+step-3,0xFF9FC6D7);border(g,x+8,yy-2,w-16,step-1,0xFF34596F);}}}
  @Override protected boolean scrollPage(double amount){if(amount==0)return false;page=AuxUiLayout.page(page+(amount<0?1:-1),AuxClient.rows().size(),visibleRows);refresh();return true;}
  @Override protected void drawContent(GuiGraphics g,int mx,int my,float partial){
   g.fill(x+12,y+53,x+w-12,y+54,0xFF999999);
   if(!AuxUiLayout.compact(w)){String[] keys={"select_column","color","shape","visible","solid","material_title","generate","delete_column"};int[] positions={22,77,183,264,300,(324+w-88)/2,w-62,w-22};for(int i=0;i<keys.length;i++)centered(g,t(keys[i]),x+positions[i],y+94,0xFF202020);}
   else text(g,t("selected_count",AuxClient.selected.size()),x+12,y+94,0xFF333333);
   int pages=Math.max(1,(AuxClient.rows().size()+visibleRows-1)/visibleRows);centered(g,literal((page+1)+" / "+pages),x+100,y+h-19,0xFF202020);
   if(pages>1){int top=y+108,bottom=y+h-70;g.fill(x+w-9,top,x+w-6,bottom,0xFF9A9A9A);int thumb=Math.max(8,(bottom-top)/pages),yy=top+(bottom-top-thumb)*page/Math.max(1,pages-1);g.fill(x+w-10,yy,x+w-5,yy+thumb,0xFFE6E6E6);}
  }
  private void flags(boolean shown,boolean micro){var n=new CompoundTag();n.putBoolean("Shown",shown);n.putBoolean("Micro",micro);AuxClient.send("flags",n);}
  private void edit(String row,String key,boolean value){AuxClient.active=row;var n=AuxClient.id(row);n.putBoolean(key,value);AuxClient.send("edit",n);}
 }
 private static final class Choices extends Base{
  private final Main parent;private final String row;private final boolean shape;private int page;Choices(Main parent,String row,boolean shape){super(shape?"shape":"color");this.parent=parent;this.row=row;this.shape=shape;}
  @Override protected void init(){super.init();int count=shape?AuxRow.SHAPE_ORDER.length:16;int per=Math.max(2,((h-75)/24)*2);page=Math.min(page,(count-1)/per);for(int i=page*per;i<Math.min(count,(page+1)*per);i++){int value=shape?AuxRow.SHAPE_ORDER[i]:i;button(t(shape?"shape_"+AuxRow.SHAPES[value]:"color_"+i),x+12+(i%2)*(w/2-6),y+28+((i%per)/2)*24,w/2-18,()->{AuxClient.active=row;var n=AuxClient.id(row);n.putInt(shape?"Shape":"Color",value);AuxClient.send("edit",n);minecraft.setScreen(parent);});}button(literal("<"),x+12,y+h-25,28,()->{page=Math.max(0,page-1);refresh();});button(literal(">"),x+46,y+h-25,28,()->{page++;refresh();});button(t("close"),x+w/2-50,y+h-25,100,this::onClose);}
  @Override public void onClose(){minecraft.setScreen(parent);}
  @Override protected Base parent(){return parent;}
  @Override protected boolean scrollPage(double amount){if(amount==0)return false;page=Math.max(0,page+(amount<0?1:-1));refresh();return true;}
 }
 public static final class Materials extends Base{
  private final Main parent;private final String row;private EditBox search;private int page;private List<net.minecraft.world.level.block.Block> filtered=List.of();private String chosen="";private boolean beacon(){return AuxClient.rows().stream().anyMatch(r->r.id().equals(row)&&r.beacon());}
  Materials(Main parent,String row){super("material_title");this.parent=parent;this.row=row;chosen=AuxClient.rows().stream().filter(r->r.id().equals(row)).findFirst().map(r->cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(r.data,"Material","")).orElse("");}
  private int columns(){return Math.max(1,(w-36)/36);}
  private int perPage(){return columns()*Math.max(1,(h-142)/36);}
  @Override protected void init(){super.init();String old=search==null?"":search.getValue();search=addRenderableWidget(new EditBox(font,x+12,y+28,w-24,20,t("search")));search.setHint(t("search"));search.setValue(old);search.setResponder(value->{page=0;rebuildWidgets();});setInitialFocus(search);
   String query=old.toLowerCase(Locale.ROOT);filtered=BuiltInRegistries.BLOCK.stream().filter(b->b.asItem() instanceof BlockItem).filter(b->b.getName().getString().toLowerCase(Locale.ROOT).contains(query)||BuiltInRegistries.BLOCK.getKey(b).toString().contains(query)).toList();int cols=columns(),per=perPage();page=AuxUiLayout.page(page,filtered.size(),per);
   for(int i=page*per;i<Math.min(filtered.size(),(page+1)*per);i++){var block=filtered.get(i);int cell=i%per;var b=button(literal(""),x+12+(cell%cols)*36,y+54+(cell/cols)*36,34,()->{chosen=BuiltInRegistries.BLOCK.getKey(block).toString();status=block.getName();});b.setHeight(34);b.hint=block.getName();}
   button(literal("<"),x+12,y+h-61,28,()->{page=Math.max(0,page-1);rebuildWidgets();});button(literal(">"),x+44,y+h-61,28,()->{page++;rebuildWidgets();});
   button(t("pure"),x+82,y+h-61,w-94,()->{chosen="";status=t("pure");});button(t("confirm"),x+w-214,y+h-25,96,()->{AuxClient.active=row;var n=AuxClient.id(row);n.putString("Material",chosen);AuxClient.send("edit",n);minecraft.setScreen(parent);});button(t("cancel"),x+w-108,y+h-25,96,this::onClose);
  }
  @Override public void onClose(){minecraft.setScreen(parent);}
  @Override protected Base parent(){return parent;}
  @Override protected boolean scrollPage(double amount){if(amount==0)return false;page=Math.max(0,page+(amount<0?1:-1));refresh();return true;}
  @Override protected void drawContent(GuiGraphics g,int mx,int my,float tick){int cols=columns(),per=perPage();for(int i=page*per;i<Math.min(filtered.size(),(page+1)*per);i++){int cell=i%per;int xx=x+12+(cell%cols)*36,yy=y+54+(cell/cols)*36;g.pose().pushPose();g.pose().translate(xx+5,yy+5,0);g.pose().scale(1.5f,1.5f,1);g.renderItem(new ItemStack(filtered.get(i).asItem()),0,0);g.pose().popPose();if(BuiltInRegistries.BLOCK.getKey(filtered.get(i)).toString().equals(chosen))border(g,xx,yy,34,34,0xFF36ADC3);}if(!chosen.isEmpty()){var block=BuiltInRegistries.BLOCK.get(new ResourceLocation(chosen));g.renderItem(new ItemStack(block),x+12,y+h-88);text(g,block.getName(),x+34,y+h-86,0xFF202020);}}
 }
 public static final class FilesScreen extends Base{
  private final Main parent;private final boolean read;private EditBox name;private List<Path> files=List.of();private int page;private boolean overwrite,busy;
  FilesScreen(Main parent,boolean read){super(read?"read":"save");this.parent=parent;this.read=read;}
  @Override protected void init(){super.init();if(read){try(var stream=Files.list(BlueprintFiles.directory(minecraft.gameDirectory.toPath()))){files=stream.filter(Files::isRegularFile).filter(p->{String f=p.getFileName().toString().toLowerCase(Locale.ROOT);return f.endsWith(".nbt")||f.endsWith(".schem")||f.endsWith(".litematic");}).sorted().toList();}catch(Exception e){status=t("failed");}int per=Math.max(1,(h-90)/24);page=Math.min(page,Math.max(0,(files.size()-1)/per));for(int i=page*per;i<Math.min(files.size(),(page+1)*per);i++){Path path=files.get(i);button(literal(path.getFileName().toString()),x+12,y+30+(i%per)*24,w-24,()->load(path));}button(literal("<"),x+12,y+h-25,28,()->{page=Math.max(0,page-1);refresh();});button(literal(">"),x+44,y+h-25,28,()->{page++;refresh();});}
   else{String old=name==null?"q5_auxiliary":name.getValue();name=addRenderableWidget(new EditBox(font,x+12,y+38,w-24,20,t("filename")));name.setMaxLength(120);name.setValue(old);name.setResponder(value->overwrite=false);button(t("save"),x+12,y+68,120,this::save);}
   button(t("cancel"),x+w-108,y+h-25,96,this::onClose);
  }
  private void load(Path path){if(busy)return;busy=true;status=t("loading");CompletableFuture.supplyAsync(()->{try{return BlueprintReader.read(path);}catch(Exception e){throw new java.util.concurrent.CompletionException(e);}}).whenComplete((data,error)->minecraft.execute(()->{busy=false;if(error!=null){status=t("failed");return;}try{AuxClient.startImport(data);AuxClient.first=null;minecraft.setScreen(null);minecraft.player.displayClientMessage(t("place_preview"),false);}catch(Exception invalid){status=t("invalid");}}));}
  private void save(){if(busy)return;try{var target=BlueprintFiles.target(minecraft.gameDirectory.toPath(),name.getValue());if(Files.exists(target)&&!overwrite){overwrite=true;status=t("overwrite");return;}var rows=AuxClient.rows().stream().filter(r->AuxClient.selected.contains(r.id())).map(r->new AuxRow(r.data.copy())).toList();String filename=name.getValue();boolean replace=overwrite;var game=minecraft.gameDirectory.toPath();busy=true;status=t("saving");CompletableFuture.supplyAsync(()->{try{return BlueprintFiles.save(game,filename,BlueprintNbt.compress(AuxFiles.export(rows)),replace);}catch(Exception e){throw new java.util.concurrent.CompletionException(e);}}).whenComplete((path,error)->minecraft.execute(()->{busy=false;status=error==null?t("saved",path.getFileName().toString()):t("failed");}));}catch(Exception e){status=t("invalid");}}
  @Override public void onClose(){minecraft.setScreen(parent);}
  @Override protected Base parent(){return parent;}
  @Override protected boolean scrollPage(double amount){if(!read||amount==0)return false;page=Math.max(0,page+(amount<0?1:-1));refresh();return true;}
 }
 public static final class MoveMenu extends Base {
  private final Runnable preview;

  public MoveMenu(Runnable preview) {
   super("move_menu");
   this.preview = preview;
  }

  @Override protected void init() {
   super.init();
   w = Math.min(width-16, 280);
   h = Math.min(height-16, 132);
   x = (width-w)/2;
   y = (height-h)/2;
   button(t("move_preview"), x+12, y+32, w-24, () -> {
    minecraft.setScreen(null);
    preview.run();
   });
   button(Component.translatable("blueprint.q5.save_title"), x+12, y+58, w-24,
       () -> minecraft.setScreen(new cn.quit5700.buildingwand.client.BlueprintSaveScreen(this, null)));
   button(t("close"), x+12, y+h-28, w-24, this::onClose);
  }
 }

 public static final class Box extends Base{
  private final long pos;private int page,selected;private CompoundTag job=new CompoundTag();private List<String> materials=List.of();
  Box(long pos){super("box_title");this.pos=pos;}
  private int cell(){return Math.max(16,Math.min(24,(h-190)/7));}
  private int gridX(){return x+(w-9*cell())/2;}
  private int inputY(){return y+66;}
  private int navigationY(){return inputY()+3*cell()+4;}
  private int inventoryY(){return navigationY()+38;}
  private int inventoryRowY(int display){return inventoryY()+(display/9)*cell()+(display>=27?4:0);}
  private int progressY(){return inventoryRowY(27)+cell()+6;}
  @Override protected void init(){super.init();job=new CompoundTag();for(var raw:cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(AuxClient.state,"Jobs")){var j=(CompoundTag)raw;if(cn.quit5700.buildingwand.aux.LegacyNbt.longOr(j,"Box",0)==pos)job=j;}if(job.isEmpty()){minecraft.setScreen(null);return;}String id=cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(job,"Id","");materials=new ArrayList<>(cn.quit5700.buildingwand.aux.LegacyNbt.keys(cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(job,"Need")));Collections.sort(materials);page=AuxUiLayout.page(page,materials.size(),27);selected=Math.max(page*27,Math.min(selected,Math.min(materials.size()-1,page*27+26)));
   int c=cell();for(int display=0;display<36;display++){int slot=AuxUiLayout.inventorySlot(display);var b=button(Component.empty(),gridX()+(display%9)*c,inventoryRowY(display),c-1,()->{var n=AuxClient.id(id);n.putInt("Slot",slot);AuxClient.send("deposit",n);});b.setHeight(c-1);if(!minecraft.player.getInventory().getItem(slot).isEmpty())b.hint=minecraft.player.getInventory().getItem(slot).getHoverName();}
   for(int slot=0;slot<27;slot++){int index=page*27+slot;var b=button(Component.empty(),gridX()+(slot%9)*c,inputY()+(slot/9)*c,c-1,()->{if(index<materials.size()){selected=index;withdraw(id,materials.get(index));}});b.setHeight(c-1);if(index<materials.size())b.hint=countLabel(materials.get(index));}
   var prev=button(literal("<"),gridX(),navigationY(),24,()->{page--;selected=Math.max(0,page*27);refresh();});prev.active=page>0;
   var next=button(literal(">"),gridX()+66,navigationY(),24,()->{page++;selected=page*27;refresh();});next.active=(page+1)*27<materials.size();
   if(minecraft.player.isCreative()){int actionWidth=Math.min(112,9*c-100);var execute=button(t(actionWidth<90?"execute_short":"execute"),gridX()+9*c-actionWidth,navigationY(),actionWidth,()->AuxClient.send("execute",AuxClient.id(id)));execute.hint=t("execute");}
   int actionWidth=(w-32)/3;
   button(t("cancel_generation"),x+12,y+h-27,actionWidth,()->{AuxClient.send("cancel_job",AuxClient.id(id));minecraft.setScreen(null);});
   button(t("save_box"),x+16+actionWidth,y+h-27,actionWidth,
       () -> minecraft.setScreen(new cn.quit5700.buildingwand.client.BlueprintSaveScreen(this,id)));
   button(t("close"),x+20+2*actionWidth,y+h-27,actionWidth,this::onClose);
  }
  private void withdraw(String id,String material){var n=AuxClient.id(id);n.putString("Item",material);AuxClient.send("withdraw",n);}
  private Component countLabel(String material){var type=BuiltInRegistries.ITEM.get(new ResourceLocation(material));long remaining=cn.quit5700.buildingwand.aux.LegacyNbt.longOr(cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(job,"Need"),material,0),total=cn.quit5700.buildingwand.aux.LegacyNbt.longOr(cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(job,"Required"),material,remaining),bank=cn.quit5700.buildingwand.aux.LegacyNbt.longOr(cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(job,"Bank"),material,0);return t(jobKind(job)?"build_material_count":"material_count",type.getName(new ItemStack(type)),Math.max(0,total-remaining)+bank,total,Math.max(0,remaining-bank));}
  private void slot(GuiGraphics g,ItemStack stack,int xx,int yy,long count,boolean requested){int c=cell();g.fill(xx,yy,xx+c-1,yy+c-1,0xFF8B8B8B);g.fill(xx,yy,xx+c-1,yy+1,0xFF373737);g.fill(xx,yy,xx+1,yy+c-1,0xFF373737);g.fill(xx+1,yy+c-2,xx+c-1,yy+c-1,0xFFFFFFFF);if(!stack.isEmpty()){g.renderItem(stack,xx+(c-16)/2,yy+(c-16)/2);if(count>1||requested){String number=count>999?"999+":Long.toString(count);g.drawString(font,literal(number),xx+c-2-font.width(number),yy+c-9,0xFFFFFFFF,false);}}}
  @Override protected boolean scrollPage(double amount){if(amount==0)return false;page=AuxUiLayout.page(page+(amount<0?1:-1),materials.size(),27);selected=page*27;refresh();return true;}
  @Override protected void drawContent(GuiGraphics g,int mx,int my,float tick){
   int c=cell();for(int slot=0;slot<27;slot++){int index=page*27+slot;int xx=gridX()+(slot%9)*c,yy=inputY()+(slot/9)*c;if(index<materials.size()){String id=materials.get(index);var item=new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(id)));long count=cn.quit5700.buildingwand.aux.LegacyNbt.longOr(cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(job,"Bank"),id,0);slot(g,item,xx,yy,count,true);if(mx>=xx&&mx<xx+c&&my>=yy&&my<yy+c)selected=index;}else slot(g,ItemStack.EMPTY,xx,yy,0,false);}
   if(selected>=0&&selected<materials.size()){String material=materials.get(selected);var item=new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(material)));g.renderItem(item,x+12,y+30);text(g,item.getHoverName(),x+34,y+29,0xFF202020);String details=countLabel(material).getString();String name=item.getHoverName().getString();if(details.startsWith(name))details=details.substring(name.length()).strip();g.drawString(font,literal(font.plainSubstrByWidth(details,w-24)),x+12,y+49,0xFF202020,false);}
   g.drawString(font,literal((page+1)+" / "+Math.max(1,(materials.size()+26)/27)),gridX()+29,navigationY()+6,0xFF202020,false);
   g.drawString(font,t("inventory"),gridX(),inventoryY()-12,0xFF202020,false);
   for(int display=0;display<36;display++){var stack=minecraft.player.getInventory().getItem(AuxUiLayout.inventorySlot(display));slot(g,stack,gridX()+(display%9)*c,inventoryRowY(display),stack.getCount(),false);}
   g.drawString(font,t(jobKind(job)?"build_progress":"progress",cn.quit5700.buildingwand.aux.LegacyNbt.longOr(job,"Cursor",0),cn.quit5700.buildingwand.aux.LegacyNbt.longOr(job,"Total",0)),gridX(),progressY(),0xFF202020,false);
   int yy=progressY()+13;for(var line:font.split(t(jobKind(job)?"build_deposit_hint":"deposit_hint"),w-24)){g.drawString(font,line,x+12,yy,0xFF202020,false);yy+=10;}
  }
 }
}
