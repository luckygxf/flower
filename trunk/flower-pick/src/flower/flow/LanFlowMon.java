package flower.flow;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.snmp4j.CommunityTarget;
import org.snmp4j.Snmp;

import flower.topology.structure.Interface;
import flower.topology.structure.Link;
import flower.topology.structure.Router;
import flower.util.DatabaseWorker;
import flower.util.OIDUtil;
import flower.util.SNMPUtil;

/**
 * 表示流量监视器的类
 * @author 易建龙
 * @author 郑旭东
 */
public class LanFlowMon extends TimerTask {
	
	Snmp snmp;
	CommunityTarget comTarget; 
	long interval = 30000;
	OIDUtil oidUtil = new OIDUtil();
	List<String> routerIPs;

	public LanFlowMon(Long interval) {
		if (interval != null) this.interval = interval;
	}	
	
	//@Override
	public void run() {
		
//		while(true) {
			Timestamp time = new Timestamp(System.currentTimeMillis());
			for (int i = 0; i < routerIPs.size(); i++) {
				String ip = routerIPs.get(i);
				List<Object[]> flowList = SNMPUtil.getIfFlow(ip);				
				storeFlow(i, flowList, time);
			}
//			try {
//				Thread.sleep(interval);
//			}catch (Exception e) { 
//			}
//		}
	}
	
	private void storeFlow(int routerID, List<Object[]> flowList, Timestamp time) {
		for (Object[] item : flowList) {
			int index = (Integer) item[0];
			long inOctets = (Long) item[1];
			long inUcastPkts = (Long) item[2];
			long inDiscards = (Long) item[3];
			long inErrors = (Long) item[4];
			long outOctets = (Long) item[5];
			long outUcastPkts = (Long) item[6];
			long outDiscards = (Long) item[7];
			long outErrors = (Long) item[8];
			String ifID = routerID + "." + index;
			DatabaseWorker.insert("INSERT INTO Flows Values(\"" + 
					time + "\",\"" + ifID + "\"," + 
					inOctets + "," + inUcastPkts + "," + inDiscards + "," + inErrors + "," + 
					outOctets + "," + outUcastPkts + "," + outDiscards + "," + outErrors + ")");
		}
	}

	public void setInterval(long newVal){
		this.interval = newVal;
	}
	
	public void init(List<Router> routerList) {
		// 创建路由器IP列表
		routerIPs = new ArrayList<String>();
		// TODO 判断表是否存在	
		// 清除原有表项
		DatabaseWorker.execute("DELETE FROM Routers");
		DatabaseWorker.execute("DELETE FROM Interfaces");
		DatabaseWorker.execute("DELETE FROM IPs");
		DatabaseWorker.execute("DELETE FROM Flows");
		// 将路由器、接口、子网信息写入数据库，并保存一份路由器IP列表在内存中
		for (Router router : routerList) {
			int rID = router.getRouterID();
			String rIP = router.getAdminIP();
			String rDescr = router.getDescription();
			routerIPs.add(rIP); // 将IP地址插入列表
			// 向数据库写入路由器信息
			DatabaseWorker.insert("INSERT INTO Routers VALUES(" + 
					rID + ",\"" + rIP + "\",\"" + rDescr +"\")");			
			for (Interface inf : router.getInterfaceMap().values()) {
				int ifIndex = inf.getIndex();
				String ifDescr = inf.getDescr();
				long ifSpeed = inf.getSpeed();
				String ifMAC = inf.getPhysAddress();
				String ifIP = inf.getIpAddress();
				String ifMask = inf.getNetMask();
				int ifType = inf.getType();
				int ifConType = inf.getConType();
				String ifLink = null;
				Link link = inf.getLink();
				if (link != null) ifLink = link.getDstRouterID() + "." + link.getDstIfIndex();
				String infID = rID + "." + ifIndex;
				// 向数据库写入接口信息
				DatabaseWorker.insert("INSERT INTO Interfaces VALUES(\"" + 
						infID + "\"," + ifIndex + "," + rID + ",\"" +
						ifDescr + "\"," + ifSpeed + ",\"" + ifMAC + "\",\"" + 
						ifIP + "\",\"" + ifMask + "\"," + ifType + "," + 
						ifConType + ",\"" + ifLink + "\")");
				if (inf.getSubnet() != null) {
					for (String ip : inf.getSubnet().getActiveIP()) {
						// 向数据库写入子网IP关联
						DatabaseWorker.insert("INSERT INTO IPs VALUES(\"" + 
								ip + "\"," + infID + ")");
					}
				}
			}
					
		}
	}
	
	public void init() {
		// 创建路由器IP列表
		routerIPs = new ArrayList<String>();
		// 从数据库中查得路由器IP并添加到列表中
		List<Object[]> ipList = DatabaseWorker.select("SELECT Router_IP FROM Routers");
		for (Object[] item : ipList) {
			routerIPs.add((String)item[0]);
		}		
	}
	
}
