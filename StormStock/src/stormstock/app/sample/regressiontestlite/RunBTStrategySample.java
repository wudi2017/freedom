package stormstock.app.sample.regressiontestlite;

import java.util.Arrays;
import java.util.List;

import stormstock.fw.tranengine_lite.ANLBTEngine;
import stormstock.fw.tranengine_lite.ANLEigen;
import stormstock.fw.tranengine_lite.ANLLog;
import stormstock.fw.tranengine_lite.ANLStock;
import stormstock.fw.tranengine_lite.ANLStockPool;
import stormstock.fw.tranengine_lite.ANLStrategy;
import stormstock.fw.tranengine_lite.ANLStrategy.SelectResult;

public class RunBTStrategySample {
	/*
	 * ����EigenPriceLoc��
	 * ��ֱλ�ñ���
	 * ���ڼ����N�յĴ�ֱ�۸�λ�ñ���
	 * ���磺��ʷ ���100 ���0 ��ǰ25��������ֵΪ0.25
	 */
	public static class EigenSamplePriceLoc extends ANLEigen {
		@Override
		public Object calc(ANLStock cANLStock, Object... args) {
			int cntDay = (int)args[0];
			if(cANLStock.historyData.size() == 0) 
				return 0.0f;
			String date = cANLStock.GetLastDate();
			float lowPrice = cANLStock.GetLow(cntDay, date);
			float highPrice = cANLStock.GetHigh(cntDay, date);
			float curPrice = cANLStock.GetLastClosePrice();
			return (curPrice-lowPrice)/(highPrice-lowPrice);
		}
	}
	/*
	 * ����EigenSampleMADeviation��
	 * ���ڼ��㵱ǰ�۸�ƫ��N�վ��߼۸�İٷֱ�
	 */
	public static class EigenSampleMADeviation extends ANLEigen {
		@Override
		public Object calc(ANLStock cANLStock, Object... args) {
			int dayCnt = (int)args[0];
			float MAVal = cANLStock.GetMA(dayCnt, cANLStock.GetLastDate());
			float curPrice = cANLStock.GetLastClosePrice();
			float val = (curPrice-MAVal)/MAVal;
			return val;
		}

	}

	// ����StrategySample��
	public static class StrategySample extends ANLStrategy {
		@Override
		public boolean strategy_preload(ANLStock cANLStock) {
			if(cANLStock.id.compareTo("000001") >= 0 && cANLStock.id.compareTo("000200") <= 0) {	
				//ANLLog.outputLog("add stockpool %s %s\n", cANLStock.id, cANLStock.curBaseInfo.name);
				return true;
			}
			return false;
		}
		
		@Override
		public void strategy_select(String in_date, ANLStock in_stock, SelectResult out_sr) {
			// ��������ֵλ��250������
			float EigenPriceLocLong = (float)in_stock.getEngen("EigenSamplePriceLoc", 250);
			// ��60�վ���ƫ��ٷֱ�
			float MADeviation60 = (float)in_stock.getEngen("EigenSampleMADeviation", 60);
			// ��250�վ���ƫ��ٷֱ�
			float MADeviation250 = (float)in_stock.getEngen("EigenSampleMADeviation", 250);
			if(MADeviation60 < -0.1 && MADeviation250 < -0.06 
					&& EigenPriceLocLong < 0.4 && EigenPriceLocLong > 0.1) {
				out_sr.bSelect = true;
				//out_sr.fPriority = (float) Math.random();
				//ANLLog.outputLog("    stock %s %s %s %.2f EigenSample1(%.3f) EigenSample2(%.3f)\n", in_stock.id, in_stock.curBaseInfo.name, in_stock.GetLastDate(), in_stock.GetLastPrice(),EigenSample1,EigenSample2);
			}
		}
	}
	
	public static void main(String[] args) {
		ANLBTEngine cANLBTEngine = new ANLBTEngine("Sample");
		// �������
		cANLBTEngine.addEigen(new EigenSampleMADeviation());
		cANLBTEngine.addEigen(new EigenSamplePriceLoc()); 
		// ���ò���
		cANLBTEngine.setStrategy(new StrategySample());
		// ���лز�
		cANLBTEngine.runBT("2016-01-01", "2016-03-01");
		//cANLBTEngine.runBTRealtimeMock("2016-11-01", "2016-11-20");
	}
}
