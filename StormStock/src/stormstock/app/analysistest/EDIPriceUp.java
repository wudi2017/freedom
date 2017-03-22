package stormstock.app.analysistest;

import java.util.List;

import stormstock.fw.base.BLog;
import stormstock.fw.base.BThread;
import stormstock.fw.tranbase.stockdata.StockDataIF;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockUtils;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultHistoryData;

public class EDIPriceUp {
	
	public static class ResultPriceUp
	{
		public ResultPriceUp()
		{
			bCheck = false;
		}
		public boolean bCheck;
		public int iHigh;
		public float fHighPrice;
		public int iLow;
		public float fLowPrice;
	}
	public static ResultPriceUp getPriceUp(List<StockDay> list, int iCheck)
	{
		String curDate = list.get(iCheck).date();
		float fAveWave = EStockDayPriceWaveThreshold.get(list, iCheck);
		//BLog.output("TEST", " (%s) %.4f\n", curDate, fAveWave);
		
		ResultPriceUp cResultPriceUp = new ResultPriceUp();
		
		// 检查日判断(至少要有一定的交易天数)
		int iBegin = iCheck-30;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return cResultPriceUp;
		}
		StockDay cEndStockDay = list.get(iEnd);
		
		// 急跌最高最低判断（当日下挫到短期最高）
		int checkTimes_up = 0;
		int checkTimes_inc = 0;
		int indexCheckLow = 0;
		for(int i = iEnd; i>iBegin+4; i--)
		{
			StockDay cStockDayI = list.get(i);
			StockDay cStockDayB1 = list.get(i-1);
			StockDay cStockDayB2 = list.get(i-2);
			StockDay cStockDayB3 = list.get(i-3);
			
			if(cStockDayI.midle() > cStockDayB3.midle())
			{
				checkTimes_up++;
				checkTimes_inc=0;
			}
			
			if(cStockDayB3.midle() >= cStockDayI.low()
					&& cStockDayB2.midle() >= cStockDayI.low()
					&& cStockDayB1.midle() >= cStockDayI.low())
			{
				indexCheckLow = i;
				break;
			}	
		}
		StockDay cB1StockDay = list.get(iEnd-1);
		StockDay cB2StockDay = list.get(iEnd-2);
		StockDay cLStockDay = list.get(indexCheckLow);
		if(checkTimes_up >= 3 
				&& cEndStockDay.close() > cB1StockDay.close()
				&& cEndStockDay.high() > cB1StockDay.high()
				&& cEndStockDay.close() > cB2StockDay.high()
				)
		{
		}
		else
		{
			return cResultPriceUp;
		}
		//BLog.output("TEST", "(%s) %s\n", curDate, list.get(indexCheckHigh).date());

		
		// 最大跌幅检查（最大跌幅需要符合股性）
		float MaxUpRate = (cEndStockDay.high()-cLStockDay.low())/cLStockDay.low();
		if(MaxUpRate > 1.5*fAveWave)
		{
		}
		else
		{
			return cResultPriceUp;
		}
		
		// 跌速检查(跌速需要大于一定幅度)
		float fUpRate = (cEndStockDay.midle() - cLStockDay.midle())/cLStockDay.midle();
		float fDropAcc = fUpRate/(iEnd-indexCheckLow+1);
		if(fDropAcc>0.01f)
		{
		}
		else
		{
			return cResultPriceUp;
		}
		//BLog.output("TEST", " (%s) fDropAcc %.4f fAveWave %.4f \n", curDate, fDropAcc, fAveWave);
		
		s_StockDayListCurve.clearMark(indexCheckLow);
		s_StockDayListCurve.markCurveIndex(indexCheckLow, "L");
		
		s_StockDayListCurve.clearMark(iEnd);
		s_StockDayListCurve.markCurveIndex(iEnd, "H");
		
		cResultPriceUp.bCheck = true;
		cResultPriceUp.iHigh = StockUtils.indexHigh(list, iEnd-3, iEnd);
		cResultPriceUp.fHighPrice = list.get(cResultPriceUp.iHigh).high();
		cResultPriceUp.iLow = indexCheckLow;
		cResultPriceUp.fLowPrice = list.get(cResultPriceUp.iLow).low();

		return cResultPriceUp;
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
		
		String stockID = "601919"; // 002425 000246
		ResultHistoryData cResultHistoryData = 
				cStockDataIF.getHistoryData(stockID, "2016-01-01", "2017-01-01");
		List<StockDay> list = cResultHistoryData.resultList;
		BLog.output("TEST", "Check stockID(%s) list size(%d)\n", stockID, list.size());
		
		s_StockDayListCurve.setCurve(list);
		
		for(int i = 0; i < list.size(); i++)  
        {  
			StockDay cCurStockDay = list.get(i);
	
			if(cCurStockDay.date().equals("2016-08-01"))
			{
				BThread.sleep(1);

			}
			


			ResultPriceUp cResultPriceDrop = EDIPriceUp.getPriceUp(list, i);
			if (cResultPriceDrop.bCheck)
			{
				BLog.output("TEST", "### CheckPoint %s H(%s %.2f) L(%s %.2f)\n", 
						cCurStockDay.date(), 
						list.get(cResultPriceDrop.iHigh).date(),
						cResultPriceDrop.fHighPrice,
						list.get(cResultPriceDrop.iLow).date(),
						cResultPriceDrop.fLowPrice);
				//s_StockDayListCurve.markCurveIndex(i, "D");
				//i=i+20;
			}
        } 
		
		s_StockDayListCurve.generateImage();
		BLog.output("TEST", "Main End\n");
	}
	public static StockDayListCurve s_StockDayListCurve = new StockDayListCurve("EDIPriceUp.jpg");
}
