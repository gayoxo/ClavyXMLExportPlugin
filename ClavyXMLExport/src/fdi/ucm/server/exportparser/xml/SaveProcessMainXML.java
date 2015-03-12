/**
 * 
 */
package fdi.ucm.server.exportparser.xml;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import fdi.ucm.server.modelComplete.CompleteImportRuntimeException;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.CompleteLogAndUpdates;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteFile;
import fdi.ucm.server.modelComplete.collection.document.CompleteLinkElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteResourceElementFile;
import fdi.ucm.server.modelComplete.collection.document.CompleteResourceElementURL;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteIterator;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteLinkElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteResourceElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteStructure;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteTextElementType;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



/**
 * Clase que define las funciones basicas para la exportacion a SQL
 * @author Joaquin Gayoso-Cabada
 *
 */
public class SaveProcessMainXML {



	public static String processCompleteCollection(CompleteLogAndUpdates cL,
			CompleteCollection salvar, boolean Estructura, boolean Documentos, boolean Archivos,
			String pathTemporalFiles) throws IOException{
		String rutaArchivo = pathTemporalFiles+"/"+System.nanoTime()+".xml";
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("Collection");
			doc.appendChild(rootElement);
			
			Attr attr = doc.createAttribute("id");
			attr.setValue(Long.toString(salvar.getClavilenoid()));
			rootElement.setAttributeNode(attr);
			
			Element CName = doc.createElement("Name");
			CName.appendChild(doc.createTextNode(salvar.getName()));
			rootElement.appendChild(CName);
			
			Element CDescripcion = doc.createElement("Descripcion");
			CDescripcion.appendChild(doc.createTextNode(salvar.getDescription()));
			rootElement.appendChild(CDescripcion);
			
			if (Estructura)
			{
			
			
			for (CompleteGrammar CG : salvar.getMetamodelGrammar()) {
				
				Element Grammar = doc.createElement("Grammar");
				rootElement.appendChild(Grammar);
		 
				
				Attr attrid = doc.createAttribute("id");
				attrid.setValue(Long.toString(CG.getClavilenoid()));
				Grammar.setAttributeNode(attrid);
				
				Attr attrName = doc.createAttribute("Name");
				attrName.setValue(CG.getNombre());
				Grammar.setAttributeNode(attrName);
				
				Element attrDescription = doc.createElement("Description");
				attrDescription.appendChild(doc.createTextNode(CG.getDescription()));
				Grammar.appendChild(attrDescription);
				
				for (CompleteStructure CS : CG.getSons()) {
					procesStructureG(Grammar,doc,CS);
				}
			}
			}
			
			if (Documentos)
			{
			for (CompleteDocuments CD : salvar.getEstructuras()) {
				
				Element Documento = doc.createElement("Document");
				rootElement.appendChild(Documento);
		 
				
				Attr attrid = doc.createAttribute("id");
				attrid.setValue(Long.toString(CD.getClavilenoid()));
				Documento.setAttributeNode(attrid);
				
				Element attrType = doc.createElement("DocumentType");
				{
					CompleteGrammar CGCD=CD.getDocument();
					Attr attrGid = doc.createAttribute("id");
					attrGid.setValue(Long.toString(CGCD.getClavilenoid()));
					attrType.setAttributeNode(attrGid);
					
					Attr attrGName = doc.createAttribute("Name");
					attrGName.setValue(CGCD.getNombre());
					attrType.setAttributeNode(attrGName);
					
					Element attrGDescription = doc.createElement("Description");
					attrGDescription.appendChild(doc.createTextNode(CGCD.getDescription()));
					attrType.appendChild(attrGDescription);
				}
				Documento.appendChild(attrType);
//				attrType.setValue(CD.getDocument());

				
				Element attrName = doc.createElement("Icon");
				attrName.appendChild(doc.createTextNode(CD.getIcon()));
				Documento.appendChild(attrName);
				
				Element attrDescription = doc.createElement("Description");
				attrDescription.appendChild(doc.createTextNode(CD.getDescriptionText()));
				Documento.appendChild(attrDescription);
				
				for (CompleteElement CE : CD.getDescription()) {
					
					
					String Ambito=genera_ambito(CE);
					Element Elemento = doc.createElement("Elemento");
					Documento.appendChild(Elemento);

					
					Attr attrEid = doc.createAttribute("id");
					attrEid.setValue(Long.toString(CE.getClavilenoid()));
					Elemento.setAttributeNode(attrEid);
					
					if (!Ambito.isEmpty())
					{
					Attr attrESco = doc.createAttribute("Scope");
					attrESco.setValue(Ambito);
					Elemento.setAttributeNode(attrESco);
					}
					
					
					Element attrETypeID = doc.createElement("ValueType");
					Elemento.appendChild(attrETypeID);
					procesStructureES(attrETypeID,doc,CE.getHastype(),false);
					
					Element attrEValue = doc.createElement("Value");
					Elemento.appendChild(attrEValue);
					getValue(attrEValue,CE,doc);

					
					
				}
			}
			}
			
			if (Archivos)
			{
			for (CompleteFile CF : salvar.getSectionValues()) {
				
				Element File = doc.createElement("File_URL");
				rootElement.appendChild(File);
		 
				
				Attr attrid = doc.createAttribute("id");
				attrid.setValue(Long.toString(CF.getClavilenoid()));
				File.setAttributeNode(attrid);
				
				Element attrName = doc.createElement("Path_URI");
				attrName.appendChild(doc.createTextNode(CF.getPath()));
				File.appendChild(attrName);
				
			}
			
			}
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			
			StreamResult result = new StreamResult(new File(rutaArchivo));
			 
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
	 
			transformer.transform(source, result);
	 
			System.out.println("File saved!");
			
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CompleteImportRuntimeException("Error en la creacion del XML: "+e.getMessage());
		}
		return rutaArchivo;
	}

	
	private static String genera_ambito(CompleteElement cE) {
		StringBuffer Ambito = new StringBuffer();
		for (Integer ambitos : cE.getAmbitos()) {
			if (!Ambito.toString().isEmpty())
				Ambito.append(".");
			Ambito.append(ambitos);
		}
		return Ambito.toString();
	}


	private static void getValue(Element attrEValue,CompleteElement cE,Document doc) {
		
		Attr attrtype = doc.createAttribute("type");
		attrEValue.setAttributeNode(attrtype);
		
		if (cE instanceof CompleteTextElement)
			{
			attrtype.setValue("Text");
			
			if (((CompleteTextElement) cE).getValue()!=null)
			{
				attrEValue.appendChild(doc.createTextNode(((CompleteTextElement) cE).getValue()));
			}
			
			}
		else
			if (cE instanceof CompleteLinkElement)
			{
				attrtype.setValue("Relation Document");
				
				if (((CompleteLinkElement) cE).getValue()!=null)
				{
				Element attrid = doc.createElement("id");
				attrid.appendChild(doc.createTextNode((Long.toString(((CompleteLinkElement) cE).getValue().getClavilenoid()))));
				attrEValue.appendChild(attrid);
				
				Element attrName = doc.createElement("Icon");
				attrName.appendChild(doc.createTextNode(((CompleteLinkElement) cE).getValue().getIcon()));
				attrEValue.appendChild(attrName);
				
				Element attrDesc = doc.createElement("Description");
				attrDesc.appendChild(doc.createTextNode(((CompleteLinkElement) cE).getValue().getDescriptionText()));
				attrEValue.appendChild(attrDesc);
				}

				
			}
			else
				if (cE instanceof CompleteResourceElementFile)
				{
					attrtype.setValue("Relation Resource");
					
					if (((CompleteResourceElementFile) cE).getValue()!=null)
					{
						Element attrid = doc.createElement("id");
						attrid.appendChild(doc.createTextNode((Long.toString(((CompleteResourceElementFile) cE).getValue().getClavilenoid()))));
						attrEValue.appendChild(attrid);
					
					Element attrName = doc.createElement("Path_URI");
					attrName.appendChild(doc.createTextNode(((CompleteResourceElementFile) cE).getValue().getPath()));
					attrEValue.appendChild(attrName);
					}

				}
				else
					if (cE instanceof CompleteResourceElementURL)
					{
						attrtype.setValue("Relation Resource");
						
						if (((CompleteResourceElementURL) cE).getValue()!=null)
						{
						Element attrName = doc.createElement("Path_URI");
						attrName.appendChild(doc.createTextNode(((CompleteResourceElementURL) cE).getValue()));
						attrEValue.appendChild(attrName);
						}
					}
		
	}


	private static void procesStructureES(Element attrETypeID, Document doc,
			CompleteElementType son, boolean b) {
		
		 
		
		Attr attrid = doc.createAttribute("id");
		attrid.setValue(Long.toString(son.getClavilenoid()));
		attrETypeID.setAttributeNode(attrid);
		
		
		String Class="unknown";
		Class="Element";
	
			if (son instanceof CompleteTextElementType)
				Class="Text Element";
			else if (son instanceof CompleteResourceElementType)
				Class="Relation Resource Element";
			else if (son instanceof CompleteLinkElementType)
				Class="Relation Document Element";

			Attr attrClase = doc.createAttribute("Class");
			attrClase.setValue(Class);
			attrETypeID.setAttributeNode(attrClase);
			
			Attr attrName = doc.createAttribute("Name");
		attrName.setValue(ProduceName(son));
		attrETypeID.setAttributeNode(attrName);
		
		
		
	}


	private static String ProduceName(CompleteStructure son) {

		String Name="unknown";
		
		if (son instanceof CompleteElementType)
			Name=((CompleteElementType) son).getName();
		else if (son instanceof CompleteIterator)
			Name="*";

		if (son.getFather()==null)
			return son.getCollectionFather().getNombre()+"/"+Name;
		else
			return ProduceName(son.getFather())+"/"+Name;
	}


	private static void procesStructureG(Element grammar, Document doc,
			CompleteStructure son) {
			
			Element Structure = doc.createElement("Structure");
			grammar.appendChild(Structure);
	 
			
			Attr attrid = doc.createAttribute("id");
			attrid.setValue(Long.toString(son.getClavilenoid()));
			Structure.setAttributeNode(attrid);
			
			
			String Name="unknown";
			String Class="unknown";
			
			if (son instanceof CompleteElementType)
			{
				Name=((CompleteElementType) son).getName();
				Class="Element";
				
				if (son instanceof CompleteTextElementType)
					Class="Text Element";
				else if (son instanceof CompleteResourceElementType)
					Class="Relation Resource Element";
				else if (son instanceof CompleteLinkElementType)
					Class="Relation Document Element";
			}
			else if (son instanceof CompleteIterator)
			{
				Name="*";
				Class="Multivalued";
			}
				
			
			Attr attrClase = doc.createAttribute("Class");
			attrClase.setValue(Class);
			Structure.setAttributeNode(attrClase);
			
			Attr attrName = doc.createAttribute("Name");
			attrName.setValue(Name);
			Structure.setAttributeNode(attrName);
			
			
			
			
				for (CompleteStructure CS : son.getSons()) {
					procesStructureG(Structure,doc,CS);
				}
			
		
		
	}





	public static void main(String argv[]) {
		 
		  try {
	 
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	 
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("company");
			doc.appendChild(rootElement);
	 
			// staff elements
			Element staff = doc.createElement("Staff");
			rootElement.appendChild(staff);
	 
			// set attribute to staff element
			Attr attr = doc.createAttribute("id");
			attr.setValue("1");
			staff.setAttributeNode(attr);
	 
			// shorten way
			// staff.setAttribute("id", "1");
	 
			// firstname elements
			Element firstname = doc.createElement("firstname");
			firstname.appendChild(doc.createTextNode("yong"));
			staff.appendChild(firstname);
	 
			// lastname elements
			Element lastname = doc.createElement("lastname");
			lastname.appendChild(doc.createTextNode("mook kim"));
			staff.appendChild(lastname);
	 
			// nickname elements
			Element nickname = doc.createElement("nickname");
			nickname.appendChild(doc.createTextNode("mkyong"));
			staff.appendChild(nickname);
	 
			// salary elements
			Element salary = doc.createElement("salary");
			salary.appendChild(doc.createTextNode("100000"));
			staff.appendChild(salary);
	 
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			
			
			//StreamResult result = new StreamResult(new File("C:\\file.xml"));
	 
			// Output to console for testing
			 StreamResult result = new StreamResult(System.out);
	 
			transformer.transform(source, result);
	 
			System.out.println("File saved!");
	 
		  } catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		  } catch (TransformerException tfe) {
			tfe.printStackTrace();
		  }
		}
	
	
	public void Hello(String[] Hola)
	{
		main(null);		
	}
	
	public void Hello2(String Hola)
	{
		main(null);		
	}
	
	public void Hello3()
	{
	main(null);	
	}
}
