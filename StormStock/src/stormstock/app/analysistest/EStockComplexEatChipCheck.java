package stormstock.app.analysistest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stormstock.app.analysistest.EDITryPress.EDITryPressResult;
import stormstock.app.analysistest.EDIVirtualUpLine.EDIVirtualUpLineResult;
import stormstock.app.analysistest.EDIWave.EDIWaveResult;
import stormstock.fw.base.BLog;
import stormstock.fw.base.BThread;
import stormstock.fw.tranbase.stockdata.StockDataIF;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockUtils;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultHistoryData;

public class EStockComplexEatChipCheck {
	
	public static boolean check(String stockId, List<StockDay> list, int iCheck)
	{
		boolean bCheck = false;
		
		// 至少80交易日
		if(iCheck-80<0)
		{
			return bCheck;
		}
		
		// 测试70交易日
		int iBegin = iCheck-70;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return bCheck;
		}
				
		StockDay cBeginStockDay = list.get(iBegin);
		StockDay cCurStockDay = list.get(iCheck);
		//BLog.output("TEST", "begin %s end %s\n", cBeginStockDay.date(), cCurStockDay.date());
		
		// 阙值获得
		float fPriceWaveTSD = EStockDayPriceWaveThreshold.get(list, iCheck);
		float fVolumeTSD = EStockDayVolumeThreshold.get(list, iCheck);
		//BLog.output("TEST", "fPriceWaveTSD(%.4f) fVolumeTSD(%.1f) \n", fPriceWaveTSD, fVolumeTSD);
		
		// @@@ 当天突破前一个月最高点
		int iHighMonth = StockUtils.indexHigh(list, iEnd-20, iEnd);
		if(iHighMonth==iEnd)
		{	
		}
		else
		{
			return false;
		}
				
		// @@@ 近60日 异常波动天计算
		int iAbnormityPriceUpInr = 0;
		int iAbnormityPriceDownInr = 0;
		for(int i=iBegin; i<=iEnd; i++)
		{
			StockDay cTmpStockDay = list.get(i);
			float fInrRatio = StockUtils.GetInreaseRatio(list, i);
			if(fInrRatio > fPriceWaveTSD*0.6)
			{
				iAbnormityPriceUpInr++;
				//BLog.output("TEST", "AbnormityPriceInr Up %s \n", cTmpStockDay.date());
			}
			if(fInrRatio < -fPriceWaveTSD*0.6)
			{
				iAbnormityPriceDownInr++;
				//BLog.output("TEST", "AbnormityPriceInr Down %s \n", cTmpStockDay.date());
			}
		}
		//BLog.output("TEST", "iAbnormityPriceInr Up(%d) Down(%d) \n", iAbnormityPriceUpInr, iAbnormityPriceDownInr);
		if(iAbnormityPriceUpInr + iAbnormityPriceDownInr > 10)
		{
		}
		else
		{
			return false;
		}
		
		// @@@ 明显上影线个数检查
		int iVirtualUpLineSharpA = 0;
		int iVirtualUpLineSharpB = 0;
		EDIVirtualUpLineResult cEDIVirtualUpLineResult = EDIVirtualUpLine.get(fPriceWaveTSD, list, iCheck);
		if(cEDIVirtualUpLineResult.bCheck)
		{
			iVirtualUpLineSharpA = cEDIVirtualUpLineResult.iVirtualUpLineSharpA;
			iVirtualUpLineSharpB = cEDIVirtualUpLineResult.iVirtualUpLineSharpB;
		}
		BLog.output("TEST", "EDIVirtualUpLine SA %d SB %d\n", iVirtualUpLineSharpA, iVirtualUpLineSharpB);
		
		// @@@ 拉升试压力天数(扩展为不同权值的k线形态)
		EDITryPressResult cEDITryPressResult = EDITryPress.get(list, iCheck);
		if(cEDITryPressResult.bCheck)
		{
			BLog.output("TEST", "EDITryPress iE60 %d iE20 %d \n", cEDITryPressResult.iTry60, cEDITryPressResult.iTry20);
		}
		
		// 波动区间区间密集成交区 高低点，均值（排除最低5天与最高5天后，取值计算）
		// 密集成交区 应该在一定范围内才合理
		EDIWaveResult cEDIWaveResult = EDIWave.get(list, iCheck);
		if (cEDIWaveResult.bCheck)
		{
			BLog.output("TEST", "EDIWave H(%.3f) L(%.3f) Wave(%.3f) iXM5M10(%d) iXM10M20(%d)\n", 
					cEDIWaveResult.fWaveHigh, 
					cEDIWaveResult.fWaveLow, cEDIWaveResult.fWaveRadio(), 
					cEDIWaveResult.iXM5M10, cEDIWaveResult.iXM10M20);
		}

		// 密集成交区均值用于判断回踩买入位置
		
		// 交易日在60/30日均线一下运行居多， 在以上运行少（贴着支撑位上方走）
		
		// 中长累积涨幅不能过高，存在大回调风险
		
		// 日内细节判断
		// 异常波动天，大阴线满足吓人趋势（存在急跌 尾盘跌，下穿重要支撑位最好） 
		// 大阳线满足吃货形态（拉高下不来！！逐步拉高，高位横盘）
		
		bCheck = true;
		return bCheck;
	}
	
	
	public static class PriceWaveInterval
	{
		public int iBegin;
		public float hHigh;
		public float fLow;
	}
	public static PriceWaveInterval checkPriceWaveInterval(List<StockDay> list, int iCheck)
	{
		PriceWaveInterval cPriceWaveInterval = new PriceWaveInterval();
		
		for(int iBeginIndex=iCheck-20; iBeginIndex>0&&iBeginIndex>iCheck-60; iBeginIndex--)
		{
			List<Float> checkList = new ArrayList<Float>();
			for(int i=iBeginIndex;i<=iCheck;i++)
			{
				StockDay cStockDay = list.get(i);
				float fHigh = cStockDay.high();
				float fLow = cStockDay.low();
				checkList.add(fHigh);
				checkList.add(fLow);
			}
			Collections.sort(checkList);
			
			int iCheckCnt = iCheck-iBeginIndex;
			for(int i=0; i<iCheckCnt%10; i++)
			{
				checkList.remove(0);
				checkList.remove(checkList.size()-1);
			}
			
			//检查上边
			for(int i=0;i<iCheckCnt%5;i++)
			{
				
			}
		}
		return cPriceWaveInterval;
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
				cStockDataIF.getHistoryData(stockID, "2011-01-01", "2017-03-01");
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
					i=i+20;
				}
			}
			

        } 
		
		s_StockDayListCurve.generateImage();
		BLog.output("TEST", "Main End\n");
	}
	public static StockDayListCurve s_StockDayListCurve = new StockDayListCurve("EStockComplexEatChipCheck.jpg");

}
