package flower.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.PDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

import flower.topology.TopDiscover;
import flower.topology.structure.IPToMacItem;
import flower.topology.structure.Interface;
import flower.topology.structure.RouteItem;

/**
 * 专门用于SNMP获取MIB信息的工具类
 * @author 官祥飞
 */
public class SNMPUtil {

	private static int port = 161;
    private static String community = "public";
    private static int version = SnmpConstants.version2c;
	private static int maxRowPerPDU = 100;
	private static long timeout = 5000;
	private static Snmp snmp;
    
    public static CommunityTarget udpTarget(String source, int port, String community, int version) {
        try {
            CommunityTarget target = new CommunityTarget();
            UdpAddress udpAddress = new UdpAddress(InetAddress.getByName(source), port);
            target.setAddress(udpAddress);
            target.setCommunity(new OctetString(community));
            target.setVersion(version);
            target.setTimeout(timeout);
            return target;
        } catch (UnknownHostException ex) {
            Logger.getLogger(TopDiscover.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

	public static CommunityTarget udpTarget(String source) {
        return udpTarget(source, port, community, version);
    }
	
    /**
     * 获取路由器的描述
     * @param adminIP 路由器IP
     * @return 路由器的描述
     */
	public static String getRouterDescr(String adminIP) {
		if (adminIP == null) return null;

		CommunityTarget target = udpTarget(adminIP, port, community, version);
        OID oid_sysObjectID = new OID(".1.3.6.1.2.1.1.1.0");	// sysDescr;
    	PDU pdu = new PDU();
    	//add by gxf
    	pdu.setType(PDU.GET);
        pdu.add(new VariableBinding(oid_sysObjectID));
        try {
        	//add by gxf
        	ResponseEvent re = snmp.send(pdu, target);
			pdu = re.getResponse();
			if (pdu == null) return null;
			@SuppressWarnings("unchecked")
			Vector<VariableBinding> vbs = pdu.getVariableBindings();
			return vbs.get(0).getVariable().toString();
		} catch (IOException ex) {
			Logger.getLogger(SNMPUtil.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
	
	/**
	 * 获取ifTable中的部分字段
	 * @param adminIP 路由器IP
	 * @return 以ifIndex为键，接口信息Interface为值的Map
	 */
	public static Map<Integer, Interface> getIfTable(String adminIP) {
		if (adminIP == null) return null;
	
		CommunityTarget target = udpTarget(adminIP, port, community, version);
		Map<Integer, Interface> infMap = new HashMap<Integer, Interface>();
		try {
			PDUFactory pF = new DefaultPDUFactory(PDU.GETBULK);
			TableUtils tableUtils = new TableUtils(snmp, pF);
			tableUtils.setMaxNumRowsPerPDU(maxRowPerPDU);
	
			OID[] columns = new OID[]{
					OIDUtil.getOIDByDesc("ifIndex"),
					OIDUtil.getOIDByDesc("ifType"),
					OIDUtil.getOIDByDesc("ifIndex"),
					OIDUtil.getOIDByDesc("ifSpeed"),
					OIDUtil.getOIDByDesc("ifPhysAddress")};
	
			@SuppressWarnings("unchecked")
			List<TableEvent> list = tableUtils.getTable(target, columns, null, null);
			for (TableEvent ev : list) {
				VariableBinding[] vbs = ev.getColumns();
				if (ev.getStatus() != -1) {
					int index = vbs[0].getVariable().toInt();
					int type = vbs[1].getVariable().toInt();
					String descr = vbs[2].getVariable().toString();
					long speed = vbs[3].getVariable().toLong();
					String physAddress = vbs[4].getVariable().toString();
					Interface inf = new Interface(
							index, type, descr, speed, physAddress);                    
	                infMap.put(index, inf);
				} else {
					System.out.println("[" + new Date() + "]WARNING: SNMP get some ipTable item failed at the " + adminIP + "!");
				}
			}
		} catch (Exception e) {
        	System.out.println("[" + new Date() + "]WARNING: SNMP get all ipTable item failed at the " + adminIP + "!");
			e.printStackTrace();
		}
		return infMap;
	}
	
	/**
	 * 获取路由地址表（ipAddrTable）的部分字段
	 * @param adminIP 路由器IP
	 * @return 存储IP地址信息的Vector，Vector中的每一项由一个三维数组构成，分析存储ipAdEntAddr,ipAdIfIndex,ipAdNetMask
	 */
	public static List<Object[]> getIpAddrTable(String adminIP) {
        if (adminIP == null) return null;
        
		CommunityTarget target = udpTarget(adminIP, port, community, version);
		Vector<Object[]> ipList = new Vector<Object[]>();
        
		try {
			PDUFactory pF = new DefaultPDUFactory(PDU.GETBULK);
			TableUtils tableUtils = new TableUtils(snmp, pF);
			tableUtils.setMaxNumRowsPerPDU(maxRowPerPDU);
			
			OID[] columns = new OID[]{
					new OID(".1.3.6.1.2.1.4.20.1.1"),		// ipAdEntAddr
					new OID(".1.3.6.1.2.1.4.20.1.2"),		// ipAdIfIndex
					new OID(".1.3.6.1.2.1.4.20.1.3")};		// ipAdNetMask
        
			@SuppressWarnings("unchecked")
			List<TableEvent> list = tableUtils.getTable(target, columns, null, null);
			for(TableEvent ev : list) {
				VariableBinding[] vbs = ev.getColumns();
				if (ev.getStatus() != -1) {
					String addr = vbs[0].getVariable().toString();
	                int ifIndex = vbs[1].getVariable().toInt();
	                String netMask = vbs[2].getVariable().toString();
	                ipList.add(new Object[]{addr, ifIndex, netMask});
				} else {
					System.out.println("[" + new Date() + "]WARNING: SNMP get some ipAddrTable item failed at the " + adminIP + "!");
				}
            }
        } catch (Exception e) {
        	System.out.println("[" + new Date() + "]WARNING: SNMP get all ipAddrTable item failed at the " + adminIP + "!");
			e.printStackTrace();
        }
        return ipList;
	}
	
	/**
	 * 获取路由表的部分字段
	 * @param adminIP 路由器IP
	 * @return 存储路由表信息RouteItem的Vector
	 */
	public static List<RouteItem> getIpRouteTable(String adminIP) {
        if (adminIP == null) return null;
        
		CommunityTarget target = udpTarget(adminIP, port, community, version);
        List<RouteItem> routeList = new ArrayList<RouteItem>();
        
		try {
			PDUFactory pF = new DefaultPDUFactory(PDU.GETBULK);
			TableUtils tableUtils = new TableUtils(snmp, pF);
			tableUtils.setMaxNumRowsPerPDU(maxRowPerPDU);
			
			OID[] columns = new OID[]{
					new OID("1.3.6.1.2.1.4.21.1.1"),		// 目的地址ipRouteDest
					new OID("1.3.6.1.2.1.4.21.1.2"),		// 接口索引ipRouteIfIndex
					new OID("1.3.6.1.2.1.4.21.1.7"),		// 下一跳地址ipRouteNextHop
					new OID("1.3.6.1.2.1.4.21.1.8"),		// 路由类型ipRouteType
					new OID("1.3.6.1.2.1.4.21.1.11")};		// 子网掩码ipRouteMask
        
			@SuppressWarnings("unchecked")
			List<TableEvent> list = tableUtils.getTable(target, columns, null, null);
			for(TableEvent ev : list) {
				VariableBinding[] vbs = ev.getColumns();
				if (ev.getStatus() != -1) {
					String dest = vbs[0].getVariable().toString();
	                int ifIndex = vbs[1].getVariable().toInt();
	                String nextHop = vbs[2].getVariable().toString();
	                int type = vbs[3].getVariable().toInt();
	                String mask = vbs[4].getVariable().toString();
	                RouteItem ri = new RouteItem(dest, ifIndex, nextHop, type, mask);
	                routeList.add(ri);
				} else {
					System.out.println("[" + new Date() + "]WARNING: SNMP get some ipRouteTable item failed at the " + adminIP + "!");
				}
            }
        } catch (Exception e) {
        	System.out.println("[" + new Date() + "]WARNING: SNMP get all ipRouteTable item failed at the " + adminIP + "!");
			e.printStackTrace();
        }
        return routeList;
	}
	
	/**
	 * 获取IP-MAC转换表
	 * @param adminIP 路由器IP
	 * @return 存储IP-MAC转换信息IPToMacItem的Vector
	 */
	public static List<IPToMacItem> getIpNetToMediaTable(String adminIP) {
		if (adminIP == null) return null;


		CommunityTarget target = udpTarget(adminIP, port, community, version);
		List<IPToMacItem> ipmList = new ArrayList<IPToMacItem>();
        
		try {
			PDUFactory pF = new DefaultPDUFactory(PDU.GETBULK);
			TableUtils tableUtils = new TableUtils(snmp, pF);
			tableUtils.setMaxNumRowsPerPDU(maxRowPerPDU);
			
			OID[] columns = new OID[]{
					new OID("1.3.6.1.2.1.4.22.1.1"),	// ipNetToMediaIfIndex
					new OID("1.3.6.1.2.1.4.22.1.3")};	// ipNetToMediaNetAddress
        
			@SuppressWarnings("unchecked")
			List<TableEvent> list = tableUtils.getTable(target, columns, null, null);
			for(TableEvent ev : list) {
				VariableBinding[] vbs = ev.getColumns();
				if (ev.getStatus() != -1) {
					int ifIndex = vbs[0].getVariable().toInt();
					String netAddress = vbs[1].getVariable().toString();
					IPToMacItem ipm = new IPToMacItem(ifIndex, netAddress);
					ipmList.add(ipm);
				} else {
					System.out.println("[" + new Date() + "]WARNING: SNMP get some ipNetToMediaTable item failed at the " + adminIP + "!");
				}
            }
        } catch (Exception e) {
			System.out.println("[" + new Date() + "]WARNING: SNMP all ipNetToMediaTable item failed at the " + adminIP + "!");
			e.printStackTrace();
        }
        return ipmList;
	}
	
	public static List<Object[]> getIfFlow(String adminIP) {
		if (adminIP == null) return null;
		
		CommunityTarget target = udpTarget(adminIP, port, community, version);
		Vector<Object[]> ifFlowList = new Vector<Object[]>();
		try {
			PDUFactory pF = new DefaultPDUFactory(PDU.GETBULK);
			TableUtils tableUtils = new TableUtils(snmp, pF);
			tableUtils.setMaxNumRowsPerPDU(maxRowPerPDU);
	
			OID[] columns = new OID[]{
					OIDUtil.getOIDByDesc("ifOperStatus"),
					OIDUtil.getOIDByDesc("ifIndex"),
					OIDUtil.getOIDByDesc("ifInOctets"),
					OIDUtil.getOIDByDesc("ifInUcastPkts"),
					OIDUtil.getOIDByDesc("ifInDiscards"),
					OIDUtil.getOIDByDesc("ifInErrors"),
					OIDUtil.getOIDByDesc("ifOutOctets"),
					OIDUtil.getOIDByDesc("ifOutUcastPkts"),
					OIDUtil.getOIDByDesc("ifOutDiscards"),
					OIDUtil.getOIDByDesc("ifOutErrors")};
	
			@SuppressWarnings("unchecked")
			List<TableEvent> list = tableUtils.getTable(target, columns, null, null);
			for (TableEvent ev : list) {
				VariableBinding[] vbs = ev.getColumns();
				if (ev.getStatus() != -1) {
					if (vbs[0] != null && vbs[0].getVariable().toInt() == 1) { // 仅保存状态为up的接口数据
						Object[] flow = new Object[9];
						flow[0] = vbs[1].getVariable().toInt();
						boolean nullFlag = false;
						for (int i = 1; i < 9; i++) {
							if (vbs[i+1] == null) {
								nullFlag = true;
								break;
							}
							flow[i] = vbs[i+1].getVariable().toLong();
						}
						if (nullFlag) continue;
						ifFlowList.add(flow);
					}
				} else {
					System.out.println("[" + new Date() + "]WARNING: SNMP get some interfaces flow failed at the " + adminIP + "!");
				}
			}
		} catch (Exception e) {
			System.out.println("[" + new Date() + "]WARNING: SNMP get all interfaces flow failed at the " + adminIP + "!");
			e.printStackTrace();
		}
		return ifFlowList;
	}
	
	public static boolean snmpListen() {
		try {
			snmp = new Snmp(new DefaultUdpTransportMapping());
			snmp.listen();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean snmpClose() {
		try {
			snmp.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
    public static int getPort() {
		return port;
	}

	public static void setPort(int port) {
		SNMPUtil.port = port;
	}

	public static String getCommunity() {
		return community;
	}

	public static void setCommunity(String community) {
		SNMPUtil.community = community;
	}

	public static int getVersion() {
		return version;
	}

	public static void setVersion(int version) {
		SNMPUtil.version = version;
	}

	public static long getTimeout() {
		return timeout;
	}

	public static void setTimeout(long timeout) {
		SNMPUtil.timeout = timeout;
	}    	
}
