package stormstock.fw.base;

public class TestBUtilsMath {
	public static void main(String[] args) {
		BLog.output("TEST", "TestBUtilsDateTime begin\n");
		
		BLog.output("TEST", "saveNDecimal %f\n", BUtilsMath.saveNDecimal(2.199f, 1));
		BLog.output("TEST", "saveNDecimal %f\n", BUtilsMath.saveNDecimal(2.199f, 2));
		BLog.output("TEST", "saveNDecimal %f\n", BUtilsMath.saveNDecimal(2.199f, 3));
		
		
		BLog.output("TEST", "randomFloat %f\n", BUtilsMath.randomFloat());
		BLog.output("TEST", "randomFloat %f\n", BUtilsMath.randomFloat());
		BLog.output("TEST", "randomFloat %f\n", BUtilsMath.randomFloat());
		
		
		
		BLog.output("TEST", "TestBUtilsDateTime end\n");
	}
}
