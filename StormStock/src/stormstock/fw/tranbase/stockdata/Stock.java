package stormstock.fw.tranbase.stockdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Stock {
	
	public Stock()
	{
		m_LatestStockInfo = new StockInfo();
		m_stockDayList = new ArrayList<StockDay>();
	}	 
	
	// ������¼۸�
	public float getLatestPrice()
	{
		if(m_stockDayList.size() > 0)
			return m_stockDayList.get(m_stockDayList.size()-1).close();
		else
			return 0.0f;
	}
	// ������¹�Ʊ��ʱ����
	public List<StockTime> getLatestStockTimeList()
	{
		if(m_stockDayList.size() > 0)
		{
			if(!m_stockDayList.get(m_stockDayList.size()-1).isEndTran())
				return m_stockDayList.get(m_stockDayList.size()-1).getStockTimeList();
		}
		return null;
	}
	
	// ������һ�������
	public String GetLastDate()
	{
		if(m_stockDayList.size() > 0)
			return m_stockDayList.get(m_stockDayList.size()-1).date();
		else
			return "0000-00-00";
	}
	// ������һ��Ŀ��̼�
	public float GetLastOpenPrice()
	{
		if(m_stockDayList.size() > 0)
			return m_stockDayList.get(m_stockDayList.size()-1).open();
		else
			return 0.0f;
	}
	// �������
	public float GetLastClosePrice()
	{
		if(m_stockDayList.size() > 0)
			return m_stockDayList.get(m_stockDayList.size()-1).close();
		else
			return 0.0f;
	}
	// ������һ��Ŀ���ʱ�İٷֱ�
	public float GetLastOpenRatio()
	{
		if(m_stockDayList.size() > 0)
		{
			float fYesterdayClose = GetLastYesterdayClosePrice();
			float fLastOpen = GetLastOpenPrice();
			float fLastOpenRatio = (fLastOpen - fYesterdayClose)/fYesterdayClose;
			return fLastOpenRatio;
		}
		else
			return 0.0f;
	}
	
	// �����һ���������̼۸�
	public float GetLastYesterdayClosePrice()
	{
		if(m_stockDayList.size() > 1) // 2������
			return m_stockDayList.get(m_stockDayList.size()-2).close();
		else if(m_stockDayList.size() > 0) // ֻ��һ����������վ��ǽ�
			return m_stockDayList.get(m_stockDayList.size()-1).open();
		else
			return 0.0f;
	}
	
	// ��õ�һ�������
	public String GetFirstDate()
	{
		if(m_stockDayList.size() > 0)
			return m_stockDayList.get(0).date();
		else
			return "0000-00-00";
	}
	
	// �����ǵ���
	public float GetInreaseRatio(int index)
	{
		return StockUtils.GetInreaseRatio(m_stockDayList, index);
	}
	public float GetInreaseRatio(String date)
	{
		return StockUtils.GetInreaseRatio(m_stockDayList, date);
	}


	// ���߼��㣬����date����ǰcount����߼۸�
	public float GetMA(int count, String date)
	{
		if(m_stockDayList.size() == 0) return 0.0f;
		float value = 0.0f;
		int iE = StockUtils.indexDayKBeforeDate(m_stockDayList, date);
		int iB = iE-count+1;
		if(iB<0) iB=0;
		float sum = 0.0f;
		int sumcnt = 0;
		for(int i = iB; i <= iE; i++)  
        {  
			StockDay cDayKData = m_stockDayList.get(i);  
			sum = sum + cDayKData.close();
			sumcnt++;
			//Log.outputConsole("%s %.2f\n", cDayKData.date, cDayKData.close);
        }
		value = sum/sumcnt;
		return value;
	}
	
	// ��ֵ���㣬����date����ǰcount����߼۸�
	public float GetHigh(int count, String date)
	{
		if(m_stockDayList.size() == 0) return 0.0f;
		float value = 0.0f;
		int iE = StockUtils.indexDayKBeforeDate(m_stockDayList, date);
		int iB = iE-count+1;
		if(iB<0) iB=0;
		for(int i = iB; i <= iE; i++)  
        {  
			StockDay cDayKData = m_stockDayList.get(i);  
			if(cDayKData.high() >= value)
			{
				value = cDayKData.high();
			}
			//Log.outputConsole("%s %.2f\n", cDayKData.date, cDayKData.close);
        }
		return value;
	}
	
	// ��ֵ���㣬����date����ǰcount����ͼ۸�
	public float GetLow(int count, String date)
	{
		if(m_stockDayList.size() == 0) return 0.0f;
		float value = 10000.0f;
		int iE = StockUtils.indexDayKBeforeDate(m_stockDayList, date);
		int iB = iE-count+1;
		if(iB<0) iB=0;
		for(int i = iB; i <= iE; i++)  
        {  
			StockDay cDayKData = m_stockDayList.get(i);  
			if(cDayKData.low() <= value)
			{
				value = cDayKData.low();
			}
			//Log.outputConsole("%s %.2f\n", cDayKData.date, cDayKData.close);
        }
		return value;
	}
	
	// ���ĳ���������
	public StockDay GetDayK(String date)
	{
		int i = StockUtils.indexDayK(m_stockDayList, date);
		if(i>=0)
		{
			return m_stockDayList.get(i);
		}
		else
		{
			return null;
		}
	}
	
	/*
	 * ******************************************************************************************
	 */
	
	public StockInfo getCurLatestStockInfo() { return m_LatestStockInfo; }
	public void setCurLatestStockInfo(StockInfo stockInfo) { m_LatestStockInfo = stockInfo; }
	
	public List<StockDay> getCurStockDayData() { return m_stockDayList; }
	public void setCurStockDayData(List<StockDay> stockDayData) { m_stockDayList = stockDayData; }
	
	/**
	 * ��Ա **********************************************************************
	 */
	private StockInfo m_LatestStockInfo;
	private List<StockDay> m_stockDayList;
}
