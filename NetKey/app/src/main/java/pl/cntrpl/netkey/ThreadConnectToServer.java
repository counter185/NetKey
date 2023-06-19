package pl.cntrpl.netkey;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import pl.cntrpl.netkey.configuration.InputConfiguration;

public class ThreadConnectToServer extends Thread{

    public String ip;
    public String port;
    public InputConfiguration conf;
    public AlertDialog awaitDialog;
    public Context caller;
    public boolean repeatButtons;
    public int pollRate;

    public int assignedID = 0;

    public ThreadConnectToServer(String ip, String port, InputConfiguration inputConfig, AlertDialog toHide, Context caller, boolean repeat, int pollRate){
        this.ip = ip;
        this.port = port;
        this.conf = inputConfig;
        this.awaitDialog = toHide;
        this.caller = caller;
        this.repeatButtons = repeat;
        this.pollRate = pollRate;
    }

    public void ToastOnMainThread(String text){
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(caller, text, Toast.LENGTH_SHORT).show());
    }

    public void closeConnectionDialog(){
        new Handler(Looper.getMainLooper()).post(() -> { awaitDialog.hide(); });
    }

    public void error(String message) {

        closeConnectionDialog();

        new Handler(Looper.getMainLooper()).post(() -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(caller);

            builder.setMessage("Unable to connect.\nError: " + message)
                    .setTitle("Connection failed")
                    .setNeutralButton("Back", (a,b)->{a.cancel();});
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }



    @Override
    public void run() {
        try {
            short buttonindex = 0;
            List<Byte> inputListToSend = new ArrayList<Byte>();
            for (List<Integer> row : conf.configuration){
                for (int col : row){
                    if (col != 0){
                        inputListToSend.add((byte)buttonindex++);
                        inputListToSend.add((byte)col);
                    }
                }
            }

            Socket tcpSocket = new Socket(ip, Integer.parseInt(port));
            OutputStream out = tcpSocket.getOutputStream();
            InputStream in = tcpSocket.getInputStream();
            out.write(new byte[] {0x4e, 0x45, 0x54, 0x4b, 0x45, 0x59});
            int sz = inputListToSend.size()/2;
            out.write(new byte[] {(byte)(sz&0xff), (byte)((sz>>8)&0xff)} );
            for (byte a : inputListToSend){
                out.write(a);
            }

            if (in.read() == 0x39) {
                assignedID += in.read();
                assignedID += in.read() << 8;
                assignedID += in.read() << 16;
                assignedID += in.read() << 24;
            } else {
                throw new IOException("Server is busy");
            }

            ToastOnMainThread("Connection successful. ID: " + assignedID);

            tcpSocket.close();

            new Handler(Looper.getMainLooper()).post(() -> {
                Intent switchActivityIntent = new Intent(caller, InputActivity.class);
                Bundle bndl = new Bundle();
                bndl.putString("ipaddr", ip);
                bndl.putString("port", port);
                bndl.putInt("pollrate", pollRate);
                bndl.putParcelable("inputconf", conf);
                bndl.putInt("connid", assignedID);
                bndl.putBoolean("repeatbtn", repeatButtons);
                switchActivityIntent.putExtra("pl.cntrpl.netkey", bndl);
                caller.startActivity(switchActivityIntent);
            });

            closeConnectionDialog();

        } catch (Exception e) {
            error(e.getClass().getSimpleName() + "\n\n  " + e.getMessage());
        }

    }
}
