package stormstock.app.progtran;

import stormstock.fw.base.BLog;
import stormstock.fw.tranengine.TranEngine;
import stormstock.fw.tranengine.TranEngine.TRANACCOUNTTYPE;
import stormstock.fw.tranengine.TranEngine.TRANTIMEMODE;

public class Application {

	public static void main(String[] args) {
		BLog.output("TEST", "--->>> MainBegin\n");
		
		TranEngine cTranEngine = new TranEngine();
		
		cTranEngine.setStockSet(new Strategy_D1.EStockComplexDZCZX.TranStockSet());
		cTranEngine.setSelectStockStrategy(new Strategy_D1.EStockComplexDZCZX.StrategySelect());
		cTranEngine.setCreatePositonStrategy(new Strategy_D1.EStockComplexDZCZX.StrategyCreate());
		cTranEngine.setClearPositonStrategy(new Strategy_D1.EStockComplexDZCZX.StrategyClear());
		
		cTranEngine.setAccountType(TRANACCOUNTTYPE.REAL); 
		cTranEngine.setTranMode(TRANTIMEMODE.REALTIME);
		
//		cTranEngine.setAccountType(TRANACCOUNTTYPE.MOCK); 
//		cTranEngine.setTranMode(TRANTIMEMODE.HISTORYMOCK);
//		cTranEngine.setHistoryTimeSpan("2017-03-29", "2017-04-04");
		

		cTranEngine.run();
		
		cTranEngine.mainLoop();
		BLog.output("TEST", "--->>> MainEnd\n");
		//BLog.config_output();
	}
}