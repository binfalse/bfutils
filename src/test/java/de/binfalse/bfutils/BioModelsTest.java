/**
 * 
 */
package de.binfalse.bfutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.binfalse.bflog.LOGGER;
import de.binfalse.bfutils.GeneralTools;



/**
 * @author Martin Scharm
 *
 */
@RunWith(JUnit4.class)
public class BioModelsTest
{

	@Test
	public void testRetrieveFile ()
	{
		try
		{
			FileRetriever.setUpCache (new File ("/tmp/m2cat/cache"));
			File tmp = File.createTempFile ("junit", "BIOMOD");
			FileRetriever.getFile (new URI ("http://www.ebi.ac.uk/biomodels/models-main/simu/MODEL3336584391.png"), tmp);
			System.out.println (tmp);
		}
		catch (Exception e)
		{
			e.printStackTrace ();
			LOGGER.error (e, "unexpected error during caching test");
			fail ("unexpected error during caching test: " + e);
		}
		
	}
}
