package stormstock.app.sample.regressiontestlite;

import stormstock.fw.tranengine_lite.ANLEigen;
import stormstock.fw.tranengine_lite.ANLStock;

/*
 * ��ֱλ�ñ���
 * ���ڼ����N�յĴ�ֱ�۸�λ�ñ���
 * ���磺��ʷ ���100 ���0 ��ǰ25��������ֵΪ0.25
 */
public class EigenPriceLoc extends ANLEigen {
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
