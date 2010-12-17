package flower;

import java.util.List;

import flower.flow.LanFlowMon;
import flower.topology.TopDiscover;
import flower.topology.structure.Router;

/**
 * 主程序
 * @author 郑旭东
 * @author 易建龙
 */
public class Flower {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// 判断参数形式是否合法
		if (args.length < 4 || (!"i".equals(args[0]) && !"c".equals(args[0]))) {
			System.err.println("Usage: java -jar *.jar " +
					"i|c public 192.168.30.1 161 [interval]");
			System.exit(1);
		}
		
		// 输入参数
		String mode = args[0]; // 运行模式，i表示初次运行，将探测拓扑，c表示继续运行，直接开始监测流量
		String community = args[1]; // SNMP协议使用的团体名
		String ip = args[2]; // 网关路由器的IP地址
		int port = Integer.parseInt(args[3]); // SNMP协议使用的端口
		Long interval = args.length == 5 ? Long.valueOf(args[4]) : null; // 流量监测的时间间隔
		
		// 准备监视
		LanFlowMon lfm = new LanFlowMon(community, ip, port, interval);
		if ("i".equals(mode)) { // 判断运行模式
			// 拓扑发现
			List<Router> routerList = TopDiscover.DiscoveryRouters("192.168.30.1");
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
		lfm.run();
	}

}
