package stormstock.app.sample.regressiontest;

import stormstock.fw.tranbase.com.IEigenStock;
import stormstock.fw.tranbase.stockdata.Stock;

public class StockEigen {
	/*
	 * ����EigenPriceLoc��
	 * ��ֱλ�ñ���
	 * ���ڼ����N�յĴ�ֱ�۸�λ�ñ���
	 * ���磺��ʷ ���100 ���0 ��ǰ25��������ֵΪ0.25
	 */
	public static class EigenSamplePriceLoc extends IEigenStock {
		@Override
		public Object calc(Stock cStock, Object... args) {
			int cntDay = (int)args[0];
			if(cStock.getCurStockDayData().size() == 0) 
				return 0.0f;
			String date = cStock.GetLastDate();
			float lowPrice = cStock.GetLow(cntDay, date);
			float highPrice = cStock.GetHigh(cntDay, date);
			float curPrice = cStock.GetLastClosePrice();
			return (curPrice-lowPrice)/(highPrice-lowPrice);
		}
	}
	/*
	 * ����EigenSampleMADeviation��
	 * ���ڼ��㵱ǰ�۸�ƫ��N�վ��߼۸�İٷֱ�
	 */
	public static class EigenSampleMADeviation extends IEigenStock {
		@Override
		public Object calc(Stock cStock, Object... args) {
			int dayCnt = (int)args[0];
			float MAVal = cStock.GetMA(dayCnt, cStock.GetLastDate());
			float curPrice = cStock.GetLastClosePrice();
			float val = (curPrice-MAVal)/MAVal;
			return val;
		}

	}
}
