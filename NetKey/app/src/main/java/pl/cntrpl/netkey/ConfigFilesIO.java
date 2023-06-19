package pl.cntrpl.netkey;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import pl.cntrpl.netkey.configuration.InputConfiguration;

public class ConfigFilesIO {

    public static File baseConfigFilesPath;

    public static List<File> getAllConfigFiles(){
        File[] files = baseConfigFilesPath.listFiles();
        List<File> ret = new ArrayList<>();
        for (File a : files){
            if (a.isFile() && a.getName().endsWith(".netkeyconf")){
                ret.add(a);
            }
        }
        return ret;
    }

    public static boolean WriteConfigToFile(File target, InputConfiguration config) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(target))) {
            for (List<Integer> sublist : config.configuration) {
                for (int num : sublist) {
                    writer.print(num + " ");
                }
                writer.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //i have no idea if the code below or above works. i just couldn't be bothered with this
    //so i let chatgpt write this.
    //sorry.

    public static InputConfiguration ReadConfigFromFile(File target){
        List<List<Integer>> integerList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(target))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] numbers = line.trim().split("\\s+");
                List<Integer> sublist = new ArrayList<>();
                for (String number : numbers) {
                    sublist.add(Integer.parseInt(number));
                }
                integerList.add(sublist);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return new InputConfiguration(integerList);
    }

    public static void SaveLastAppSettings(ConfigActivity configActivity, File target){
        try {
            FileWriter writer = new FileWriter(target, false);
            writer.write("[NetKeyConfig]\n");
            writer.write("IP="+((EditText)configActivity.findViewById(R.id.editTextIP)).getText().toString()+"\n");
            writer.write("port="+((EditText)configActivity.findViewById(R.id.editTextPort)).getText().toString()+"\n");
            writer.write("pollRate="+((SeekBar)configActivity.findViewById(R.id.sliderPollRate)).getProgress()+"\n");
            writer.write("repeatButtons="+((CheckBox)configActivity.findViewById(R.id.checkBoxRepeat)).isChecked()+"\n");
            writer.close();

        } catch (IOException e) {
            Toast.makeText(configActivity, "Unable to save app settings!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void ReadLastAppSettings(ConfigActivity configActivity, File file) {
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("IP=")) {
                        ((EditText) configActivity.findViewById(R.id.editTextIP)).setText(line.substring(line.indexOf('=') + 1));
                    } else if (line.startsWith("port=")) {
                        ((EditText) configActivity.findViewById(R.id.editTextPort)).setText(line.substring(line.indexOf('=') + 1));
                    } else if (line.startsWith("pollRate=")) {
                        int val = Integer.parseInt(line.substring(line.indexOf('=') + 1));
                        ((SeekBar) configActivity.findViewById(R.id.sliderPollRate)).setProgress(val);
                        ((TextView) configActivity.findViewById(R.id.sliderPollRateText)).setText((1000/(17-val)) + "hz");
                    } else if (line.startsWith("repeatButtons=")) {
                        ((CheckBox) configActivity.findViewById(R.id.checkBoxRepeat)).setChecked(line.substring(line.indexOf('=') + 1).equals("true"));
                    }
                }
            } catch (Exception e) {
                Toast.makeText(configActivity, "Unable to read app settings!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
