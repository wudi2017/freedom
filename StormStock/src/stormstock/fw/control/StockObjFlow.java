package stormstock.fw.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stormstock.fw.base.BLog;

public class StockObjFlow {
	/*
	 * StockIDSet��  ��Ʊ���׼��ϣ���������
	 */
	public static void setTranStockIDSet(List<String> stockSet)
	{
		s_stockIDSet = stockSet;
	}
	public static List<String> getTranStockIDSet()
	{
		return s_stockIDSet;
	}
	private static List<String> s_stockIDSet = new ArrayList<String>();
}
