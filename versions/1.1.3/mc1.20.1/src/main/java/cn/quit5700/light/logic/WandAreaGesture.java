package cn.quit5700.light.logic;

/** Two-point/drag gesture: release prepares a preview; next press confirms it. */
public final class WandAreaGesture<P> {
    public enum Action { FIRST, CONFIRM, NONE }
    private P first,pending;
    private boolean initialPress;
    public Action press(P point) {
        if(pending!=null)return Action.CONFIRM;
        initialPress=first==null;
        if(initialPress){first=point;return Action.FIRST;}
        return Action.NONE;
    }
    public void release(P point) {if(first!=null && (!initialPress || !first.equals(point)))pending=point;}
    public P first(){return first;}
    public P pending(){return pending;}
    public void reject(){pending=null;}
    public void clear(){first=null;pending=null;initialPress=false;}
}
