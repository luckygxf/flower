package flower.topology.structure;

/**
 * 表示IP-MAC表项的类
 * @author 徐海航
 */
public class IPToMacItem {
    private int ifindex;
    private String ipAddr;
    
    //暂时不用Mac
    public IPToMacItem(int ifindex,String ipAddr){
        this.ifindex=ifindex;
        this.ipAddr=ipAddr;
    }

    /**
     * @return the ifindex
     */
    public int getIfindex() {
        return ifindex;
    }

    /**
     * @return the ipAddr
     */
    public String getIpAddr() {
        return ipAddr;
    }

}
