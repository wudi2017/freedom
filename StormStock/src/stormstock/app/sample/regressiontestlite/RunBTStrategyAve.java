package stormstock.app.sample.regressiontestlite;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import stormstock.fw.tranengine_lite.ANLStock;
import stormstock.fw.tranengine_lite.ANLStockDayKData;
import stormstock.fw.tranengine_lite.ANLDataProvider;
import stormstock.ori.stockdata.CommonDef.StockSimpleItem;
import stormstock.ori.stockdata.DataEngine;
import stormstock.ori.stockdata.DataWebStockAllList.ResultAllStockList;

public class RunBTStrategyAve {
	public static Formatter fmt = new Formatter(System.out);
	public static String strLogName = "ANLPolicyAve.txt";
	public static float fStopProfit = 1.0f;  //ֹӯ��
	public static float fStopLoss = 2.0f;  //ֹ���
	public static int nMaxTradeDays = 5;  //�����ʱ�䣨�죩
	
	public static void rmlog()
	{
		File cfile =new File(strLogName);
		cfile.delete();
	}
	public static void outputLog(String s, boolean enable)
	{
		if(!enable) return;
		fmt.format("%s", s);
		File cfile =new File(strLogName);
		try
		{
			FileOutputStream cOutputStream = new FileOutputStream(cfile, true);
			cOutputStream.write(s.getBytes());
			cOutputStream.close();
		}
		catch(Exception e)
		{
			System.out.println("Exception:" + e.getMessage()); 
		}
	}
	public static void outputLog(String s)
	{
		outputLog(s, true);
	}
	
	//��������
	public static class BounceData implements Comparable
	{
		public String id;  //��ƱID
		public int total;  //����������ǰ����һ���ϴ��Ƿ������Ѿ����������߸������Ĵ���
		public int incCount;  //�������������ǣ����������ߺ�5�����Ƿ�����2���㣩�Ĵ���
		public int decCount;  //�����������µ������������ߺ�5���ڵ�������2���㣩�Ĵ���
		public float incRate;  //������
		public float decRate;  //�µ���
		@Override
		public int compareTo(Object arg0) {
			// TODO Auto-generated method stub
			BounceData sdto = (BounceData)arg0;
			if(this.incRate <= sdto.incRate)
			{
				return -1;
			}
			else
			{
				return 1;
			}
		}
	}
	
	/*
	 * ���ܣ��ж�δ������������Ƿ��Ƿ񳬹�incRate
	 * ������today������������������dayK_list������
	 * ������dayK_list����K����
	 * ����ֵ���Ƿ�ɹ�
	 * */
	public static boolean isIncInFuture(int today, List<ANLStockDayKData> dayK_list)
	{
		ANLStockDayKData cTodayKData = dayK_list.get(today);
		float todayClose = cTodayKData.close;
		int nDayAfter = today+nMaxTradeDays+2;
		if(nDayAfter >= dayK_list.size())
		{
			return false;
		}
		
		//�����������2���򣬵�3����ܿ�ʼ���ף����Դӵ�3�쿪ʼ��
		for(int i=today+2; i<=nDayAfter ; ++i)
		{
			ANLStockDayKData cDayKData = dayK_list.get(i);
			
			//���������ȵ�����ֹ��λ������ʧ��
			if(cDayKData.close < todayClose)
			{
				float decRate = (todayClose-cDayKData.close)/todayClose*100;
				if(decRate >= fStopLoss)
				{
					break;
				}
			}
			
			if(cDayKData.close > todayClose)
			{
				float realRate = (cDayKData.close-todayClose)/todayClose*100;
				if(realRate >= fStopProfit)
				{
					//outputLog(String.format("future inc rate:%.2f\n", realRate));
					return true;
				}
			}
		}
		
		
		return false;
	}
	
	/*
	 * ���ܣ��ж�δ���������������Ƿ񳬹�decRate
	 * ������today������������������dayK_list������
	 * ������dayK_list����K����
	 * ����ֵ���Ƿ�ɹ�
	 * */
	public static boolean isDecInFuture(int today, List<ANLStockDayKData> dayK_list)
	{
		ANLStockDayKData cTodayKData = dayK_list.get(today);
		float todayClose = cTodayKData.close;
		int nDayAfter = today+nMaxTradeDays+2;
		if(nDayAfter >= dayK_list.size())
		{
			return false;
		}
		
		//�����������2���򣬵�3����ܿ�ʼ���ף����Դӵ�3�쿪ʼ��
		for(int i=today+2; i<=nDayAfter ; ++i)
		{
			ANLStockDayKData cDayKData = dayK_list.get(i);
			
			//���������ȵ�����ֹ��λ������ʧ��
			if(cDayKData.close < todayClose)
			{
				float realDecRate = (todayClose-cDayKData.close)/todayClose*100;
				if(realDecRate >= fStopLoss)
				{
					return true;
				}
			}
			
			if(cDayKData.close > todayClose)
			{
				float realRate = (cDayKData.close-todayClose)/todayClose*100;
				if(realRate >= fStopProfit)
				{
					//outputLog(String.format("future inc rate:%.2f\n", realRate));
					break;
				}
			}
		}
		
		
		return false;
	}
	
	/*
	 * ���ܣ��ж�����Ƿ�ǿ��
	 * ������day��������
	 * ������incRate���Ƿ�����
	 * ������today������������������dayK_list������
	 * ������dayK_list����K����
	 * ����ֵ���Ƿ�ɹ�
	 * */
	public static boolean isRecentStrong(int day, float incRate, int today, List<ANLStockDayKData> dayK_list)
	{
		int nDayBefore = today-day;
		if(nDayBefore < 0)
		{
			return false;
		}
		
		float low = 1000.0f;
		float high = 0.0f;
		ANLStockDayKData cNDayBeforeKData = dayK_list.get(nDayBefore);
		for(int i=nDayBefore; i<=today ; ++i)
		{
			ANLStockDayKData cDayKData = dayK_list.get(i);
			if(cDayKData.close < low)
			{
				low = cDayKData.close;
			}
			if(cDayKData.close > high)
			{
				high = cDayKData.close;
			}
		}
		
		int midIndex = today-day/2;
		ANLStockDayKData cMidDayKData = dayK_list.get(midIndex);
		
		if(cMidDayKData.close > cNDayBeforeKData.close) //�ж��Ƿ�������
		{
			float realIncRate = (high-low)/low*100;
			if(realIncRate>=incRate)
			{
				//System.out.println(String.format("date:%s,incRate:%.2f\n", cTodayKData.date,realIncRate));
				return true;
			}
		}
		
		return false;
	}
	
	/*
	 * ���ܣ�����n�վ���
	 * n�����վ���
	 * ������index��Ҫ�����������dayK_list�е�����
	 * ������dayK_list����K����
	 * ����ֵ������
	 * */
	public static float getNDayAverage(int n, int index, List<ANLStockDayKData> dayK_list)
	{
		if(n <= 0)
		{
			return 0.0f;
		}
		
		if(-1 == index)
		{
			return 0.0f;
		}
		
		int indexNDayBefore = index-(n-1);
		if(indexNDayBefore < 0)
		{
			indexNDayBefore = 0;
		}
		int count = index-indexNDayBefore+1;
		
		float nDayAve = 0.0f;
		for(int j=indexNDayBefore; j<=index ; ++j)
		{
			ANLStockDayKData cDayKData = dayK_list.get(j);
			nDayAve += cDayKData.close;
		}
		
		nDayAve /= count;
		
		return nDayAve;
	}
	
	/*
	 * ���ܣ��ж������Ƿ����㣨������ͼ۵��ƻ�ӽ����߼۸���ǰ�ڽ�ǿ�ƣ�
	 * ������nAveOfDays�����վ���
	 * ������index��Ҫ�����������dayK_list�е�����
	 * ������dayK_list����K����
	 * ����ֵ���Ƿ���������
	 * */
	public static boolean isDaySatisfied(int nAveOfDays,int index,List<ANLStockDayKData> dayK_list)
	{
		if(index < 0)
		{
			return false;
		}
		
		if(index >= dayK_list.size())
		{
			return false;
		}
		
		float fNDayAve = getNDayAverage(nAveOfDays,index,dayK_list);
		
		ANLStockDayKData cDayKData = dayK_list.get(index);
		if(cDayKData.close<cDayKData.open  //�������µ���
				&& cDayKData.open>fNDayAve  //���̲�û���ƾ���
				&& (cDayKData.low/*-0.15*/)<=fNDayAve)  //��͵���ƻ�ӽ�����(TODO��0.15ֻ�Ǵ��ֵ)
		{
			int day = 10;
			float incRate = 15.0f;
			switch(nAveOfDays)
			{
				case 5:
				{
					day = 5;
					incRate = 10.0f;
				}
					break;
				case 10:
					break;
				case 20:
				{
					day = 10;
					incRate = 15.0f;
				}
					break;
				case 30:
				{
					day = 15;
					incRate = 20.0f;
				}
					break;
				case 60:
					break;
				default:
					break;
			}
			
			if(isRecentStrong(day,incRate,index,dayK_list))  //���ҽ���ǿ��
			{
				return true;
			}
		}
		
		return false;
	}

	/*
	 * ���ܣ�����ĳֻ��Ʊ
	 * ������id����ƱID
	 * ������bounceList�����������Ĺ�Ʊ�ķ�������
	 * ������nAveOfDays�����վ���
	 * ������bCalToday:�Ƿ�ֻ�����������������
	 * */
	public static void analyzeOne(String id,List<BounceData> bounceList,int nAveOfDays,boolean bCalToday)
	{
		ANLStock cANLStock = ANLDataProvider.getANLStock(id);
		if(null == cANLStock)
		{
			return;
		}
		
		int size = cANLStock.historyData.size();
		if(size < nAveOfDays)
		{
			return;
		}

		if(bCalToday)
		{
			//�������һ��
			if(!isDaySatisfied(nAveOfDays,size-1,cANLStock.historyData))
			{
				return;
			}
		}
		
		int total = 0;
		int incCount = 0;
		int decCount = 0;
		
		for (int i =nAveOfDays; i< size; i++)
		{
			if(isDaySatisfied(nAveOfDays,i,cANLStock.historyData))
			{
				++total;
				
				if(isIncInFuture(i,cANLStock.historyData))
				{
					//ANLStockDayKData cDayKData = cANLStock.historyData.get(i);
					//System.out.println(cDayKData.date);
					++incCount;
				}
				
				if(isDecInFuture(i,cANLStock.historyData))
				{
					++decCount;
				}
			}
		}
		
		if(total > 15)
		{
			float incRate = (float)incCount/total*100;
			float decRate = (float)decCount/total*100;
			
			BounceData tempData = new BounceData();
			tempData.id = id;
			tempData.incCount = incCount;
			tempData.decCount = decCount;
			tempData.total = total;
			tempData.incRate = incRate;
			tempData.decRate = decRate;
			
			bounceList.add(tempData);
		}
	}

	
	public static void main(String[] args) {
		rmlog();
		outputLog("Main Begin\n\n");
		
		// ��Ʊ�б�
		List<StockSimpleItem> cStockList = new ArrayList<StockSimpleItem>();
		//cStockList.add(new StockItem("002054"));
		if(cStockList.size() <= 0)
		{
			ResultAllStockList cResultAllStockList = DataEngine.getLocalAllStock();
		}
		
		List<BounceData> bounceList = new ArrayList<BounceData>();
		
		boolean bCalToday = false;
		int nAveOfDay = 20;
		
		for(int i=0; i<cStockList.size();i++)
		{
			String stockId = cStockList.get(i).id;
			
			analyzeOne(stockId,bounceList,nAveOfDay,bCalToday);
		}

		//���ɹ��ʽ���
		Collections.sort(bounceList);
		Collections.reverse(bounceList);
		
		int total = 0;
		int incCount = 0;
		float incRate = 0.0f;
		int decCount = 0;
		float decRate = 0.0f;
		
		//��ӡ��ÿ����Ʊ�ķ�������
		for(int i=0; i<bounceList.size();i++)
		{
			BounceData tempData = bounceList.get(i);
			total += tempData.total;
			incCount += tempData.incCount;
			decCount += tempData.decCount;
			
			String strLog = String.format("%d:,  id:%s,total:%d,incCount:%d,decCount:%d,incRate:%.2f,decRate:%.2f\n",
					i,tempData.id,tempData.total,tempData.incCount,tempData.decCount,tempData.incRate,tempData.decRate);
			outputLog(strLog);
		}

		if(!bCalToday)
		{
			//��ӡ�����ܵķ�������
			if(total > 0)
			{
				incRate = (float)incCount/total*100;
				decRate = (float)decCount/total*100;
			}
			
			String strLog = String.format("sum:  total:%d,incCount:%d,decCount:%d,incRate:%.2f,decRate:%.2f\n",
					total,incCount,decCount,incRate,decRate);
			outputLog(strLog);
		}
		
		
		outputLog("\n\nMain End");
	}
}
