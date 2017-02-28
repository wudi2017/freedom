package stormstock.fw.tranengine_lite;

import java.util.ArrayList;
import java.util.List;

import stormstock.ori.stockdata.DataEngine;
import stormstock.ori.stockdata.DataEngine.ExKData;
import stormstock.ori.stockdata.DataEngine.ResultMinKDataOneDay;
 
public class ANLStockDayKData {
	public class DetailData
	{
		public Float price;
		public String time;
	}
	public ANLStockDayKData()
	{
		detailDataList = new ArrayList<DetailData>();
	} 
	public ANLStockDayKData(ANLStockDayKData c, ANLStock refStock)
	{

		date = c.date;
		open = c.open;
		close = c.close;
		high = c.high;
		low = c.low;
		volume = c.volume;
		detailDataList = new ArrayList<DetailData>();
		ref_ANLStock = refStock;
	} 
	
	public int LoadDetail()
	{
		int iSizedetailDataList = detailDataList.size();
		if(iSizedetailDataList != 0)
		{
			return 0;
		}
		
		if(null == ref_ANLStock) return -10;
		if(date.length() < 6) return -20;
		
		// load new detail data
		ResultMinKDataOneDay cResultMinKDataOneDay = DataEngine.get1MinKDataOneDay(ref_ANLStock.id, date);
		if(0 == cResultMinKDataOneDay.error && cResultMinKDataOneDay.exKDataList.size() != 0)
		{
			// ���ڿ����Ǹ�Ȩ��λ����Ҫ���¼�����Լ۸�
			float baseOpenPrice = open;
			float actruaFirstPrice = cResultMinKDataOneDay.exKDataList.get(0).open;
			for(int i = 0; i < cResultMinKDataOneDay.exKDataList.size(); i++)  
	        {  
				ExKData cExKData = cResultMinKDataOneDay.exKDataList.get(i);  
//	            System.out.println(cExKData.datetime + "," 
//	            		+ cExKData.open + "," + cExKData.close + "," 
//	            		+ cExKData.low + "," + cExKData.high + "," 
//	            		+ cExKData.volume);  
				
				float actrualprice = cExKData.close;
				float changeper = (actrualprice - actruaFirstPrice)/actruaFirstPrice;
				float changedprice = baseOpenPrice + baseOpenPrice * changeper;
				
				DetailData cDetail = new DetailData();
				cDetail.price = changedprice;
				cDetail.time = cExKData.getTime();
				detailDataList.add(cDetail);
	        } 
		}
		else
		{
			// System.out.println("ERROR: LoadDetail");
			return -30;
		}
		return 0;
	}
	
	public String date;
	public float open;
	public float close;
	public float high;
	public float low;
	public float volume;
	
	public List<DetailData> detailDataList;
	
	public ANLStock ref_ANLStock;
}