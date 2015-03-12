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
		String rutaArchivo = pathTemporalFiles+"/"+System.nanoTime()+".xls";
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("Collection");
			doc.appendChild(rootElement);
			
			
			if (Estructura)
			{
			Attr attr = doc.createAttribute("id");
			attr.setValue(Long.toString(salvar.getClavilenoid()));
			rootElement.setAttributeNode(attr);
			
			Element CName = doc.createElement("Name");
			CName.appendChild(doc.createTextNode(salvar.getName()));
			rootElement.appendChild(CName);
			
			Element CDescripcion = doc.createElement("Descripcion");
			CDescripcion.appendChild(doc.createTextNode(salvar.getDescription()));
			rootElement.appendChild(CDescripcion);
			
			for (CompleteGrammar CG : salvar.getMetamodelGrammar()) {
				
				Element Grammar = doc.createElement("Grammar");
				rootElement.appendChild(Grammar);
		 
				
				Attr attrid = doc.createAttribute("id");
				attrid.setValue(Long.toString(CG.getClavilenoid()));
				Grammar.setAttributeNode(attrid);
				
				Element attrName = doc.createElement("Name");
				attrName.appendChild(doc.createTextNode(CG.getNombre()));
				Grammar.appendChild(attrName);
				
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
					
					Element attrGName = doc.createElement("Name");
					attrGName.appendChild(doc.createTextNode(CGCD.getNombre()));
					attrType.appendChild(attrGName);
					
					Element attrGDescription = doc.createElement("Description");
					attrGDescription.appendChild(doc.createTextNode(CGCD.getDescription()));
					attrType.appendChild(attrGDescription);
				}
//				attrType.setValue(CD.getDocument());

				
				Element attrName = doc.createElement("Icon");
				attrName.appendChild(doc.createTextNode(CD.getIcon()));
				Documento.appendChild(attrName);
				
				Element attrDescription = doc.createElement("Description");
				attrDescription.appendChild(doc.createTextNode(CD.getDescriptionText()));
				Documento.appendChild(attrDescription);
				
				for (CompleteElement CE : CD.getDescription()) {
					
					Element Elemento = doc.createElement("Elemento");
					rootElement.appendChild(Elemento);
			 
					
					Attr attrEid = doc.createAttribute("id");
					
					try {
						attrEid.setValue(Long.toString(CE.getClavilenoid()));
						Elemento.setAttributeNode(attrEid);
					} catch (Exception e) {
						e.printStackTrace();
					}
					

					Element attrETypeID = doc.createElement("ValueType");
					procesStructureES(attrETypeID,doc,CE.getHastype(),false);
					
					Element attrEValue = doc.createElement("Value");
					getValue(attrEValue,CE,doc);

					
					
				}
			}
			
			if (Archivos)
			{
			for (CompleteFile CF : salvar.getSectionValues()) {
				
				Element File = doc.createElement("File/URL");
				rootElement.appendChild(File);
		 
				
				Attr attrid = doc.createAttribute("id");
				attrid.setValue(Long.toString(CF.getClavilenoid()));
				File.setAttributeNode(attrid);
				
				Element attrName = doc.createElement("Path/URI");
				attrName.appendChild(doc.createTextNode(CF.getPath()));
				File.appendChild(attrName);
				
			}
			}
			}
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			
			StreamResult result = new StreamResult(new File("rutaArchivo"));
			 
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
	 
			transformer.transform(source, result);
	 
			System.out.println("File saved!");
			
			
		} catch (Exception e) {
			cL.getLogLines().add("Error en la creacion del XML: " + e.getMessage() );
			e.printStackTrace();
		}
		return rutaArchivo;
	}

	
	private static void getValue(Element attrEValue,CompleteElement cE,Document doc) {
		if (cE instanceof CompleteTextElement)
			{
			Element attrV = doc.createElement("Text");
			attrV.appendChild(doc.createTextNode(((CompleteTextElement) cE).getValue()));
			attrEValue.appendChild(attrV);
			}
		else
			if (cE instanceof CompleteLinkElement)
			{
				Element attrV = doc.createElement("Relation");
				attrEValue.appendChild(attrV);
//				attrV.appendChild(doc.createTextNode(((CompleteTextElement) cE).getValue()));
				
			}
			else
				if (cE instanceof CompleteResourceElementFile)
				{
					Element attrV = doc.createElement("File");
					attrEValue.appendChild(attrV);
//					attrV.setValue(((CompleteTextElement) cE).getValue());
				}
				else
					if (cE instanceof CompleteResourceElementURL)
					{
						Element attrV = doc.createElement("URL");
						attrEValue.appendChild(attrV);
//						attrV.setValue(((CompleteTextElement) cE).getValue());
					}
		
	}


	private static void procesStructureES(Element attrETypeID, Document doc,
			CompleteElementType son, boolean b) {
		
		
		Element Structure = doc.createElement("Structure");
		attrETypeID.appendChild(Structure);
 
		
		Attr attrid = doc.createAttribute("id");
		attrid.setValue(Long.toString(son.getClavilenoid()));
		Structure.setAttributeNode(attrid);
		
		
		String Class="unknown";
		Class="Element";
	
			if (son instanceof CompleteTextElementType)
				Class="Text Element";
			else if (son instanceof CompleteResourceElementType)
				Class="Relation Resource Element";
			else if (son instanceof CompleteLinkElementType)
				Class="Relation Document Element";

			Element attrClase = doc.createElement("Class");
			attrClase.appendChild(doc.createTextNode(Class));
			Structure.appendChild(attrClase);
			
			Element attrName = doc.createElement("Name");
		attrName.appendChild(doc.createTextNode(ProduceName(son)));
		Structure.appendChild(attrName);
		
		
		
	}


	private static String ProduceName(CompleteStructure son) {

		String Name="unknown";
		
		if (son instanceof CompleteElementType)
			Name=((CompleteElementType) son).getName();
		else if (son instanceof CompleteIterator)
			Name="*";

		if (son.getFather()==null)
			return son.getCollectionFather()+"/"+Name;
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
				
			
			Element attrClase = doc.createElement("Class");
			attrClase.appendChild(doc.createTextNode(Class));
			Structure.appendChild(attrClase);
			
			Element attrName = doc.createElement("Name");
			attrName.appendChild(doc.createTextNode(Name));
			Structure.appendChild(attrName);
			
			
			
			
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
	
	

}
