package stormstock.app.analysistest;

import java.util.List;

import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockUtils;

public class EDIPricePos {

	public static class ResultLongDropParam
	{
		public ResultLongDropParam()
		{
			bCheck = false;
		}
		public boolean bCheck;
		public float refLow;
		public float refHigh;
	}
	public static ResultLongDropParam getLongDropParam(List<StockDay> list, int iCheck)
	{
		ResultLongDropParam cResultLongDropParam = new ResultLongDropParam();
		
		int iBegin = iCheck-500;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return cResultLongDropParam;
		}
		
		StockDay cCurStockDay = list.get(iEnd);
		
		int iIndexH = StockUtils.indexHigh(list, iBegin, iEnd);
		StockDay cStockDayH = list.get(iIndexH);
		int iIndexL = StockUtils.indexLow(list, iBegin, iEnd);
		StockDay cStockDayL = list.get(iIndexL);
		
		cResultLongDropParam.refHigh = (cCurStockDay.close() - cStockDayH.close())/cStockDayH.close();
		cResultLongDropParam.refLow = (cCurStockDay.close() - cStockDayL.close())/cStockDayL.close();
		
		return cResultLongDropParam;
	}
}
