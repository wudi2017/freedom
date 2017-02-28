package stormstock.fw.tranbase.com;

abstract public class IStrategyCreate {
	// �����µ�����
	public static class CreateResult {
		public CreateResult() {
			bCreate = false;
			fMaxPositionRatio = 0.3333f;
			fMaxMoney = 10000*100;
		}
		public boolean bCreate; // ���ֱ�־
		public float fMaxPositionRatio; // �����λ
		public float fMaxMoney; // �����������ֵ���ƣ�
	}
	
	abstract public void strategy_create(TranContext ctx, CreateResult out_sr);
	abstract public int strategy_create_max_count();
}
