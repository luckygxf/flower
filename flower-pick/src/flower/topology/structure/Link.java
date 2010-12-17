package flower.topology.structure;

/**
 * 表示链路的类
 * @author 徐海航
 * @author 郑旭东
 */
public class Link {

    private int dstRouterID;
    private int srcIfIndex;
    private int dstIfIndex;

    public Link(int dstRouterID) {
    	this.dstRouterID = dstRouterID;
    }
    
	public int getSrcIfIndex() {
		return srcIfIndex;
	}

	public void setSrcIfIndex(int srcIfIndex) {
		this.srcIfIndex = srcIfIndex;
	}

	public int getDstRouterID() {
		return dstRouterID;
	}

	public int getDstIfIndex() {
		return dstIfIndex;
	}

	public void setDstIfIndex(int dstIfIndex) {
		this.dstIfIndex = dstIfIndex;
	}

	public void print() {
    	System.out.print(srcIfIndex + " " + dstIfIndex + " " + dstRouterID);
    }

}
