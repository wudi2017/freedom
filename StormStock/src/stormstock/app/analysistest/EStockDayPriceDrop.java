package stormstock.app.analysistest;

import java.util.List;

import stormstock.fw.base.BLog;
import stormstock.fw.base.BThread;
import stormstock.fw.tranbase.stockdata.StockDataIF;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockUtils;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultHistoryData;

/**
 * 
 * @author wudi
 * ��鵱ǰλ���Ƿ��Ƕ��ڼ������ȵ�
 */

public class EStockDayPriceDrop {

	public static class ResultCheckPriceDrop
	{
		public ResultCheckPriceDrop()
		{
			bCheck = false;
		}
		public boolean bCheck;
		public float maxDropRate;
	}
	// ���iCheck�Ƿ�������ڼ���
	public ResultCheckPriceDrop checkPriceDrop(List<StockDay> list, int iCheck)
	{
		String curDate = list.get(iCheck).date();
		float fAveWave = EStockDayPriceWave.checkPriceAveWave(list, iCheck);
		//BLog.output("TEST", " (%s) %.4f\n", curDate, fAveWave);
		
		ResultCheckPriceDrop cResultCheck = new ResultCheckPriceDrop();
		
		// ������ж�
		int iBegin = iCheck-30;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return cResultCheck;
		}
		
		// ��͵����ٽ����ж�
		int iCheckForm = iCheck-10;
		int iCheckTo = iCheck;
		int indexLow = StockUtils.indexLow(list, iCheckForm, iCheckTo);
		StockDay cStockDayLow = list.get(indexLow);
		float fStockDayLow_midle = cStockDayLow.midle();
		if(iCheckTo - indexLow <= 5 && iCheckTo - indexLow > 0)
		{
		}
		else
		{
			return cResultCheck;
		}
		//BLog.output("TEST", " %d %d \n", indexHigh, indexLow);
		
		// �����»���ߵ�,����3�ո������۶�С�ڸߵ�
		float ave3Next = StockUtils.GetAveNear(list, 1, indexLow-1);
		int checkTimes = 0;
		int indexHigh = 0;
		for(int i = indexLow; i>iBegin; i--)
		{
			String tmpDate = list.get(i).date();
			float ave3 = StockUtils.GetAveNear(list, 1, i);
			if(ave3 < ave3Next)
			{
				checkTimes++;
			}
			else
			{
				checkTimes=0;
				ave3Next = ave3;
			}
			
			if(checkTimes>=3)
			{
				indexHigh = i+3;
				break;
			}
		}
		//BLog.output("TEST", "(%s) %s - %s \n", curDate, list.get(indexHigh).date(), list.get(indexLow).date());
		float fStockDayHigh_midle = 0.0f;
		if(indexHigh != 0)
		{
			fStockDayHigh_midle = list.get(indexHigh).midle();
		}
		else
		{
			return cResultCheck;
		}
		
		// ������
		float MaxDropRate = (fStockDayLow_midle-fStockDayHigh_midle)/fStockDayLow_midle;
		if(MaxDropRate < -1.5*fAveWave)
		{
			cResultCheck.maxDropRate = MaxDropRate;
		}
		else
		{
			return cResultCheck;
		}
		
		// ������֮����ڴ�����
		//BLog.output("TEST", "%s fAveWave %.4f\n", list.get(iCheck).date(), fAveWave);
		boolean bDaYin = false;
		for(int i = indexHigh; i<=indexLow; i++)
		{
			if(i-1<0) continue;
			StockDay cCheckDay = list.get(i);
			StockDay cCheckDayBefore = list.get(i-1);
			float fCheckRateOpen = (cCheckDay.close() - cCheckDay.open())/cCheckDay.open();
			float fCheckRateYesterday = (cCheckDay.close() - cCheckDayBefore.close())/cCheckDayBefore.close();
			if(fCheckRateYesterday < -fAveWave
					&& fCheckRateOpen < -fAveWave*0.6)
			{
				bDaYin = true;
			}
		}
		if(bDaYin)
		{
		}
		else
		{
			return cResultCheck;
		}
		
//		// ��ǰ�ռ۸��ܷ�������
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

		cResultCheck.bCheck = true;
		return cResultCheck;
	}
	// �ָ������iCheck�Ƿ�������ڼ������ų������Ը��ţ�
	public ResultCheckPriceDrop checkPriceDrop_sep(List<StockDay> list, int iCheck)
	{
		ResultCheckPriceDrop cResultCheckPriceDrop = new ResultCheckPriceDrop();
		
		ResultCheckPriceDrop cResultCheckPriceDropSingle = checkPriceDrop(list, iCheck);
		if(cResultCheckPriceDropSingle.bCheck) // ��������iCheck����CheckPoint
		{
			int iCheckOKLast = -1;
			int iBegin = iCheck - 20;
			int iEnd = iCheck;
			for(int i=iBegin; i<=iEnd; i++)
			{
				ResultCheckPriceDrop cResultCheckPriceDropBefore = checkPriceDrop(list, i);
				if(cResultCheckPriceDropBefore.bCheck)
				{
					iCheckOKLast = i;
					cResultCheckPriceDrop.maxDropRate = cResultCheckPriceDropBefore.maxDropRate;
					i=i+10;
				}
			}
			
			if(iCheckOKLast == iCheck)
			{
				cResultCheckPriceDrop.bCheck = true;
			}
		}

		return cResultCheckPriceDrop;
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
		
		String stockID = "000151"; // 300163 300165
		ResultHistoryData cResultHistoryData = 
				cStockDataIF.getHistoryData(stockID, "2010-09-01", "2012-01-01");
		List<StockDay> list = cResultHistoryData.resultList;
		BLog.output("TEST", "Check stockID(%s) list size(%d)\n", stockID, list.size());
		
		s_StockDayListCurve.setCurve(list);
		
		EStockDayPriceDrop cEStockDayPriceDrop = new EStockDayPriceDrop();
		
		for(int i = 0; i < list.size(); i++)  
        {  
			StockDay cCurStockDay = list.get(i);
	
			if(cCurStockDay.date().equals("2016-11-02"))
			{
				BThread.sleep(1);
			}
			
			ResultCheckPriceDrop cResultCheckPriceDrop = cEStockDayPriceDrop.checkPriceDrop(list, i);
			if (cResultCheckPriceDrop.bCheck)
			{
				BLog.output("TEST", "### CheckPoint %s\n", cCurStockDay.date());
				s_StockDayListCurve.markCurveIndex(i, "D");
				//i=i+2;
			}

        } 
		
		s_StockDayListCurve.generateImage();
		BLog.output("TEST", "Main End\n");
	}
	public static StockDayListCurve s_StockDayListCurve = new StockDayListCurve("EStockDayPriceDrop.jpg");
}
