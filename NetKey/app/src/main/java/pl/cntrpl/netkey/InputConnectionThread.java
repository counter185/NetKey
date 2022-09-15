package pl.cntrpl.netkey;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class InputConnectionThread extends Thread{

    public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    public InputActivity caller;
    public DatagramSocket socket;
    public InputConnectionThread(InputActivity c){
        caller = c;
    }

    public InetAddress address;

    public void WriteOut(DatagramSocket clientOut){
        byte[] buf = new byte[512];
        DatagramPacket udpPacket = new DatagramPacket(buf, 512, address, caller.port);
        try {
            System.arraycopy(intToByteArray(caller.customInputs.size()), 0, buf, 0, 4);
            //clientOut.writeInt(caller.customInputs.size());
            //Log.d("netinputthread","Sending size: " + caller.customInputs.size());
            int i = 0;
            for (CustomInput a : caller.customInputs){
                //Log.d("netinputthread","Sending state");
                System.arraycopy(intToByteArray(a.GetState()), 0, buf, 4+i*4, 4);
                //clientOut.writeInt(a.GetState());
                i++;
            }
            socket.send(udpPacket);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        while (!caller.isDestroyed()) {
            try {
                socket = new DatagramSocket(caller.port);
                address = InetAddress.getByName(caller.ip);
                if (!caller.paused) {
                    while (true) {
                        if (caller.isDestroyed() || caller.paused) {
                            System.out.println("goobye");
                            socket.close();
                            break;
                        }
                        WriteOut(socket);
                        Thread.sleep(ConfigActivity.pollRate);
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            System.out.println("Reconnecting");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Log.e("threadinput", "end");
    }
}
