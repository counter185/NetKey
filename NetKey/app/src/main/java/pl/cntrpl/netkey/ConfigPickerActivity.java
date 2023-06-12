package pl.cntrpl.netkey;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.zip.Inflater;

public class ConfigPickerActivity extends Activity {

    public boolean isSavePrompt = false;

    public void passBackPath(String path){
        Intent intent = new Intent();
        intent.putExtra("path", path);
        setResult(RESULT_OK, intent);
        finish();
    }

    public View newListElement(String path, String name, LinearLayout attachTo){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.config_list_element, null);

        TextView cname = v.findViewById(R.id.configname);
        TextView cpath = v.findViewById(R.id.filepath);

        cname.setText(name);
        cpath.setText(path);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passBackPath(path);
            }
        });

        attachTo.addView(v);
        return v;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_picker);
        isSavePrompt = getIntent().getBooleanExtra("saveprompt", false);

        LinearLayout configlist = findViewById(R.id.config_item_list);

        EditText txt = ((EditText)findViewById(R.id.fileedittext));
        ((Button)findViewById(R.id.filenameconfirmbtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!txt.getText().toString().equals("")){
                    passBackPath(new File(ConfigFilesIO.baseConfigFilesPath, txt.getText() + ".netkeyconf").toString());
                }
            }
        });

        if (!isSavePrompt){
            ((LinearLayout)findViewById(R.id.filesavebar)).removeAllViews();
        }

        List<File> files = ConfigFilesIO.getAllConfigFiles();
        for (File a : files){
            newListElement(a.getPath(), a.getName(), configlist);
        }
    }
}