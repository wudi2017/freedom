package stormstock.app.analysistest;

import java.util.List;

import stormstock.app.analysistest.EDITryPress.EDITryPressResult;
import stormstock.fw.base.BLog;
import stormstock.fw.base.BThread;
import stormstock.fw.tranbase.stockdata.StockDataIF;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultHistoryData;

/**
 * 超跌反弹模型
 * @author wudi
 *
 * 应用于选择60日内跌幅最大企稳的股票
 * 持续持有20日，设定止盈止损，轮动运行。
 */

public class EStockComplexDYCheck {
	
	public static boolean get(List<StockDay> list, int iCheck)
	{
		boolean bCheck = false;
		
		int iBegin = iCheck-1;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return bCheck;
		}
		
		StockDay cStockDay = list.get(iEnd);
		
		bCheck = true;
		return bCheck;
	}
	
	
	/*
	 * ********************************************************************
	 * Test
	 * ********************************************************************
	 */
	public static void main(String[] args)
	{
		BLog.output("TEST", "Main Begin\n");
		StockDataIF cStockDataIF = new StockDataIF();
		
		String stockID = "600213"; // 300217 300227 300163 300165 00
		ResultHistoryData cResultHistoryData = 
				cStockDataIF.getHistoryData(stockID, "2016-01-01", "2017-03-01");
		List<StockDay> list = cResultHistoryData.resultList;
		BLog.output("TEST", "Check stockID(%s) list size(%d)\n", stockID, list.size());
		
		s_StockDayListCurve.setCurve(list);
		
		for(int i = 0; i < list.size(); i++)  
        {  
			StockDay cCurStockDay = list.get(i);
			//BLog.output("TEST", "cCurStockDay %s \n", cCurStockDay.date());

			if(cCurStockDay.date().equals("2016-09-06"))
			{
				BThread.sleep(1);


			}
			
			boolean bCheck = EStockComplexDYCheck.get(list, i);
			if(bCheck)
			{
				BLog.output("TEST", "CheckPoint %s\n", cCurStockDay.date());
			}

        } 
		
		s_StockDayListCurve.generateImage();
		BLog.output("TEST", "Main End\n");
	}
	public static StockDayListCurve s_StockDayListCurve = new StockDayListCurve("EDIStrongUp.jpg");
}
