package stormstock.app.analysistest;

import java.util.List;

import stormstock.fw.base.BLog;
import stormstock.fw.base.BThread;
import stormstock.fw.base.BImageCurve.CurvePoint;
import stormstock.fw.tranbase.stockdata.StockDataIF;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockTime;
import stormstock.fw.tranbase.stockdata.StockUtils;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultDayDetail;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultHistoryData;

public class EStockDayPriceUpShadowLineTestPress {
	public static class ResultCheckUpShadowLineTestPress
	{
		public ResultCheckUpShadowLineTestPress()
		{
		}
		public boolean bCheck;
	}
	
	public static ResultCheckUpShadowLineTestPress checkUpShadowLineTestPress(String stockId, List<StockDay> list, int iCheck)
	{
		ResultCheckUpShadowLineTestPress cResultCheckUpShadowLineTestPress = new ResultCheckUpShadowLineTestPress();

		// ����60������
		int iBegin = iCheck-60;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return cResultCheckUpShadowLineTestPress;
		}
		
		/*
		 * *********************************************************************************
		 * ������������
		 */
		
		// ����ͻ��ǰһ������ߵ�
		int iHighMonth = StockUtils.indexHigh(list, iEnd-20, iEnd);
		if(iHighMonth!=iEnd)
		{	
		}
		else
		{
			return cResultCheckUpShadowLineTestPress;
		}
		StockDay cCurStockDay = list.get(iHighMonth);
		String curDate = cCurStockDay.date();
		BLog.output("TEST", "date %s Open %.3f Close %.3f\n", 
				curDate, cCurStockDay.open(), cCurStockDay.close());
		
		// ���������ߣ� ��������Ӱ��
		float wave = EStockDayPriceWave.checkPriceAveWave(list, iEnd);
		float curAccRate = StockUtils.GetInreaseRatio(list, iEnd);
		float curAccRateOpen = StockUtils.GetInreaseRatioRefOpen(list, iEnd);
		float fUpShadowRatio = 0.0f;
		if(cCurStockDay.high() - cCurStockDay.low()>0)
		{
			fUpShadowRatio = (cCurStockDay.high() - cCurStockDay.close())/(cCurStockDay.close() - cCurStockDay.low());
		}
		else
		{
			return cResultCheckUpShadowLineTestPress;
		}
		
		if(curAccRate > 0.6*wave
				&& fUpShadowRatio <= 0.33
				&& curAccRateOpen > 0)
		{
			
		}
//		BLog.output("TEST", "date %s wave %.4f curAccRate %.4f curAccRateOpen %.4f fUpShadowRatio %.2f\n", 
//				curDate, wave, curAccRate, curAccRateOpen, fUpShadowRatio);
		
		// ������ͻ������ѹ����
		float MA5_Cur = StockUtils.GetMA(list, 5, iEnd);
		float MA10_Cur = StockUtils.GetMA(list, 10, iEnd);
		float MA20_Cur = StockUtils.GetMA(list, 20, iEnd);
		float MA60_Cur = StockUtils.GetMA(list, 60, iEnd);
		if(cCurStockDay.close() > MA5_Cur
				&& cCurStockDay.close() > MA10_Cur
				&& cCurStockDay.close() > MA20_Cur
				&& cCurStockDay.close() > MA60_Cur)
		{
		}
		else
		{
			return cResultCheckUpShadowLineTestPress;
		}
		//BLog.output("TEST", "date %s Topo MA...\n", curDate);
		
		//�����඼�ڸ�λ����
		StockDataIF cStockDataIF = new StockDataIF();
		ResultDayDetail cResultDayDetail = cStockDataIF.getDayDetail(stockId, curDate, "09:30:00", "15:00:00");
		if(0 == cResultDayDetail.error)
		{
			boolean bHighRun = EStockTimePriceHighRun.checkHighRun(stockId, curDate, cResultDayDetail.resultList);
			if(bHighRun)
			{
			}
			else
			{
				return cResultCheckUpShadowLineTestPress;
			}
		}
		else
		{
			return cResultCheckUpShadowLineTestPress;
		}
		
		/*
		 * *********************************************************************************
		 * �ο�������ʷ����
		 */
		
		// �ۼ��Ƿ����ܹ���
		int iLowMonth = StockUtils.indexLow(list, iEnd-20, iEnd);
		float lowPriceMonth = list.get(iLowMonth).midle();
		float fMonthAccRate = (cCurStockDay.midle() - lowPriceMonth)/lowPriceMonth;
		//BLog.output("TEST", "date %s fMonthAccRate %.3f\n", curDate, fMonthAccRate);
		if(fMonthAccRate < 0.19f)
		{
		}
		else
		{
			return cResultCheckUpShadowLineTestPress;
		}
		// ǰ��60���ڣ���̽ѹ��λ�Ӷ࣬��Ӱ�ߴ���ѹ��λ��Ϊ������
		
		// �����߲���β�����ߣ���Ŀ��Ϊ�˳Ի�
		
		// �����߼����Ӷ࣬Ϊ�����
		
		// �����߲����Ǿ��������ǳ���
		
		cResultCheckUpShadowLineTestPress.bCheck = true;
		return cResultCheckUpShadowLineTestPress;
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
		
		String stockID = "300227"; // 300217 300227 300217
		ResultHistoryData cResultHistoryData = 
				cStockDataIF.getHistoryData(stockID, "2012-04-01", "2016-01-01");
		List<StockDay> list = cResultHistoryData.resultList;
		BLog.output("TEST", "Check stockID(%s) list size(%d)\n", stockID, list.size());
		
		s_StockDayListCurve.setCurve(list);
		
		for(int i = 0; i < list.size(); i++)  
        {  
			StockDay cCurStockDay = list.get(i);
	
			if(cCurStockDay.date().equals("2012-08-03"))
			{
				BThread.sleep(1);
				
				ResultCheckUpShadowLineTestPress cResultCheckUpShadowLineTestPress = EStockDayPriceUpShadowLineTestPress.checkUpShadowLineTestPress(stockID, list, i);
				if (cResultCheckUpShadowLineTestPress.bCheck)
				{
					BLog.output("TEST", "### CheckPoint %s\n", cCurStockDay.date());
					s_StockDayListCurve.markCurveIndex(i, "D");
					//i=i+2;
				}
			}
			

        } 
		
		s_StockDayListCurve.generateImage();
		BLog.output("TEST", "Main End\n");
	}
	public static StockDayListCurve s_StockDayListCurve = new StockDayListCurve("EStockDayPriceUpShadowLineTestPress.jpg");

}
