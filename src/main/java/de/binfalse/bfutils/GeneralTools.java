/**
 * 
 */
package de.binfalse.bfutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import javax.xml.bind.DatatypeConverter;

import de.binfalse.bflog.LOGGER;


/**
 * @author martin
 *
 */
public class GeneralTools
{
	/**
	 * Get the minimum of 3 values.
	 *
	 * @param a the a
	 * @param b the b
	 * @param c the c
	 * @return the min
	 */
	public static int minimum(int a, int b, int c)
	{
	    return Math.min (Math.min (a, b), c);
	}

	/**
	 * Compute levenshtein distance of two strings.
	 *
	 * @param str1 the str1
	 * @param str2 the str2
	 * @return the distance
	 */
	public static int computeLevenshteinDistance (String str1, String str2)
	{
	    int [][] distance = new int [str1.length () + 1][str2.length () + 1];
	
	    for (int i = 0; i <= str1.length (); i++)
	            distance[i][0] = i;
	    for (int j = 1; j <= str2.length (); j++)
	            distance[0][j] = j;
	
	    for (int i = 1; i <= str1.length (); i++)
	            for (int j = 1; j <= str2.length (); j++)
	                    distance[i][j] = minimum (
	                                    distance[i - 1][j] + 1,
	                                    distance[i][j - 1] + 1,
	                                    distance[i - 1][j - 1]+ ((str1.charAt (i - 1) == str2.charAt (j - 1)) ? 0 : 1));
	
	    return distance[str1.length ()][str2.length ()];    
	}
	
	/**
	 * Repeat a string.
	 *
	 * @param s the string
	 * @param times the number of times to repeat s
	 * @return the string
	 */
	public static String repeat (String s, int times)
	{
		if (times <= 0)
			return "";
		StringBuilder str = new StringBuilder ();
		for (int i = 0; i < times; i++)
			str.append (s);
		return str.toString ();
	}
	
	
	/**
	 * Beautify the display of a double <code>d</code>. If the double is an int
	 * we'll omit the <code>.0</code>. Additionally you may define an int to
	 * neglect (e.g. <code>0</code> or <code>1</code>), thus, if
	 * <code>d == neglect</code> you'll get an empty string. Especially designed
	 * to display equations and stuff (e.g. omit an multiplier
	 * of <code>1</code> or an offset of <code>0</code>).
	 * 
	 * @param d
	 *          the double to print
	 * @param neglect
	 *          an integer to neglect. Can be null if you don't want to omit any
	 *          number
	 * @return the pretty string
	 */
	public static String prettyDouble (Double d, Integer neglect)
	{
		if (d == null)
			return "";
		
		if ((d == Math.rint (d)) && !Double.isInfinite (d) && !Double.isNaN (d))
		{
			int s = d.intValue ();
			if (neglect != null && s == neglect)
				return "";
	    return s + "";
		}
		
		return d.toString ();
	}
	
	
	/**
	 * Beautify the display of a double <code>d</code>. If the double is an int
	 * we'll omit the <code>.0</code>. Additionally you may define an int to
	 * neglect (e.g. <code>0</code> or <code>1</code>), thus, if
	 * <code>d == neglect</code> you'll get an empty string. Especially designed
	 * to display equations and stuff (e.g. omit an multiplier
	 * of <code>1</code> or an offset of <code>0</code>).
	 * You can also define a pre- and post-word to be put before or after,
	 * respectively. That means, if <code>d == neglect</code> we will return
	 * <code>""</code>, otherwise <code>pre + pretty (d) + post</code>.
	 *
	 * @param d the double to print
	 * @param neglect an integer to neglect. Can be null if you don't want to omit any
	 * number
	 * @param pre the string to put in front of d, if d != neglect
	 * @param post the string to put after d, if d != neglect
	 * @return the pretty string
	 */
	public static String prettyDouble (Double d, Integer neglect, String pre, String post)
	{
		if (d == null)
			return "";
		
		if ((d == Math.rint (d)) && !Double.isInfinite (d) && !Double.isNaN (d))
		{
			int s = d.intValue ();
			if (neglect != null && s == neglect)
				return "";
	    return pre + s + "" + post;
		}
		
		return pre + d.toString () + post;
	}
	

  /**
   * Byte to hex.
   *
   * @param data the data
   * @return the string
   */
  public static String byteToHex(byte[] data)
  {
      StringBuilder buf = new StringBuilder();
      for (int i = 0; i < data.length; i++) {
          int halfbyte = (data[i] >>> 4) & 0x0F;
          int two_halfs = 0;
          do {
              if ((0 <= halfbyte) && (halfbyte <= 9))
                  buf.append((char) ('0' + halfbyte));
              else
                      buf.append((char) ('a' + (halfbyte - 10)));
              halfbyte = data[i] & 0x0F;
          } while(two_halfs++ < 1);
      }
      return buf.toString();
  }
  

	/**
	 * Hash a message using either sha256/sha1/sha512/md5, whatever is available.
	 *
	 * @param msg the message
	 * @return the hash or null if we're not able to produce the hash
	 */
	public static String hash (String msg)
	{
		// speed vs size
		String hash = hashSha256 (msg);
		if (hash == null)
			hash = hashSha1 (msg);
		if (hash == null)
			hash = hashSha512 (msg);
		if (hash == null)
			hash = hashMd5 (msg);
		return hash;
	}

	/**
	 * Hash a message.
	 *
	 * @param msg the message
	 * @param algorithm the hash algorithm
	 * @return the hash or null if we're not able to produce the hash
	 */
	private static String hash (String msg, String algorithm)
	{
		MessageDigest md;
		try
		{
			md = MessageDigest.getInstance (algorithm);
	    md.update(msg.getBytes());
	
	    return DatatypeConverter.printHexBinary (md.digest());
		}
		catch (NoSuchAlgorithmException e)
		{
			LOGGER.error (e, "cannot hash message because algorithm unknown");
			return null;
		}
		
	}

	/**
	 * Hash a message using MD5.
	 *
	 * @param msg the message
	 * @return the hash or null if we're not able to produce the hash
	 */
	public static String hashMd5 (String msg)
	{
		return hash (msg, "MD5");
	}

  /**
   * Hash a message using SHA1.
   *
   * @param msg the message
   * @return the hash or null if we're not able to produce the hash
   */
  public static String hashSha1 (String msg)
  {
		return hash (msg, "SHA-1");
  }

  /**
   * Hash a message using SHA256.
   *
   * @param msg the message
   * @return the hash or null if we're not able to produce the hash
   */
  public static String hashSha256 (String msg)
  {
		return hash (msg, "SHA-256");
  }

  /**
   * Hash a message using SHA512.
   *
   * @param msg the message
   * @return the hash or null if we're not able to produce the hash
   */
  public static String hashSha512 (String msg)
  {
		return hash (msg, "SHA-512");
  }
	
	
  /**
   * Encode some byte in base64.
   *
   * @param bytes the bytes
   * @return the base64 encoded string
   */
  public static String encodeBase64 (byte [] bytes)
  {
  	return DatatypeConverter.printBase64Binary (bytes);
  }
	
	
  /**
   * Decode a base64 string.
   *
   * @param str the string encoded in base64
   * @return the bytes
   */
  public static byte [] decodeBase64 (String str)
  {
  	return DatatypeConverter.parseBase64Binary (str);
  }
	
	/**
	 * Read a file and return its contents as a string.
	 *
	 * @param f the file
	 * @return the string
	 * @throws IOException 
	 */
	public static String fileToString (File f) throws IOException
	{
		BufferedReader br = new BufferedReader (new FileReader (f));
		StringBuilder sb = new StringBuilder ();
		while (br.ready ())
			sb.append (br.readLine ()).append (LOGGER.NEWLINE);
		br.close ();
		return sb.toString ();
	}
	
	/**
	 * Write a string to a file.
	 *
	 * @param s the string
	 * @param f the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void stringToFile (String s, File f) throws IOException
	{
		BufferedWriter bw = new BufferedWriter (new FileWriter (f));
		bw.write (s);
		bw.close ();
	}
	
	/**
	 * Calculate the size of iterable object.
	 *
	 * @param iterable the iterable
	 * @return the its size
	 */
	public static int sizeOfIterable (Iterable<?> iterable)
	{
		int size = 0;
		if (iterable instanceof Collection<?>)
			size = ((Collection<?>)iterable).size();
		else
			for (@SuppressWarnings("unused") Object i : iterable)
				size++;
		return size;
	}
	

	
	
	/**
	 * Recursively delete file or directory.
	 * This function was cheekily stolen from the
	 * CombineArchive Toolkit, see
	 * <a href="https://sems.uni-rostock.de/trac/combinearchive/browser/src/main/java/de/unirostock/sems/cbarchive/Utils.java">original version</a>
	 *
	 * @param f the file/dir to delete
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static boolean delete (File f) throws IOException
	{
		boolean succ = true;
		if (f.isDirectory ())
			for (File c : f.listFiles ())
				succ = succ && delete (c);
		if (!f.delete ())
			succ = false;
		
		return succ;
	}
}
