package flower.data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 进行数据处理的类
 * @author 郑旭东
 *
 */
public class DataProcessor {

	/**
	 * 将从数据库获取的原始流量累计数据，转化为相邻两个时间点之间的流量变化数据
	 * @param data 流量累计数据
	 * @return 流量变化数据
	 */
	public static List<FlowData> transToDiff(List<Object[]> data) {
		try {
			List<FlowData>result = new ArrayList<FlowData>();
			if (data.size() > 0) {
				long preIn = (Long) data.get(0)[1];
				long preOut = (Long) data.get(0)[2];;
				Timestamp preTime = (Timestamp) data.get(0)[0];
				for (int i = 1; i < data.size(); i++) {
					long nowIn, nowOut;
					// 流入流量
					nowIn = (Long)data.get(i)[1] - preIn;
					if (nowIn < 0) nowIn += 4294967295L;
					preIn = (Long)data.get(i)[1];
					// 流出流量
					nowOut = (Long)data.get(i)[2] - preOut;
					if (nowOut < 0) nowOut += 4294967295L;
					preOut = (Long)data.get(i)[2];
					// 加入列表
					result.add(new FlowData(nowIn, nowOut, preTime, (Timestamp)data.get(i)[0]));
					preTime = (Timestamp)data.get(i)[0];
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<FlowData>();
		}
	}
	
	/**
	 * 将从数据库获取的多个接口流量累计数据，转化为相邻两个时间点之间的这些接口的流量变化值之和
	 * @param data 多个接口流量累计数据
	 * @return 流量变化值之和
	 */
	public static List<FlowData> calSumFlow(List<List<Object[]>> data) {
		List<FlowData> flowList = new ArrayList<FlowData>();
		try {
			// 以下的步骤，首先将各个接口的流量累计值转化为流量变化值
			// 然后逐个接口向总和中添加进行合并，如果在某个时间段上，某个接口的流量信息缺失，将其计为0
			if (data.size() > 0) {
				flowList = transToDiff(data.get(0)); // 首先以每一个接口的数据作为总表
				for (int i = 1; i < data.size(); i++) { // 然后将以后的每个接口向总表中添加
					List<FlowData> speedTmpList = transToDiff(data.get(i));
					int j = 0, k = 0;
					while (j < flowList.size() && k < speedTmpList.size()) {
						Timestamp time1 = (Timestamp) flowList.get(j).getEndTime(); 
						Timestamp time2 = (Timestamp) speedTmpList.get(k).getEndTime();
						if (time1.equals(time2)) { // 时间段相同，将当前接口的数据向总表中累加
							flowList.get(j).setInFlow(flowList.get(j).getInFlow() + 
									speedTmpList.get(k).getInFlow());
							flowList.get(j).setOutFlow(flowList.get(j).getOutFlow() + 
									speedTmpList.get(k).getOutFlow());
							j++; k++;
						} else if (time1.after(time2)) { // 总表中该时间段缺失，向其添加一项 
							flowList.add(j, speedTmpList.get(k));
							j++; k++;
						} else { // 当前接口中该时间段缺失，不更新总表
							j++;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flowList;
	}
	
}
