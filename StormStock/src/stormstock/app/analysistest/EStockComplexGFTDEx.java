package stormstock.app.analysistest;

import java.util.List;

import stormstock.app.analysistest.EStockComplexDYCheck.ResultDYCheck;
import stormstock.fw.base.BLog;
import stormstock.fw.base.BThread;
import stormstock.fw.tranbase.stockdata.StockDataIF;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultHistoryData;

public class EStockComplexGFTDEx {
	
	public static class ResultComplexGFTDEx
	{
		public ResultComplexGFTDEx()
		{
		}
	}
	
	public enum GFTDStatus
	{
		SELL_CHECK_START,
		BUY_CHECK_START,
		BUY_SIGNAL,
		SELL_SIGNAL,
		INVALID,
	}
	public static ResultComplexGFTDEx get(String stockId, List<StockDay> list, int iCheck)
	{
		ResultComplexGFTDEx cResultComplexGFTDEx_buy = get_buy(stockId, list, iCheck);
		ResultComplexGFTDEx cResultComplexGFTDEx_sell = get_buy(stockId, list, iCheck);
		
		return cResultComplexGFTDEx_buy;
	}
	public static ResultComplexGFTDEx get_buy(String stockId, List<StockDay> list, int iCheck)
	{
		ResultComplexGFTDEx cResultComplexGFTDEx = new ResultComplexGFTDEx();
		
		int iBegin = iCheck-120;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return cResultComplexGFTDEx;
		}
		
		int N1 = 5;
		int N2 = 3;
		int N3 = 6;
		
		
		GFTDStatus eGFTDStatus = GFTDStatus.INVALID;
		
		int udi_sum = 0;
		int udi_last = 0;
		
		int signal_countor = 0; // 买卖信号计数器
		int signal_last_index = 0;
		
		int i = iBegin + N1;
		StockDay cStockDay_T = list.get(i);
		StockDay cStockDay_Tn1 = list.get(i-N1);
		if(cStockDay_T.close() > cStockDay_Tn1.close())  udi_sum = udi_last = 1;
		if(cStockDay_T.close() < cStockDay_Tn1.close())  udi_sum = udi_last = -1;
		if(cStockDay_T.close() == cStockDay_Tn1.close())  udi_sum = udi_last = 0;
		
		for(i = i+1; i<=iEnd; i++ )
		{
			cStockDay_T = list.get(i);
			cStockDay_Tn1 = list.get(i-N1);
			// udi处理  *******************
			int udi = 0;
			if(cStockDay_T.close() > cStockDay_Tn1.close())  udi = 1;
			if(cStockDay_T.close() < cStockDay_Tn1.close())  udi = -1;
			if(cStockDay_T.close() == cStockDay_Tn1.close())  udi = 0;
			if(udi==udi_last) // udi变化检查
			{
				udi_sum = udi_sum + udi;
			}
			else
			{
				udi_sum = udi;
			}
			udi_last = udi;// 保存udi
			
			// 买入启动判断  *******************
			if((eGFTDStatus == GFTDStatus.INVALID 
					|| eGFTDStatus == GFTDStatus.BUY_CHECK_START) 
					&& udi_sum == -N2)
			{
				eGFTDStatus = GFTDStatus.BUY_CHECK_START; // 买入启动形成
				
				//s_StockDayListCurve.clearMark(i);
				//s_StockDayListCurve.markCurveIndex(i, "BC");
				
				signal_last_index = i;
				signal_countor = 0;
				continue;
			}

			// 买卖信号检查   *******************
			if(eGFTDStatus == GFTDStatus.BUY_CHECK_START)
			{
				StockDay cStockDay_TB1 = list.get(i-1);
				StockDay cStockDay_TB2 = list.get(i-2);
				StockDay cStockDay_Last = list.get(signal_last_index);
				if(cStockDay_T.close() >= cStockDay_TB2.high()
						&& cStockDay_T.high() > cStockDay_TB1.high()
						&& cStockDay_T.close() > cStockDay_Last.close())
				{
					signal_countor++;
					signal_last_index = i;
				}
				
				if(signal_countor == N3)
				{
					eGFTDStatus = GFTDStatus.BUY_SIGNAL;
					
					s_StockDayListCurve.clearMark(i);
					s_StockDayListCurve.markCurveIndex(i, "BS");
					
					signal_countor = 0;
					eGFTDStatus = GFTDStatus.INVALID;
				}
			}

		} // 每天遍历

		return cResultComplexGFTDEx;
	}
	
	public static ResultComplexGFTDEx get_sell(String stockId, List<StockDay> list, int iCheck)
	{
		ResultComplexGFTDEx cResultComplexGFTDEx = new ResultComplexGFTDEx();
		
		int iBegin = iCheck-120;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return cResultComplexGFTDEx;
		}
		
		int N1 = 5;
		int N2 = 3;
		int N3 = 6;
		
		
		GFTDStatus eGFTDStatus = GFTDStatus.INVALID;
		
		int udi_sum = 0;
		int udi_last = 0;
		
		int signal_countor = 0; // 买卖信号计数器
		int signal_last_index = 0;
		
		int i = iBegin + N1;
		StockDay cStockDay_T = list.get(i);
		StockDay cStockDay_Tn1 = list.get(i-N1);
		if(cStockDay_T.close() > cStockDay_Tn1.close())  udi_sum = udi_last = 1;
		if(cStockDay_T.close() < cStockDay_Tn1.close())  udi_sum = udi_last = -1;
		if(cStockDay_T.close() == cStockDay_Tn1.close())  udi_sum = udi_last = 0;
		
		for(i = i+1; i<=iEnd; i++ )
		{
			cStockDay_T = list.get(i);
			cStockDay_Tn1 = list.get(i-N1);
			// udi处理  *******************
			int udi = 0;
			if(cStockDay_T.close() > cStockDay_Tn1.close())  udi = 1;
			if(cStockDay_T.close() < cStockDay_Tn1.close())  udi = -1;
			if(cStockDay_T.close() == cStockDay_Tn1.close())  udi = 0;
			if(udi==udi_last) // udi变化检查
			{
				udi_sum = udi_sum + udi;
			}
			else
			{
				udi_sum = udi;
			}
			udi_last = udi;// 保存udi
			
			// 卖出启动判断  *******************
			if((eGFTDStatus == GFTDStatus.INVALID 
					|| eGFTDStatus == GFTDStatus.SELL_CHECK_START) 
					&& udi_sum == N2)
			{
				eGFTDStatus = GFTDStatus.SELL_CHECK_START; // 买入启动形成
				
				//s_StockDayListCurve.clearMark(i);
				//s_StockDayListCurve.markCurveIndex(i, "SC");
				
				signal_last_index = i;
				signal_countor = 0;
				continue;
			}

			// 买卖信号检查   *******************
			if(eGFTDStatus == GFTDStatus.SELL_CHECK_START)
			{
				StockDay cStockDay_TB1 = list.get(i-1);
				StockDay cStockDay_TB2 = list.get(i-2);
				StockDay cStockDay_Last = list.get(signal_last_index);
				if(cStockDay_T.close() <= cStockDay_TB2.low()
						&& cStockDay_T.low() < cStockDay_TB1.low()
						&& cStockDay_T.close() < cStockDay_Last.close())
				{
					signal_countor++;
					signal_last_index = i;
				}
				
				if(signal_countor == N3)
				{
					eGFTDStatus = GFTDStatus.SELL_SIGNAL;
					
					s_StockDayListCurve.clearMark(i);
					s_StockDayListCurve.markCurveIndex(i, "SS");
					
					signal_countor = 0;
					eGFTDStatus = GFTDStatus.INVALID;
				}
			}

		} // 每天遍历

		return cResultComplexGFTDEx;
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
		
		String stockID = "600103"; // 300163 300165
		ResultHistoryData cResultHistoryData = 
				cStockDataIF.getHistoryData(stockID, "2015-05-06", "2017-02-01");
		List<StockDay> list = cResultHistoryData.resultList;
		BLog.output("TEST", "Check stockID(%s) list size(%d)\n", stockID, list.size());
		
		s_StockDayListCurve.setCurve(list);
		
		for(int i = 0; i < list.size(); i++)  
        {  
			StockDay cCurStockDay = list.get(i);
			//BLog.output("TEST", "cCurStockDay %s \n", cCurStockDay.date());

			if(cCurStockDay.date().equals("2016-12-01"))
			{
				BThread.sleep(1);
				
				

			}
			ResultComplexGFTDEx cResultComplexGFTDEx = EStockComplexGFTDEx.get_buy(stockID, list, i);
//			if(cResultDYCheck.bCheck)
//			{
//				BLog.output("TEST", "CheckPoint %s\n", cCurStockDay.date());
//			}

        } 
		
		s_StockDayListCurve.generateImage();
		BLog.output("TEST", "Main End\n");
	}
	public static StockDayListCurve s_StockDayListCurve = new StockDayListCurve("EStockComplexGFTDEx.jpg");
}
