package pl.cntrpl.netkey;

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
}
