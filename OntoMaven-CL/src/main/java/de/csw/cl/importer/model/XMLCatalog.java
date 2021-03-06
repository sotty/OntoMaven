package de.csw.cl.importer.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;

import util.XMLUtil;

/**
 * 
 * @author ralph
 *
 */
public class XMLCatalog {
	
	
	private HashMap<String, String> uriMappings = new HashMap<String, String>();
	
	public XMLCatalog() {
	    
	}
	
	public XMLCatalog(File catalogFile) {
		if(catalogFile.exists()) {
		    Document catalogDocument = XMLUtil.readLocalDoc(catalogFile);
		    Element root = catalogDocument.getRootElement();
		    for (Element c: root.getChildren()) {
		        String uriString = c.getAttributeValue("uri");
		        addMapping( c.getAttributeValue("name"), uriString );
		    }
		}
	}
	
	public void addMapping(String uri, String fileHash) {
		uriMappings.put(uri, fileHash);
	}
	
	public void removeMapping(String uri) {
	    uriMappings.remove(uri);
	    
	}
	
	public XMLCatalog clone() {
	    XMLCatalog newCatalog = new XMLCatalog();
	    for ( Entry<String, String> entry: this.getMappings().entrySet() ){
	        newCatalog.addMapping(entry.getKey(), entry.getValue());
	    }
	    return newCatalog;
	}
	
	public String getFileHash(String uri) {
		return uriMappings.get(uri);
	}
	
	public HashMap<String, String> getMappings() {
	    return uriMappings;
	}
	
	public void write(File resultDir) {
	    if (uriMappings.size() > 0) {
	        File outCatalogFile = new File(resultDir, "catalog.xml");
    		Document catalogDocument = new Document();
    		catalogDocument.setDocType(new DocType("catalog",
    						"-//OASIS//DTD XML Catalogs V1.1//EN",
    						"http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd"));
    		Element root = new Element("catalog", XMLUtil.NS_CATALOG);
    		root.setAttribute("prefer", "public");
    		catalogDocument.setRootElement(root);
    		for (Entry<String, String> mapping : uriMappings.entrySet()) {
    			Element newElement = new Element("uri");
    			newElement.setAttribute("name", mapping.getKey());
    			newElement.setAttribute("uri", mapping.getValue());
    			newElement.setNamespace(XMLUtil.NS_CATALOG);
    			catalogDocument.getRootElement().addContent(newElement);
    		}
    		XMLUtil.writeXML(catalogDocument, outCatalogFile);
	    }
	}
	
}
