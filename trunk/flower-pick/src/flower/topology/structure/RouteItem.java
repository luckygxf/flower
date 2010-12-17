package flower.topology.structure;

/**
 * 表示路由表项的类
 * @author 徐海航
 */
public class RouteItem {
    
    private String destination;
    private int ifIndex;
    private String nextHop;
    private int routeType;
    private String netMask;
    
    public RouteItem(){
        this.destination=null;
        this.ifIndex=0;
        this.nextHop=null;
        this.routeType=0;
        this.netMask=null;
    }
    public RouteItem(String destination, int ifIndex, String nextHop, 
    		int routeType, String netMask){
        this.destination=destination;
        this.ifIndex=ifIndex;
        this.nextHop=nextHop;
        this.routeType=routeType;
        this.netMask=netMask;
    }
    /**
     * @return the index
     */
    public int getIfIndex() {
        return ifIndex;
    }

    /**
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * @return the nextHop
     */
    public String getNextHop() {
        return nextHop;
    }

    /**
     * @return the netMask
     */
    public String getNetMask() {
        return netMask;
    }

    /**
     * @return the routeType
     */
    public int getRouteType() {
        return routeType;
    }

}
