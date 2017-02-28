package stormstock.fw.tranengine_lite;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import stormstock.ori.stockdata.CommonDef.*;

import stormstock.ori.stockdata.DataEngine;

public class ANLStock {
	
	public ANLStock()
	{
		historyData = new ArrayList<ANLStockDayKData>();
		curBaseInfo = new StockBaseInfo();
		m_eigenObjMap = null;
	}	 
	public ANLStock(String sid, StockBaseInfo scurBaseInfo)
	{
		id = sid;
		curBaseInfo = scurBaseInfo;
		historyData = new ArrayList<ANLStockDayKData>();
		curBaseInfo = new StockBaseInfo();
		m_eigenObjMap = null;
	}	 
	
	// ������һ����������̼�
	public float GetLastYesterdayClosePrice()
	{
		if(historyData.size() > 1) // 2������
			return historyData.get(historyData.size()-2).close;
		else if(historyData.size() > 0) // ֻ��һ����������վ��ǽ�
			return historyData.get(historyData.size()-1).open;
		else
			return 0.0f;
	}
	
	// ������һ��Ŀ��̼�
	public float GetLastOpenPrice()
	{
		if(historyData.size() > 0)
			return historyData.get(historyData.size()-1).open;
		else
			return 0.0f;
	}

	// ������һ��Ŀ���ʱ�İٷֱ�
	public float GetLastOpenRatio()
	{
		if(historyData.size() > 0)
		{
			float fYesterdayClose = GetLastYesterdayClosePrice();
			float fLastOpen = GetLastOpenPrice();
			float fLastOpenRatio = (fLastOpen - fYesterdayClose)/fYesterdayClose;
			return fLastOpenRatio;
		}
		else
			return 0.0f;
	}
	
	// ������һ������̼�
	public float GetLastClosePrice()
	{
		if(historyData.size() > 0)
			return historyData.get(historyData.size()-1).close;
		else
			return 0.0f;
	}
	
	// ������һ�������
	public String GetLastDate()
	{
		if(historyData.size() > 0)
			return historyData.get(historyData.size()-1).date;
		else
			return "0000-00-00";
	}
		
	// ���߼��㣬����date����ǰcount����߼۸�
	public float GetMA(int count, String date)
	{
		if(historyData.size() == 0) return 0.0f;
		float value = 0.0f;
		int iE = ANLUtils.indexDayKBeforeDate(historyData, date);
		int iB = iE-count+1;
		if(iB<0) iB=0;
		float sum = 0.0f;
		int sumcnt = 0;
		for(int i = iB; i <= iE; i++)  
        {  
			ANLStockDayKData cANLDayKData = historyData.get(i);  
			sum = sum + cANLDayKData.close;
			sumcnt++;
			//ANLLog.outputConsole("%s %.2f\n", cANLDayKData.date, cANLDayKData.close);
        }
		value = sum/sumcnt;
		return value;
	}
	
	// ��ֵ���㣬����date����ǰcount����߼۸�
	public float GetHigh(int count, String date)
	{
		if(historyData.size() == 0) return 0.0f;
		float value = 0.0f;
		int iE = ANLUtils.indexDayKBeforeDate(historyData, date);
		int iB = iE-count+1;
		if(iB<0) iB=0;
		for(int i = iB; i <= iE; i++)  
        {  
			ANLStockDayKData cANLDayKData = historyData.get(i);  
			if(cANLDayKData.high >= value)
			{
				value = cANLDayKData.high;
			}
			//ANLLog.outputConsole("%s %.2f\n", cANLDayKData.date, cANLDayKData.close);
        }
		return value;
	}
	
	// ��ֵ���㣬����date����ǰcount����ͼ۸�
	public float GetLow(int count, String date)
	{
		if(historyData.size() == 0) return 0.0f;
		float value = 10000.0f;
		int iE = ANLUtils.indexDayKBeforeDate(historyData, date);
		int iB = iE-count+1;
		if(iB<0) iB=0;
		for(int i = iB; i <= iE; i++)  
        {  
			ANLStockDayKData cANLDayKData = historyData.get(i);  
			if(cANLDayKData.low <= value)
			{
				value = cANLDayKData.low;
			}
			//ANLLog.outputConsole("%s %.2f\n", cANLDayKData.date, cANLDayKData.close);
        }
		return value;
	}
	
	public void addEigenMap(Map<String, ANLEigen> cEngenMap)
	{
		m_eigenObjMap = cEngenMap;
	}
	public Object getEngen(String name, Object... args)
	{
		if(null == m_eigenObjMap) return null;
		ANLEigen cANLEigen = m_eigenObjMap.get(name);
		if(null == cANLEigen) return null;
		Object engenObj = cANLEigen.calc(this, args);
		return engenObj;
	}
	
	public String id;
	public StockBaseInfo curBaseInfo;
	public List<ANLStockDayKData> historyData;
	private Map<String, ANLEigen> m_eigenObjMap;
}
