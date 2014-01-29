/**
 * 
 */
package de.binfalse.bftools;

import java.util.Random;
import java.util.UUID;


/**
 * @author martin
 *
 */
public class PerfomanceTest
{
	public static void main (String [] args)
	{
		Random r = new Random ();
		String [] testStrings = new String [1000];
		for (int i = 0; i < testStrings.length; i++)
			testStrings[i] = GeneralTools.repeat (UUID.randomUUID ().toString (), r.nextInt (50) + 1);

		
		// start

		long md5 = 0;
		long sha1 = 0;
		long sha256 = 0;
		long sha512 = 0;
		int md5Size = 0;
		int sha1Size = 0;
		int sha256Size = 0;
		int sha512Size = 0;
		StringBuilder h;
		long now = 0;
		
		h = new StringBuilder ();
		now = System.currentTimeMillis ();
		for (int i = 0; i < testStrings.length; i++)
		{
			h.append (testStrings[i]);
		}
		md5 = System.currentTimeMillis () - now;
		

		h = new StringBuilder ();
		now = System.currentTimeMillis ();
		for (int i = 0; i < testStrings.length; i++)
		{
			h.append (GeneralTools.hashSha512 (testStrings[i]));
		}
		sha512 = System.currentTimeMillis () - now;
		sha512Size = h.toString ().length ();

		h = new StringBuilder ();
		now = System.currentTimeMillis ();
		for (int i = 0; i < testStrings.length; i++)
		{
			h.append (GeneralTools.hashSha1 (testStrings[i]));
		}
		sha1 = System.currentTimeMillis () - now;
		sha1Size = h.toString ().length ();
	

		h = new StringBuilder ();
		now = System.currentTimeMillis ();
		for (int i = 0; i < testStrings.length; i++)
		{
			h.append (GeneralTools.hashSha256 (testStrings[i]));
		}
		sha256 = System.currentTimeMillis () - now;
		sha256Size = h.toString ().length ();

		h = new StringBuilder ();
		now = System.currentTimeMillis ();
		for (int i = 0; i < testStrings.length; i++)
		{
			h.append (GeneralTools.hashMd5 (testStrings[i]));
		}
		md5 = System.currentTimeMillis () - now;
		md5Size = h.toString ().length ();
		

		System.out.println ("md5: " + md5 + " / " + md5Size);
		System.out.println ("sha1: " + sha1 + " / " + sha1Size);
		System.out.println ("sha256: " + sha256 + " / " + sha256Size);
		System.out.println ("sha512: " + sha512 + " / " + sha512Size);
	}
}
