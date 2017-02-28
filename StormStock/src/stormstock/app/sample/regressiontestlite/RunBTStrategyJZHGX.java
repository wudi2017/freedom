package stormstock.app.sample.regressiontestlite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import stormstock.fw.tranengine_lite.ANLImgShow;
import stormstock.fw.tranengine_lite.ANLLog;
import stormstock.fw.tranengine_lite.ANLStock;
import stormstock.fw.tranengine_lite.ANLStockDayKData;
import stormstock.fw.tranengine_lite.ANLStockPool;
import stormstock.fw.tranengine_lite.ANLUserAcc;

public class RunBTStrategyJZHGX  {
//	
//	// ��Ʊ��ֵ�����ڸ�ÿ��Ĺ�Ʊ���
//	static class ANLPolicyStockCK
//	{
//		public String stockID;
//		
//		public float param_PianLiBi; // ƫ��ȷ�ֵ
//		public float mingci_PianLiBi; // ƫ�������
//		
//		public float param_XiaCuo; // �����´��ֵ
//		public float mingci_XiaCuo; // �´�����
//		
//		// ��ֵ�ع��ֵ   ϵ��(��ǰ��-�������)+(��ǰ��-�������)+(��ǰ��-15�վ�)
//		// ǰ3����������ֵ��    ����Ҫѹ��λ�ϴ��Ի���飬20 60�վ��� �� - ����Ҫ֧��λ�´�����
//		// ���׷�ֵ�� ����5�ս��״�����ͻ�ƴ���
//		// ϴ�̷�ֵ�� ���������ߴ�����ֱ��һ���Ի�����Ҫ֧��λ
//		
//		static class ZongHeCompare implements Comparator 
//		{
//			public int compare(Object object1, Object object2) {
//				ANLPolicyStockCK ck1 = (ANLPolicyStockCK)object1;
//				ANLPolicyStockCK ck2 = (ANLPolicyStockCK)object2;
//				float zonghe1 = ck1.mingci_PianLiBi * 7/10.0f + ck1.mingci_XiaCuo * 3/10.0f;
//				float zonghe2 = ck2.mingci_PianLiBi * 7/10.0f + ck2.mingci_XiaCuo * 3/10.0f;
//				if(zonghe1 <= zonghe2)
//				{
//					return -1;
//				}
//				else
//				{
//					return 1;
//				}
//			}
//		}
//		static class PianLiBiCompare implements Comparator 
//		{
//			public int compare(Object object1, Object object2) {
//				ANLPolicyStockCK ck1 = (ANLPolicyStockCK)object1;
//				ANLPolicyStockCK ck2 = (ANLPolicyStockCK)object2;
//				if(ck1.param_PianLiBi <= ck2.param_PianLiBi)
//				{
//					return -1;
//				}
//				else
//				{
//					return 1;
//				}
//			}
//		}
//		static class XiaCuoCompare implements Comparator 
//		{
//			public int compare(Object object1, Object object2) {
//				ANLPolicyStockCK ck1 = (ANLPolicyStockCK)object1;
//				ANLPolicyStockCK ck2 = (ANLPolicyStockCK)object2;
//				if(ck1.param_XiaCuo <= ck2.param_XiaCuo)
//				{
//					return -1;
//				}
//				else
//				{
//					return 1;
//				}
//			}
//		}
//		static public void sortZongHe(List<ANLPolicyStockCK> cList)
//		{
//			Collections.sort(cList, new ANLPolicyStockCK.PianLiBiCompare()); //ƫ�������
//			for(int i=0; i<cList.size(); i++)
//			{
//				int mingci = i+1;
//				ANLPolicyStockCK cANLPolicyStockCK = cList.get(i);
//				cANLPolicyStockCK.mingci_PianLiBi = mingci;
//			}
//			Collections.sort(cList, new ANLPolicyStockCK.XiaCuoCompare()); //ƫ�������
//			for(int i=0; i<cList.size(); i++)
//			{
//				int mingci = i+1;
//				ANLPolicyStockCK cANLPolicyStockCK = cList.get(i);
//				cANLPolicyStockCK.mingci_XiaCuo = mingci;
//			}
//			Collections.sort(cList, new ANLPolicyStockCK.ZongHeCompare()); // �������ۺ�����
//		}
//	}
//	
//	// ƫ��ȷ��������࣬����������ڳ��ڶ�������Եײ��ķ�ֵ
//	static class CalPianLiBi
//	{
//		static float calculate(ANLStock cANLStock)
//		{
//			if(cANLStock.historyData.size() == 0) 
//				return -1.0f;
//			String date = cANLStock.historyData.get(cANLStock.historyData.size()-1).date;
//			// ���ڼ۸��ֵ��Ϊ��׼ֵ
//			float long_base_price = cANLStock.GetMA(500, date);
//			// ��ǰ�۸����
//			float cur_price = cANLStock.historyData.get(cANLStock.historyData.size()-1).close;
//			float cur_pricePa = (cur_price - long_base_price)/long_base_price;
//			
//			// �������ƫ���
//			float short_low_price = cANLStock.GetLow(20, date);
//			float short_low_pricePa = (short_low_price - long_base_price)/long_base_price;
//			float short_high_price = cANLStock.GetHigh(20, date);
//			float short_high_pricePa = (short_high_price - long_base_price)/long_base_price;
//			float short_pianlirate = 0.0f;
//			if(short_high_pricePa-short_low_pricePa != 0)
//				short_pianlirate = (cur_pricePa-short_low_pricePa)/(short_high_pricePa-short_low_pricePa);
//
//			// ��������ƫ���
//			float mid_low_price = cANLStock.GetLow(60, date);
//			float mid_low_pricePa = (mid_low_price - long_base_price)/long_base_price;
//			float mid_high_price = cANLStock.GetHigh(60, date);
//			float mid_high_pricePa = (mid_high_price - long_base_price)/long_base_price;
//			float mid_pianlirate = 0.0f;
//			if(mid_high_pricePa-mid_low_pricePa != 0)
//				mid_pianlirate = (cur_pricePa-mid_low_pricePa)/(mid_high_pricePa-mid_low_pricePa);
//
//			// ���㳤��ƫ���
//			float long_low_price = cANLStock.GetLow(250, date);
//			float long_low_pricePa = (long_low_price - long_base_price)/long_base_price;
//			float long_high_price = cANLStock.GetHigh(250, date);
//			float long_high_pricePa = (long_high_price - long_base_price)/long_base_price;
//			float long_pianlirate = 0.0f;
//			if(long_high_pricePa-long_low_pricePa != 0)
//				long_pianlirate = (cur_pricePa-long_low_pricePa)/(long_high_pricePa-long_low_pricePa);
//			
//			// ���ھ�ֵ����
//			float fenshu = short_pianlirate*(3/10.0f) 
//					+ mid_pianlirate*(4/10.0f) 
//					+ long_pianlirate*(3/10.0f);
//			
//			String logstr = String.format(" --- Test ---  PianLiBi %s [%.2f %.2f %.2f %.2f] %.2f\n",
//					cANLStock.id, 
//					fenshu, short_pianlirate,mid_pianlirate,long_pianlirate, 
//					cur_price);
//			// OutLog(logstr);
//			
//			return fenshu;
//		}
//	}
//	
//	static class DuanQiXiaCuo
//	{
//		// ȷ��i���Ƿ����´����ȵ�
//		static public class XiaCuoRange
//		{
//			public int iBeginIndex;
//			public int iHighIndex;
//			public int iLowIndex;
//			public int iEndEndex;
//			public float highPrice;
//			public float lowPrice;
//			public float maxZhenFu()
//			{
//				return (lowPrice-highPrice)/highPrice;
//			}
//			public float xiaCuoXieLv()
//			{
//				return maxZhenFu()/(iLowIndex-iHighIndex);
//			}
//		}
//		// ����i��j�յ���߼۸������
//		static public int indexHigh(List<ANLStockDayKData> dayklist, int i, int j)
//		{
//			int index = i;
//			float high = 0.0f;
//			for(int k = i; k<=j; k++ )
//			{
//				ANLStockDayKData cDayKDataTmp = dayklist.get(k);
//				if(cDayKDataTmp.high > high) 
//				{
//					high = cDayKDataTmp.high;
//					index = k;
//				}
//			}
//			return index;
//		}
//		// ����i��j�յ���ͼ۸������
//		static public int indexLow(List<ANLStockDayKData> dayklist, int i, int j)
//		{
//			int index = i;
//			float low = 100000.0f;
//			for(int k = i; k<=j; k++ )
//			{
//				ANLStockDayKData cDayKDataTmp = dayklist.get(k);
//				if(cDayKDataTmp.low < low) 
//				{
//					low = cDayKDataTmp.low;
//					index = k;
//				}
//			}
//			return index;
//		}
//		static public XiaCuoRange CheckXiaCuoRange(List<ANLStockDayKData> dayklist, int i)
//		{
//			String logstr;
//			int iCheckE = i;
//			// ��鵱ǰ�죬ǰ6��20���������㼱����������
//			for(int iCheckB = i-6; iCheckB>=i-20; iCheckB--)
//			{
//				if(iCheckB >= 0)
//				{
//					// @ ��ߵ�����͵�������λ�õ��ж�
//					boolean bCheckHighLowIndex = false;
//					int indexHigh = indexHigh(dayklist, iCheckB, iCheckE);
//					int indexLow = indexLow(dayklist, iCheckB, iCheckE);
//					if(indexHigh>iCheckB && indexHigh<=(iCheckB+iCheckE)/2
//							&& indexLow > (iCheckB+iCheckE)/2 && indexLow < iCheckE)
//					{
//						bCheckHighLowIndex = true;
//					}
//					
//					// @ ��ߵ�����͵��´�����ж�
//					boolean bCheckXiaCuo = false;
//					float highPrice = dayklist.get(indexHigh).high;
//					float lowPrice = dayklist.get(indexLow).low;
//					float xiaCuoZhenFu = (lowPrice-highPrice)/highPrice;
//					float xiaCuoMinCheck = -1.5f*0.01f*(indexLow-indexHigh);
//					if(xiaCuoMinCheck > -0.06f) xiaCuoMinCheck = -0.06f;
//					if(xiaCuoZhenFu < xiaCuoMinCheck)
//					{
//						bCheckXiaCuo = true;
//					}
//					
//					// @ ǰ���䣬�������λ�ж�
//					boolean bCheck3 = true;
//					int cntDay = iCheckE-iCheckB;
//					float midPrice = (highPrice+lowPrice)/2;
//					for(int c= iCheckB; c<iCheckB + cntDay/3; c++)
//					{
//						if(dayklist.get(c).low < midPrice)
//						{
//							bCheck3 = false;
//						}
//					}
//					for(int c= iCheckE; c>iCheckE - cntDay/3; c--)
//					{
//						if(dayklist.get(c).low > midPrice)
//						{
//							bCheck3 = false;
//						}
//					}
//					
//					if(bCheckHighLowIndex && 
//							bCheckXiaCuo && 
//							bCheck3)
//					{
////						outputLog("    Test findLatestXiaCuoRange [%s,%s] ZhenFu(%.3f,%.3f)\n",
////								dayklist.get(iCheckB).date,
////								dayklist.get(iCheckE).date,
////								xiaCuoZhenFu,xiaCuoMinCheck);
//						
//						XiaCuoRange retXiaCuoRange = new XiaCuoRange();
//						retXiaCuoRange.iBeginIndex = iCheckB;
//						retXiaCuoRange.iEndEndex = iCheckE;
//						retXiaCuoRange.iHighIndex = indexHigh;
//						retXiaCuoRange.iLowIndex = indexLow;
//						retXiaCuoRange.highPrice = highPrice;
//						retXiaCuoRange.lowPrice = lowPrice;
//						return retXiaCuoRange;
//					}
//				}
//			}
//			return null;
//		}
//		static float calculate(ANLStock cANLStock)
//		{
//			float param_XiaCuo = 100.0f;
//			String date = cANLStock.historyData.get(cANLStock.historyData.size()-1).date;
//			float cur_price = cANLStock.historyData.get(cANLStock.historyData.size()-1).close;
//			XiaCuoRange cXiaCuoRange = CheckXiaCuoRange(cANLStock.historyData, cANLStock.historyData.size()-1);
//			String logstr = "";
//			if(null != cXiaCuoRange)
//			{
//				param_XiaCuo = cXiaCuoRange.maxZhenFu();
//				
//				logstr = String.format(" --- Test ---  XiaCuo %s [%.3f %.3f %.3f %.3f %.3f] %.2f\n",
//						cANLStock.id, 
//						param_XiaCuo, cXiaCuoRange.highPrice, cXiaCuoRange.lowPrice, cXiaCuoRange.maxZhenFu(), cXiaCuoRange.xiaCuoXieLv(),
//						cur_price);
//			}
//			else
//			{
//				logstr = String.format(" --- Test ---  XiaCuo %s [%.3f  - - - -] %.2f\n",
//						cANLStock.id, 
//						param_XiaCuo,
//						cur_price);
//			}
//			// OutLog(logstr);
//			return param_XiaCuo;
//		}
//	}
//
//	public boolean strategy_preload(ANLStock cANLStock)
//	{
//		// for test
//		boolean bEnableTest = true;
//		if(bEnableTest)
//		{
//			List<String> testStockList = Arrays.asList(
//					"000001",
//					"600030"
//					);
//			for(int i=0; i< testStockList.size();i++)
//			{
//				if(cANLStock.id.compareTo(testStockList.get(i)) == 0 )
//				{
//					return true;
//				}
//			}
//			return false;
//		}
//		
//		// ��ֵ������˵�
//		if(cANLStock.curBaseInfo.allMarketValue > 200.0f 
//				|| cANLStock.curBaseInfo.circulatedMarketValue > 100.0f)
//		{
//			return false;
//		}
//		// ������ʷ����ʱ��϶̵Ĺ��˵�
//		if(cANLStock.historyData.size() < 250*2)
//		{
//			return false;
//		}
//		// PE��ӯ�ʹ���Ĺ��˵�
//		if(cANLStock.curBaseInfo.peRatio > 100.0f 
//				|| cANLStock.curBaseInfo.peRatio == 0)
//		{
//			return false;
//		}
//		// ���ֹ���
//		if(cANLStock.curBaseInfo.name.contains("S")
//				|| cANLStock.curBaseInfo.name.contains("*")
//				|| cANLStock.curBaseInfo.name.contains("N"))
//		{
//			return false;
//		}
//		
//		ANLLog.outputLog("add userpool %s %s\n", cANLStock.id, cANLStock.curBaseInfo.name);
//		return true;
//	}
//
//	public void strategy_today(String date, ANLStockPool spool)
//	{
//		ANLLog.outputLog("check_today %s --------------------------------- >>>\n", date);
//		
//		// ---------������Ʊ��ֵ�� ---------
//		List<ANLPolicyStockCK> stockCKList = new ArrayList<ANLPolicyStockCK>();
//		for(int i = 0; i < spool.stockList.size(); i++)
//		{
//			ANLStock cANLStock = spool.stockList.get(i);
//			ANLLog.outputLog("    %s EISample %.3f\n",cANLStock.id, cANLStock.getEngen("EISample"));
//			if(cANLStock.historyData.size() == 0) continue;
//			
//			ANLPolicyStockCK cANLPolicyStockCK = new ANLPolicyStockCK();
//			cANLPolicyStockCK.stockID = cANLStock.id;
//			cANLPolicyStockCK.param_PianLiBi = CalPianLiBi.calculate(cANLStock);
//			cANLPolicyStockCK.param_XiaCuo = DuanQiXiaCuo.calculate(cANLStock);
//			
//			stockCKList.add(cANLPolicyStockCK);
//		}
//		ANLPolicyStockCK.sortZongHe(stockCKList);
//		
//		// ---------��ӡ�ɲ�����Ʊ��Ϣ ---------
//		if(stockCKList.size() != 0) 
//		{
//			for(int i = 0; i < stockCKList.size(); i++)
//			{
//				ANLPolicyStockCK cANLPolicyStockCK = stockCKList.get(i);
//				ANLLog.outputLog("    %s PianLiBi[ %.3f %.1f] XiaCuo[ %.3f %.1f]\n", 
//						cANLPolicyStockCK.stockID, 
//						cANLPolicyStockCK.param_PianLiBi, cANLPolicyStockCK.mingci_PianLiBi,
//						cANLPolicyStockCK.param_XiaCuo, cANLPolicyStockCK.mingci_XiaCuo);
//				if(i>20) break; // ֻ��ӡ����ǰ��
//			}
//		}
//			
//		// ---------�û��������� ---------
//		int iMaxHoldCnt = 2; // ���ֹɸ���
//		for(int i = 0; i < cUserAcc.stockList.size(); i++) // �����ֲ�Ʊ�����������ж�
//		{
//			ANLUserAcc.ANLUserAccStock cANLUserAccStock = cUserAcc.stockList.get(i);
//			float cprice = spool.getStock(cANLUserAccStock.id).GetLastClosePrice();
//			if(cANLUserAccStock.holdDayCnt > 5) // ����һ��ʱ������
//			{
//				cUserAcc.sellStock(cANLUserAccStock.id, cprice, cANLUserAccStock.totalAmount);
//			}
//			float shouyi = (cprice - cANLUserAccStock.buyPrices)/cANLUserAccStock.buyPrices;
//			if(shouyi > 0.03 || shouyi < -0.03) // ֹӯֹ������
//			{
//				cUserAcc.sellStock(cANLUserAccStock.id, cprice, cANLUserAccStock.totalAmount);
//			}
//		}
//		int iNeedBuyCnt = iMaxHoldCnt - cUserAcc.stockList.size();
//		for(int i = 0; i< iNeedBuyCnt; i++) // ���г�Ʊ��������ʱ��������
//		{
//			float usedMoney = cUserAcc.money/(iNeedBuyCnt-i);//�ó���Ӧ��λǮ
//			for(int j=0;j<stockCKList.size();j++)// �����ɲ���Ʊ
//			{
//				ANLPolicyStockCK cANLPolicyStockCK = stockCKList.get(j);
//				if(spool.getStock(cANLPolicyStockCK.stockID).GetLastDate().compareTo(date)!=0) //��Ʊ��������뵱ǰ������ڲ�ͬ ������һ��
//				{
//					continue;
//				}
//				
//				boolean alreayHas = false;
//				for(int k = 0; k < cUserAcc.stockList.size(); k++) // �����ֲ�Ʊ���ж��Ƿ��Ѿ�����
//				{
//					ANLUserAcc.ANLUserAccStock cANLUserAccStock = cUserAcc.stockList.get(k);
//					if(cANLPolicyStockCK.stockID.compareTo(cANLUserAccStock.id) == 0)
//					{
//						alreayHas = true;
//						break;
//					}
//				}
//				if(!alreayHas)
//				{
//					String buy_id = cANLPolicyStockCK.stockID;
//					float buy_price = spool.getStock(buy_id).GetLastClosePrice();
//					int buy_amount = (int)(usedMoney/buy_price)/100*100;
//					cUserAcc.buyStock(buy_id, buy_price, buy_amount);
//					stockCKList.remove(j); // �������б����
//					break;
//				}
//			}
//		}
//		return;
//	}
//	public static void main(String[] args) throws InterruptedException {
//		RunBTStrategyJZHGX cANLPolicyJZHG = new RunBTStrategyJZHGX();
//		cANLPolicyJZHG.run("2010-01-01", "2016-01-05");
//	}
}
