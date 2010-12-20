package flower.topology;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import flower.topology.structure.*;
import flower.util.SNMPUtil;

/**
 * 进行拓扑发现的类
 * @author 徐海航
 * @author 郑旭东
 */
public class TopDiscover {

	/**
	 * 进行拓扑发现的方法
	 * @param source 网关地址
	 * @return 路由器信息列表
	 */
    public static List<Router> DiscoveryRouters(String source) {
        
    	int count = 0;
    	int i = 0;
    	
        //以第一个入口路由器为起点进行拓扑发现,使用全局意义的IP，不要用回环地址127.0.0.1什么的。
    	String descr = SNMPUtil.getRouterDescr(source);
    	if (descr == null) return null;
    	
    	List<Router> routerList = new ArrayList<Router>();
    	Router curRouter = new Router(count++, source, descr);
        routerList.add(curRouter);

        // 下面是遍历路由器表，对每个路由器取路由表，获得相邻路由器信息，附加到路由器表尾部
        while (i < routerList.size()) {
            /******************************找相邻那个啥路由器的***************************************/
            /**有两种方式：
             * 一：把拓扑看成双向图，每个路由都有完整的连接信息。
             * 利于刻画单个路由器，更符合路由类的直观定义，但是在拓扑显示的时候需要判断。
             * 二：把拓扑看成单根分层有序图，每个路由只包含与在其广度遍历排序后的路由器之间的连接信息，不符合直观。
             * 这样在判断的时候只需要判断后面的路由器，而且便于拓扑显示。
             * 
             * 我选择第二种方式，第一是效率有些提高，而是方便之后的拓扑显示
             */
            curRouter = routerList.get(i);
            
            List<RouteItem> routeTable = SNMPUtil.getIpRouteTable(curRouter.getAdminIP());
            
            Map<Integer, Link> crMap = curRouter.getLinkMap();
            
            for (RouteItem ri : routeTable) {//通过每一条表项试图发现路由器连接信息，及子网信息
                String nextHop = ri.getNextHop();
                int ifIndex = ri.getIfIndex();
                if (ifIndex <= 0 && curRouter.getOtherIpToIfMap().containsKey(nextHop)) 
                	ifIndex = curRouter.getOtherIpToIfMap().get(nextHop);
                Interface inf = curRouter.getInterfaceMap().get(ifIndex);
                switch (ri.getRouteType()) {
                    case 3: { // direct表示这里的目的地址是直连的子网或者主机地址
                        //将目的网段存为子网，然后将地址转换表的中涉及到的Ip分配到网段中
                        String netMask = ri.getNetMask();
                        if (!netMask.equals("255.255.255.255") && !ri.getDestination().equals("0.0.0.0")) {//全1表示一个地址，其他表示子网。
                            Subnet subnet = new Subnet(ri.getDestination(), netMask);//子网连接有接口标志，
                            if (inf != null) {
                            	inf.setSubnet(subnet);
                            	if (inf.getConType() == -1) inf.setConType(0);
                            }
                        }
                    } // case 3 结束
                    break;

                    case 4: { // indirect表示下一跳是邻接路由器
                    	if (nextHop.equals("0.0.0.0")) {
                            break;
                        }
                        int index = curRouter.rankAtIfIndex(nextHop);
                        if (index == -1) {//非回环路由，排除本机地址
                        	// 路由器判重
                            int k = 0;
                            while (k < count) {
                                index = routerList.get(k).rankAtIfIndex(nextHop);
                                if (index != -1) {
                                    break;
                                }
                                k++;
                            }
                            
                            int curRouterID = curRouter.getRouterID();
                            if (index > 0) { // 属于某个已探测到的路由器
                            	int nextHopID = k;
                            	Router adjRouter = routerList.get(nextHopID);
                            	int adjIfIndex = adjRouter.rankAtIfIndex(nextHop);
                            	// 创建或更新从当前路由器到下一跳的链路
                            	if (!crMap.containsKey(nextHopID)) { // 如果Map中还没有这一条链接，就先插入一项
                            		crMap.put(nextHopID, new Link(nextHopID));
                            	}
                            	crMap.get(nextHopID).setDstIfIndex(adjIfIndex); // 一定可以更新目的接口信息
                            	if (inf != null) { // 如果当前路由表中存有接口信息，还可以补充源接口信息
                            		crMap.get(nextHopID).setSrcIfIndex(inf.getIndex());
                            	}
                            	// 创建或更新从下一跳到当前路由器的链路
                                Map<Integer, Link> adjCrMap = adjRouter.getLinkMap();
                                if (!adjCrMap.containsKey(curRouterID)) { // 如果Map中还没有这一条链接，就先插入一项
                                	adjCrMap.put(curRouterID, new Link(curRouterID));
                            	}
                                adjCrMap.get(curRouterID).setSrcIfIndex(adjIfIndex); // 一定可以更新源接口信息
                                if (inf != null) { // 如果当前路由表中存有接口信息，还可以补充目的接口信息
                                	adjCrMap.get(curRouterID).setDstIfIndex(inf.getIndex());
                                }
                            } else { // 不是已有的路由器
                        		descr = SNMPUtil.getRouterDescr(nextHop);
                        		if (descr != null) { // 有响应，需要构造新的路由器
		                        	// 构造新的路由器
		                        	Router adjRouter = new Router(count, nextHop, descr);
		                        	routerList.add(adjRouter);
		                        	int nextHopID = count++;
		                            // 构造连接关系
		                        	int adjIfIndex = adjRouter.rankAtIfIndex(nextHop);
		                        	// 创建或更新从当前路由器到下一跳的链路
		                        	crMap.put(nextHopID, new Link(nextHopID));
		                        	crMap.get(nextHopID).setDstIfIndex(adjIfIndex); // 一定可以更新目的接口信息
		                        	if (inf != null) { // 如果当前路由表中存有接口信息，还可以补充源接口信息
		                        		crMap.get(nextHopID).setSrcIfIndex(inf.getIndex());
		                        	}
		                        	// 创建或更新从下一跳到当前路由器的链路
		                            Map<Integer, Link> adjCrMap = adjRouter.getLinkMap();
		                            adjCrMap.put(curRouterID, new Link(curRouterID));
		                            adjCrMap.get(curRouterID).setSrcIfIndex(adjIfIndex); // 一定可以更新源接口信息
		                            if (inf != null) { // 如果当前路由表中存有接口信息，还可以补充目的接口信息
		                            	adjCrMap.get(curRouterID).setDstIfIndex(inf.getIndex());
		                            }
                        		} else { // 无响应，看作边界路由接口
                        			if (inf != null) {
                        				inf.setConType(2);
                        			}
                        		}
                            }
                        }                     
                    } // case 4 结束
                    break;

                    default:
                        break;
                }

            }
            i++;
        }
        
        for (Router router : routerList) {
            router.assignLink();
            router.assignDevices();
        }
        
        return routerList;

    }

}
