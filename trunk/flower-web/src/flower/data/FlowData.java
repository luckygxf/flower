package flower.data;

import java.sql.Timestamp;

/**
 * 存储流量数据的类，记录从起始时间到截止时间范围内，产生的流量之和
 * 暂时只处理了inOctets和outOctets这两个字段
 * @author 郑旭东
 */
public class FlowData {

	private long inFlow; 		// 输入流量
	private long outFlow;		// 输出流量
	private Timestamp staTime;	// 起始时间
	private Timestamp endTime;	// 截止时间
	
	/**
	 * 构造方法
	 * @param inFlow 输入流量
	 * @param outFlow 输出流量
	 * @param staTime 起始时间
	 * @param endTime 截止时间
	 */
	public FlowData(long inFlow, long outFlow, Timestamp staTime, Timestamp endTime) {
		this.inFlow = inFlow;
		this.outFlow = outFlow;
		this.staTime = staTime;
		this.endTime = endTime;
	}
	
	/**
	 * 获取输入流量
	 * @return 输入流量
	 */
	public long getInFlow() {
		return inFlow;
	}

	/**
	 * 设置输入流量
	 * @param inFlow 输入流量
	 */
	public void setInFlow(long inFlow) {
		this.inFlow = inFlow;
	}

	/**
	 * 获取输入流量
	 * @return 输入流量
	 */
	public long getOutFlow() {
		return outFlow;
	}

	/**
	 * 设置输出流量
	 * @param inFlow 输出流量
	 */
	public void setOutFlow(long outFlow) {
		this.outFlow = outFlow;
	}

	/**
	 * 获取起始时间
	 * @return 起始时间
	 */
	public Timestamp getStaTime() {
		return staTime;
	}

	/**
	 * 获取截止时间
	 * @return 截止时间
	 */
	public Timestamp getEndTime() {
		return endTime;
	}
	
	/**
	 * 获取中间时间点
	 * @return 起始时间和截止时间的中间时间点
	 */
	public Timestamp getMidTime() {
		return new Timestamp((staTime.getTime() + endTime.getTime())/2);
	}
	
}
