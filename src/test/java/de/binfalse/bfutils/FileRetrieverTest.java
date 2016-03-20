/**
 * 
 */
package de.binfalse.bfutils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.binfalse.bflog.LOGGER;


/**
 * @author martin
 *
 */
@RunWith(JUnit4.class)
public class FileRetrieverTest
{

	@Test
	public void testContentNegotiation ()
	{
		try
		{
			File tmp = File.createTempFile ("junit", "test");
			FileRetriever.getFile (new URI ("http://purl.uni-rostock.de/comodi/comodi#Target"), tmp);
			String result = GeneralTools.fileToString (tmp);
			assertTrue ("expected an OWL/RDF file but did not find <!DOCTYPE rdf:RDF", result.contains ("<!DOCTYPE rdf:RDF"));
			assertTrue ("expected an OWL/RDF file but did not find http://www.w3.org/2002/07/owl", result.contains ("http://www.w3.org/2002/07/owl"));

			Map<String, String> addHeaders = new HashMap<String, String> ();
			addHeaders.put ("Accept", "text/html");
			FileRetriever.getFile (new URI ("http://purl.uni-rostock.de/comodi/comodi#Target"), tmp, addHeaders);
			result = GeneralTools.fileToString (tmp);
			assertTrue ("expected an html file but did not find <!DOCTYPE html>", result.contains ("<!DOCTYPE html>"));
			assertTrue ("expected an html file but did not find Please cite COMODI as:", result.contains ("Please cite COMODI as:"));

			addHeaders.put ("Accept", "something");
			FileRetriever.getFile (new URI ("http://purl.uni-rostock.de/comodi/comodi#Target"), tmp, addHeaders);
			result = GeneralTools.fileToString (tmp);
			assertTrue ("expected an OWL/RDF file but did not find <!DOCTYPE rdf:RDF", result.contains ("<!DOCTYPE rdf:RDF"));
			assertTrue ("expected an OWL/RDF file but did not find http://www.w3.org/2002/07/owl", result.contains ("http://www.w3.org/2002/07/owl"));
			
			tmp.delete ();
		}
		catch (Exception e)
		{
			LOGGER.error (e, "unexpected error during content negotiation test");
			fail ("unexpected error during content negotiation test: " + e);
		}
	}
}
