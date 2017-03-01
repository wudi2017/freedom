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

public class EStockDayVolumeLevel {
	
	/**
	 * 
	 * @author wudi
	 * ��鵱ǰλ�ý����Ƿ������ܻ�Ծ��
	 */
	public enum VOLUMELEVEL
	{
		ACTIVE, // ��Ծ��
		UNACTIVE, // ����Ծ
		DEATH, // ������
		UNKNOWN,
		INVALID,
	}
	public static  VOLUMELEVEL checkVolumeLevel(List<StockDay> list, int iCheck)
	{		
//		// ���㳤���ھ����� ȥ�����30�����30�����ƽ��ֵ
//		float aveVol200 = 0.0f;
//		{
//			int iBegin = iCheck - 200;
//			int iEnd = iCheck;
//			if(iBegin < 0)
//			{
//				return VOLUMELEVEL.INVALID;
//			}
//			List<Float> volList = new ArrayList<Float>();
//			for(int i= iBegin; i <= iEnd; i++)  
//	        {  
//				StockDay cCurStockDay = list.get(i);
//				volList.add(cCurStockDay.volume());
//	        }
//			Collections.sort(volList);
//			for(int i=0; i<30; i++)
//			{
//				volList.remove(0);
//				volList.remove(volList.size()-1);
//			}
//			for(int i= 0; i < volList.size(); i++)  
//	        {  
//				aveVol200 = aveVol200 + volList.get(i);
//	        }
//			aveVol200 = aveVol200/volList.size();
//		}
//		BLog.output("TEST", "aveVol200 $.3f\n", aveVol200);
		
		// �����г����ھ����� ȥ�����5�������5�����ƽ��ֵ
		float aveVol60 = 0.0f;
		{
			int iBegin = iCheck - 60;
			int iEnd = iCheck;
			if(iBegin < 0)
			{
				return VOLUMELEVEL.INVALID;
			}
			List<Float> volList = new ArrayList<Float>();
			for(int i= iBegin; i <= iEnd; i++)  
	        {  
				StockDay cCurStockDay = list.get(i);
				volList.add(cCurStockDay.volume());
	        }
			Collections.sort(volList);
			volList.remove(0);
			volList.remove(0);
			volList.remove(0);
			volList.remove(0);
			volList.remove(0);
			volList.remove(volList.size()-1);
			volList.remove(volList.size()-1);
			volList.remove(volList.size()-1);
			volList.remove(volList.size()-1);
			volList.remove(volList.size()-1);
			for(int i= 0; i < volList.size(); i++)  
	        {  
				aveVol60 = aveVol60 + volList.get(i);
	        }
			aveVol60 = aveVol60/volList.size();
		}
		
		// �������ھ����� ȥ�����5�������5�����ƽ��ֵ
		float aveVol20 = 0.0f;
		{
			int iBegin = iCheck - 20;
			int iEnd = iCheck;
			if(iBegin < 0)
			{
				return VOLUMELEVEL.INVALID;
			}
			List<Float> volList = new ArrayList<Float>();
			for(int i= iBegin; i <= iEnd; i++)  
	        {  
				StockDay cCurStockDay = list.get(i);
				volList.add(cCurStockDay.volume());
	        }
			Collections.sort(volList);
			volList.remove(0);
			volList.remove(0);
			volList.remove(0);
			volList.remove(0);
			volList.remove(0);
			volList.remove(volList.size()-1);
			volList.remove(volList.size()-1);
			volList.remove(volList.size()-1);
			volList.remove(volList.size()-1);
			volList.remove(volList.size()-1);
			for(int i= 0; i < volList.size(); i++)  
	        {  
				aveVol20 = aveVol20 + volList.get(i);
	        }
			aveVol20 = aveVol20/volList.size();
		}
		
		
		// ������վ��� ȥ�����1�������1�����ƽ��ֵ
		float aveVol5 = 0.0f;
		{
			int iBegin = iCheck - 5;
			int iEnd = iCheck;
			if(iBegin < 0)
			{
				return VOLUMELEVEL.INVALID;
			}
			List<Float> volList = new ArrayList<Float>();
			for(int i= iBegin; i <= iEnd; i++)  
	        {  
				StockDay cCurStockDay = list.get(i);
				volList.add(cCurStockDay.volume());
	        }
			Collections.sort(volList);
			volList.remove(0);
			volList.remove(volList.size()-1);
			for(int i= 0; i < volList.size(); i++)  
	        {  
				aveVol5 = aveVol5 + volList.get(i);
	        }
			aveVol5 = aveVol5/volList.size();
		}
		
		// �����ɽ����ж�
		StockDay cStockDay = list.get(iCheck);
		if(cStockDay.volume()/aveVol5 < 0.8
				&& cStockDay.volume()/aveVol20 < 0.8
				&& cStockDay.volume()/aveVol60 < 0.8
//				&& cStockDay.volume()/aveVol200 < 1.5
				)
			
		{
			return VOLUMELEVEL.DEATH;
		}
		
		// ��Ծ�ɽ����ж�
		if(aveVol5/aveVol20 > 1.2f)
		{
			return VOLUMELEVEL.ACTIVE;
		}
		
		if(aveVol5/aveVol60 < 0.7f)
		{
			return VOLUMELEVEL.UNACTIVE;
		}
		return VOLUMELEVEL.UNKNOWN;
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

		String stockID = "000151"; // 300163 300165 000401 300439
		ResultHistoryData cResultHistoryData = 
				cStockDataIF.getHistoryData(stockID, "2010-09-01", "2012-01-01");
		List<StockDay> list = cResultHistoryData.resultList;
		BLog.output("TEST", "Check stockID(%s) list size(%d) end(%s)\n", 
				stockID, list.size(), list.get(list.size()-1).date());
		
		s_StockDayListCurve.setCurve(list);
		
		for(int i = 0; i < list.size(); i++)  
        {  
			StockDay cCurStockDay = list.get(i);

			VOLUMELEVEL volLev = EStockDayVolumeLevel.checkVolumeLevel(list, i);
			if (volLev == VOLUMELEVEL.DEATH)
			{
				BLog.output("TEST", "CheckPoint %s\n", cCurStockDay.date());
				s_StockDayListCurve.markCurveIndex(i, "A");
			}
        } 
		
		s_StockDayListCurve.generateImage();
		BLog.output("TEST", "Main End\n");
	}
	public static StockDayListCurve s_StockDayListCurve = new StockDayListCurve("EStockDayVolumeLevel.jpg");
}
