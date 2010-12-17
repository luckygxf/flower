package flower;

import java.util.List;
import java.util.Timer;

import flower.flow.LanFlowMon;
import flower.topology.TopDiscover;
import flower.topology.structure.Router;
import flower.util.DatabaseWorker;
import flower.util.SNMPUtil;

/**
 * 主程序
 * @author 郑旭东
 * @author 易建龙
 */
public class Flower {

	private static String mode; // 运行模式，i表示初次运行，将探测拓扑，c表示继续运行，直接开始监测流量
	private static String community; // SNMP协议使用的团体名
	private static String ip; // 网关路由器的IP地址
	private static int port; // SNMP协议使用的端口
	private static long interval; // 流量监测的时间间隔
	private static long timeout; // 流量监测的时间间隔
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// 判断参数形式是否合法
			if (args.length != 6 || (!"i".equals(args[0]) && !"c".equals(args[0]))) 
				throw new Exception();
			// 输入参数
			mode = args[0]; // 运行模式，i表示初次运行，将探测拓扑，c表示继续运行，直接开始监测流量
			community = args[1]; // SNMP协议使用的团体名
			ip = args[2]; // 网关路由器的IP地址
			port = Integer.parseInt(args[3]); // SNMP协议使用的端口
			interval = Long.valueOf(args[4]); // 流量监测的时间间隔
			timeout = Long.valueOf(args[5]); // 流量监测的时间间隔
		} catch (Exception e) {
			System.err.println("Parameters error!\n" + 
					"Usage: java -jar *.jar i|c COMMUNITY IP PORT INTERVAL TIMEOUT\n" +
					"Example: java -jar *.jar i public 192.168.30.1 161 30000 5000");
			System.exit(1);
		}
		
		// 设置参数
		SNMPUtil.setCommunity(community);
		SNMPUtil.setPort(port);
		SNMPUtil.setTimeout(timeout);
		LanFlowMon lfm = new LanFlowMon(interval);
		
		// 准备监视
		SNMPUtil.snmpListen();
		DatabaseWorker.connect();
		if ("i".equals(mode)) { // 判断运行模式
			// 拓扑发现
			List<Router> routerList = TopDiscover.DiscoveryRouters(ip);
			for (Router router : routerList) {
				router.print();
			}
			// 初始化拓扑信息
			lfm.init(routerList);
			System.out.println("Router Discory Completed!");
		} else {
			lfm.init();
		}
		// 开始监视
		System.out.println("Flow Monitoring Started!");
		Timer timer = new Timer();
		timer.schedule(lfm, 0, interval);
		//lfm.run();
		// 结束
		//SNMPUtil.snmpClose();
		//DatabaseWorker.release();
	}

}
