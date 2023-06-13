package pl.cntrpl.netkey;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import pl.cntrpl.netkey.input.CustomInput;

public class InputConnectionThread extends Thread{

    public static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)value,
                (byte)(value >>> 8),
                (byte)(value >>> 16),
                (byte)(value >>> 24) };
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
            System.arraycopy(intToByteArray(caller.connectionID), 0, buf, 0, 4);
            System.arraycopy(intToByteArray(caller.customInputs.size()), 0, buf, 4, 4);
            //clientOut.writeInt(caller.customInputs.size());
            //Log.d("netinputthread","Sending size: " + caller.customInputs.size());
            int i = 0;
            for (CustomInput a : caller.customInputs){
                //Log.d("netinputthread","Sending state");
                System.arraycopy(intToByteArray(a.GetState()), 0, buf, 8+i*4, 4);
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
                        Thread.sleep(caller.pollRate);
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
