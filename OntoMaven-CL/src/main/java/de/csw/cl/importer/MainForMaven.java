package de.csw.cl.importer;

import java.io.File;
import java.util.LinkedList;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.xml.security.Init;
import org.jdom2.Document;
import org.jdom2.Element;

import util.TitlingLocationResolver;
import util.XMLUtil;
import de.csw.cl.importer.model.Catalog;
import de.csw.cl.importer.model.Construct;
/**
 * Loads transitive the imports of a given common logic file
 * in xml format.
 * 
 * @goal Import
 */
public class MainForMaven extends AbstractMojo {
	
	/**
	 * Working directory, where owl files are stored. It should be
	 * a relative path in the maven project directory.
	 * 
	 * @parameter (property="clFile")
	 * @required
	 */
	File clFile;
	
	/**
	 * Destination directory for the files resulting from the importation
	 * process. If left empty, a directory result will be created in the parent
	 * directory of clFile.
	 * 
	 * @parameter property="destinationDir"
	 */
	File destinationDir;
	
	
	
	public static Catalog catalog;
	public static LinkedList<Document> documentsToDo = new LinkedList<Document>();
	public static Document mainDocument;
	public static File destDir;
	public static TitlingLocationResolver titlingLocationResolver;
	
	/**
	 * Executes the importing.
	 */
	public void execute() throws MojoExecutionException {
		
//		if (!destinationDir.mkdirs()) {
//			System.err.println("Error creating directory " + destinationDir.getAbsolutePath());
//		}
		
		destDir = destinationDir;
		
		MainForMaven.catalog = new Catalog();
		
		mainDocument = XMLUtil.readLocalDoc(clFile);
		
		// Some initialization...
		System.out.println("Loading main document...");
		Init.init(); // for using the library to building canonical xml
		new File(destinationDir, "includes").mkdirs(); // for saving include files
	
		// Adding main document to the ToDo List to begin with it.
		System.out.println("Main document has been loaded.");
		documentsToDo.add(mainDocument);
		
		// Loading imports of all documents, which have been to the todo list
		// Only the main document and documents which have imports are added
		while (!documentsToDo.isEmpty()) {
			Document currentDocument = documentsToDo.pop();
			Element currentRoot = currentDocument.getRootElement();
			Construct currentConstruct = ContentLoader.loadConstruct(currentRoot);
			currentConstruct.solveImports();
		}
		
		// Saving the main document
		System.out.println("Saving the main document...");
		XMLUtil.writeXML(mainDocument, new File(destinationDir, "main.xml"));
		
		// Saving the catalog file
		System.out.println("Saving catalog file...");
		catalog.saveCatalogFile();
	}
	
	public void setClFile(File clFile) {
		this.clFile = clFile;
		titlingLocationResolver = new TitlingLocationResolver(clFile.getParentFile());
	}
	
	public void setDestinationDir(File destinationDir) {
		this.destinationDir = destinationDir;
	}

}
