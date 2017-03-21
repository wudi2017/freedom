package stormstock.app.analysistest;

import java.util.List;

import stormstock.fw.base.BLog;
import stormstock.fw.base.BThread;
import stormstock.fw.tranbase.stockdata.StockDataIF;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockUtils;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultHistoryData;

public class EDILunDongParam {
	public static float getLunDongVal(List<StockDay> list, int iCheck)
	{
		float retVal = 0.0f;
		
		int iBegin = iCheck-250;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return 0.0f;
		}
		
		StockDay cCurStockDayB1 = list.get(iEnd-1);
		StockDay cCurStockDay = list.get(iEnd);
		if(cCurStockDay.close() - cCurStockDayB1.close() > 0)
		{
			
		}
		else
		{
			return 0.0f;
		}
		
		int iIndex250H = StockUtils.indexHigh(list, iEnd-250, iEnd);
		StockDay cStockDay250H = list.get(iIndex250H);
		float fDrop250 = (cCurStockDay.close() - cStockDay250H.close())/cStockDay250H.close();
		
		int iIndex60H = StockUtils.indexHigh(list, iEnd-60, iEnd);
		StockDay cStockDay60H = list.get(iIndex60H);
		float fDrop60 = (cCurStockDay.close() - cStockDay60H.close())/cStockDay60H.close();
		
		retVal = fDrop250 + fDrop60*1.3f;
		return retVal;
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
		
		String stockID = "002425"; // 002425 000246
		ResultHistoryData cResultHistoryData = 
				cStockDataIF.getHistoryData(stockID);
		List<StockDay> list = cResultHistoryData.resultList;
		BLog.output("TEST", "Check stockID(%s) list size(%d)\n", stockID, list.size());
		
		s_StockDayListCurve.setCurve(list);
		
		for(int i = 0; i < list.size(); i++)  
        {  
			StockDay cCurStockDay = list.get(i);
	
			if(cCurStockDay.date().equals("2016-08-01"))
			{
				BThread.sleep(1);
				
				
				float fVal = EDILunDongParam.getLunDongVal(list, i);
				BLog.output("TEST", "### CheckPoint %s fVal %.3f\n", 
						cCurStockDay.date(), 
						fVal);
			}
			
        } 
		
		s_StockDayListCurve.generateImage();
		BLog.output("TEST", "Main End\n");
	}
	public static StockDayListCurve s_StockDayListCurve = new StockDayListCurve("EDILunDongParam.jpg");
}
