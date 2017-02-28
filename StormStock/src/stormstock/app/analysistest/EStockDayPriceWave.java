package stormstock.app.analysistest;

import java.util.List;

import stormstock.app.analysistest.EStockDayPriceDrop.ResultCheckPriceDrop;
import stormstock.fw.base.BLog;
import stormstock.fw.tranbase.stockdata.StockDataIF;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultHistoryData;

public class EStockDayPriceWave {
	
	public static class ResultCheckPriceWave
	{
		public ResultCheckPriceWave()
		{
			bCheck = false;
		}
		public float refWave()
		{
			int iYearWaveUnit = iYearWave/100 - 1;
			if(iYearWaveUnit<0) iYearWaveUnit = 0;
			float fRefWave = 0.01f + iYearWaveUnit*0.008f;
			return fRefWave;
		}
		public boolean bCheck;
		/*
		 * 2016��ο�
		 * ΢С��ֵ����ͨ20�����£�
		 *     603737 = 510
		 *     002805 = 591
		 *     300421 = 576
		 *     300165 = 768
		 *     002810 = 405
		 * @С��ֵ����ͨ20-50�ڣ�
		 *     300583 = 825
		 *     600589 = 534
		 *     002374 = 627
		 *     002629 = 846
		 * ��С��ֵ����ͨ50-200�ڣ�
		 *     600096���컯 = 435
		 *     600845������� = 618
		 *     600485ʱ���²� = 462
		 *     000739����ҩҵ = 354
		 *     000666�Ļ�  = 450
		 *     000822ɽ������ = 414
		 * @����ֵ����ͨ200-500�ڣ�
		 *     TCL����000100 = 453
		 *     ͨ������600867 = 228
		 *     600606�̵ؿع�  = 414
		 *     600699��ʤ����  = 507
		 *     000686����֤ȯ = 408
		 *     600160�޻��ɷ� = 495
		 * �д���ֵ����ͨ500-1000�ڣ�
		 *     601225����úҵ  = 552
		 *     600029�Ϸ�����  = 378
		 *     600011���ܹ���  = 153
		 *     600518����ҩҵ  = 243
		 *     600663½����  = 585
		 *     600031��һ�ع� = 300
		 * @����ֵ����ͨ1000-2500�ڣ�
		 *     ��̩֤ȯ601688 = 402
		 *     ��������601238 = 378
		 *     ��������000651 = 648
		 *     ���ĵ���000333 = 444
		 *     �й�̫��601601 = 189
		 * ������ֵ����ͨ2500�����ϣ�
		 *     �й�ʯ��601857 = 108
		 *     �й�����601988 = 93
		 *     �й��г�601766 = 306
		 *     �ַ�����600000 = 156
		 */
		public int iYearWave;
	}
	
	private ResultCheckPriceWave checkPriceWave_single(List<StockDay> list, int iCheck)
	{
		ResultCheckPriceWave cResultCheckPriceWave = new ResultCheckPriceWave();
		
		// �������ȷ��
		int iBegin = iCheck-200;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			iBegin = 0;
		}
		if(iEnd-iBegin<20)
		{
			return cResultCheckPriceWave;
		}
		
		//�жϲ����� �껯
		int iCntWaveB8 = 0;
		int iCntWaveB5 = 0;
		int iCntWaveB3 = 0;
		int iCntWaveB1 = 0;
		for(int i=iBegin+1; i<=iEnd; i++)
		{
			StockDay cStockDayBefore1 = list.get(i-1);
			StockDay cStockDay = list.get(i);
			float curZhangDieRate = (cStockDay.close() - cStockDayBefore1.close())/cStockDayBefore1.close();
			//BLog.output("TEST", "%s %.4f\n", cStockDay.date(), curZhangDieRate);
			float waveAbs = Math.abs(curZhangDieRate);
			if(waveAbs>=0.08)
			{
				iCntWaveB8++;
			}
			else if(waveAbs>=0.05)
			{
				iCntWaveB5++;
			}
			else if(waveAbs>=0.03)
			{
				iCntWaveB3++;
			}
			else if(waveAbs>=0.01)
			{
				iCntWaveB1++;
			}
		}
		
		int iYWC = (iCntWaveB8*4 + iCntWaveB5*3 + iCntWaveB3*2 + iCntWaveB1*1)*3;
//		BLog.output("TEST", "B8(%d) B5(%d) B3(%d) B1(%d) iYWC=%d\n", 
//				iCntWaveB8, iCntWaveB5, iCntWaveB3, iCntWaveB1, iYWC);
		cResultCheckPriceWave.bCheck = true;
		cResultCheckPriceWave.iYearWave = iYWC;
				
		return cResultCheckPriceWave;
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
		
		String stockID = "002474"; // 300163 300165
		ResultHistoryData cResultHistoryData = 
				cStockDataIF.getHistoryData(stockID, "2015-01-01", "2017-01-01");
		List<StockDay> list = cResultHistoryData.resultList;
		BLog.output("TEST", "Check stockID(%s) list size(%d)\n", stockID, list.size());
		
		s_StockDayListCurve.setCurve(list);
		
		EStockDayPriceWave cEStockDayPriceWave = new EStockDayPriceWave();
		
		for(int i = 0; i < list.size(); i++)  
        {  
			StockDay cCurStockDay = list.get(i);
	
			ResultCheckPriceWave cResultCheckPriceWave = cEStockDayPriceWave.checkPriceWave_single(list, i);
			BLog.output("TEST", "Check stockID(%s) refWave(%.3f)\n", cCurStockDay.date(), cResultCheckPriceWave.refWave());
			
        } 
		
		s_StockDayListCurve.generateImage();
		BLog.output("TEST", "Main End\n");
	}
	public static StockDayListCurve s_StockDayListCurve = new StockDayListCurve("EStockDayPriceWave.jpg");
}
