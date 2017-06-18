package de.csw.ontomaven;

import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.HSTExplanationGenerator;
import de.csw.ontomaven.util.Util;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;
import java.util.Optional;
import java.util.Set;

/**
 * Tests an ontology regarding the syntax and consistency. The
 * result will be printed in the console and written into a
 * specified file.
 * 
 * @goal TestOntology
 * @phase test
 */
public class TestOntology extends AbstractMojo {

	/**
	 * Working directory, where owl files are stored. It should be
	 * a relative path in the maven project directory.
	 * 
	 * @parameter 	property="owlDirectory"
	 * 				default-value=""
	 * @required
	 */
	private String owlDirectory;
	
	/**
	 * Name of the ontology file, which should be in the working directory.
	 * It should be a name like "myOntology.owl".
	 *
	 * @parameter 	property="owlFileName"
	 * 				default-value="ontology.owl"
	 * @required
	 */
	private String owlFileName;
	
	/**
	 * IRI of the aspect annotation property. All annotations which have
	 * this iri as annotation property value, will be classified as
	 * aspect annotations.
	 * 
	 * @parameter property="aspectsIRI"
	 * default-value="http://www.corporate-semantic-web.de/ontologies/aspect_owl#hasAspect"
	 */
	private String aspectsIRI;
	
	/**
	 * If the goal should apply the aspects on the ontology before it
	 * works with the ontology.
	 * 
	 * @parameter 	expression=${ifApplyAspects}
	 *				defaul-value="false"
	 */
	private boolean ifApplyAspects;
	
	/**
	 * Aspects given by the user. These aspects will be applied on
	 * the ontology, if they are contained in the ontology.
	 * 
	 * @parameter
	 */
	private String[] userAspects;

	/**
	 * Name of the catalog file, where the imports are registered.
	 *
	 * @parameter 	property="catalogFileName"
	 * 				default-value="catalog-v001.xml"
	 * @required
	 */
	private String catalogFileName;


	/** Tests an ontology regarding syntax and consistency */
	public void execute() throws MojoExecutionException {

		Log log = getLog();
		Util.printHead("Testing ontology...", log);

		// Loading ontology
		File owlFile = new File(owlDirectory + File.separator + owlFileName);
		OWLOntologyManager manager = Util.createManager(Util.createCatalog( Optional.ofNullable( catalogFileName ) ) );
		Optional<OWLOntology> oontology = Util.loadOntologyFile( manager, log, owlFile );
		if (!oontology.isPresent()) {
			log.warn("Could not load ontology " + owlFile.getAbsolutePath() );
			return;
		}
		OWLOntology ontology = oontology.get();
		// Printing that the syntax was OK. If it would be not OK, the execution
		// would not come until here
		log.info("Ontology loaded,  syntax OK.");
		
		
		// Applying aspects, if the user have sets the boolean true
		if(ifApplyAspects)
			Util.applyAspects(manager, aspectsIRI, ontology, userAspects, log);
		
		
		// Getting inconsistent axioms
		ReasonerFactory factory = new ReasonerFactory();

		Configuration configuration=new Configuration();
		configuration.throwInconsistentOntologyException=false;

		OWLReasoner reasoner = factory.createReasoner( ontology, configuration );
		BlackBoxExplanation exp=new BlackBoxExplanation( ontology, factory, reasoner );
		HSTExplanationGenerator multExplanator=new HSTExplanationGenerator( exp );
		// Now we can get explanations for the unsatisfiability.

		Set<OWLAxiom> inconsistentAxioms = multExplanator.getExplanation( ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing() );
	
		// Printing result of consistency check
		log.info("Testing consistency...");
		log.info("");
		if(inconsistentAxioms.size() == 0){
			log.info("Consistency OK, ontology is consistent");
		}else{
			log.info("Ontology inconsistent because of these axioms:");
			for (OWLAxiom inconsistentAxiom: inconsistentAxioms){
				log.info(" - " + inconsistentAxiom.toString());
			}
		}		
		
		Util.printTail(log);
	}
}