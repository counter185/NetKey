package pl.cntrpl.netkey.thread;

import static android.content.Context.WIFI_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import androidx.viewbinding.BuildConfig;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

import pl.cntrpl.netkey.activity.ConfigActivity;

public class LANDiscoverThread extends Thread {
    ConfigActivity caller;

    public LANDiscoverThread(ConfigActivity caller) {
        this.caller = caller;
    }
    
    public boolean ActivityDestroyed(Activity a){
        if (Build.VERSION.SDK_INT >= 17){
            return a.isDestroyed();
        } else {
            return a.isFinishing();
        }
    }
    
    @Override
    public void run() {
        WifiManager wifi = (WifiManager) caller.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiManager.MulticastLock lock = wifi.createMulticastLock("netkey_lan");
        lock.setReferenceCounted(true);
        lock.acquire();
        DatagramSocket socket = null;
        try {

            socket = new DatagramSocket(6603, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);
            byte[] matchString = "NETKEYSERVERDISCOVER".getBytes("ASCII");

            while (!ActivityDestroyed(caller)) {
                try {
                    DatagramPacket p = new DatagramPacket(new byte[20], 20);
                    socket.receive(p);
                    byte[] d = p.getData();
                    if (Arrays.equals(matchString, d)) {

                        InetAddress addr = p.getAddress();
                        caller.lanServerFound(addr.getHostAddress(), 5555);
                        Log.i("netkey", "Found host on lan: " + addr.getHostAddress());
                    }
                } catch (IOException e) {
                }
            }
        }
        catch (SocketException ignored) {}
        catch (UnsupportedEncodingException ignored) {}
        catch (UnknownHostException ignored) {}
        finally {
            lock.release();
            if (socket != null) {
                socket.close();
            }
        }
    }
}
