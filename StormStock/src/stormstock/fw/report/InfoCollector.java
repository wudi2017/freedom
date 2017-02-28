package stormstock.fw.report;

import java.util.ArrayList;
import java.util.List;

import stormstock.fw.tranbase.account.AccountPublicDef.DealOrder;

/*
 * ��Ϣ�ռ���
 */
public class InfoCollector {
	/*
	 * �ձ���ṹ��
	 */
	public static class DailyReport
	{
		public DailyReport(String date)
		{
			cClearDealOrder = new ArrayList<DealOrder>();
		}
		public String date; // ����
		public float fTotalAssets; // ���ʲ�
		public float fAvailableMoney; // �����ʽ�
		public List<DealOrder> cClearDealOrder; // ��ֽ���б�
		
		public float fSHComposite;
	}
	
	public InfoCollector()
	{
		m_cDailyReportList = new ArrayList<DailyReport>();
	}
	public void addDailyReport(DailyReport cDailyReport)
	{
		m_cDailyReportList.add(cDailyReport);
	}
	public List<DailyReport> getDailyReportList()
	{
		return m_cDailyReportList;
	}
	public void clearDailyReport()
	{
		m_cDailyReportList.clear();
	}
	
	private List<DailyReport> m_cDailyReportList;
}
