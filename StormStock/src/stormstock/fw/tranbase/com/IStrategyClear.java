package stormstock.fw.tranbase.com;

abstract public class IStrategyClear {
	// ����µ�����
	public static class ClearResult {
		public ClearResult() {
			bClear = false;
		}
		public boolean bClear; // ��ֱ�־
	}
	abstract public void strategy_clear(TranContext ctx, ClearResult out_sr);
}
