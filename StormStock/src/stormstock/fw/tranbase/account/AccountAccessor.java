package stormstock.fw.tranbase.account;

import java.util.List;

import stormstock.fw.base.BTypeDefine.RefFloat;
import stormstock.fw.tranbase.account.AccountPublicDef.CommissionOrder;
import stormstock.fw.tranbase.account.AccountPublicDef.DealOrder;
import stormstock.fw.tranbase.account.AccountPublicDef.HoldStock;

/*
 * �û��˻���ĳ����ʱ��ķ�����
 * ���Է����˻���ǰ��Ϣ
 */
public class AccountAccessor {
	public AccountAccessor(String date, String time, AccountControlIF cAccountControlIF)
	{
		m_date = date;
		m_time = time;
		m_accountControlIF = cAccountControlIF;
	}
	
	public float getTotalAssets()
	{
		return m_accountControlIF.getTotalAssets(m_date, m_time);
	}
	
	public int getAvailableMoney(RefFloat out_availableMoney)
	{
		return m_accountControlIF.getAvailableMoney(out_availableMoney);
	}
	
	public int getCommissionOrderList(List<CommissionOrder> out_list)
	{
		return m_accountControlIF.getCommissionOrderList(out_list);
	}
	
	public int getHoldStockList(List<HoldStock> out_list)
	{
		return m_accountControlIF.getHoldStockList(m_date, m_time, out_list);
	}
	
	public HoldStock getHoldStock(String stockID)
	{
		return m_accountControlIF.getHoldStock(m_date, m_time, stockID);
	}
	
	public int getDealOrderList(List<DealOrder> out_list)
	{
		return m_accountControlIF.getDealOrderList(out_list);
	}

	private String m_date;
	private String m_time;
	private AccountControlIF m_accountControlIF;
}
