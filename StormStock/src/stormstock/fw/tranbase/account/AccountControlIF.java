package stormstock.fw.tranbase.account;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import stormstock.fw.base.BLog;
import stormstock.fw.base.BTypeDefine.RefFloat;
import stormstock.fw.tranbase.account.AccountPublicDef.ACCOUNTTYPE;
import stormstock.fw.tranbase.account.AccountPublicDef.CommissionOrder;
import stormstock.fw.tranbase.account.AccountPublicDef.DealOrder;
import stormstock.fw.tranbase.account.AccountPublicDef.HoldStock;

/*
 * �˻�������
 * ���Ի�ȡ�˻���Ϣ
 * ���Բ����˻�����
 */
public class AccountControlIF {
	
	public AccountControlIF()
	{
		m_account = null;
	}
	
	public String getAccountID()
	{
		return m_account.getAccountID();
	}
	
	/*
	 * ���ĳ����ʱ����˻�������
	 * ���Ի�ȡ�˻���Ϣ
	 */
	public AccountAccessor getAccountAccessor(String date, String time)
	{
		return new AccountAccessor(date, time, this);
	}
	
	public void setAccountType(ACCOUNTTYPE eAccIFType)
	{
		if(ACCOUNTTYPE.MOCK == eAccIFType)
		{
			m_account = new Account(eAccIFType, "mock001", "mock001_password");
		}
		if(ACCOUNTTYPE.REAL == eAccIFType)
		{
			m_account = new Account(eAccIFType, "real001", "real001_password");
		}
	}
	
	public void printAccount(String date, String time)
	{
		m_account.printAccount(date, time);
	}
	
	/*
	 * �˻������ڳ�ʼ��
	 * �ֹɾ�������
	 */
	public boolean newDayInit(String date, String time)
	{
		boolean bRet = true;
		BLog.output("ACCOUNT", "[%s %s] account new day reset \n", date, time);
		// �˻����³�ʼ��
		int err = m_account.newDayInit(date, time);
		if(0 != err)
		{
			bRet = false;
			BLog.error("ACCOUNT", "[%s %s] m_account.newDayInit err(%d) \n", date, time, err);
		}
		return bRet;
	}
	
	public boolean newDayTranEnd(String date, String time)
	{
		boolean bRet = true;
		BLog.output("ACCOUNT", "[%s %s] account new day tran end \n", date, time);
		// �˻����ս���
		// ���¹�Ʊ�������������ί�б��
		int err = m_account.newDayTranEnd(date, time);
		if(0 != err)
		{
			bRet = false;
			BLog.error("ACCOUNT", "[%s %s] m_account.newDayTranEnd err(%d) \n", date, time, err);
		}
		return bRet;
	}
	
	public int getLockedMoney(RefFloat out_lockedMoney)
	{
		return m_account.getLockedMoney(out_lockedMoney);
	}
	
	// ��ȡ�˻����ʲ�����������ʱ����ȷ���ɼۣ�
	public int getTotalAssets(String date, String time, RefFloat out_totalAssets)
	{
		return m_account.getTotalAssets(date, time, out_totalAssets);
	}

	public int getAvailableMoney(String date, String time, RefFloat out_availableMoney)
	{
		return m_account.getAvailableMoney(date, time, out_availableMoney);
	}
	
	public int getMoney(String date, String time, RefFloat out_money)
	{
		return m_account.getMoney(date, time, out_money);
	}
	
	public int setStockSelectList(String date, String time, List<String> stockIDList)
	{
		return m_account.setStockSelectList(date, time, stockIDList);
	}
	
	public int getStockSelectList(List<String> out_list)
	{
		return m_account.getStockSelectList(out_list);
	}

	/*
	 * ����ί��
	 * ʱ���������ɽ��
	 */
	public int pushBuyOrder(String date, String time, String stockID, int amount, float price)
	{
		return m_account.pushBuyOrder(date, time, stockID, amount, price);
	}

	/*
	 * ������ί��
	 * ʱ���������ɽ��
	 */
	public int pushSellOrder(String date, String time, String stockID, int amount, float price)
	{
		return m_account.pushSellOrder(date, time, stockID, amount, price);
	}
	
	// ���ί���б�δ�ɽ���
	public int getCommissionOrderList(List<CommissionOrder> out_list)
	{
		return m_account.getCommissionOrderList(out_list);
	}
	// �����ί���б�
	public int getBuyCommissionOrderList(List<CommissionOrder> out_list)
	{
		return m_account.getBuyCommissionOrderList(out_list);
	}
	// �������ί���б�
	public int getSellCommissionOrderList(List<CommissionOrder> out_list)
	{
		return m_account.getSellCommissionOrderList(out_list);
	}
	
	
	/*
	 * ��óֹ��б�
	 * ʱ���û������ּ�
	 * �������null���򲻸����ּ�
	 */
	public int getHoldStockList(String date, String time, List<HoldStock> out_list)
	{
		return m_account.getHoldStockList(date, time, out_list);
	}
	public int getHoldStock(String date, String time, String stockID, Vector<HoldStock> out_vector)
	{
		out_vector.clear();
		List<HoldStock> cHoldStockList = new ArrayList<HoldStock>();
		int iRet = getHoldStockList(date, time, cHoldStockList);
		for(int i=0;i<cHoldStockList.size();i++)
		{
			if(cHoldStockList.get(i).stockID.equals(stockID))
			{
				HoldStock cNewHoldStock = new HoldStock();
				cNewHoldStock.CopyFrom(cHoldStockList.get(i));
				out_vector.add(cNewHoldStock);
				break;
			}
		}
		return iRet;
	}
	
	/**
	 * ��Ա-----------------------------------------------------------------
	 */
	// �˻�ʵ��
	private Account m_account;
}
