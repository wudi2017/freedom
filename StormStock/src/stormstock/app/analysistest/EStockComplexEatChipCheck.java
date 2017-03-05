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
		
		// ����80������
		if(iCheck-80<0)
		{
			return bCheck;
		}
		
		// ����70������
		int iBegin = iCheck-70;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return bCheck;
		}
				
		StockDay cBeginStockDay = list.get(iBegin);
		StockDay cCurStockDay = list.get(iCheck);
		//BLog.output("TEST", "begin %s end %s\n", cBeginStockDay.date(), cCurStockDay.date());
		
		// ��ֵ���
		float fPriceWaveTSD = EStockDayPriceWaveThreshold.get(list, iCheck);
		float fVolumeTSD = EStockDayVolumeThreshold.get(list, iCheck);
		//BLog.output("TEST", "fPriceWaveTSD(%.4f) fVolumeTSD(%.1f) \n", fPriceWaveTSD, fVolumeTSD);
		
		// @@@ ����ͻ��ǰһ������ߵ�
		int iHighMonth = StockUtils.indexHigh(list, iEnd-20, iEnd);
		if(iHighMonth==iEnd)
		{	
		}
		else
		{
			return false;
		}
				
		// @@@ ��60�� �쳣���������
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
		
		// @@@ ������Ӱ�߸������
		int iVirtualUpLineSharpA = 0;
		int iVirtualUpLineSharpB = 0;
		for(int i=iBegin; i<=iEnd; i++)
		{
			StockDay cTmpStockDay = list.get(i);
			if(cTmpStockDay.wave() < fPriceWaveTSD*0.3) // ���������С����
			{
				continue;
			}

			float fTmpHigh = cTmpStockDay.high();
			float fTmpEntityHigh = cTmpStockDay.entityHigh();
			float fTmpEntityMid = cTmpStockDay.midle();
			float fTmpEntityLow = cTmpStockDay.entityLow();
			float fTmpLow = cTmpStockDay.low();
	
			// ��Ӱ����԰ٷֱȼ���
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
		
		// @@@ ������ѹ������(��չΪ��ͬȨֵ��k����̬)
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
		} // ��̽ѹ��Ҫ�в�����
		//BLog.output("TEST", "iE60 %d iE20 %d \n", iE60, iE20);
		if(iE60+iE20 > 10)
		{
		}
		else
		{
			return false;
		}
		
		// �������������ܼ��ɽ��� �ߵ͵㣬��ֵ���ų����5�������5���ȡֵ���㣩
		// �ܼ��ɽ��� Ӧ����һ����Χ�ڲź���
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
			
			// ���߽������
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
			
			//����
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
		
		// �����ͻ���ܼ��ɽ����ߵ㣨������ߵ㣬��ǰ������ľ�ֵ��
//		if(cCurStockDay.high() > fIntervalWaveHigh)
//		{
//			
//		}
//		else
//		{
//			return bCheck;
//		}
		
		// �ܼ��ɽ�����ֵ�����жϻز�����λ��
		
		// ��������60/30�վ���һ�����оӶ࣬ �����������٣�����֧��λ�Ϸ��ߣ�
		
		// �г��ۻ��Ƿ����ܹ��ߣ����ڴ�ص�����
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
		
		// ����ϸ���ж�
		// �쳣�����죬�����������������ƣ����ڼ��� β�̵����´���Ҫ֧��λ��ã� 
		// ����������Ի���̬�������²������������ߣ���λ���̣�
		
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
			
			//����ϱ�
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
