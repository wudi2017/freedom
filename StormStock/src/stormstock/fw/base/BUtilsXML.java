package stormstock.fw.base;

import java.io.StringReader;
import java.io.StringWriter;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class BUtilsXML {
	
	public static String format(String str) throws Exception {
        SAXReader reader = new SAXReader();
        // System.out.println(reader);
        // ע�ͣ�����һ�������ַ�������
        StringReader in = new StringReader(str);
        Document doc = reader.read(in);
        // System.out.println(doc.getRootElement());
        // ע�ͣ����������ʽ
        OutputFormat formater = OutputFormat.createPrettyPrint();
        //formater=OutputFormat.createCompactFormat();
        // ע�ͣ�����xml���������
        formater.setEncoding("utf-8");
        // ע�ͣ��������(Ŀ��)
        StringWriter out = new StringWriter();
        // ע�ͣ����������
        XMLWriter writer = new XMLWriter(out, formater);
        // ע�ͣ������ʽ���Ĵ���Ŀ���У�ִ�к󡣸�ʽ����Ĵ�������out�С�
        writer.write(doc);
 
        writer.close();
        //System.out.println(out.toString());
        // ע�ͣ��������Ǹ�ʽ����Ľ��
        return out.toString();
    }
}
