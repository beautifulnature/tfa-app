package tfa.tickets.pdf;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Fill a pdf template with form fields and generate a pdf file
 *  (template can be produce with libreoffice)
 */
public class PdfGenerator
{
    // Standard SLF4J logger
    private static Logger log = LoggerFactory.getLogger(PdfGenerator.class);

    private PDDocument pdf;
    private PDAcroForm form ;
    
    public PdfGenerator( String template ) throws IOException
    {
        super();

        try
        {
            // load the template
            pdf = PDDocument.load(new File(template));

            // get the form
            form = pdf.getDocumentCatalog().getAcroForm();
        }
        catch (IOException e)
        {
            pdf = null;
            form = null;
            throw e;
        }

    }
    
    public void set( String name, String value ) throws IOException
    {        
        if (form == null) return;
        
        // set field
        PDField field = form.getField(name);
        if ( field != null ) 
            field.setValue(value);  
        else
            log.warn("Field not found : " + name );
    }
    
     public void save( String filename ) throws IOException
    {  
        // Save and close 
        form.refreshAppearances();
        form.flatten();
        pdf.save(filename);
        pdf.close();
    }
}
