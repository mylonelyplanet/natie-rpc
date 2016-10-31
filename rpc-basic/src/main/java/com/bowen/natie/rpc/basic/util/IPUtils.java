package com.bowen.natie.rpc.basic.util;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mylonelyplanet on 16/7/24.
 */

public class IPUtils {
    private static Logger logger = LoggerFactory.getLogger(IPUtils.class);

    private static final String LOCALHOST = "127.0.0.1";

    private static InetAddress localInetAddress;
    private static String localHostAddress = "127.0.0.1";

    private static ConcurrentMap<String, InetAddress> inetAddressCache = Maps.newConcurrentMap();
    static {
        try {
            List<InetAddress> listAdr = getAllLocalIPs();
            if(listAdr != null && listAdr.size() > 0){
                localInetAddress = listAdr.get(0);
            }else {
                localInetAddress = InetAddress.getLocalHost();
            }
            localHostAddress =localInetAddress.getHostAddress();

        } catch (UnknownHostException e) {
            logger.error("InetAddress.getLocalHost error.", e);
        } catch (SocketException e) {
            logger.error("InetAddress.getLocalHost error.", e);
        } catch (Throwable e) {
            logger.error("[init IpUtils error][please configure hostname or bond0 or eth0]", e);
        }
    }

    public static InetAddress getAddresses(String ip) {
        InetAddress result = inetAddressCache.get(ip);
        if (result == null) {
            try {
                result = InetAddress.getByName(ip);
            } catch (UnknownHostException e) {
                logger.error("InetAddress.getByName(+ip+) error.", e);
                result = localInetAddress;
            }
            inetAddressCache.put(ip, result);
        }

        return result;
    }

    public static String localIp4Str() {
        return localHostAddress;
    }


    public static InetAddress localIp() {
        return localInetAddress;
    }

    public static String getHostName(){
        return localInetAddress.getHostName();
    }

    /**
     * 分析字符串，返回InetAddress,如果不存在则返回本地地址
     */
    public static InetAddress getInetAddress(String hostName) {
        InetAddress ipAddr = null;
        try {
            ipAddr = hostName != null ? getAddresses(hostName) : IPUtils.localIp();
        } catch (Exception e) {
            logger.warn("hostName format is worng: " + hostName, e);
            ipAddr = IPUtils.localIp();
        }
        return ipAddr;
    }

    /**
     * 分析字符串，去除字符串中的端口号，转换localhost地址
     */
    public static String getHostName(String hostString) {
        String hostName = null;
        if (hostString != null && hostString.length() > 0) {
            int index = hostString.indexOf(':');
            if (index != -1) {
                hostName = hostString.substring(0, index);
            } else {
                hostName = hostString;
            }

            if (LOCALHOST.equals(hostName)) {
                return IPUtils.localIp4Str();
            }
        }

        return hostName;
    }

    /**
     * 分析字符串，返回HostAddress与Port，转换localhost地址
     */
    public static HostAndPort getHostAndPort(String hostString) {
        HostAndPort hostAndPort = new HostAndPort();

        if (hostString == null || hostString.length() == 0) {
            return hostAndPort;
        }

        int index = hostString.indexOf(':');
        if (index != -1) {
            hostAndPort.hostAddress = hostString.substring(0, index);
            hostAndPort.port = Integer.parseInt(hostString.substring(index + 1));
        } else {
            hostAndPort.hostAddress = hostString;
        }


        index = hostAndPort.hostAddress.indexOf('/');
        if (index != -1) {
            hostAndPort.hostAddress = hostAndPort.hostAddress.substring(index + 1, hostAndPort.hostAddress.length());
        }

        //replace 127.0.0.1 to machine local address
        if (LOCALHOST.equals(hostAndPort.hostAddress)) {
            hostAndPort.hostAddress = IPUtils.localIp4Str();
        }

        return hostAndPort;
    }

    /***
     * true:already in using false:not using
     */
    public static boolean isLocalPortUsing(int port) {
        try {
            return isPortUsing(LOCALHOST, port);
        } catch (Exception e) {// NOSONAR
            logger.info("local host port {} doesn't use", port);
            return false;
        }
    }

    /***
     * true:already in using false:not using
     */
    public static boolean isPortUsing(String host, int port) throws UnknownHostException {
        InetAddress theAddress = InetAddress.getByName(host);
        try {
            Socket socket = new Socket(theAddress, port);
            RpcIOs.closeQuietly(socket);
            return true;
        } catch (Exception e) {// NOSONAR
            logger.info(host + ':' + port + " doesn't use");
        }
        return false;
    }

    public static class HostAndPort {
        public String hostAddress;
        public int port = -1;

        @Override
        public String toString() {
            return hostAddress + ":" + port;
        }
    }

    /**
     * based on http://pastebin.com/5X073pUc
     * <p>
     *
     * Returns all available IP addresses.
     * <p>
     * In error case or if no network connection is established, we return
     * an empty list here.
     * <p>
     * Loopback addresses are excluded - so 127.0.0.1 will not be never
     * returned.
     * <p>
     * The "primary" IP might not be the first one in the returned list.
     *
     * @return  Returns all IP addresses (can be an empty list in error case
     *          or if network connection is missing).
     * @since   0.1.0
     * @throws SocketException errors
     */
    public static List<InetAddress> getAllLocalIPs() throws SocketException
    {
        List<InetAddress> listAdr = Lists.newArrayList();
        Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
        if (nifs == null) return listAdr;

        while (nifs.hasMoreElements())
        {
            NetworkInterface nif = nifs.nextElement();
            Enumeration<InetAddress> adrs = nif.getInetAddresses();
            while ( adrs.hasMoreElements() )
            {
                InetAddress adr = adrs.nextElement();
                if ( localIpFilter(nif, adr) )
                {
                    listAdr.add(adr);
                }
            }
        }
        return listAdr;
    }

    public static boolean localIpFilter(NetworkInterface nif, InetAddress adr) throws SocketException
    {
        return (adr != null) && !adr.isLoopbackAddress() && (nif.isPointToPoint() || !adr.isLinkLocalAddress()) && !nif.getName().contains("net") && !nif.getDisplayName().contains("Virtual");

    }

    public static void main(String[] args) throws Exception{

        System.out.println(IPUtils.localIp4Str());
        System.out.println(getHostName());
    }
}