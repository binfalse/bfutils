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
 * @author martin
 * 
 */
@RunWith(JUnit4.class)
public class GeneralTest
{
	
	@Test
	public void testHashing ()
	{
		String msg = "sampleMessage\n";
		// test md5
		assertEquals ("md5 sum is wrong", "018680997a7d2c332ef384cd99174d38",
			GeneralTools.hashMd5 (msg));
		// test sha1
		assertEquals ("sha1 sum is wrong", "d1bce95fe1e2e9e3cc7c934c694c795"
			+ "69a29993d", GeneralTools.hashSha1 (msg));
		// test sha265
		assertEquals ("sha256 sum is wrong", "63b07320d6d6385e09168ee594934"
			+ "684f6948b68706fa3b9d87461de3a4c7e8e", GeneralTools.hashSha256 (msg));
		// test sha512
		assertEquals ("sha512 sum is wrong", "4418ab0a057072c9e607322c828e5"
			+ "6a5f9643d921a1e204424056506ac028f1d45889209bd0a3177eb59e1d017f"
			+ "3d18e14ba5e4196de9a454d5ff064daf0f185", GeneralTools.hashSha512 (msg));
	}
	
	private void testMin (int a, int b, int c, int min)
	{
		assertEquals ("minimum of " + a + "/" + b + "/" + c + " is incorrect",
			min, GeneralTools.minimum (a, b, c));
	}

	@Test
	public void testMin ()
	{
		testMin (1,2,3,1);
		testMin (1,-2,3,-2);
		testMin (1,0,30,0);
		testMin (-1,-2,-3,-3);
		testMin (10,0,-30,-30);
	}
	

	private void testLevenshtein (String a, String b, int expected)
	{
		assertEquals ("Levenshtein of " + a + " and " + b + " is incorrect",
			expected, GeneralTools.computeLevenshteinDistance (a, b));
	}
	
	@Test
	public void testLevenshtein ()
	{
		testLevenshtein ("same", "same", 0);
		testLevenshtein ("kitten", "sitting", 3);
		testLevenshtein ("book", "back", 2);
		testLevenshtein ("martin", "rocks", 6);
		testLevenshtein ("Levenshtein", "meilenstein", 4);
	}
	
	public void testRepeat (String expected, String got)
	{
		assertEquals ("repeat produced wrong result", expected, got);
	}

	@Test
	public void testRepeat ()
	{
		testRepeat ("sssss", GeneralTools.repeat ("s", 5));
		testRepeat ("ssssss", GeneralTools.repeat ("ss", 3));
		testRepeat ("", GeneralTools.repeat ("ss", 0));
		testRepeat ("", GeneralTools.repeat ("ss", -1));
	}
	

	@Test
	public void testPrettyDouble ()
	{
		assertEquals ("pretty double produced wrong result", "5", GeneralTools.prettyDouble (new Double (5.0), null));
		assertEquals ("pretty double produced wrong result", "", GeneralTools.prettyDouble (new Double (5.0), 5));
		assertEquals ("pretty double produced wrong result", "5.01", GeneralTools.prettyDouble (5.01, null));
		assertEquals ("pretty double produced wrong result", "-5.01", GeneralTools.prettyDouble (-5.01, null));
		assertEquals ("pretty double produced wrong result", "-5", GeneralTools.prettyDouble (-5.000, null));
		assertEquals ("pretty double produced wrong result", "", GeneralTools.prettyDouble (-5.000, -5));
		

		assertEquals ("pretty double produced wrong result", "5*", GeneralTools.prettyDouble (new Double (5.0), null, "", "*"));
		assertEquals ("pretty double produced wrong result", "", GeneralTools.prettyDouble (new Double (5.0), 5, "", "*"));
		assertEquals ("pretty double produced wrong result", "+5.01", GeneralTools.prettyDouble (5.01, null, "+", ""));
		assertEquals ("pretty double produced wrong result", "+-5.01", GeneralTools.prettyDouble (-5.01, null, "+", ""));
		assertEquals ("pretty double produced wrong result", "-5", GeneralTools.prettyDouble (-5.000, 0, "", ""));
		assertEquals ("pretty double produced wrong result", "", GeneralTools.prettyDouble (-5.000, -5, "", ""));
		
	}
	
	@Test
	public void testByteToHex ()
	{
		assertEquals ("byte to hex has a problem", "010204080f1020", GeneralTools.byteToHex (new byte [] { 1, 2, 4, 8, 15, 16, 32 }));
	}
	
	@Test
	public void testFileRetriever ()
	{
		try
		{
			File tmp1 = File.createTempFile ("junit", "test");
			File tmp2 = File.createTempFile ("junit", "test");
			tmp1.deleteOnExit ();tmp2.deleteOnExit ();
			
			// test succ
			FileRetriever.getFile (new URI ("http://binfalse.de/"), tmp1);
			boolean succ = false;
			BufferedReader br = new BufferedReader (new FileReader (tmp1));
			while (br.ready ())
				if (br.readLine ().toLowerCase ().contains ("martin scharm"))
				{
					succ = true;
					break;
				}
			br.close ();
			assertTrue ("download of binfalse.de failed", succ);
			
			// test failed
			testDownloadInvalidFile ("http://should-not.exist.bcause.inval/this/should/not/exist", tmp2);
			testDownloadInvalidFile ("http://binfalse.de/this/should/not/exist", tmp2);
		}
		catch (Exception e)
		{
			e.printStackTrace ();
			LOGGER.error (e, "unexpected error during caching test");
			fail ("unexpected error during caching test: " + e);
		}
		
	}
	
	private void testDownloadInvalidFile (String url, File file)
	{
		try
		{
			FileRetriever.getFile (new URI (url), file);
			fail ("was able to download ");
		}
		catch (Exception e)
		{
		}
		assertTrue ("did not download but file was created?", !file.exists () || file.length () == 0);
	}
	
	
	@Test
	public void testCache ()
	{
		try
		{
			File tmp1 = File.createTempFile ("junit", "test");
			File tmp2 = File.createTempFile ("junit", "test");
			tmp1.deleteOnExit ();tmp2.deleteOnExit ();
			
			// let's go around the world
			long one = System.currentTimeMillis ();
			FileRetriever.getFile (new URI ("http://models.cellml.org/exposure/29a0ec2468a49a64a123f927083260f0/BestPractice_Cdecoupled.cellml"), tmp1);
			one = System.currentTimeMillis () - one;
			

			long two = System.currentTimeMillis ();
			FileRetriever.getFile (new URI ("http://models.cellml.org/exposure/29a0ec2468a49a64a123f927083260f0/BestPractice_Cdecoupled.cellml"), tmp2);
			two = System.currentTimeMillis () - two;
			
			LOGGER.info ("caching safes " + (one - two) + "ms for one file from NZ");
			
			assertTrue ("mmh, caching slows the process??", one > two);
			
			// make sure both files are the same
			StringBuilder str1 = new StringBuilder ();
			StringBuilder str2 = new StringBuilder ();
			BufferedReader br = new BufferedReader (new FileReader (tmp1));
			while (br.ready ())
				str1.append (br.readLine ()).append ("\n");
			br.close ();
			br = new BufferedReader (new FileReader (tmp2));
			while (br.ready ())
				str2.append (br.readLine ()).append ("\n");
			br.close ();
			
			assertEquals ("the files differ!?", str1.toString (), str2.toString ());
		}
		catch (Exception e)
		{
			LOGGER.error (e, "unexpected error during caching test");
			fail ("unexpected error during caching test: " + e);
		}
	}
}
