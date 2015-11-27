package flower.topology.structure;

/**
 * 表示接口的类
 * @author 官祥飞
 */
public class Interface {

	private int index = -1;				// 接口的索引，来自ifIndex
    private int type = 0;				// 接口的类型，来自ifType
    private String descr = null; 		// 接口的描述，来自ifDescr
    private long speed = -1;			// 接口的速度，来自ifSpeed
    private String physAddress = null;	// 接口的物理地址，来自ifPhysAddress
    private String netMask = null;		// 接口的子网掩码，仅适用于有IP的接口
    private String ipAddress = null;	// 接口的IP，仅适用于有IP的接口
    
    private Subnet subnet = null; 		// 接口上的子网
    private Link link = null; 			// 接口上的链路（连接其它路由器）
    
    private int conType = -1;			// 接口上的连接类型：-1代表直连主机（无IP），0代表子网，1代表内部路由间链路，2代表边界路由链路
    
	public Interface(int index, int type, String descr, long speed, String physAddress) {
        this.index = index;
        this.type = type;
        this.descr = descr;
        this.speed = speed;
        this.physAddress = physAddress;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }
    
    public int getType() {
		return type;
	}

	public String getDescr() {
		return descr;
	}
    
    public long getSpeed() {
		return speed;
	}

	public String getPhysAddress() {
		return physAddress;
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

    public Subnet getSubnet() {
		return subnet;
	}

	public void setSubnet(Subnet subnet) {
		this.subnet = subnet;
	}

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}
	
	public int getConType() {
		return conType;
	}

	public void setConType(int conType) {
		this.conType = conType;
	}

	public void print() {
		System.out.print("\t" + index + " " + type + " " + 
				descr + " " + ipAddress + " " + netMask + " ");
		if (subnet != null) {
			System.out.print("[");
			link.print();
			System.out.print("]");
		}
		System.out.print(" ");
		if (link != null) {
			System.out.print("(");
			link.print();
			System.out.print(")");
		}
		System.out.println();
	}
	
}
