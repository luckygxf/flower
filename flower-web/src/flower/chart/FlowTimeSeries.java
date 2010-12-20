package flower.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;

import flower.data.DataProcessor;
import flower.data.FlowData;
import flower.util.DatabaseWorker;

/**
 * 绘制流量-时间图的类
 * @author 郑旭东
 */
public class FlowTimeSeries {

	private static final int POINT_NUM = 30;	// 控制粗粒度曲线的点数
	private static final int LATEST_NUM = 1000;	// 控制绘制最新的流量数据的点数
	// 图表标题
	private static final String[] conTypeTitles = {"Internal Others", 
												   "Internal Subnets", 
												   "Internal Backbone", 
												   "Boundary Interfaces"};
	
	/**
	 * 绘制指定接口、指定时间段内的流量速率图
	 * @param ifID 接口ID
	 * @param sta 开始时间
	 * @param end 结束时间
	 * @return 流量速率图
	 */
	public static JFreeChart drawByIfID(String ifID, Timestamp sta, Timestamp end) {
		// 从数据库中读取流量数据
		DatabaseWorker.connect();
		List<Object[]> rsList = DatabaseWorker.query("SELECT Flow_Time, Flow_InOctets, Flow_OutOctets FROM " +
				"Flows WHERE Flow_Interface='" + ifID + 
				"' AND Flow_Time>'" + sta + "' AND Flow_Time<'" + end + "'");
		DatabaseWorker.release();
		// 根据流量和时间间隔计算速率，并将数据输入图表中
		List<FlowData> flowList = DataProcessor.transToDiff(rsList);
		// 画图并返回图表
		return draw(flowList, "Interface "+ifID+" Flow Chart");
	}
	
	/**
	 * 绘制指定接口、最近LATEST_NUM个时间间隔内的流量速率图
	 * @param ifID 接口ID
	 * @return 流量速率图
	 */
	public static JFreeChart drawByIfID(String ifID) {
		// 从数据库中读取流量数据
		DatabaseWorker.connect();
		List<Object[]> rsList = DatabaseWorker.query("SELECT Flow_Time, Flow_InOctets, Flow_OutOctets FROM " +
				"Flows WHERE Flow_Interface='" + ifID +	"' LIMIT 0, " + LATEST_NUM);
		DatabaseWorker.release();
		// 根据流量和时间间隔计算速率，并将数据输入图表中
		List<FlowData> flowList = DataProcessor.transToDiff(rsList);
		// 画图并返回图表
		return draw(flowList, "Interface "+ifID+" Flow Chart");
	}
	
	/**
	 * 绘制指定类型接口、指定时间段内的流量之和速率图
	 * @param conType 接口类型：-1是无IP接口；0是IP子网；1是内网路由器间的接口；2是连接外网路由器的接口
	 * @param sta 开始时间
	 * @param end 结束时间
	 * @return 流量速率图
	 */
	public static JFreeChart drawByConType(int conType, Timestamp sta, Timestamp end) {
		// 从数据库中读取数据
		DatabaseWorker.connect();
		// 首先获得所有指定类型的接口
		List<Object[]> infList = DatabaseWorker.query("SELECT Interface_ID FROM " + 
				"Flows LEFT JOIN Interfaces ON Flow_Interface=Interface_ID WHERE Interface_ConType=" + 
				conType + " AND Flow_Time>'" + sta + "' AND Flow_Time<'" + end + "' GROUP BY Flow_Interface");
		// 然后将这些接口的流量聚合并计算速率
		List<List<Object[]>> flowListList = new ArrayList<List<Object[]>>();
		for (int i = 0; i < infList.size(); i++) {
			flowListList.add(DatabaseWorker.query("SELECT Flow_Time, Flow_InOctets, Flow_OutOctets FROM " +
					"Flows WHERE Flow_Interface='" + infList.get(i)[0] + 
					"' AND Flow_Time>'" + sta + "' AND Flow_Time<'" + end + "'"));
		}
		DatabaseWorker.release();
		// 将多个接口的原始流量监测数据转换为这些接口的流量速率之和
		List<FlowData> flowSumList = DataProcessor.calSumFlow(flowListList);	
		// 画图并返回图表
		return draw(flowSumList, conTypeTitles[conType+1]+" Flow Chart");
	}
	
	/**
	 * 绘制指定类型接口的最近LATEST_NUM个时间间隔内的流量之和速率图
	 * @param conType 接口类型：-1是无IP接口；0是IP子网；1是内网路由器间的接口；2是连接外网路由器的接口
	 * @return 流量速率图
	 */
	public static JFreeChart drawByConType(int conType) {
		// 从数据库中读取数据
		DatabaseWorker.connect();
		// 首先获得所有指定类型的接口
		List<Object[]> infList = DatabaseWorker.query("SELECT Interface_ID FROM " + 
				"Flows LEFT JOIN Interfaces ON Flow_Interface=Interface_ID WHERE Interface_ConType=" + 
				conType + " GROUP BY Flow_Interface");
		// 然后将这些接口的流量聚合并计算速率
		List<List<Object[]>> flowListList = new ArrayList<List<Object[]>>();
		for (int i = 0; i < infList.size(); i++) {
			flowListList.add(DatabaseWorker.query("SELECT Flow_Time, Flow_InOctets, Flow_OutOctets FROM " +
					"Flows WHERE Flow_Interface='" + infList.get(i)[0] + "' LIMIT 0, " + LATEST_NUM));
		}
		DatabaseWorker.release();
		// 将多个接口的原始流量监测数据转换为这些接口的流量速率之和
		List<FlowData> flowSumList = DataProcessor.calSumFlow(flowListList);	
		// 画图并返回图表
		return draw(flowSumList, conTypeTitles[conType+1]+" Flow Chart");
	}

	private static JFreeChart draw(List<FlowData> flowList, String title) {
		// 一共画出四条曲线
		// 其中两条是以测量的实际粒度画出的输入/输出流量曲线，
		// 另外两条是进行一定尺度的聚合后的曲线，聚合的程度由POINT_NUM控制，表示图上最多画出的点数
		TimeSeries timeseriesIn = new TimeSeries("InOctets Flow");
		TimeSeries timeseriesInS = new TimeSeries("InOctets Flow");
		TimeSeries timeseriesOut = new TimeSeries("OutOctets Flow");
		TimeSeries timeseriesOutS = new TimeSeries("OutOctets Flow");
		int aggr = (flowList.size() + 1) / POINT_NUM + 1;
		for (int i = 0; i < flowList.size();) {
			Timestamp staTime = flowList.get(i).getStaTime();
			long inSum = 0;
			long outSum = 0;
			for (int j = 0;i < flowList.size() && j < aggr; j++, i++) {
				FlowData item = flowList.get(i);
				// 获取精确流量并加入图表数据
				Timestamp midTime = item.getMidTime(); // 计算时间中点
				long period = item.getEndTime().getTime() - item.getStaTime().getTime(); // 计算时间时隔
				double inVal = (double) item.getInFlow() / period; // 计算速率
				double outVal = (double) item.getOutFlow() / period; // 计算速率
				timeseriesInS.addOrUpdate(new Second(midTime), inVal);
				timeseriesOutS.addOrUpdate(new Second(midTime), outVal);
				// 累计聚合流量
				inSum += item.getInFlow();
				outSum += item.getOutFlow();
			}
			// 将聚合的流量加入图表数据
			Timestamp endTime = flowList.get(i-1).getEndTime();
			Timestamp midTime = new Timestamp((staTime.getTime() + endTime.getTime())/2); // 计算时间中点
			long period = endTime.getTime() - staTime.getTime(); // 计算时间时隔
			double inVal = (double) inSum / period; // 计算速率
			double outVal = (double) outSum / period; // 计算速率
			timeseriesIn.addOrUpdate(new Second(midTime), inVal);
			timeseriesOut.addOrUpdate(new Second(midTime), outVal);
		}
		// 创建图表，并设置参数
		TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
		timeseriescollection.addSeries(timeseriesIn);
		timeseriescollection.addSeries(timeseriesInS);
		timeseriescollection.addSeries(timeseriesOut);
		timeseriescollection.addSeries(timeseriesOutS);
		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				title, "Time", "Flow Speed(KBytes Per Scecond)",
				timeseriescollection, true, true, false);
		chart.setBackgroundPaint(Color.white);
		// 获得XYPlot，设置图表的参数
		XYPlot xyplot = (XYPlot) chart.getPlot();
		xyplot.setBackgroundPaint(Color.lightGray);
		xyplot.setDomainGridlinePaint(Color.white);
		xyplot.setRangeGridlinePaint(Color.white);
		xyplot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));
		xyplot.setDomainCrosshairVisible(true);
		xyplot.setRangeCrosshairVisible(true);
		// 获取Renderer，设置曲线参数
		XYSplineRenderer renderer = new XYSplineRenderer(); 
		renderer.setBaseShapesVisible(false); // 绘制的线条上不显示点
		renderer.setPrecision(5); // 设置精度，将对曲线进行插值拟合，该值表示插值的点数
		renderer.setSeriesStroke(0, new BasicStroke(3f));
		renderer.setSeriesPaint(0, new Color(48,128,20));
		renderer.setSeriesStroke(1, new BasicStroke(1f));
		renderer.setSeriesPaint(1,  Color.GREEN);
		renderer.setSeriesStroke(2, new BasicStroke(3f));
		renderer.setSeriesPaint(2, new Color(255,97,3));
		renderer.setSeriesStroke(3, new BasicStroke(1f));
		renderer.setSeriesPaint(3, Color.YELLOW);
		xyplot.setRenderer(renderer);
		// 返回图表
		return chart;
	}
		
}
