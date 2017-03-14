package stormstock.app.analysistest;

import java.util.List;

import stormstock.fw.base.BLog;
import stormstock.fw.base.BThread;
import stormstock.fw.tranbase.stockdata.StockDataIF;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockUtils;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultHistoryData;

/**
 * 中阴线尾盘急跌下穿重要支撑位后再次收复操作法
 * @author wudi
 *
 */
public class EStockComplexDXP {
	
	// 检查该天是否是中阴线下穿重要支撑位
	public static boolean checkCrossImpSup(String stockId, List<StockDay> list, int iCheck)
	{
		boolean bCheck = false;
		
		// 检查日判断(至少要有一定的交易天数)
		int iBegin = iCheck-20;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return bCheck;
		}
				
		
		StockDay cCurStockDay = list.get(iCheck);
		String curDate = list.get(iCheck).date();
		float fAveWave = EStockDayPriceWaveThreshold.get(list, iCheck);
		
		// 当天中阴线
		int iBefore = iCheck-1;
		if(iBefore<0) return bCheck;
		StockDay cStockDayB1 = list.get(iBefore);
		float dieFu = (cCurStockDay.close() - cStockDayB1.close())/cStockDayB1.close();
		float curEntityRatio = (cCurStockDay.close()-cCurStockDay.open())/cCurStockDay.open();
		if(dieFu < fAveWave && curEntityRatio<fAveWave*0.8f)
		{
			
		}
		else
		{
			return bCheck;
		}
		
		// 创新近期新低
		int iLowJin = StockUtils.indexLow(list, iCheck-10, iCheck);
		if(iLowJin==iCheck)
		{
		}
		else
		{
			return bCheck;
		}
		
		// 下穿重要支撑位
		float fMA60 = StockUtils.GetMA(list, 60, iCheck);
		float fMA20 = StockUtils.GetMA(list, 20, iCheck);
		if(
			(cCurStockDay.open()>fMA60 && cCurStockDay.close() < fMA60)
			|| (cCurStockDay.open()>fMA20 && cCurStockDay.close() < fMA20)
			)
		{
			
		}
		else
		{
			return false;
		}
		
		bCheck=true;
		return bCheck;
	}
	
	public static void main(String[] args)
	{
		BLog.output("TEST", "Main Begin\n");
		StockDataIF cStockDataIF = new StockDataIF();
		
		String stockID = "002478"; // 002478
		ResultHistoryData cResultHistoryData = 
				cStockDataIF.getHistoryData(stockID, "2016-01-01", "2017-01-01");
		List<StockDay> list = cResultHistoryData.resultList;
		BLog.output("TEST", "Check stockID(%s) list size(%d)\n", stockID, list.size());
		
		s_StockDayListCurve.setCurve(list);
		
		for(int i = 0; i < list.size(); i++)  
        {  
			StockDay cCurStockDay = list.get(i);
	
			if(cCurStockDay.date().equals("2016-07-27"))
			{
				BThread.sleep(1);
				

			}
			boolean bCheck = EStockComplexDXP.checkCrossImpSup(stockID,list,i);
			if(bCheck)
			{
				BLog.output("TEST", "### CheckPoint %s\n", 
						cCurStockDay.date());
				s_StockDayListCurve.markCurveIndex(i, "S");
			}
        } 
		
		s_StockDayListCurve.generateImage();
		BLog.output("TEST", "Main End\n");
	}
	public static StockDayListCurve s_StockDayListCurve = new StockDayListCurve("EStockComplexDXP.jpg");
}
