package stormstock.fw.tranbase.com;

import stormstock.fw.base.BLog;
import stormstock.fw.tranbase.stockdata.StockInfo;

abstract public class ITranStockSetFilter {
	// ���׹�Ʊ��
	abstract public boolean tran_stockset_byLatestStockInfo(StockInfo cStockInfo);
}
