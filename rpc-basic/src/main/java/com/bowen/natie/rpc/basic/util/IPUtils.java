package com.bowen.natie.rpc.basic.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.bowen.natie.rpc.basic.exception.RpcErrorCode;
import com.bowen.natie.rpc.basic.exception.RpcException;
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
    private static String localHostAddressPrefix = "127.0.";

    private static ConcurrentMap<Integer, String> integer2IPV4Map = PlatformDependent.newConcurrentHashMap();
    private static ConcurrentMap<String, Integer> IPV42IntegerMap = com.bowen.natie.rpc.basic.util.PlatformDependent.newConcurrentHashMap();
    private static ConcurrentMap<String, InetAddress> inetAddressCache = PlatformDependent.newConcurrentHashMap();
    static {
        try {
            localInetAddress = InetAddress.getLocalHost();

            if (localInetAddress.getHostAddress() == null || LOCALHOST.equals(localInetAddress.getHostAddress())) {
                NetworkInterface ni = NetworkInterface.getByName("bond0");
                if (ni == null) {
                    ni = NetworkInterface.getByName("eth0");
                }
                if (ni == null) {
                    throw new RpcException(RpcErrorCode.GENERAL_EXCEPTION,
                            " fail to get ip address, could not read any info from local host,bond0 and eth0");
                }

                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    InetAddress nextElement = ips.nextElement();
                    if (LOCALHOST.equals(nextElement.getHostAddress()) || nextElement instanceof Inet6Address
                            || nextElement.getHostAddress().contains(":")) {
                        continue;
                    }
                    localInetAddress = nextElement;
                }
            }

            setHostAddress(localInetAddress.getHostAddress());

        } catch (UnknownHostException e) {
            logger.error("InetAddress.getLocalHost error.", e);
        } catch (SocketException e) {
            logger.error("InetAddress.getLocalHost error.", e);
        } catch (RpcException e) {
            logger.error("[init IpUtils error][please configure hostname or bond0 or eth0]", e);
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

    private static void setHostAddress(String address) {
        IPUtils.localHostAddress = address;
        List<String> hostAddressIpDigitals = RpcStrings.split(address, '.', 4);
        IPUtils.localHostAddressPrefix = hostAddressIpDigitals.get(0) + '.' + hostAddressIpDigitals.get(1) + '.';
    }

    public static String integer2IPV4(Integer iIPV4) {
        if (iIPV4 == null || iIPV4 == 0) {
            return null;
        }

        String result = integer2IPV4Map.get(iIPV4);
        if (result != null) {
            return result;
        }

        StringBuilder sb = PlatformDependent.stringBuilder();
        sb.append(0xff & (iIPV4 >> 24)).append('.').append(0xff & (iIPV4 >> 16)).append('.').append(0xff & (iIPV4 >> 8))
                .append('.').append(0xff & (iIPV4));
        result = sb.toString();

        integer2IPV4Map.put(iIPV4, result);
        return result;
    }

    public static Integer IPV42Integer(String strIPV4) {
        if (strIPV4 == null) {
            return null;
        }

        Integer result = IPV42IntegerMap.get(strIPV4);
        if (result != null) {
            return result;
        }
        List<String> it = RpcStrings.split(strIPV4, '.', 4);
        int tempInt;
        byte[] byteAddress = new byte[4];
        for (int i = 0; i < 4; i++) {
            tempInt = Integer.parseInt(it.get(i));
            byteAddress[i] = (byte) tempInt;
        }

        result = ((byteAddress[0] & 0xff) << 24) | ((byteAddress[1] & 0xff) << 16) | ((byteAddress[2] & 0xff) << 8)
                | (byteAddress[3] & 0xff);
        IPV42IntegerMap.put(strIPV4, result);
        return result;
    }

    public static String localIp4Str() {
        return localHostAddress;
    }

    public static String localIp4Prefix() {
        return localHostAddressPrefix;
    }

    public static InetAddress localIp() {
        return localInetAddress;
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


    public static String getHostAddress(SocketAddress socketAddress) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        return inetSocketAddress.getAddress().getHostAddress() + ':' + inetSocketAddress.getPort();
    }

    @Deprecated
    public static String getHostName(SocketAddress socketAddress) {
        if (socketAddress != null) {
            return getHostName(socketAddress.toString());
        }

        return null;
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

}