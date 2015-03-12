/**
 * 
 */
package fdi.ucm.server.exportparser.xml;

import java.io.IOException;
import java.util.ArrayList;

import fdi.ucm.server.modelComplete.ImportExportDataEnum;
import fdi.ucm.server.modelComplete.ImportExportPair;
import fdi.ucm.server.modelComplete.CompleteImportRuntimeException;
import fdi.ucm.server.modelComplete.SaveCollection;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.CompleteLogAndUpdates;

/**
 * @author Joaquin Gayoso-Cabada
 *
 */
public class SaveRemoteCollectionXML extends SaveCollection {

	
	private String FileO = null;
	private ArrayList<ImportExportPair> Parametros;
	private boolean Estructura;
	private boolean Documentos;
	private boolean Files;


	public SaveRemoteCollectionXML() {
		super();
	}
	
	
	/* (non-Javadoc)
	 * @see fdi.ucm.server.SaveCollection#processCollecccion(fdi.ucm.shared.model.collection.Collection)
	 */
	@Override
	public CompleteLogAndUpdates processCollecccion(CompleteCollection Salvar,
			String PathTemporalFiles) throws CompleteImportRuntimeException {

		CompleteLogAndUpdates CL=new CompleteLogAndUpdates();
		try {
			FileO=SaveProcessMainXML.processCompleteCollection(CL,Salvar,Estructura,Documentos,Files,PathTemporalFiles);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error en carpeta y escritura del archivo en el servidor");
		}
		return CL;

	}

	/* (non-Javadoc)
	 * @see fdi.ucm.server.SaveCollection#getConfiguracion()
	 */
	@Override
	public ArrayList<ImportExportPair> getConfiguracion() {
		if (Parametros==null)
		{
			ArrayList<ImportExportPair> ListaCampos=new ArrayList<ImportExportPair>();
			ListaCampos.add(new ImportExportPair(ImportExportDataEnum.Boolean, "Structure"));
			ListaCampos.add(new ImportExportPair(ImportExportDataEnum.Boolean, "Documents"));
			ListaCampos.add(new ImportExportPair(ImportExportDataEnum.Boolean, "Files/URLs"));
			Parametros=ListaCampos;
			return ListaCampos;
		}
		else return Parametros;
	}

	/* (non-Javadoc)
	 * @see fdi.ucm.server.SaveCollection#setConfiguracion(java.util.ArrayList)
	 */
	@Override
	public void setConfiguracion(ArrayList<String> DateEntrada) {
		if (DateEntrada!=null)	
		{
			String SoloEstructuraT = DateEntrada.get(0);
			if (SoloEstructuraT.equals(Boolean.toString(true)))
				Estructura=true;
			else 
				Estructura=false;
			
			String SoloDocumentosT = DateEntrada.get(1);
			if (SoloDocumentosT.equals(Boolean.toString(true)))
				Documentos=true;
			else 
				Documentos=false;
			
			String SoloFilesT = DateEntrada.get(2);
			if (SoloFilesT.equals(Boolean.toString(true)))
				Files=true;
			else 
				Files=false;
		}
	}

	/* (non-Javadoc)
	 * @see fdi.ucm.server.SaveCollection#getName()
	 */
	@Override
	public String getName() {
		return "XML Clavy";
	}
	
	
	/**
	 * QUitar caracteres especiales.
	 * @param str texto de entrada.
	 * @return texto sin caracteres especiales.
	 */
	public String RemoveSpecialCharacters(String str) {
		   StringBuilder sb = new StringBuilder();
		   for (int i = 0; i < str.length(); i++) {
			   char c = str.charAt(i);
			   if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_') {
			         sb.append(c);
			      }
		}
		   return sb.toString();
		}



	@Override
	public boolean isFileOutput() {
		return true;
	}


	@Override
	public String FileOutput() {
		return FileO;
	}


	@Override
	public void SetlocalTemporalFolder(String TemporalPath) {
		
	}

}
