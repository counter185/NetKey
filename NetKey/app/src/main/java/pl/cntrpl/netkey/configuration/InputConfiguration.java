package pl.cntrpl.netkey.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputConfiguration {
    public List<List<Integer>> configuration;

    public InputConfiguration(){
        configuration = new ArrayList<List<Integer>>();
    }

    public int NRows(){
        return configuration.size();
    }

    public int NCols(int row){
        return configuration.get(row).size();
    }

    public int GetInputAt(int row, int col){
        return configuration.get(row).get(col);
    }

    public void SetInputAt(int row, int col, int value) {
        configuration.get(row).set(col, value);
    }

    public boolean NewColAt(int row) {
        if (NCols(row) < 100){
            configuration.get(row).add(1);
            return true;
        }
        return false;
    }

    public boolean RemoveLastColAt(int row) {
        if (NCols(row) > 1){
            configuration.get(row).remove(NCols(row)-1);
            return true;
        }
        return false;
    }

    public boolean NewRow(){
        configuration.add(new ArrayList<Integer>(Arrays.asList(1,1,1,1)));
        return true;
    }

    public boolean RemoveLastRow(){
        if (NRows() > 1) {
            configuration.remove(NRows()-1);
            return true;
        }
        return false;
    }
}
