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
		EDIVirtualUpLineResult cEDIVirtualUpLineResult = EDIVirtualUpLine.get(fPriceWaveTSD, list, iCheck);
		if(cEDIVirtualUpLineResult.bCheck)
		{
			iVirtualUpLineSharpA = cEDIVirtualUpLineResult.iVirtualUpLineSharpA;
			iVirtualUpLineSharpB = cEDIVirtualUpLineResult.iVirtualUpLineSharpB;
		}
		BLog.output("TEST", "EDIVirtualUpLine SA %d SB %d\n", iVirtualUpLineSharpA, iVirtualUpLineSharpB);
		
		// @@@ ������ѹ������(��չΪ��ͬȨֵ��k����̬)
		EDITryPressResult cEDITryPressResult = EDITryPress.get(list, iCheck);
		if(cEDITryPressResult.bCheck)
		{
			BLog.output("TEST", "EDITryPress iE60 %d iE20 %d \n", cEDITryPressResult.iTry60, cEDITryPressResult.iTry20);
		}
		
		// �������������ܼ��ɽ��� �ߵ͵㣬��ֵ���ų����5�������5���ȡֵ���㣩
		// �ܼ��ɽ��� Ӧ����һ����Χ�ڲź���
		EDIWaveResult cEDIWaveResult = EDIWave.get(list, iCheck);
		if (cEDIWaveResult.bCheck)
		{
			BLog.output("TEST", "EDIWave H(%.3f) L(%.3f) Wave(%.3f) iXM5M10(%d) iXM10M20(%d)\n", 
					cEDIWaveResult.fWaveHigh, 
					cEDIWaveResult.fWaveLow, cEDIWaveResult.fWaveRadio(), 
					cEDIWaveResult.iXM5M10, cEDIWaveResult.iXM10M20);
		}

		// �ܼ��ɽ�����ֵ�����жϻز�����λ��
		
		// ��������60/30�վ���һ�����оӶ࣬ �����������٣�����֧��λ�Ϸ��ߣ�
		
		// �г��ۻ��Ƿ����ܹ��ߣ����ڴ�ص�����
		
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
