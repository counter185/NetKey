package pl.cntrpl.netkey.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collector;

import pl.cntrpl.netkey.configuration.ConfigFilesIO;
import pl.cntrpl.netkey.R;
import pl.cntrpl.netkey.configuration.InputConfiguration;
import pl.cntrpl.netkey.thread.ThreadConnectToServer;
import pl.cntrpl.netkey.thread.LANDiscoverThread;

public class ConfigActivity extends Activity {

    final int CONFIG_SAVE = 3000;
    final int CONFIG_LOAD = 3001;
    LANDiscoverThread lanThread = null;

    public InputConfiguration inputs = new InputConfiguration();

    public File configDir;
    public File appSettingsFile;

    public void CreateRows(){

        TextView rowText = findViewById(R.id.textRows);
        rowText.setText("Rows: " + inputs.NRows());

        LinearLayout colLayout = findViewById(R.id.allRows);
        colLayout.removeAllViewsInLayout();

        final float scale = getResources().getDisplayMetrics().density;

        for (int x = 0; x != inputs.NRows(); x++){
            //TextView txt = new TextView(this);
            //txt.setText("Row " + (x+1));
            //colLayout.addView(txt);

            LinearLayout nLayout = new LinearLayout(this);
            nLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            nLayout.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams ltParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams ltParams2 = new LinearLayout.LayoutParams((int)(100*scale*0.5f), LinearLayout.LayoutParams.WRAP_CONTENT);
            ltParams.weight = 1;
            for (int y = 0; y != inputs.NCols(x); y++){
                Button nBtn = new Button(this);
                nBtn.setLayoutParams(ltParams);
                int a = x;
                int b = y;
                int st = inputs.GetInputAt(a,b);
                nBtn.setText(st == 0 ? "OFF"
                        : st == 1 ? "BTN"
                        : st == 2 ? "SLIDER"
                        : "???");
                nBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int currentState = inputs.GetInputAt(a,b);
                        currentState++;
                        currentState%=3;
                        inputs.SetInputAt(a, b, currentState);
                        nBtn.setText(currentState == 0 ? "OFF"
                                : currentState == 1 ? "BTN"
                                : currentState == 2 ? "SLIDER"
                                : "???");
                    }
                });
                nLayout.addView(nBtn);
            }
            Button nPlusBtn = new Button(this);
            Button nMinusBtn = new Button(this);
            nPlusBtn.setLayoutParams(ltParams2);
            nMinusBtn.setLayoutParams(ltParams2);
            nPlusBtn.setText("+");
            nMinusBtn.setText("-");
            int a = x;
            nPlusBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (inputs.NewColAt(a)){
                        CreateRows();
                    }
                }
            });
            nMinusBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (inputs.RemoveLastColAt(a)){
                        CreateRows();
                    }
                }
            });
            nLayout.addView(nPlusBtn);
            nLayout.addView(nMinusBtn);

            colLayout.addView(nLayout);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigActivity bruh = this;
        setContentView(R.layout.activity_config);

        appSettingsFile = new File(getExternalFilesDir(null), "/appsettings.ini");
        configDir = new File(getExternalFilesDir(null), "/input_configurations/");
        if (!configDir.exists()){
            configDir.mkdir();
        }
        ConfigFilesIO.baseConfigFilesPath = configDir;
        ConfigFilesIO.ReadLastAppSettings(this, appSettingsFile);

        inputs.NewRow();

        findViewById(R.id.buttonOpenNetworkSettings).setOnClickListener(view -> {
            final Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity( intent);
        });
        findViewById(R.id.buttonShowNetworks).setOnClickListener(view -> {
            new Thread(() -> {
                String networkAddresses = "";
                try {
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                            .hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        //if (intf.getName().contains("wlan")) {
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                                .hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress()
                                    && (inetAddress.getAddress().length == 4)) {
                                networkAddresses += intf.getName() + ": " + inetAddress.getHostAddress() + "\n";
                                Log.d("net", inetAddress.getHostAddress());
                                //return inetAddress.getHostAddress();
                            }
                        }
                        //}
                    }
                } catch (SocketException ex) {
                    Log.e("net", ex.toString());
                }
                final String netAddr2 = networkAddresses;
                new Handler(Looper.getMainLooper()).post(()-> {
                    Toast.makeText(bruh, netAddr2, Toast.LENGTH_LONG).show();
                });
            }).start();
        });

        findViewById(R.id.buttonRowsUp).setOnClickListener(view -> {
            inputs.NewRow();
            CreateRows();
        });
        findViewById(R.id.buttonRowsDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputs.RemoveLastRow()){
                    CreateRows();
                }
            }
        });

        findViewById(R.id.buttonPreviewConf).setOnClickListener((v)->{
            Intent switchActivityIntent = new Intent(bruh, InputActivity.class);
            Bundle bndl = new Bundle();
            bndl.putBoolean("preview", true);
            bndl.putParcelable("inputconf", inputs);
            switchActivityIntent.putExtra("pl.cntrpl.netkey", bndl);
            startActivity(switchActivityIntent);
        });
        findViewById(R.id.buttonConnect).setOnClickListener(view -> {

            ConfigFilesIO.SaveLastAppSettings(bruh, appSettingsFile);

            AlertDialog.Builder builder = new AlertDialog.Builder(bruh);

            builder.setMessage("Connecting to " + ((EditText)findViewById(R.id.editTextIP)).getText().toString())
                    .setTitle("Connecting...");
            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.show();

            new ThreadConnectToServer(
                    ((EditText)findViewById(R.id.editTextIP)).getText().toString(),
                    ((EditText)findViewById(R.id.editTextPort)).getText().toString(),
                    inputs,
                    dialog,
                    bruh,
                    ((CheckBox)findViewById(R.id.checkBoxRepeat)).isChecked(),
                    17-((SeekBar)findViewById(R.id.sliderPollRate)).getProgress()
            ).start();
        });

        ((SeekBar)findViewById(R.id.sliderPollRate)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                ((TextView)findViewById(R.id.sliderPollRateText)).setText((1000/(17-i)) + "hz");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        findViewById(R.id.buttonSaveConfFile).setOnClickListener(view -> {
            Intent i = new Intent(bruh, ConfigPickerActivity.class);
            i.putExtra("saveprompt", true);
            startActivityForResult(i, CONFIG_SAVE);
        });
        findViewById(R.id.buttonLoadConfFile).setOnClickListener(view -> {
            Intent i = new Intent(bruh, ConfigPickerActivity.class);
            i.putExtra("saveprompt", false);
            startActivityForResult(i, CONFIG_LOAD);
        });

        CreateRows();

        lanThread = new LANDiscoverThread(this);
        lanThread.start();
    }

    @Override
    protected void onDestroy() {
        if (lanThread != null) {
            lanThread.interrupt();
        }
        super.onDestroy();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONFIG_SAVE) {
            if(resultCode == RESULT_OK) {
                String strPath = data.getStringExtra("path");
                System.out.println("Taken path: " + strPath);
                ConfigFilesIO.WriteConfigToFile(new File(strPath), inputs);
            }
        }
        else if (requestCode == CONFIG_LOAD) {
            if(resultCode == RESULT_OK) {
                String strPath = data.getStringExtra("path");
                inputs = ConfigFilesIO.ReadConfigFromFile(new File(strPath));
                CreateRows();
            }
        }
    }

    List<String> lanServers = new ArrayList<>();
    public void lanServerFound(String address, int port) {
        new Handler(Looper.getMainLooper()).post(() -> {
            String connStr = address + ":" + port;
            if (!lanServers.contains(connStr)) {
                lanServers.add(connStr);
                LinearLayout addTarget = findViewById(R.id.lanServers);
                Button btn = new Button(this);
                btn.setText(connStr);
                btn.setOnClickListener((v) -> {
                    ((EditText)findViewById(R.id.editTextIP)).setText(address);
                    ((EditText)findViewById(R.id.editTextPort)).setText(Integer.toString(port));
                });
                addTarget.addView(btn);
            }
        });
    }
}
