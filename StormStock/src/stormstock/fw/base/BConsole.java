package stormstock.fw.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BConsole extends BThread {
	@Override
	public void run() {
		// TODO Auto-generated method stub
		//BLog.output("TEST", "BConsole Run\n");
		while(!checkQuit())
		{
			//BLog.output("TEST", "readDataFromConsole...\n");
			String cmd = readDataFromConsole();
			//BLog.output("TEST", "readDataFromConsole:%s\n", cmd);
			command(cmd);
		}
		//BLog.output("TEST", "BConsole Run exit\n");
	} 
	
	private String readDataFromConsole() {  
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  
        String str = null;  
        try {  
            str = br.readLine();  

        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return str;  
    }
	
	// implement by user
	public void command(String cmd) {}
}
