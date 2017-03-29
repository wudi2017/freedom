package stormstock.fw.tranbase.account;

import java.util.List;

import stormstock.fw.base.BTypeDefine.RefFloat;
import stormstock.fw.tranbase.account.AccountPublicDef.CommissionOrder;
import stormstock.fw.tranbase.account.AccountPublicDef.DealOrder;
import stormstock.fw.tranbase.account.AccountPublicDef.HoldStock;
import stormstock.fw.tranbase.stockdata.StockDay;

abstract public class IAccountOpe {
	public IAccountOpe() { }
	// ���տ�ʼ�˻���ʼ��
	abstract public int newDayInit(String date, String time);
	// ���տ�ʼ�˻����׽���
	abstract public int newDayTranEnd(String date, String time);
	// ������ί��
	abstract public int pushBuyOrder(String date, String time, String id, int amount, float price); 
	// ��������ί��
	abstract public int pushSellOrder(String date, String time, String id, int amount, float price);
	// ����˻������ʽ�
	abstract public int getAvailableMoney(RefFloat out_availableMoney);
	// ��óֹ��б�
	abstract public int getHoldStockList(String date, String time, List<HoldStock> out_list);
}
