package com.bowen.natie.rpc.basic.entity;

import com.google.common.collect.Lists;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by mylonelyplanet on 16/7/24.
 * POJO that represents a service instance
 */
public class ServerInfo<T> {

    private final String    name;
    private final String    id;
    private final String    address;
    private final Integer   port;
    private final T         payload;
    private final long      registrationTimeUTC;






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
    public static Collection<InetAddress> getAllLocalIPs() throws SocketException
    {
        List<InetAddress> listAdr = Lists.newArrayList();
        Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
        if (nifs == null) return listAdr;

        while (nifs.hasMoreElements())
        {
            NetworkInterface nif = nifs.nextElement();
            // We ignore subinterfaces - as not yet needed.

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
        return (adr != null) && !adr.isLoopbackAddress() && (nif.isPointToPoint() || !adr.isLinkLocalAddress());

    }

}
