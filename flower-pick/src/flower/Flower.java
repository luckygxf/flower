package flower;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import flower.flow.LanFlowMon;
import flower.topology.TopDiscover;
import flower.topology.structure.Interface;
import flower.topology.structure.RouteItem;
import flower.topology.structure.Router;
import flower.util.DatabaseWorker;
import flower.util.SNMPUtil;

/**
 * 流量采集程序的主类
 * @author 官祥飞
 */
public class Flower {

	private static String mode; // 运行模式，i表示初次运行，将探测拓扑，c表示继续运行，直接开始监测流量
	private static String community; // SNMP协议使用的团体名
	private static String ip; // 网关路由器的IP地址
	private static int port; // SNMP协议使用的端口
	private static long interval; // 流量监测的时间间隔
	private static long timeout; // 流量监测的时间间隔
	
	/**
	 * 主程序
	 * @param args 运行参数
	 */
	public static void main(String[] args) {
//		try {
//			// 判断参数形式是否合法
//			if (args.length != 6 || (!"i".equals(args[0]) && !"c".equals(args[0]))) 
//				throw new Exception();
//			// 输入参数
//			mode = args[0]; // 运行模式，i表示初次运行，将探测拓扑，c表示继续运行，直接开始监测流量
//			community = args[1]; // SNMP协议使用的团体名
//			ip = args[2]; // 网关路由器的IP地址
//			port = Integer.parseInt(args[3]); // SNMP协议使用的端口
//			interval = Long.valueOf(args[4]); // 流量监测的时间间隔
//			timeout = Long.valueOf(args[5]); // 流量监测的时间间隔
//		} catch (Exception e) {
//			System.err.println("Parameters error!\n" + 
//					"Usage: java -jar *.jar i|c COMMUNITY IP PORT INTERVAL TIMEOUT\n" +
//					"Example: java -jar *.jar i public 192.168.30.1 161 30000 5000");
//			System.exit(1);
//		}
		//add by gxf
		mode = "i"; // 运行模式，i表示初次运行，将探测拓扑，c表示继续运行，直接开始监测流量
		community = "public"; // SNMP协议使用的团体名
		ip = "127.0.0.1"; // 网关路由器的IP地址
		port = 161; // SNMP协议使用的端口
		interval = 30000; // 流量监测的时间间隔
		timeout = 5000; // 流量监测的时间间隔
		
		// 设置参数
		SNMPUtil.setCommunity(community);
		SNMPUtil.setPort(port);
		SNMPUtil.setTimeout(timeout);
		LanFlowMon lfm = new LanFlowMon(interval);
		
		// 准备监视
		SNMPUtil.snmpListen();
		
		//add by gxf
		List<RouteItem> listOfRouteItem = SNMPUtil.getIpRouteTable("210.38.235.254");
		System.out.println("size = " + listOfRouteItem.size());
		for(int i = 0; i < listOfRouteItem.size(); i++){
			System.out.println(listOfRouteItem.get(i).getDestination());
		}
		
		
//		DatabaseWorker.connect();
//		if ("i".equals(mode)) { // 判断运行模式
//			// 拓扑发现
//			List<Router> routerList = TopDiscover.DiscoveryRouters(ip);
//			for (Router router : routerList) {
//				router.print();
//			}
//			// 初始化拓扑信息
//			lfm.init(routerList);
//			System.out.println("Router Discory Completed!");
//		} else {
//			lfm.init();
//		}
//		// 定时监视
//		System.out.println("Flow Monitoring Started!");
//		Timer timer = new Timer();
//		timer.schedule(lfm, 0, interval);
	}

}
