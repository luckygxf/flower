package flower.topology.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import flower.util.SNMPUtil;

/**
 * 表示路由器的类
 * @author 官祥飞
 */
public class Router extends Device {

    private int routerID;	// 唯一标识符
    private String adminIP = null;	// 路由器IP
    private Map<Integer, Interface> interfaceMap; // 接口号映射到接口信息的Map
    private Map<String, Integer> ipToIndexMap; // 接口IP（本路由IP）到接口号的Map
    private Map<String, Integer> otherIpToIfMap; // 接口上连接的IP（非本路由IP）到接口号的Map  从代码来看应该是通过ARP映射添加的其他机器的IP
	private List<IPToMacItem> ipmList; // IP到MAC映射信息的列表
    private Map<Integer, Link> linkMap = new HashMap<Integer, Link>(); // 下一条路由标识到连接信息的Map
    private List<Subnet> subNetList = new ArrayList<Subnet>();	// 子网信息的列表
    
    public Router(int routerID, String adminIP, String descr) {
    	this.routerID = routerID;
        this.adminIP = adminIP;
        this.setDescription(descr);
		// 更新接口表和路由表
        updateInterface();
        updateAddress();
        updateIPMList();
    }

    /**
     * 获取路由器上的接口信息
     * @return
     */
    public boolean updateInterface() {
    	interfaceMap = SNMPUtil.getIfTable(adminIP);
    	if (interfaceMap != null) return true;
    	return false;
    }

    /**
     * 获取路由器上的IP地址表信息
     * @return
     */
    public boolean updateAddress() {
    	ipToIndexMap = new HashMap<String, Integer>();
    	List<Object[]> ipList = SNMPUtil.getIpAddrTable(adminIP);
    	if (ipList != null) {
			for (Object[] item : ipList) {
				String addr = (String) item[0];
				int ifIndex = (Integer) item[1];
				String netMask = (String) item[2];
				// 将信息补充到接口中，并建立根据IP索引的MAP
				Interface inf = interfaceMap.get(ifIndex);
				if (inf != null) {
					inf.setIpAddress(addr);
					inf.setNetMask(netMask);
					ipToIndexMap.put(addr, ifIndex);
				}
			}
    	}
    	return true;
    }
    
    public boolean updateIPMList() {
    	otherIpToIfMap = new HashMap<String, Integer>();
    	ipmList = SNMPUtil.getIpNetToMediaTable(adminIP);
    	if (ipmList != null) {
    		for (IPToMacItem item : ipmList) {
    			otherIpToIfMap.put(item.getIpAddr(), item.getIfindex());
    		}
    		return true;
    	}
    	return false;

    }
    
    public void assignLink() {
    	for (Link link : linkMap.values()) {
    		Integer srcIfIndex = link.getSrcIfIndex();
    		Integer dstIfIndex = link.getDstIfIndex();
    		Integer dstRouter = link.getDstRouterID();
    		if (srcIfIndex != null && dstIfIndex != null && dstRouter != null) {
    			Interface inf = interfaceMap.get(srcIfIndex);
    			if (inf != null) {
    				inf.setLink(link);
    				inf.setConType(1);
    			}
    		}
    	}
    }
    //获取主机ip对应接口号，如果ip不是主机ip，返回-1
    public int rankAtIfIndex(String ip) {
        if (ipToIndexMap != null) {
            Integer flag = ipToIndexMap.get(ip);
            if (flag != null) return flag;
        }
        return -1;
    }
    
    //通过arp表获取子网内活动的ip主机
    public void assignDevices() {
        int i = 0;
        int s = this.ipmList.size();
        for (i = 0; i < s; i++) {
            IPToMacItem ipm=this.ipmList.get(i);
            this.assignIP2SubNet(ipm.getIfindex(), ipm.getIpAddr());
        }
    }
    
    public void assignIP2SubNet(int index, String ipAddr) {
        Interface inf = interfaceMap.get(index);
    	if (inf != null && inf.getSubnet() != null) {
    		Subnet cws = inf.getSubnet(); 
    		if (cws.isValidate(ipAddr)) {
    			cws.addActiveIp(ipAddr);
    		}
    	}
    }
    
    /**
     * @return the routerID
     */
    public int getRouterID() {
        return routerID;
    }

    /**
     * @return the adminIP
     */
    public String getAdminIP() {
        return adminIP;
    }

    public Map<Integer, Interface> getInterfaceMap() {
		return interfaceMap;
	}

    public Map<Integer, Link> getLinkMap() {
		return linkMap;
	}

	public List<Subnet> getSubNetList() {
		return subNetList;
	}
	
    public Map<String, Integer> getIpToIndexMap() {
		return ipToIndexMap;
	}

	public Map<String, Integer> getOtherIpToIfMap() {
		return otherIpToIfMap;
	}

	public void print() {
    	System.out.println(routerID + " " + adminIP);
    	System.out.println("Interfaces:");
//    	for (Interface inf : interfaceMap.values()) {
//    		inf.print();
//    	}
    	for (Link link : linkMap.values()) {
    		System.out.print("\t");
    		link.print();
    		System.out.println();
    	}
    }
}
