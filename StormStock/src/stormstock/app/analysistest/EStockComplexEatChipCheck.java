package stormstock.app.analysistest;

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
		
		// �����ܼ��ɽ��� �ߵ͵㣬��ֵ���ų����5�������5���ȡֵ���㣩
		// �ܼ��ɽ��� Ӧ����һ����Χ�ڲź���
		
		// �����ͻ���ܼ��ɽ����ߵ㣨������ߵ㣬��ǰ������ľ�ֵ��
		
		// �ܼ��ɽ�����ֵ�����жϻز�����λ��
		
		// ��������60/30�վ���һ�����оӶ࣬ �����������٣�����֧��λ�Ϸ��ߣ�
		
		// �ۻ��Ƿ����ܹ��ߣ����ڴ�ص�����
		
		// ����ϸ���ж�
		// �쳣�����죬�����������������ƣ����ڼ��� β�̵����´���Ҫ֧��λ��ã� 
		// ����������Ի���̬�������²������������ߣ���λ���̣�
		
		bCheck = true;
		return bCheck;
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
		
		String stockID = "300217"; // 300217 300227
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


			}
			
			boolean bCheck = EStockComplexEatChipCheck.check(stockID, list, i);
			if (bCheck)
			{
				BLog.output("TEST", "### CheckPoint %s\n", cCurStockDay.date());
				s_StockDayListCurve.markCurveIndex(i, "D");
				//i=i+2;
			}
        } 
		
		s_StockDayListCurve.generateImage();
		BLog.output("TEST", "Main End\n");
	}
	public static StockDayListCurve s_StockDayListCurve = new StockDayListCurve("EStockComplexEatChipCheck.jpg");

}
