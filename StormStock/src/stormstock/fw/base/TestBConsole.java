package stormstock.fw.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestBConsole {
	public static class TestConsole extends BConsole 
	{
		public void command(String cmd) 
		{
			BLog.output("TEST", "command:%s\n", cmd);
		}
	}
	public static void main(String[] args) {
		BLog.output("TEST", "Test TestBConsole begin\n");
		
		TestConsole cTestConsole = new TestConsole();
		cTestConsole.startThread();
		

		BThread.sleep(5000);
		
		
		BLog.output("TEST", "Test TestBConsole end\n");
	}
}
