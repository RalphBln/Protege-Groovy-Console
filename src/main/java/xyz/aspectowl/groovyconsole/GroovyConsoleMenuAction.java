package xyz.aspectowl.groovyconsole;

import groovy.console.ui.Console;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.tools.shell.util.Preferences;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import java.awt.event.ActionEvent;

/**
 * @author Ralph Schäfermeier
 */
public class GroovyConsoleMenuAction extends ProtegeOWLAction implements OWLModelManagerListener {
  
  private static final Logger log = LoggerFactory.getLogger(GroovyConsoleMenuAction.class);
  
  private static final AttributeSet style = SimpleAttributeSet.EMPTY;
  
  private Thread consoleThread;
  private Console console;
  
  @Override
  public void initialise() throws Exception {
    
    log.info("Initializing Groovy Console menu item plug-in");
    
    consoleThread = new Thread("Protege Groovy Console Thread");
    
    console = new Console();
    
    Preferences.put("captureStdOut", "false");
    Preferences.put("captureStdErr", "false");
    Preferences.put("visualizeScriptResults", "true");
    
    ImportCustomizer imports = new ImportCustomizer().addStarImports("org.semanticweb.owlapi.model");
    console.getConfig().addCompilationCustomizers(imports);
    
    console.setRunThread(consoleThread);
    
    getOWLWorkspace().getOWLModelManager().addListener(this);
  }
  
  @Override
  public void dispose() throws Exception {
    
    log.info("Stopping Groovy Console menu item plug-in");
    
    getOWLWorkspace().getOWLModelManager().removeListener(this);
    console = null;
    consoleThread = null;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    
    log.debug("Showing Groovy Console");
    
    console.setVariable("onto", getOWLModelManager().getActiveOntology());
    console.setVariable("om", getOWLModelManager().getOWLOntologyManager());
    console.setVariable("df", getOWLDataFactory());
    
    console.run();
    
    console.appendOutputNl("Welcome the Protégé Groovy Console.", style);
    console.appendOutputNl(" ", style);
    console.appendOutputNl("The following pre-set variables are available:", style);
    console.appendOutputNl(" ", style);
    console.appendOutputNl("  - onto: The currently active ontology (the value changes as the user switches between active ontologies)", style);
    console.appendOutputNl("  - om: The OWLOntologyManager instance", style);
    console.appendOutputNl("  - df: The OWLDataFactory instance", style);
    console.appendOutputNl(" ", style);
  }
  
  @Override
  public void handleChange(OWLModelManagerChangeEvent event) {
    if (event.isType(EventType.ACTIVE_ONTOLOGY_CHANGED)) {
      console.setVariable("onto", getOWLModelManager().getActiveOntology());
      console.appendOutputNl("Active ontology changed to " + getOWLModelManager().getActiveOntology().getOntologyID().getOntologyIRI().get(), style);
    }
  }
  
}
