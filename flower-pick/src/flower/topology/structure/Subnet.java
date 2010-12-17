package flower.topology.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * 表示子网的类
 * @author 徐海航
 */
public class Subnet {

    private String subNetAddress;
    private String subNetMask;
    private List<String> activeIP=new ArrayList<String>(30);
    
    public Subnet(){
        this.subNetAddress=null;
        this.subNetMask=null;
        //this.activeIP=null;
    }
    public Subnet(String subNetAddress, String subNetMask){
        this.subNetAddress=subNetAddress;
        this.subNetMask=subNetMask;
    }
    
    public boolean isValidate(String ip){
        
        String[] ss=ip.split("\\.");
        if(ss.length!=4)return false;
        String[] mm=this.subNetMask.split("\\.");
        if(mm.length!=4)return false;
        int i=0;StringBuffer sb=new StringBuffer();
        while(i<4){          
            sb.append(Integer.parseInt(ss[i])&Integer.parseInt(mm[i]));
            if(i!=3)
                sb.append(".");
            i++;
        }
        return this.subNetAddress.equals(sb.toString());
    }
    public void addActiveIp(String ip){
        this.activeIP.add(ip);
    }
    /**
     * @return the subNetAddress
     */
    public String getSubNetAddress() {
        return subNetAddress;
    }

    /**
     * @param subNetAddress the subNetAddress to set
     */
    public void setSubNetAddress(String subNetAddress) {
        this.subNetAddress = subNetAddress;
    }

    /**
     * @return the subNetMask
     */
    public String getSubNetMask() {
        return subNetMask;
    }

    /**
     * @param subNetMask the subNetMask to set
     */
    public void setSubNetMask(String subNetMask) {
        this.subNetMask = subNetMask;
    }

    /**
     * @return the activeIP
     */
    public List<String> getActiveIP() {
        return activeIP;
    }

    /**
     * @param activeIP the activeIP to set
     */
    public void setActiveIP(Vector<String> activeIP) {
        this.activeIP = activeIP;
    }
    
    public void print() {
    	System.out.print(subNetAddress + " " + subNetMask);
    	for (String ip : activeIP) {
    		System.out.print(" " + ip);
    	}
    }

}
