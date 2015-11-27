package flower.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.snmp4j.smi.OID;

/**
 * OID名字信息的类
 * @author 官祥飞
 */
public class OIDUtil {

	private static String[][] OIDS = {

			{ "sysDescr", 			"1.3.6.1.2.1.1.1.0" },
			{ "sysName", 			"1.3.6.1.2.1.1.5.0" },
			{ "ifIndex", 			"1.3.6.1.2.1.2.2.1.1" },
			{ "ifDescr", 			"1.3.6.1.2.1.2.2.1.2" },
			{ "ifType", 			"1.3.6.1.2.1.2.2.1.3" },
			{ "ifSpeed", 			"1.3.6.1.2.1.2.2.1.5" },
			{ "ifPhysAddress",		"1.3.6.1.2.1.2.2.1.6" } ,
			{ "sysUpTime", 			"1.3.6.1.2.1.1.3.0" },
			{ "ifAlias", 			"1.3.6.1.2.1.31.1.1.1.18" } ,
			{ "ifOperStatus", 		"1.3.6.1.2.1.2.2.1.8" },
			{ "ifInOctets", 		"1.3.6.1.2.1.2.2.1.10" },
			{ "ifInUcastPkts", 		"1.3.6.1.2.1.2.2.1.11" },
			//{ "ifInNUcastPkts", 	"1.3.6.1.2.1.2.2.1.12" },
			{ "ifInDiscards", 		"1.3.6.1.2.1.2.2.1.13" },
			{ "ifInErrors", 		"1.3.6.1.2.1.2.2.1.14" }, 
			//{ "ifInUnknownProtos", 	"1.3.6.1.2.1.2.2.1.15" },
			{ "ifOutOctets", 		"1.3.6.1.2.1.2.2.1.16" }, 
			{ "ifOutUcastPkts",		"1.3.6.1.2.1.2.2.1.17" },
			//{ "ifOutNUcastPkts", 	"1.3.6.1.2.1.2.2.1.18" },
			{ "ifOutDiscards", 		"1.3.6.1.2.1.2.2.1.19" },
			{ "ifOutErrors", 		"1.3.6.1.2.1.2.2.1.20" },

	};
	
	private static HashMap<String, String> OIDMap = new HashMap<String, String>();
	private static HashMap<String, String> ROIDMap	= new HashMap<String, String>();
	
	static {
		for (String[] KeyValue : OIDS) {
			OIDMap.put(KeyValue[0], KeyValue[1]);
			ROIDMap.put(KeyValue[1], KeyValue[0]);
		}
	}
	
	/**
	 * 得到与端口有关的OID列表
	 * @return 与端口有关的OID列表
	 */
	public static List<String> getInterfaceFlowOIDs() {
		ArrayList<String> al = new ArrayList<String>();
		
		for(String desc : OIDMap.keySet()) {
			if(desc.startsWith("ifIn")||desc.startsWith("ifOut")) {
				al.add(OIDMap.get(desc));
			}
		}  
		return al;
	}
	
	/**
	 * 通过OID字符串获取一个OID的描述
	 * @param num OID字符串，例如1.3.6.1.2.1.1.1.0
	 * @return OID的描述，例如sysDescr
	 */
	public static String getOIDDesc(String num) {
		return ROIDMap.get(num);
	}
	
	/**
	 * 通过OID的描述符获得一个OID对象
	 * @param desc OID描述符 
	 * @return OID对象
	 */
	public static OID getOIDByDesc(String desc) {
		return new OID(OIDMap.get(desc));
	}
	
}
