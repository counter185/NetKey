package pl.cntrpl.netkey;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigActivity extends Activity {

    public List<List<Boolean>> inputs = new ArrayList<List<Boolean>>();

    public void CreateRows(){

        TextView rowText = findViewById(R.id.textRows);
        rowText.setText("Rows: " + inputs.size());

        LinearLayout colLayout = findViewById(R.id.allRows);
        colLayout.removeAllViewsInLayout();

        for (int x = 0; x != inputs.size(); x++){
            TextView txt = new TextView(this);
            txt.setText("Row " + (x+1));
            colLayout.addView(txt);

            LinearLayout nLayout = new LinearLayout(this);
            nLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            nLayout.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams ltParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ltParams.weight = 1;
            for (int y = 0; y != inputs.get(x).size(); y++){
                Button nBtn = new Button(this);
                nBtn.setLayoutParams(ltParams);
                int a = x;
                int b = y;
                nBtn.setText(!inputs.get(a).get(b) ? "OFF" : "ON");
                nBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean currentState = inputs.get(a).get(b);
                        inputs.get(a).set(b, !currentState);
                        nBtn.setText(!currentState ? "ON" : "OFF");
                    }
                });
                nLayout.addView(nBtn);
            }
            Button nPlusBtn = new Button(this);
            Button nMinusBtn = new Button(this);
            nPlusBtn.setLayoutParams(ltParams);
            nMinusBtn.setLayoutParams(ltParams);
            nPlusBtn.setText("+");
            nMinusBtn.setText("-");
            int a = x;
            nPlusBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (inputs.get(a).size() != 100){
                        inputs.get(a).add(true);
                        CreateRows();
                    }
                }
            });
            nMinusBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (inputs.get(a).size() != 1){
                        inputs.get(a).remove(inputs.get(a).size()-1);
                        CreateRows();
                    }
                }
            });
            nLayout.addView(nPlusBtn);
            nLayout.addView(nMinusBtn);

            colLayout.addView(nLayout);

        }
    }

    public static List<CustomInput> customInputs = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        inputs.add(new ArrayList<Boolean>(Arrays.asList(true,true,true,true)));

        ((Button)findViewById(R.id.buttonRowsUp)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputs.add(new ArrayList<Boolean>(Arrays.asList(true,true,true,true)));
                CreateRows();
            }
        });
        ((Button)findViewById(R.id.buttonRowsDown)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputs.size() != 1){
                    inputs.remove(inputs.size()-1);
                    CreateRows();
                }
            }
        });

        Context bruh = this;
        ((Button)findViewById(R.id.buttonConnect)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent switchActivityIntent = new Intent(bruh, InputActivity.class);
                Bundle bndl = new Bundle();
                bndl.putString("ipaddr", ((EditText)findViewById(R.id.editTextIP)).getText().toString());
                bndl.putString("port", ((EditText)findViewById(R.id.editTextPort)).getText().toString());
                switchActivityIntent.putExtra("pl.cntrpl.netkey", bndl);
                customInputs = new ArrayList<CustomInput>();
                for (int x = 0; x != inputs.size(); x++){
                    for (int y = 0; y != inputs.get(x).size(); y++) {
                        if (inputs.get(x).get(y)) {
                            customInputs.add(new InputButton(inputs.get(x).size()*x+ y, inputs.get(x).size(), inputs.size()));
                        }
                    }
                }
                startActivity(switchActivityIntent);
            }
        });

        CreateRows();
    }
}
