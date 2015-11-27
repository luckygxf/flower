package flower.topology.structure;

/**
 *
 * @author 官祥飞
 */
public class Device {
    private String description;
    private String ipAddress;
    private String netMask;
    public Device(){
        description=null;
        ipAddress=null;
        netMask=null;
    }
    public Device(String description,String ipAddress, String netMask){
        this.description=description;
        this.ipAddress=ipAddress;
        this.netMask=netMask;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the ipAddress
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * @param ipAddress the ipAddress to set
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * @return the netMask
     */
    public String getNetMask() {
        return netMask;
    }

    /**
     * @param netMask the netMask to set
     */
    public void setNetMask(String netMask) {
        this.netMask = netMask;
    }
    
}
