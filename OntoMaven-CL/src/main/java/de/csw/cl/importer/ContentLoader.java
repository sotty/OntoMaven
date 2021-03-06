package de.csw.cl.importer;

import java.io.File;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import util.FileUtil;
import util.XMLUtil;
import de.csw.cl.importer.model.Construct;
import de.csw.cl.importer.model.Import;
import de.csw.cl.importer.model.Restrict;
import de.csw.cl.importer.model.Titling;

/**
 * A factory class which loads from given xml elements their representations
 * as java objects.
 */
public class ContentLoader {
	
	/**
	 * Creates a Construct object from a given construct xml element.
	 * Also the inner elements will be loaded and added to the construct
	 * object.
	 * 
	 * @param constructElement from that the representation will be created
	 * @return created Construct object
	 */
	public static Construct loadConstruct(Element constructElement) {
		// 1. Loading the titlings in this construct
		List<Titling> titlings = new LinkedList<Titling>();
		if (constructElement.getDocument() != Main.mainDocument) {
			for (Element titlingElement : constructElement.getChildren(
					"Titling", constructElement.getNamespace()))
				titlings.add(loadTitling(titlingElement));
		}

		// 2. Loading the restricts in this construct
		List<Restrict> restricts = new LinkedList<Restrict>();
		for (Element restrictElement : constructElement.getChildren("Restrict",
				constructElement.getNamespace()))
			restricts.add(loadRestrict(restrictElement));
		
		// 3. Loading the imports in this construct
		List<Import> imports = new LinkedList<Import>();
		for (Element importElement : constructElement.getChildren("Import",
				constructElement.getNamespace())) {
			
			String importUrl = getNameOf(importElement);
			
			// Case 3.1: This construct is the child of a titling element
			Element parent = constructElement.getParentElement();
			if (parent != null && parent.getName().equals("Titling")) {
				String titlingName = getNameOf(parent);
				
				// Case 3.1.1: This is not a circular import where a titling
				// is importing itself.
				if (!importUrl.equals(titlingName)) {
					loadImport(importElement, "", imports);
					
				// Case 3.1.2: This is not a circular import
				} else {
					
					if (constructElement.getDocument() != Main.mainDocument)
						activateTitling(importElement, false);
				}
				
			// Case 3.2: This construct is NOT a child of a titling -> regular import
			} else
				loadImport(importElement, "", imports);
		}
		return new Construct(titlings, restricts, imports);
	}

	/**
	 * Loads a titling element and its inner children.
	 * 
	 * @param titlingElement to be loaded
	 * @return the created representation of the titling
	 */
	private static Titling loadTitling(Element titlingElement) {
		String titlingName = getNameOf(titlingElement);
		
		// 1. Loading the construct elements
		List<Construct> constructs = new LinkedList<Construct>();
		for (Element constructElement : titlingElement.getChildren("Construct",
				titlingElement.getNamespace()))
			constructs.add(loadConstruct(constructElement));

		// 2. Loading the imports
		List<Import> imports = new LinkedList<Import>();
		for (Element importElement : titlingElement.getChildren("Import",
				titlingElement.getNamespace())){
			// Case 2.1: No circular importing
			if (!getNameOf(importElement).equals(titlingName)){
				loadImport(importElement, "", imports);
			
			// Case 2.2: Circular importing
			} else {
				if (titlingElement.getDocument() != Main.mainDocument)
					activateTitling(importElement, false);
			}
		}

		// 3. Loading the restricts in this construct
		List<Restrict> restricts = new LinkedList<Restrict>();
		for (Element restrictElement : titlingElement.getChildren("Restrict",
				titlingElement.getNamespace()))
			restricts.add(loadRestrict(restrictElement));
				
		return new Titling(constructs, titlingName, imports, restricts);
	}

	/**
	 * Creates from a given xml element an object, which represents an
	 * import. Adds the created import to a given list if it is loadable.
	 * 
	 * @param importElement from which the import will be created
	 * @param restrictURI which will be concatted to the url
	 * @param listToAdd the import will be added to the list.
	 */
	private static void loadImport(Element importElement, String restrictURI,
			List<Import> listToAdd) {
		
		// Creating url's, file name, fragment
		String url = getNameOf(importElement);
		String fragment = URI.create(url).getFragment();
		String downloadURL = url.replace("#" + fragment, "");
		if (!downloadURL.endsWith(".xml")) 
			downloadURL += ".xml";
		
		// Creating import declaration object
		Import importDecl = new Import("http://ontomaven.org?uri=" + url
			+ restrictURI, downloadURL, importElement, false, url, fragment);
		
		// If the file to import is not loadable, print an error message.
		if (!FileUtil.existsHTTPFile(importDecl.getDownloadURL())){
			System.err.println("Import not realized -> File"
				+ "not existing: " + importDecl.getDownloadURL());
		}
		
		// If import is already existing, don't taje ut against
		if (Main.catalog.containsImportDeclaration(importDecl)){
			System.out.println("Import " + importDecl.getOriginalURL()
					+ " already existing.");
			activateTitling(importElement, true);
			return;
		} else {
			Main.catalog.addImport(importDecl);
		}
		
		// Adding import to the target list
		listToAdd.add(importDecl);
	}
	

	/**
	 * Loads a restrict element to a representing restrict object. Also the
	 * inner import declarations will be loaded. The name of the restrict
	 * will concatted to the url of import.
	 * 
	 * @param restrictElement to be loaded
	 * @return created restrict object
	 */
	private static Restrict loadRestrict(Element restrictElement) {
		String restrictURI = getNameOf(restrictElement);
		
		List<Import> imports = new LinkedList<Import>();
		for (Element importElement : restrictElement.getChildren("Import",
				restrictElement.getNamespace()))
			loadImport(importElement, ";dom1=" + restrictURI, imports);

		return new Restrict(restrictURI, imports);
	}

	/**
	 * Checks if a given document is consistent according to the names of the
	 * children of the root element. If there are two or more children of
	 * the root which have the same name, the document is inconsistent.
	 * 
	 * @param document which will be tested
	 * @return if the document is inconsistent
	 */
	public static boolean isConsistent(Document document){
		List<Element> children = document.getRootElement().getChildren();
		for (Element currentChild: children){
			for (Element child: children){
				if (child != currentChild && getNameOf(currentChild).equals(getNameOf(child)))
					return false;
			}
		}
		return true;
	}
	
	/**
	 * Removes for a given element the wrapper titling elements until the first
	 * titling element. For example if the directly parent is a construct, it
	 * searches for more parents. If a titling is found it stops.
	 * 
	 * @param element from that the wrapper titling elements will be removed
	 */
	public static void activateTitling(Element element, boolean ifRemoveElement) {
		// Finding all titling parents which are not the root of the document
		Element currentParent = element.getParentElement();
	
		if (ifRemoveElement)
			element.detach();
		
		while (currentParent != null && !currentParent.isRootElement()) {
			
			for (Element child: currentParent.getChildren()){
				child.detach();
				if (!child.getName().equals("Name"))
					currentParent.getParentElement().addContent(child);
			}
			
			
			
			if (currentParent.getName().equals("Titling")){
				
				currentParent.detach();
				break;
			}
			

			Element oldCurrentParent = currentParent;
			currentParent = currentParent.getParentElement();
			oldCurrentParent.detach();
			
			
		}
		
		
	}
	
	/**
	 * Returns the name of given element by returning the value of
	 * the attribute "cri" of the child "Name".
	 * 
	 * @param element from that the name will be returned
	 * @return name of the given element
	 */
	private static String getNameOf(Element element){
		Element nameChild = element.getChild("Name", element.getNamespace());
		if (nameChild == null)
			return null;
		String name =  nameChild.getAttributeValue("cri");
		if (name == null || name.equals("null"))
			return null;
		return name;
	}

	/**
	 * Saves a given document in the includes directory, if not existing
	 */
	
	public static void saveInclude(Document documentToSave, String fileName){
		boolean ifSaveFile = true;
		for (File existingFile : new File("includes").listFiles()) {
			if (existingFile.getName().equals(fileName))
				ifSaveFile = false;
		}
		if (ifSaveFile){
			XMLUtil.writeXML(documentToSave, new File("includes"
					+ File.separator + fileName));
			System.out.println("File " + fileName + " has been saved.");
		}else {
			System.out.println(fileName + " already existing and not saved.");
		}
	}
}