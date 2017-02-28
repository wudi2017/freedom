package stormstock.app.sample.regressiontestlite;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import stormstock.ori.stockdata.CommonDef.StockSimpleItem;
import stormstock.ori.stockdata.DataEngine;
import stormstock.ori.stockdata.DataWebStockAllList.ResultAllStockList;
import stormstock.fw.tranengine_lite.ANLLog;
import stormstock.fw.tranengine_lite.ANLStock;
import stormstock.fw.tranengine_lite.ANLStockDayKData;
import stormstock.fw.tranengine_lite.ANLDataProvider;

public class RunBTStrategyCD {
	// ����ͳ�ƽ��ȫ�ּ�¼
	public String stockId;
	public int succCnt = 0;
	public int failCnt = 0;
	public float initMoney = 10000.0f;
	public float curMoney = initMoney;
	

	// ������������
	public int indexDayK(List<ANLStockDayKData> dayklist, String dateStr)
	{
		int index = 0;
		for(int k = dayklist.size()-1; k >= 0; k-- )
		{
			ANLStockDayKData cDayKDataTmp = dayklist.get(k);
			if(cDayKDataTmp.date.compareTo(dateStr) <= 0)
			{
				index = k;
				break;
			}
		}
		return index;
	}
	// ����i��j�յļ۸��ֵ
	public float priceAve(List<ANLStockDayKData> dayklist, int i, int j)
	{
		float ave = 0.0f;
		for(int k = i; k<=j; k++ )
		{
			ANLStockDayKData cDayKDataTmp = dayklist.get(k);
			ave = ave + cDayKDataTmp.close;
		}
		ave = ave/(j-i+1);
		return ave;
	}
	// ����i��j�յ���߼۸�
	public float priceHigh(List<ANLStockDayKData> dayklist, int i, int j)
	{
		float high = 0.0f;
		for(int k = i; k<=j; k++ )
		{
			ANLStockDayKData cDayKDataTmp = dayklist.get(k);
			if(cDayKDataTmp.high > high) 
				high = cDayKDataTmp.high;
		}
		return high;
	}
	// ����i��j�յ���ͼ۸�
	public float priceLow(List<ANLStockDayKData> dayklist, int i, int j)
	{
		float low = 100000.0f;
		for(int k = i; k<=j; k++ )
		{
			ANLStockDayKData cDayKDataTmp = dayklist.get(k);
			if(cDayKDataTmp.low < low) 
				low = cDayKDataTmp.low;
		}
		return low;
	}
	// ����i��j�յ���߼۸������
	public int indexHigh(List<ANLStockDayKData> dayklist, int i, int j)
	{
		int index = i;
		float high = 0.0f;
		for(int k = i; k<=j; k++ )
		{
			ANLStockDayKData cDayKDataTmp = dayklist.get(k);
			if(cDayKDataTmp.high > high) 
			{
				high = cDayKDataTmp.high;
				index = k;
			}
		}
		return index;
	}
	// ����i��j�յ���ͼ۸������
	public int indexLow(List<ANLStockDayKData> dayklist, int i, int j)
	{
		int index = i;
		float low = 100000.0f;
		for(int k = i; k<=j; k++ )
		{
			ANLStockDayKData cDayKDataTmp = dayklist.get(k);
			if(cDayKDataTmp.low < low) 
			{
				low = cDayKDataTmp.low;
				index = k;
			}
		}
		return index;
	}
	// ����i��j�յĲ������Ȳ���,��ֵԽ�󲨶�Խ��
	public float waveParam(List<ANLStockDayKData> dayklist, int i, int j)
	{
		float wave = 0.0f;
		for(int k = i; k<=j; k++ )
		{
			ANLStockDayKData cDayKDataTmp = dayklist.get(k);
			float wave1 = Math.abs((cDayKDataTmp.close - cDayKDataTmp.open)/cDayKDataTmp.open);
			float wave2 = (cDayKDataTmp.high - cDayKDataTmp.low)/cDayKDataTmp.low;
			wave = wave + (wave1+wave2)/2;
		}
		wave = wave/(j-i+1);
		return wave;
	}
	
	// ȷ��i���Ƿ����´����ȵ�
	public class XiaCuoRange
	{
		public int iBeginIndex;
		public int iHighIndex;
		public int iLowIndex;
		public int iEndEndex;
		public float highPrice;
		public float lowPrice;
		public float maxZhenFu()
		{
			return (lowPrice-highPrice)/highPrice;
		}
		public float xiaCuoXieLv()
		{
			return maxZhenFu()/(iLowIndex-iHighIndex);
		}
	}
	public XiaCuoRange CheckXiaCuoRange(List<ANLStockDayKData> dayklist, int i)
	{
		String logstr;
		int iCheckE = i;
		// ��鵱ǰ�죬ǰ6��20���������㼱����������
		for(int iCheckB = i-6; iCheckB>=i-20; iCheckB--)
		{
			if(iCheckB >= 0)
			{
				// @ ��ߵ�����͵�������λ�õ��ж�
				boolean bCheckHighLowIndex = false;
				int indexHigh = indexHigh(dayklist, iCheckB, iCheckE);
				int indexLow = indexLow(dayklist, iCheckB, iCheckE);
				if(indexHigh>iCheckB && indexHigh<=(iCheckB+iCheckE)/2
						&& indexLow > (iCheckB+iCheckE)/2 && indexLow < iCheckE)
				{
					bCheckHighLowIndex = true;
				}
				
				// @ ��ߵ�����͵��´�����ж�
				boolean bCheckXiaCuo = false;
				float highPrice = dayklist.get(indexHigh).high;
				float lowPrice = dayklist.get(indexLow).low;
				float xiaCuoZhenFu = (lowPrice-highPrice)/highPrice;
				float xiaCuoMinCheck = -1.5f*0.01f*(indexLow-indexHigh);
				if(xiaCuoMinCheck > -0.06f) xiaCuoMinCheck = -0.06f;
				if(xiaCuoZhenFu < xiaCuoMinCheck)
				{
					bCheckXiaCuo = true;
				}
				
				// @ ǰ���䣬�������λ�ж�
				boolean bCheck3 = true;
				int cntDay = iCheckE-iCheckB;
				float midPrice = (highPrice+lowPrice)/2;
				for(int c= iCheckB; c<iCheckB + cntDay/3; c++)
				{
					if(dayklist.get(c).low < midPrice)
					{
						bCheck3 = false;
					}
				}
				for(int c= iCheckE; c>iCheckE - cntDay/3; c--)
				{
					if(dayklist.get(c).low > midPrice)
					{
						bCheck3 = false;
					}
				}
				
				if(bCheckHighLowIndex && 
						bCheckXiaCuo && 
						bCheck3)
				{
//						logstr = String.format("    Test findLatestXiaCuoRange [%s,%s] ZhenFu(%.3f,%.3f)\n",
//								dayklist.get(iCheckB).date,
//								dayklist.get(iCheckE).date,
//								xiaCuoZhenFu,xiaCuoMinCheck);
//						outputLog(logstr);
					
					XiaCuoRange retXiaCuoRange = new XiaCuoRange();
					retXiaCuoRange.iBeginIndex = iCheckB;
					retXiaCuoRange.iEndEndex = iCheckE;
					retXiaCuoRange.iHighIndex = indexHigh;
					retXiaCuoRange.iLowIndex = indexLow;
					retXiaCuoRange.highPrice = highPrice;
					retXiaCuoRange.lowPrice = lowPrice;
					return retXiaCuoRange;
				}
			}
		}
		return null;
	}
	
	// ���в��Խ���
	public String transectionAnalysis(List<ANLStockDayKData> dayklist, 
			String enterDate, float enterPrice)
	{
		int iMaxTranDays = 10;
		float zhisun = -0.03f;
		float zhiying = 0.03f;
		String logstr;

		int iTranBegin = indexDayK(dayklist, enterDate);
		float zhisunPrice = enterPrice * (1+zhisun);
		float zhiyingPrice = enterPrice * (1+zhiying);
		
		
		float outPrice = 0.0f;
		int outIndex = 0;
		for(int i = iTranBegin+1; i < iTranBegin + iMaxTranDays; i++ )
		{
			if(i >= dayklist.size())
			{
				// �������ݲ����ˣ���Ҫ�ȴ�����
				break;
			}
			ANLStockDayKData cDayKDataTmp = dayklist.get(i);
			if(cDayKDataTmp.low < zhisunPrice) {
				outPrice = zhisunPrice;
				outIndex = i;
				// ֹ�����
				break;
			}
			if(cDayKDataTmp.high > zhiyingPrice) {
				outPrice = zhiyingPrice;
				outIndex = i;
				// ֹӯ����
				break;
			}
			if(i == iTranBegin + iMaxTranDays - 1)
			{
				outPrice = cDayKDataTmp.close;
				outIndex = i;
				// ��ʱ����
				break;
			}
		}
		
		if(outIndex > 0)
		{
			float profit = (outPrice - enterPrice)/enterPrice;
			if(profit>0)
			{
				succCnt = succCnt + 1;
			}
			else
			{
				failCnt = failCnt + 1;
			}
			curMoney = curMoney*(1+profit);
			
			logstr = String.format("TRAN[ %s %.2f -> %s %.2f %.3f ]",
					dayklist.get(iTranBegin).date, enterPrice,
					dayklist.get(outIndex).date, outPrice, 
					profit);
			//outputLog(logstr);
		}
		else
		{
			logstr = String.format("TRAN[ %s %.2f -> wait 0.00 0.000 ]",
					dayklist.get(iTranBegin).date, enterPrice
					);
			//outputLog(logstr);
		}
		return logstr;
	}
	public void initProfitInfo(String inStockId) {
		stockId = inStockId;
		succCnt = 0;
		failCnt = 0;
		curMoney = initMoney;
	}
	public static void printProfitInfo() {
//			String logstr;
//			logstr = String.format("\n --- printProfitInfo (stockId) ---\n");
//			outputLog(logstr);
//			logstr = String.format("    succCnt: %d \n", succCnt);
//			outputLog(logstr);
//			logstr = String.format("    failCnt: %d \n", failCnt);
//			outputLog(logstr);
//			logstr = String.format("    initMoney: %.2f \n", initMoney);
//			outputLog(logstr);
//			logstr = String.format("    curMoney: %.2f \n", curMoney);
//			outputLog(logstr);
//			logstr = String.format("    totalProfit: %.3f \n", (curMoney- initMoney)/initMoney);
//			outputLog(logstr);
	}
	
	// ������Ʊid��fromDate���ڵ�toDate���ڣ�ֻ���enableLogMinDate���֮��ķ���log
	public void analysisOne(String id, String fromDate, String toDate, String enableLogMinDate)
	{
		initProfitInfo(id);
		
		String logstr;
		
		ANLStock cANLStock = ANLDataProvider.getANLStock(id);
		if(null == cANLStock) return;
		
		int iB = indexDayK(cANLStock.historyData, fromDate);
		int iE = indexDayK(cANLStock.historyData, toDate);
		int iLogEnable = indexDayK(cANLStock.historyData, enableLogMinDate);
		boolean bLogEnable = false;
		//String dateLogEnable = cANLStock.historyData.get(retXiaCuoRange.iBeginIndex).date;

		XiaCuoRange cLastXiaCuoRange = new XiaCuoRange();
		for(int i = iB; i<= iE; i++)
		{
			
//				logstr = String.format("CheckDay %s\n",
//						cANLStock.historyData.get(i).date);
//				outputLog(logstr);
			if(i >= iLogEnable) bLogEnable = true;
			
			// ����ȷ��
			XiaCuoRange retXiaCuoRange = CheckXiaCuoRange(cANLStock.historyData, i);
			if(null != retXiaCuoRange && cLastXiaCuoRange.highPrice != retXiaCuoRange.highPrice)
			{
				
				float hisSucc = 0.0f;
				if(0!=(failCnt+succCnt)) hisSucc = (float)succCnt/(failCnt+succCnt);
				logstr = String.format("S( %s ) CXCR[ %s %s %.3f %.3f ] "
						+ "HIS[ %.3f %d ] ",
						stockId, cANLStock.historyData.get(retXiaCuoRange.iBeginIndex).date,
						cANLStock.historyData.get(retXiaCuoRange.iEndEndex).date,
						retXiaCuoRange.maxZhenFu(),retXiaCuoRange.xiaCuoXieLv(),
						hisSucc, failCnt+succCnt);
					
				// �ز�ȷ��
				int iEnterIndex = retXiaCuoRange.iEndEndex + 1;
				float enterPrice = 0.0f;
				float latastHigh = cANLStock.historyData.get(retXiaCuoRange.iEndEndex).high;
				for(int k = retXiaCuoRange.iEndEndex + 1; k<=retXiaCuoRange.iEndEndex + 6 && k<cANLStock.historyData.size(); k++)
				{
					if(cANLStock.historyData.get(k).low < (latastHigh + retXiaCuoRange.lowPrice)/2)
					{
						iEnterIndex = k;
						enterPrice = (latastHigh + retXiaCuoRange.lowPrice)/2;
						// ���ײ��Խ׶�
						logstr += transectionAnalysis(cANLStock.historyData, 
								cANLStock.historyData.get(iEnterIndex).date,enterPrice);
						break;
					}
					if(cANLStock.historyData.get(k).high > latastHigh) 
						latastHigh = cANLStock.historyData.get(k).high;
				}
			
				logstr+= "\n";
				if(bLogEnable)
					ANLLog.outputLog(logstr);
				
				cLastXiaCuoRange = retXiaCuoRange;
			}
		}
		
		printProfitInfo();
	}
	
	public static void main(String[] args) {
		RunBTStrategyCD objANLPolicy = new RunBTStrategyCD();
		ANLLog.outputConsole("Main Begin\n\n");
		// ��Ʊ�б�
		List<StockSimpleItem> cStockList = new ArrayList<StockSimpleItem>();
//			cStockList.add(new StockSimpleItem("300312"));
//			cStockList.add(new StockSimpleItem("300132"));
//			cStockList.add(new StockSimpleItem("002344"));
//			cStockList.add(new StockSimpleItem("002695"));
//			cStockList.add(new StockSimpleItem("300041"));
//			cStockList.add(new StockSimpleItem("600030"));
		if(cStockList.size() <= 0)
		{
			// cStockList =  DataEngine.getLocalRandomStock(30);
			ResultAllStockList cResultAllStockList = DataEngine.getLocalAllStock();
		}
		
		for(int i=0; i<cStockList.size();i++)
		{
			String stockId = cStockList.get(i).id;
			objANLPolicy.analysisOne(stockId, "2008-01-01", "2116-01-01", "2016-08-29");
		}
		
		ANLLog.outputConsole("\n\nMain End");
	}
}
