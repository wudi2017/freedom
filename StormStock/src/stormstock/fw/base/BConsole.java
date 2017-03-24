package stormstock.fw.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BConsole {
	public static String readDataFromConsole() {  
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  
        String str = null;  
        try {  
            str = br.readLine();  
  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return str;  
    } 
}
