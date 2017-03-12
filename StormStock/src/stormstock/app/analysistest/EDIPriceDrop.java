package stormstock.app.analysistest;

import java.util.List;

import stormstock.fw.base.BLog;
import stormstock.fw.base.BThread;
import stormstock.fw.base.BUtilsMath;
import stormstock.fw.tranbase.stockdata.StockDataIF;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockUtils;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultHistoryData;

/**
 * 
 * @author wudi
 */

public class EDIPriceDrop {

	public static class ResultCheckPriceDrop
	{
		public ResultCheckPriceDrop()
		{
			bCheck = false;
		}
		public boolean bCheck;
		public int iHigh;
		public float fHighPrice;
		public int iLow;
		public float fLowPrice;
		public float fDropRatio()
		{
			return (fLowPrice - fHighPrice)/fHighPrice;
		}
		public float fDropAcc()
		{
			return fDropRatio()/(iLow-iHigh+1);
		}
	}
	
	public static float getMidDropParam(List<StockDay> list, int iCheck)
	{
		int iBegin = iCheck-60;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return 0.0f;
		}
		
		StockDay cCurStockDay = list.get(iEnd);
		
		int iIndex60H = StockUtils.indexHigh(list, iBegin, iEnd);
		StockDay cStockDay60H = list.get(iIndex60H);
		int iIndex60L = StockUtils.indexLow(list, iBegin, iEnd);
		StockDay cStockDay60L = list.get(iIndex60L);
		float fMA20 = StockUtils.GetMA(list, 20, iEnd);
		
		float Pa = (cCurStockDay.close() - cStockDay60H.close())
				+ (cCurStockDay.close() - cStockDay60L.close())
				+ (cCurStockDay.close() - fMA20);
		return Pa;
	}

	public static ResultCheckPriceDrop checkPriceDrop(List<StockDay> list, int iCheck)
	{
		String curDate = list.get(iCheck).date();
		float fAveWave = EStockDayPriceWaveThreshold.get(list, iCheck);
		//BLog.output("TEST", " (%s) %.4f\n", curDate, fAveWave);
		
		ResultCheckPriceDrop cResultCheck = new ResultCheckPriceDrop();
		
		// 检查日判断
		int iBegin = iCheck-30;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return cResultCheck;
		}
		StockDay cEndStockDay = list.get(iEnd);
		
		// 反向查找高位
		int checkTimes_drop = 0;
		int checkTimes_inc = 0;
		int indexCheckHigh = 0;
		for(int i = iEnd; i>iBegin+4; i--)
		{
			StockDay cStockDay = list.get(i);
			float ave3PreCheck = StockUtils.GetAveNear(list, 1, i-3);
			if(cStockDay.close() < ave3PreCheck)
			{
				checkTimes_drop++;
				checkTimes_inc=0;
			}
			else
			{
				checkTimes_inc++;
			}
			
			if(checkTimes_inc >=2)
			{
				indexCheckHigh = i;
				break;
			}
			
		}
		StockDay cB1StockDay = list.get(iEnd-1);
		StockDay cB2StockDay = list.get(iEnd-2);
		StockDay cHStockDay = list.get(indexCheckHigh);
		if(checkTimes_drop > 3 
				&& cEndStockDay.close() < cB1StockDay.close()
				&& cEndStockDay.close() < cB1StockDay.close()
				&& cEndStockDay.low() < cB2StockDay.low()
				&& cEndStockDay.low() < cB2StockDay.low())
		{
		}
		else
		{
			return cResultCheck;
		}
		//BLog.output("TEST", "(%s) %s\n", curDate, list.get(indexCheckHigh).date());

		
		// 最大跌幅
		float MaxDropRate = (cEndStockDay.close()-cHStockDay.close())/cHStockDay.close();
		if(MaxDropRate < -0.6*fAveWave)
		{
		}
		else
		{
			return cResultCheck;
		}
		
		// 最后一天非跌停
		float fDieTingPrice = cB1StockDay.close() * 0.90f;
		fDieTingPrice = BUtilsMath.saveNDecimal(fDieTingPrice, 2);
		float fcloseCompare = BUtilsMath.saveNDecimal(cEndStockDay.close(), 2);
		if(Float.compare(fDieTingPrice, fcloseCompare) != 0)
		{
		}
		else
		{
			return cResultCheck;
		}
		
		s_StockDayListCurve.clearMark(indexCheckHigh);
		s_StockDayListCurve.markCurveIndex(indexCheckHigh, "H");
		
		s_StockDayListCurve.clearMark(iEnd);
		s_StockDayListCurve.markCurveIndex(iEnd, "L");
		
		cResultCheck.bCheck = true;
		cResultCheck.iHigh = indexCheckHigh;
		cResultCheck.fHighPrice = cHStockDay.close();
		cResultCheck.iLow = iEnd;
		cResultCheck.fLowPrice = cEndStockDay.close();
		
		
//		// 最低点在临近日判断
//		int iCheckForm = iCheck-10;
//		int iCheckTo = iCheck;
//		int indexLow = StockUtils.indexLow(list, iCheckForm, iCheckTo);
//		StockDay cStockDayLow = list.get(indexLow);
//		float fStockDayLow_midle = cStockDayLow.midle();
//		if(iCheckTo - indexLow <= 5 && iCheckTo - indexLow > 0)
//		{
//		}
//		else
//		{
//			return cResultCheck;
//		}
//		//BLog.output("TEST", " %d %d \n", indexHigh, indexLow);
		

		

		
		// 最高最低之间存在大阴线
		//BLog.output("TEST", "%s fAveWave %.4f\n", list.get(iCheck).date(), fAveWave);
//		boolean bDaYin = false;
//		for(int i = indexHigh; i<=indexLow; i++)
//		{
//			if(i-1<0) continue;
//			StockDay cCheckDay = list.get(i);
//			StockDay cCheckDayBefore = list.get(i-1);
//			float fCheckRateOpen = (cCheckDay.close() - cCheckDay.open())/cCheckDay.open();
//			float fCheckRateYesterday = (cCheckDay.close() - cCheckDayBefore.close())/cCheckDayBefore.close();
//			if(fCheckRateYesterday < -fAveWave
//					&& fCheckRateOpen < -fAveWave*0.6)
//			{
//				bDaYin = true;
//			}
//		}
//		if(bDaYin)
//		{
//		}
//		else
//		{
//			return cResultCheck;
//		}
		
//		// 当前日价格不能反弹过高
//		StockDay cCurStockDay = list.get(iCheck);
//		float fCurStockDay_close =  cCurStockDay.close();
//		float CurPriceRate = (fCurStockDay_close - fStockDayLow_close)/fStockDayLow_close;
//		CurPriceRate = Math.abs(CurPriceRate);
//		if(CurPriceRate <= 0.03f)
//		{
//		}
//		else
//		{
//			return cResultCheck;
//		}

		return cResultCheck;
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
		
		String stockID = "002425"; // 300163 300165
		ResultHistoryData cResultHistoryData = 
				cStockDataIF.getHistoryData(stockID, "2016-01-01", "2017-01-01");
		List<StockDay> list = cResultHistoryData.resultList;
		BLog.output("TEST", "Check stockID(%s) list size(%d)\n", stockID, list.size());
		
		s_StockDayListCurve.setCurve(list);
		
		for(int i = 0; i < list.size(); i++)  
        {  
			StockDay cCurStockDay = list.get(i);
	
			if(cCurStockDay.date().equals("2016-05-11"))
			{
				BThread.sleep(1);
				
				ResultCheckPriceDrop cResultCheckPriceDrop = EDIPriceDrop.checkPriceDrop(list, i);
				if (cResultCheckPriceDrop.bCheck)
				{
					BLog.output("TEST", "### CheckPoint %s H(%s %.2f) L(%s %.2f) Ratio(%.3f)\n", 
							cCurStockDay.date(), 
							list.get(cResultCheckPriceDrop.iHigh).date(),
							cResultCheckPriceDrop.fHighPrice,
							list.get(cResultCheckPriceDrop.iLow).date(),
							cResultCheckPriceDrop.fLowPrice,
							cResultCheckPriceDrop.fDropRatio());
					//s_StockDayListCurve.markCurveIndex(i, "D");
					//i=i+20;
				}
			}
			


        } 
		
		s_StockDayListCurve.generateImage();
		BLog.output("TEST", "Main End\n");
	}
	public static StockDayListCurve s_StockDayListCurve = new StockDayListCurve("EStockDayPriceDrop.jpg");
}
