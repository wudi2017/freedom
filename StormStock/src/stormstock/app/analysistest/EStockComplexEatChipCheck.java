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
		for(int i=iBegin; i<=iEnd; i++)
		{
			StockDay cTmpStockDay = list.get(i);
			if(cTmpStockDay.wave() < fPriceWaveTSD*0.3) // 整体振幅过小忽略
			{
				continue;
			}

			float fTmpHigh = cTmpStockDay.high();
			float fTmpEntityHigh = cTmpStockDay.entityHigh();
			float fTmpEntityMid = cTmpStockDay.midle();
			float fTmpEntityLow = cTmpStockDay.entityLow();
			float fTmpLow = cTmpStockDay.low();
	
			// 上影线相对百分比计算
			float fUpLineRatio = (fTmpHigh-fTmpEntityHigh)/fTmpEntityHigh;
			if(fUpLineRatio > fPriceWaveTSD*0.5)
			{
				iVirtualUpLineSharpA++;
				//BLog.output("TEST", "SA %s %.4f\n", cTmpStockDay.date(),fUpLineRatio );
			}
			else if(fUpLineRatio > fPriceWaveTSD*0.3)
			{
				iVirtualUpLineSharpB++;
				//BLog.output("TEST", "SB %s %.4f\n", cTmpStockDay.date(),fUpLineRatio );
			}
		}
		//BLog.output("TEST", "SA %d SB %d\n", iVirtualUpLineSharpA, iVirtualUpLineSharpB);
		if(iVirtualUpLineSharpA>3 && iVirtualUpLineSharpB>7)
		{
			
		}
		else
		{
			return false;
		}
		
		// @@@ 拉升试压力天数(扩展为不同权值的k线形态)
		int iE60 = 0;
		int iE20 = 0;
		for(int i=iBegin; i<=iEnd; i++)
		{
			StockDay cTmpStockDay = list.get(i);
			
			float fTmpHigh = cTmpStockDay.high();
			float fTmpEntityHigh = cTmpStockDay.entityHigh();
			float fTmpEntityMid = cTmpStockDay.midle();
			float fTmpEntityLow = cTmpStockDay.entityLow();
			float fTmpLow = cTmpStockDay.low();

			float fMA60 = StockUtils.GetMA(list, 60, i);
			float fMA20 = StockUtils.GetMA(list, 20, i);
			
			if(fTmpHigh >= fMA60 && fTmpEntityMid < fMA60)
			{
				//BLog.output("TEST", "X %s \n", cTmpStockDay.date());
				iE60++;
			}
			
			if(fTmpHigh >= fMA20 && fTmpEntityMid < fMA20)
			{
				//BLog.output("TEST", "Y %s \n", cTmpStockDay.date());
				iE20++;
			}
		} // 试探压力要有波顶端
		//BLog.output("TEST", "iE60 %d iE20 %d \n", iE60, iE20);
		if(iE60+iE20 > 10)
		{
		}
		else
		{
			return false;
		}
		
		// 波动区间区间密集成交区 高低点，均值（排除最低5天与最高5天后，取值计算）
		// 密集成交区 应该在一定范围内才合理
		float fIntervalWaveHigh = 0.0f;
		float fIntervalWaveLow = 0.0f;
		int iXM5M10 = 0; 
		int iXM10M20 = 0; 
		{
			List<Float> checkList = new ArrayList<Float>();
			for(int i=iBegin+20; i<=iEnd; i++)
			{
				StockDay cTmpStockDay = list.get(i);
				float fHigh = cTmpStockDay.high();
				float fLow = cTmpStockDay.low();
				checkList.add(fHigh);
				checkList.add(fLow);
			}
			Collections.sort(checkList);
			
			// 均线交叉计算
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
			
			//检查边
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
		}
		float fIntervalWave = (fIntervalWaveHigh-fIntervalWaveLow)/fIntervalWaveLow;
//		BLog.output("TEST", "WaveInterval  H(%.3f) L(%.3f) Wave(%.3f) iXM5M10(%d) iXM10M20(%d)\n", 
//				fIntervalWaveHigh, fIntervalWaveLow, fIntervalWave, iXM5M10, iXM10M20);
//		if(fIntervalWave<0.15 && iXM5M10>=5 && iXM10M20>=3)
//		{
//		}
//		else
//		{
//			return bCheck;
//		}
		
		// 最后日突破密集成交区高点（不是最高点，是前若干天的均值）
//		if(cCurStockDay.high() > fIntervalWaveHigh)
//		{
//			
//		}
//		else
//		{
//			return bCheck;
//		}
		
		// 密集成交区均值用于判断回踩买入位置
		
		// 交易日在60/30日均线一下运行居多， 在以上运行少（贴着支撑位上方走）
		
		// 中长累积涨幅不能过高，存在大回调风险
		int iBeginIndexLong = iBegin-120;
		if(iBeginIndexLong<0) iBeginIndexLong = 0;
		
		int iIndexLongLow = StockUtils.indexLow(list, iBeginIndexLong, iCheck);
		StockDay cStockDayLongLow = list.get(iIndexLongLow);
		
		float fLeiji = (cCurStockDay.close() - cStockDayLongLow.close())/cStockDayLongLow.close();
		//BLog.output("TEST", "BeginIndexLong %s fLeiji %.3f\n", cStockDayLongLow.date(),fLeiji);
		if(fLeiji < 0.50)
		{
			
		}
		else
		{
			return bCheck;
		}
		
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
			
			if(cCurStockDay.date().equals("2014-06-13"))
			{
				BThread.sleep(1);


			}
			
			boolean bCheck = EStockComplexEatChipCheck.check(stockID, list, i);
			if (bCheck)
			{
				BLog.output("TEST", "### CheckPoint %s\n", cCurStockDay.date());
				s_StockDayListCurve.markCurveIndex(i, "D");
				i=i+20;
			}
        } 
		
		s_StockDayListCurve.generateImage();
		BLog.output("TEST", "Main End\n");
	}
	public static StockDayListCurve s_StockDayListCurve = new StockDayListCurve("EStockComplexEatChipCheck.jpg");

}
