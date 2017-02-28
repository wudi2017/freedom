package stormstock.ori.stockdata;

public class CommonDef {
	
	/*
	 * ��Ʊ����
	 * id-����
	 */
	public static class StockSimpleItem
	{
		public StockSimpleItem(){}
		public StockSimpleItem(String in_id, String in_name)
		{
			id = in_id;
			name = in_name;
		}
		public StockSimpleItem(StockSimpleItem cStockSimpleItem)
		{
			name = cStockSimpleItem.name;
			id = cStockSimpleItem.id;
		}
		public String name;
		public String id;
	}
	
	/*
	 * ���ڽ�����ϸ
	 * ʱ��-�۸�-�ɽ���
	 */
	public static class DayDetailItem implements Comparable
	{
		public String time;
		public float price;
		public float volume; 
		@Override
		public int compareTo(Object o) {
			// TODO Auto-generated method stub
			DayDetailItem sdto = (DayDetailItem)o;
		    return this.time.compareTo(sdto.time);
		}
	}
	
	/*
	 * ��K����
	 * ����-���̼�-���̼�-��ͼ�-��߼�-�ɽ���
	 */
	public static class DayKData implements Comparable
	{
		// 2015-09-18
		public String date;
		public float open;
		public float close;
		public float low;
		public float high;
		public float volume;
		@Override
		public int compareTo(Object arg0) {
			// TODO Auto-generated method stub
			DayKData sdto = (DayKData)arg0;
		    return this.date.compareTo(sdto.date);
		}
	}
	
	/*
	 * �ֺ���Ϣ����
	 * ����-�͹�-ת��-��Ϣ
	 */
	public static class DividendPayout implements Comparable
	{
		public String date;
		public float songGu;
		public float zhuanGu;
		public float paiXi;
		@Override
		public int compareTo(Object o) {
			// TODO Auto-generated method stub
			DividendPayout sdto = (DividendPayout)o;
		    return this.date.compareTo(sdto.date);
		}
	}
	
	/*
	 * ��Ʊʵʱ��Ϣ
	 * ����-����-ʱ��-��ǰ��
	 */
	public static class RealTimeInfo implements Comparable
	{
		public String name;
		public String date;
		public String time;
		public float curPrice;
		
		@Override
		public int compareTo(Object o) {
			// TODO Auto-generated method stub
			RealTimeInfo sdto = (RealTimeInfo)o;
			int iCheck1 = this.date.compareTo(sdto.date);
			if(0 == iCheck1)
			{
				int iCheck2 = this.time.compareTo(sdto.time);
				return iCheck2;
			}
			else
			{
				return iCheck1;
			}
		}
	}
	/*
	 * ��Ʊʵʱ��Ϣ��������Ϣ��
	 * ��Ʊʵʱ��Ϣ +������ֵ����ͨ��ֵ����ӯ��
	 */
	public static class RealTimeInfoMore extends RealTimeInfo
	{
		public float allMarketValue; //����ֵ
		public float circulatedMarketValue; // ��ͨ��ֵ
		public float peRatio; //��ӯ��
	}
	
	
	/*
	 * ��Ʊ������Ϣ
	 * ����-��ǰ��-����ֵ-��ͨ��ֵ-��ӯ�ʵȣ�������չΪ��ҵ�ȣ�
	 */
	public static class StockBaseInfo
	{
		public String name;
		public float price; // Ԫ
		public float allMarketValue; // ��
		public float circulatedMarketValue; // ��
		public float peRatio;
		public StockBaseInfo()
		{
			name = "";
			price = 0.0f;
			allMarketValue = 0.0f;
			circulatedMarketValue = 0.0f;
			peRatio = 0.0f;
		}
		public void CopyFrom(StockBaseInfo cCopyFromObj)
		{
			name = cCopyFromObj.name;
			price = cCopyFromObj.price;
			allMarketValue = cCopyFromObj.allMarketValue;
			circulatedMarketValue = cCopyFromObj.circulatedMarketValue;
			peRatio = cCopyFromObj.peRatio;
		}
	}
}
