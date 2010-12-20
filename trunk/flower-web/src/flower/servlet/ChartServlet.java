package flower.servlet;

import java.io.IOException;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import flower.chart.FlowTimeSeries;

/**
 * 响应Web客户端画图请求的Servlet
 * @author 郑旭东
 *
 */
@SuppressWarnings("serial")
public class ChartServlet extends HttpServlet { 

	public void service(ServletRequest req, ServletResponse res) 
			throws ServletException, IOException {
		// 获取参数
		String ifID = req.getParameter("ifID");
		Integer conType = req.getParameter("conType") != null ? 
				Integer.parseInt(req.getParameter("conType")) : null;
		Timestamp sta =req.getParameter("sta") != null ?
				Timestamp.valueOf(req.getParameter("sta")) : null;
		Timestamp end =req.getParameter("end") != null ?
				Timestamp.valueOf(req.getParameter("end")) : null;
		// 绘图
		JFreeChart chart = null;
		if (ifID != null) {
			if (sta != null && end != null) {
				chart = FlowTimeSeries.drawByIfID(ifID, sta, end);
			} else {
				chart = FlowTimeSeries.drawByIfID(ifID);
			}
		} else if (conType != null) {
			if (sta != null && end != null) {
				chart = FlowTimeSeries.drawByConType(conType, sta, end);
			} else {
				chart = FlowTimeSeries.drawByConType(conType);
			}
		}
		// 直接返回图片类型的响应
		res.setContentType("image/jpeg");
		ChartUtilities.writeChartAsJPEG(res.getOutputStream(), 1, chart, 1024, 768, null); 
	}
	
}