package stormstock.app.analysistest;

import java.util.List;

import stormstock.fw.base.BLog;
import stormstock.fw.base.BThread;
import stormstock.fw.tranbase.stockdata.StockDataIF;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultHistoryData;

public class EStockComplexEatChipCheck {
	public static boolean check(String stockId, List<StockDay> list, int iCheck)
	{
		boolean bCheck = false;
		
		// 至少60交易日
		int iBegin = iCheck-60;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return bCheck;
		}
				
		StockDay cCurStockDay = list.get(iCheck);
		BLog.output("TEST", "cCurStockDay %s\n", cCurStockDay.date());
		
		float fWave = EStockDayPriceWave.checkPriceAveWave(list, iCheck);
		float fVolAve = EStockDayVolumeWave.get(list, iCheck);
		BLog.output("TEST", "fWave %.4f fVolAve %.1f\n", fWave, fVolAve);
		
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
		
		String stockID = "300217"; // 300217 300227
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
				
				boolean bCheck = EStockComplexEatChipCheck.check(stockID, list, i);
				if (bCheck)
				{
					BLog.output("TEST", "### CheckPoint %s\n", cCurStockDay.date());
					s_StockDayListCurve.markCurveIndex(i, "D");
					//i=i+2;
				}
			}
        } 
		
		s_StockDayListCurve.generateImage();
		BLog.output("TEST", "Main End\n");
	}
	public static StockDayListCurve s_StockDayListCurve = new StockDayListCurve("EStockComplexEatChipCheck.jpg");

}
