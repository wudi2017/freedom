package stormstock.fw.tranbase.account;

import java.util.List;
import java.util.Vector;

import stormstock.fw.base.BTypeDefine.RefFloat;
import stormstock.fw.tranbase.account.AccountPublicDef.CommissionOrder;
import stormstock.fw.tranbase.account.AccountPublicDef.DealOrder;
import stormstock.fw.tranbase.account.AccountPublicDef.HoldStock;

/*
 * 用户账户的某日期时间的访问器
 * 可以访问账户当前信息
 */
public class AccountAccessor {
	public AccountAccessor(String date, String time, AccountControlIF cAccountControlIF)
	{
		m_date = date;
		m_time = time;
		m_accountControlIF = cAccountControlIF;
	}
	
	public int getTotalAssets(RefFloat out_totalAssets)
	{
		return m_accountControlIF.getTotalAssets(m_date, m_time, out_totalAssets);
	}
	
	public int getAvailableMoney(RefFloat out_availableMoney)
	{
		return m_accountControlIF.getAvailableMoney(out_availableMoney);
	}
	
	public int getHoldStockList(List<HoldStock> out_list)
	{
		return m_accountControlIF.getHoldStockList(m_date, m_time, out_list);
	}
	
	public int getHoldStock(String stockID, Vector<HoldStock> out_vector)
	{
		return m_accountControlIF.getHoldStock(m_date, m_time, stockID, out_vector);
	}

	private String m_date;
	private String m_time;
	private AccountControlIF m_accountControlIF;
}
