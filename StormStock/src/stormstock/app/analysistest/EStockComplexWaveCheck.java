package stormstock.app.analysistest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stormstock.fw.base.BLog;
import stormstock.fw.base.BThread;
import stormstock.fw.tranbase.stockdata.StockDataIF;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockUtils;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultHistoryData;

/**
 * 震荡行情检查
 * @author wudi
 *
 */
public class EStockComplexWaveCheck {
	
	public static class ComplexWaveResult
	{
		public ComplexWaveResult()
		{
			bCheck = false;
		}
		public boolean bCheck;
	}
	public static ComplexWaveResult get(List<StockDay> list, int iCheck)
	{
		ComplexWaveResult cComplexWaveResult = new ComplexWaveResult();
		
		// 测试60交易日
		int iBegin = iCheck-60;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return cComplexWaveResult;
		}
		
		float fIntervalWaveHigh = 0.0f;
		float fIntervalWaveLow = 0.0f;
		int iXM5M10 = 0; 
		int iXM10M20 = 0; 

		List<Float> checkList = new ArrayList<Float>();
		for(int i=iBegin; i<=iEnd; i++)
		{
			StockDay cTmpStockDay = list.get(i);
			float fHigh = cTmpStockDay.high();
			float fLow = cTmpStockDay.low();
			checkList.add(fHigh);
			checkList.add(fLow);
		}
		Collections.sort(checkList);

		float fHighAve = 0.0f;
		for(int i=0;i<30;i++)
		{
			fHighAve=fHighAve+checkList.get(checkList.size()-1-i);
		}
		fHighAve = fHighAve/30;
		fIntervalWaveHigh = fHighAve;
		
		float fLowAve = 0.0f;
		for(int i=0;i<30;i++)
		{
			fLowAve=fLowAve+checkList.get(i);
		}
		fLowAve = fLowAve/30;
		fIntervalWaveLow = fLowAve;

		for(int i=iBegin+20; i<=iEnd; i++)
		{
			float M5 = StockUtils.GetMA(list, 5, i);
			float M10 = StockUtils.GetMA(list, 10, i);
			
			float M5_B1 = StockUtils.GetMA(list, 5, i-1);
			float M10_B1 = StockUtils.GetMA(list,10, i-1);
			if((M10_B1-M5_B1)*(M10-M5) <= 0)
			{
				iXM5M10++;
				i=i+3;
			}
		}
		
		for(int i=iBegin+20; i<=iEnd; i++)
		{
			float M10 = StockUtils.GetMA(list, 10, i);
			float M20 = StockUtils.GetMA(list, 20, i);
			
			float M10_B1 = StockUtils.GetMA(list, 10, i-1);
			float M20_B1 = StockUtils.GetMA(list,20, i-1);
			if((M20_B1-M10_B1)*(M20-M10) <= 0)
			{
				iXM10M20++;
			}
		}
		
		float fIntervalWave = (fIntervalWaveHigh-fIntervalWaveLow)/fIntervalWaveLow;
		BLog.output("TEST", "WaveInterval  H(%.3f) L(%.3f) Wave(%.3f) iXM5M10(%d) iXM10M20(%d)\n", 
				fIntervalWaveHigh, fIntervalWaveLow, fIntervalWave, iXM5M10, iXM10M20);
	
		return cComplexWaveResult;
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
		
		String stockID = "300217"; // 300217 300227 300163 300165 00
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

				ComplexWaveResult cComplexWaveResult = EStockComplexWaveCheck.get(list, i);
				if (cComplexWaveResult.bCheck)
				{
					BLog.output("TEST", "### CheckPoint %s\n", cCurStockDay.date());
					s_StockDayListCurve.markCurveIndex(i, "D");
					i=i+20;
				}
			}
			

        } 
		
		s_StockDayListCurve.generateImage();
		BLog.output("TEST", "Main End\n");
	}
	public static StockDayListCurve s_StockDayListCurve = new StockDayListCurve("EStockComplexWaveCheck.jpg");

}
