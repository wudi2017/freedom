package stormstock.fw.base;

public class TestBUtilsMath {
	public static void main(String[] args) {
		BLog.output("TEST", "TestBUtilsDateTime begin\n");
		
		BLog.output("TEST", "saveNDecimal %f\n", BUtilsMath.saveNDecimalIgnore(2.199f, 1));
		BLog.output("TEST", "saveNDecimal %f\n", BUtilsMath.saveNDecimalIgnore(2.199f, 2));
		BLog.output("TEST", "saveNDecimal %f\n", BUtilsMath.saveNDecimalIgnore(2.199f, 3));
		
		
		BLog.output("TEST", "randomFloat %f\n", BUtilsMath.randomFloat());
		BLog.output("TEST", "randomFloat %f\n", BUtilsMath.randomFloat());
		BLog.output("TEST", "randomFloat %f\n", BUtilsMath.randomFloat());
		
		
		
		BLog.output("TEST", "saveNDecimal45 %.3f = 9.19?\n", BUtilsMath.saveNDecimal(8.35f*1.1f, 2));
		BLog.output("TEST", "saveNDecimal45 %.3f = 27.42?\n", BUtilsMath.saveNDecimal(24.93f*1.1f, 2));
		BLog.output("TEST", "saveNDecimal45 %.3f = 30.16?\n", BUtilsMath.saveNDecimal(27.42f*1.1f, 2));
		BLog.output("TEST", "saveNDecimal45 %.3f = 30.18?\n", BUtilsMath.saveNDecimal(30.16f*1.1f, 2));
		
		
		
		
		BLog.output("TEST", "TestBUtilsDateTime end\n");
	}
}
