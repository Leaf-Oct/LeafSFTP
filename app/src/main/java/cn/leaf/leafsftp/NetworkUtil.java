package cn.leaf.leafsftp;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public class NetworkUtil {
    public static ArrayList<String> getAllAddress() {
        final ArrayList<String> result = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            ;
            while (true) {
                NetworkInterface networkInterface;
                try {
                    networkInterface=networkInterfaces.nextElement();
                } catch (NoSuchElementException e){
                    break;
                }
                if(networkInterface==null){
                    break;
                }
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();

                try {

                    while (true){
                        InetAddress inetAddress;
                        try {
                            inetAddress=inetAddresses.nextElement();
                        } catch (NoSuchElementException e){
                            break;
                        }
                        if(inetAddress==null){
                            break;
                        }
                        if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                        String ip = inetAddress.getHostAddress();
                        if (!result.contains(ip)) result.add(ip);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(result.isEmpty()){
            result.add("127.0.0.1");
        }
        return result;
    }
}
