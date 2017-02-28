package stormstock.fw.tranbase.stockdata;

import java.util.List;

import stormstock.fw.tranbase.stockdata.StockDataIF.ResultAllStockID;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultHistoryData;

/*
 * ��Ʊ���ݵ�ĳ����ʱ��ķ�����
 * ���Է������й�Ʊ��Ϣ
 */
public class StockDataAccessor {
	
	public StockDataAccessor(String date, String time, StockDataIF cStockDataIF)
	{
		m_date = date;
		m_time = time;
		m_stockDataIF = cStockDataIF;
	}
	
	/*
	 * ��ȡ���й�ƱId�б�
	 */
	public ResultAllStockID getAllStockID()
	{
		return m_stockDataIF.getAllStockID();
	}
	
	public ResultHistoryData getHistoryData(String stockID)
	{
		return m_stockDataIF.getHistoryData(stockID, m_date);
	}
	
	
	private String m_date;
	private String m_time;
	private StockDataIF m_stockDataIF;
}
