/**
 * 
 */
package de.binfalse.bfutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;

import de.binfalse.bflog.LOGGER;


/**
 * Class FileRetriever to retrieve files from disk or from web.
 *
 * @author Martin Scharm
 */
public class FileRetriever
{
	
	/** The cache dir. */
	public static File CACHE_DIR;
	
	/** should we delete the cache after exit?*/
	private static boolean keepCache = false;
	
	/** Are we allowed to search for local files?. */
	public static boolean FIND_LOCAL = true;
	
	/** Are we allowed to search for files on the Internet?. */
	public static boolean FIND_REMOTE = true;
	
	static {
		setUpCache ();
	}
	
	/**
	 * Sets the up caching.
	 */
	private static void setUpCache ()
	{
		try
		{
			CACHE_DIR = Files.createTempDirectory ("BFToolsFileRetrieverCache").toFile ();
		}
		catch (IOException e)
		{
			LOGGER.error (e, "error creating temporary directory for caching");
		}
	}
	
	/**
	 * Sets the up caching. Pass null as an argument to disable caching.
	 *
	 * @param directory the directory to write to
	 * @return true, if successfully configured caching
	 */
	public static boolean setUpCache (File directory)
	{
		keepCache = false;
		CACHE_DIR = directory;
		if (CACHE_DIR == null)
			return false;
		
		if (!CACHE_DIR.exists ())
			CACHE_DIR.mkdirs ();
		
		if (CACHE_DIR.exists () && CACHE_DIR.isDirectory () && CACHE_DIR.canWrite () && CACHE_DIR.canRead ())
		{
			keepCache = true;
			return true;
		}
		
		LOGGER.warn ("cache directory isn't appropriate");
		CACHE_DIR = null;
		return false;
	}
	
	/**
	 * Compute an URI location. If href is relative we try to resolve it using base
	 *
	 * @param href the href
	 * @param base the base URI
	 * @return the URI
	 * @throws URISyntaxException the URI syntax exception
	 * @throws IOException the IO exception
	 */
	public static URI getUri (String href, URI base) throws URISyntaxException, IOException
	{
		URI theUri = new URI (href);
		
		// is full
		if (theUri.isAbsolute ())
			return theUri;

		// href is absolut in fs
		if (href.startsWith ("/"))
			return new URI ("file://" + href);

		// is realtive
		if (base == null || !base.isAbsolute ())
			throw new IOException ("don't know where this relative path points to: "+href+" (no base provided, or base also relative).");
		
		return base.resolve (theUri);
	}
	
	/**
	 * Copy a file. Will throw an exception if FIND_LOCAL is false.
	 *
	 * @param from the origin
	 * @param to the destination
	 * @throws IOException the IO exception
	 */
	protected static void copy (URI from, File to, boolean local) throws IOException
	{
		if (local && !FIND_LOCAL)
			throw new IOException ("local resolving disabled");

		LOGGER.debug ("copying " + from + " to " + to);
		
		BufferedWriter bw = new BufferedWriter (new FileWriter (to));
		BufferedReader br = new BufferedReader (new FileReader (
			from.toURL ().getFile ()));
		while (br.ready ())
		{
			bw.write (br.readLine ());
			bw.newLine ();
		}
		bw.close ();
		br.close ();
	}
	
	/**
	 * Download a file from a remote location.. Will throw an exception if FIND_REMOTE is false.
	 *
	 * @param from the origin
	 * @param to the destination
	 * @throws IOException the IO exception
	 */
	protected static void download (URI from, File to) throws IOException
	{
		if (!FIND_REMOTE)
			throw new IOException ("remote resolving disabled");

		LOGGER.debug ("downloading " + from + " to " + to);

		File cached = null;
		if (CACHE_DIR != null)
		{
			String cachedDir = CACHE_DIR.getAbsolutePath () + File.separatorChar;
			String cachedName = GeneralTools.encodeBase64 (from.toString ().getBytes ());
			while (cachedName.length () > 51)
			{
				cachedDir += cachedName.substring (0, 50) + File.separatorChar;
				cachedName = cachedName.substring (50);
			}
			//cached = new File (CACHE_DIR.getAbsolutePath () + File.separatorChar + GeneralTools.encodeBase64 (from.toString ().getBytes ()));
			cached = new File (cachedDir + cachedName);
			if (cached.exists ())
			{
				copy (cached.toURI (), to, false);
				return;
			}
		}
		
		URL website = from.toURL ();
		ReadableByteChannel rbc = Channels.newChannel (website.openStream ());
		FileOutputStream fos = new FileOutputStream (to);
		fos.getChannel ().transferFrom (rbc, 0, 1 << 24);
		fos.close ();

		if (CACHE_DIR != null)
		{
			cached.getParentFile ().mkdirs ();
			copy (to.toURI (), cached, false);
			if (!keepCache)
				cached.deleteOnExit ();
		}
		
	}
	
	
	/**
	 * Retrieves a file from an URI.
	 *
	 * @param file the URI to the file
	 * @param dest the destination to write to
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException thrown if file or base have a strange format
	 */
	public static void getFile (URI file, File dest) throws IOException, URISyntaxException
	{
		if (!dest.canWrite ())
			throw new IOException ("cannot write to file: " + dest.getAbsolutePath ());
		
		LOGGER.info ("trying to retrieve file from ", file, " to ", dest, file.getScheme ().toLowerCase ());
		
		// file: -> copy
		if (file.getScheme ().toLowerCase ().startsWith ("file"))
			copy (file, dest, true);
		// otherwise download
		else
			download (file, dest);
	}
}
