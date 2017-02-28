package stormstock.fw.tranbase.account;

import stormstock.ori.capi.CATHSAccount.TRANACT;

public class AccountPublicDef {
	
	public enum ACCOUNTTYPE 
	{
		MOCK,
		REAL,
	}
	
	/*
	 * ���׶���ö��
	 */
	public enum TRANACT 
	{
		BUY,
		SELL,
	}
	
	/*
	 * ��Ʊ����ί�е�����
	 */
	public static class CommissionOrder 
	{
		public String time;
		public String stockID;
		public TRANACT tranAct;
		public int amount; // ί������
		public float price; // ί�м۸�
		
		public void CopyFrom(CommissionOrder c)
		{
			time = c.time;
			stockID = c.stockID;
			tranAct = c.tranAct;
			amount = c.amount;
			price = c.price;
		}
	}
	
	/*
	 * ��Ʊ���ճɽ�������
	 */
	public static class DealOrder 
	{
		public String time;
		public String stockID;
		public TRANACT tranAct;
		public int amount; // �ɽ�����
		public float price; // �ɽ��۸�
		
		public void CopyFrom(DealOrder c)
		{
			time = c.time;
			stockID = c.stockID;
			tranAct = c.tranAct;
			amount = c.amount;
			price = c.price;
		}
	}
	
	/*
	 * �ֹɶ���
	 */
	public static class HoldStock 
	{
		public String stockID; // ��ƱID
		public int totalAmount; // �����������ɣ�
		public int availableAmount; // ��������
		public float refPrimeCostPrice; // �ο��ɱ���
		public float curPrice; // ��ǰ��
		public int investigationDays; // ��������������������տ�ʼ����
		
		public HoldStock()
		{
			stockID = "";
			totalAmount = 0;
			availableAmount = 0;
			refPrimeCostPrice = 0.0f;
			curPrice = 0.0f;
			investigationDays = 0;
		}
		
		public void CopyFrom(HoldStock c)
		{
			stockID = c.stockID;
			totalAmount = c.totalAmount;
			availableAmount = c.availableAmount;
			refPrimeCostPrice = c.refPrimeCostPrice;
			curPrice = c.curPrice;
			investigationDays = c.investigationDays;
		}
		
		public float profit() // ����ֵ��ӯ���������㽻�׷��ã�
		{
			return (curPrice - refPrimeCostPrice)*totalAmount;
		}
		
		public float profitRatio() // ����ȣ�ӯ��������
		{
			return (curPrice - refPrimeCostPrice)/refPrimeCostPrice;
		}
	}
	
}
