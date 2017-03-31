package stormstock.fw.report;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import stormstock.fw.base.BLog;
import stormstock.fw.base.BUtilsXML;
import stormstock.fw.tranbase.account.AccountPublicDef.CommissionOrder;
import stormstock.fw.tranbase.account.AccountPublicDef.DealOrder;
import stormstock.fw.tranbase.account.AccountPublicDef.TRANACT;

/*
 * 信息收集器
 */
public class InfoCollector {
	/*
	 * 日报告结构体
	 */
	public static class DailyReport
	{
		public DailyReport()
		{
		}
		public String date; // 日期
		public float fTotalAssets; // 总资产
		public float fSHComposite; // 当日上证指数
	}
	
	public InfoCollector(String accountID)
	{
		m_xmlFile = "rw\\INFOCOLLECTOR_" + accountID + ".xml";;
		m_cDailyReportList = new ArrayList<DailyReport>();
		load();
	}
	public void addDailyReport(DailyReport cDailyReport)
	{
		m_cDailyReportList.add(cDailyReport);
		store();
	}
	public List<DailyReport> getDailyReportList()
	{
		return m_cDailyReportList;
	}

	private void store()
	{
		File cfile=new File(m_xmlFile);
		if(cfile.exists())
		{
			cfile.delete();
		}
		
		Document doc=null;
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder= factory.newDocumentBuilder();
			doc=builder.newDocument();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 创建元素
		Element root=doc.createElement("history");
        doc.appendChild(root);
        
        if(null != m_cDailyReportList)
        {
        	for(int i=0;i<m_cDailyReportList.size();i++)
        	{
        		DailyReport cDailyReport = m_cDailyReportList.get(i);
        		
        		
        		String date = cDailyReport.date;
        		String totalAssets = String.format("%.3f", cDailyReport.fTotalAssets);
        		String SHComposite = String.format("%.3f", cDailyReport.fSHComposite);
        				
        		Element Node_DailyReport = doc.createElement("DailyReport");
        		Node_DailyReport.setAttribute("date", date);
        		Node_DailyReport.setAttribute("totalAssets", totalAssets);
        		Node_DailyReport.setAttribute("SHComposite", SHComposite);
        		
        		root.appendChild(Node_DailyReport);
        	}
        }
		
		TransformerFactory tfactory=TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = tfactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 获得最终oriXmlStr
		String oriXmlStr ="";
		if(null != doc && null != transformer)
		{
			transformer.setOutputProperty("encoding","GBK");
			DOMSource source=new DOMSource(doc);
			
			StringWriter writer = new StringWriter();
			StreamResult result=new StreamResult(writer);
			try {
				transformer.transform(source,result);
				oriXmlStr = writer.toString();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// 格式化XmlStr
		String formatedXmlStr = "";
		try {
			formatedXmlStr = BUtilsXML.format(oriXmlStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 更新到文件
		File cfile_new = new File(m_xmlFile);
		try {
			FileWriter fw = new FileWriter(cfile_new.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(formatedXmlStr);
		    bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void load()
	{
		String xmlStr = "";
		File cfile=new File(m_xmlFile);
		if(!cfile.exists())
		{
			return; // 没有文件
		}
		
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(cfile));
	        int fileLen = (int)cfile.length();
	        char[] chars = new char[fileLen];
	        reader.read(chars);
	        xmlStr = String.valueOf(chars);
			reader.close();
			//fmt.format("XML:\n" + xmlStr);
			if(xmlStr.length()<=0)
			{
				return; // 没有内容
			}
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder builder = factory.newDocumentBuilder();
		    StringReader sr = new StringReader(xmlStr);
		    InputSource is = new InputSource(sr);
		    Document doc = builder.parse(is);
		    Element rootElement = doc.getDocumentElement();
		    
		    // 检查返回数据有效性
		    if(!rootElement.getTagName().contains("history")) 
			{
				return; // 没有root
			}
		    
		    NodeList nodelist_DailyReport = rootElement.getElementsByTagName("DailyReport");
		    if(null!=nodelist_DailyReport)
        	{
		        for (int i = 0; i < nodelist_DailyReport.getLength(); i++) {
		        	Node node_DailyReport = nodelist_DailyReport.item(i);
		        	if(node_DailyReport.getNodeType() == Node.ELEMENT_NODE)
		        	{
		        		String date = ((Element)node_DailyReport).getAttribute("date");
		        		String totalAssets = ((Element)node_DailyReport).getAttribute("totalAssets");
		        		String SHComposite = ((Element)node_DailyReport).getAttribute("SHComposite");
		        		
		        		DailyReport cDailyReport = new DailyReport();
		        		cDailyReport.date = date;
		        		cDailyReport.fTotalAssets = Float.parseFloat(totalAssets);
		        		cDailyReport.fSHComposite = Float.parseFloat(SHComposite);
		        		
		        		m_cDailyReportList.add(cDailyReport);
		        	}
		        }
        	}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
			return;
		}
	}
	
	private String m_xmlFile;
	private List<DailyReport> m_cDailyReportList;
}
