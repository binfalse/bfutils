/**
 * 
 */
package de.binfalse.bfutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import de.binfalse.bflog.LOGGER;



/**
 * Class FileRetriever to retrieve files from disk or from web.
 * TODO: JDOC
 * 
 * @author Martin Scharm
 */
public class FileRetriever
{
	
	/** The cache dir. */
	public static File				CACHE_DIR;
	
	/** should we delete the cache after exit? */
	private static boolean		keepCache		= false;
	
	/** Are we allowed to search for local files?. */
	public static boolean			FIND_LOCAL	= true;
	
	/** Are we allowed to search for files on the Internet?. */
	public static boolean			FIND_REMOTE	= true;
	
	private static Properties	cacheProps;
	private static File				cacheFile;
	
	static
	{
		setUpCache ();
	}
	
	
	/**
	 * Sets the up caching.
	 */
	private static void setUpCache ()
	{
		cacheProps = new Properties ();
		try
		{
			CACHE_DIR = Files.createTempDirectory ("BFToolsFileRetrieverCache")
				.toFile ();
			CACHE_DIR.deleteOnExit ();
		}
		catch (IOException e)
		{
			LOGGER.error (e, "error creating temporary directory for caching");
		}
	}
	
	
	/**
	 * Sets the up caching. Pass null as an argument to disable caching.
	 * 
	 * @param directory
	 *          the directory to write to
	 * @return true, if successfully configured caching
	 * @throws IOException
	 */
	public static boolean setUpCache (File directory) throws IOException
	{
		keepCache = false;
		CACHE_DIR = directory;
		if (CACHE_DIR == null)
			return false;
		
		if (!CACHE_DIR.exists ())
			CACHE_DIR.mkdirs ();
		
		if (CACHE_DIR.exists () && CACHE_DIR.isDirectory ()
			&& CACHE_DIR.canWrite () && CACHE_DIR.canRead ())
		{
			keepCache = true;
			
			cacheFile = new File (CACHE_DIR + File.separator + "cache.props");
			if (cacheFile.exists ())
			{
				FileInputStream in = new FileInputStream (cacheFile);
				cacheProps.load (in);
				in.close ();
			}
			else
				cacheFile.createNewFile ();
			return true;
		}
		
		LOGGER.warn ("cache directory isn't appropriate");
		CACHE_DIR = null;
		return false;
	}
	
	
	private static void storeCacheProps () throws IOException
	{
		if (cacheFile != null)
		{
			FileOutputStream out = new FileOutputStream (cacheFile);
			cacheProps.store (out, "---No Comment---");
			out.close ();
		}
	}
	
	
	/**
	 * Compute an URI location. If href is relative we try to resolve it using
	 * base
	 * 
	 * @param href
	 *          the href
	 * @param base
	 *          the base URI
	 * @return the URI
	 * @throws URISyntaxException
	 *           the URI syntax exception
	 * @throws IOException
	 *           the IO exception
	 */
	public static URI getUri (String href, URI base)
		throws URISyntaxException,
			IOException
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
			throw new IOException ("don't know where this relative path points to: "
				+ href + " (no base provided, or base also relative).");
		
		return base.resolve (theUri);
	}
	
	
	/**
	 * Copy a file. Will throw an exception if FIND_LOCAL is false.
	 *
	 * @param from the origin
	 * @param to the destination
	 * @param local from local?
	 * @return the string
	 * @throws IOException the IO exception
	 */
	protected static String copy (URI from, File to, boolean local)
		throws IOException
	{
		if (local && !FIND_LOCAL)
			throw new IOException ("local resolving disabled");
		
		LOGGER.debug ("copying " + from + " to " + to);
		
		Files.copy (Paths.get (from), Paths.get (to.getAbsolutePath ()),
			new CopyOption[] { StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.COPY_ATTRIBUTES });
		// TODO: FIXME
		return null;
	}
	
	
	/**
	 * Download a file from a remote location.. Will throw an exception if
	 * FIND_REMOTE is false.
	 *
	 * @param from the origin
	 * @param to the destination
	 * @param downloadAnyway the download anyway
	 * @param addidionalHeaders the addidional headers
	 * @return the suggested name
	 * @throws IOException the IO exception
	 */
	protected static String download (URI from, File to, boolean downloadAnyway,
		Map<String, String> addidionalHeaders) throws IOException
	{
		if (!FIND_REMOTE)
			throw new IOException ("remote resolving disabled");
		
		LOGGER.debug ("downloading ", from, " to ", to);
		
		String chacheAdd = "";
		HttpGet httpGet = new HttpGet (from);
		httpGet.addHeader ("User-Agent",
			"Java FileRetriever of the BFUtils package (http://binfalse.de)");
		if (addidionalHeaders != null)
			for (String addHead : addidionalHeaders.keySet ())
			{
				String value = addidionalHeaders.get (addHead);
				LOGGER.debug ("user supplied additional header: ", addHead, value);
				httpGet.addHeader (addHead, value);
				chacheAdd += "?" + addHead + "!" + value;
			}
		
		File cached = null;
		String cachedName = GeneralTools
			.encodeBase64 ( (from.toString () + chacheAdd).getBytes ());
		if (CACHE_DIR != null)
		{
			String cachedDir = CACHE_DIR.getAbsolutePath () + File.separatorChar;
			while (cachedName.length () > 51)
			{
				cachedDir += cachedName.substring (0, 50) + File.separatorChar;
				cachedName = cachedName.substring (50);
			}
			// cached = new File (CACHE_DIR.getAbsolutePath () + File.separatorChar +
			// GeneralTools.encodeBase64 (from.toString ().getBytes ()));
			cached = new File (cachedDir + cachedName);
			if (cached.exists ())
			{
				copy (cached.toURI (), to, false);
				return cacheProps.getProperty (cachedName, null);
			}
		}
		
		String suggestedName = null;
		HttpClient client = HttpClientBuilder.create ().build ();
		
		HttpResponse getResponse = client.execute (httpGet);
		
		// check if file exists
		if (!downloadAnyway && getResponse.getStatusLine ().getStatusCode () != 200)
			throw new IOException (getResponse.getStatusLine ().getStatusCode ()
				+ " " + getResponse.getStatusLine ().getReasonPhrase ()
				+ " while download " + from);
		
		HttpEntity entity = getResponse.getEntity ();
		if (entity == null)
			throw new IOException (
				"No content returned while donwloading remote file " + from);
		
		// for name suggestions
		Header dispositionHeader = getResponse
			.getFirstHeader ("Content-Disposition");
		if (dispositionHeader != null && dispositionHeader.getValue () != null
			&& dispositionHeader.getValue ().isEmpty () == false)
		{
			Matcher matcher = Pattern.compile (
				"filename=\\\"?(([a-zA-Z0-9-_\\+]+).(\\w+))\\\"?",
				Pattern.CASE_INSENSITIVE).matcher (dispositionHeader.getValue ());
			if (matcher.find ())
				suggestedName = matcher.group (1);
		}
		
		// download it
		OutputStream output = new FileOutputStream (to);
		IOUtils.copy (entity.getContent (), output);
		
		if (CACHE_DIR != null)
		{
			cached.getParentFile ().mkdirs ();
			copy (to.toURI (), cached, false);
			if (suggestedName != null)
				cacheProps.put (cachedName, suggestedName);
			if (!keepCache)
				cached.deleteOnExit ();
		}
		return suggestedName;
	}
	
	
	/**
	 * Retrieves a file from an URI.
	 *
	 * @param file the URI to the file
	 * @param dest the destination to write to
	 * @return the suggested name
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException thrown if file or base have a strange format
	 */
	public static String getFile (URI file, File dest)
		throws IOException,
			URISyntaxException
	{
		if (!dest.canWrite ())
			throw new IOException ("cannot write to file: " + dest.getAbsolutePath ());
		
		LOGGER.info ("trying to retrieve file from ", file, " to ", dest);
		
		// file: -> copy
		if (file.getScheme () == null
			|| file.getScheme ().toLowerCase ().startsWith ("file"))
			return copy (file, dest, true);
		// otherwise download
		else
			return download (file, dest, false, null);
	}
	
	
	/**
	 * Retrieves a file from an URI.
	 * 
	 * You may supply additional headers in case the file needs to be downloaded
	 * from the internet and you need to negotiate the content, for example. Just
	 * add a map such as:
	 * 
	 * <pre>
	 * 
	 * {
	 * 	&#064;code Map&lt;String, String&gt; addHeaders = new HashMap&lt;String, String&gt; ();
	 * 	addHeaders.put (&quot;Accept&quot;, &quot;text/html&quot;);
	 * 	FileRetriever.getFile (new URI (&quot;URL&quot;), DESTFILE, addHeaders);
	 * }
	 * </pre>
	 * 
	 * @param file
	 *          the URI to the file
	 * @param dest
	 *          the destination to write to
	 * @param additionalDownloadHeaders
	 *          the additional headers to be supplied in case of a download
	 * @return the file
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws URISyntaxException
	 *           thrown if file or base have a strange format
	 */
	public static String getFile (URI file, File dest,
		Map<String, String> additionalDownloadHeaders)
		throws IOException,
			URISyntaxException
	{
		if (!dest.canWrite ())
			throw new IOException ("cannot write to file: " + dest.getAbsolutePath ());
		
		LOGGER.info ("trying to retrieve file from ", file, " to ", dest);
		
		// file: -> copy
		if (file.getScheme () == null
			|| file.getScheme ().toLowerCase ().startsWith ("file"))
			return copy (file, dest, true);
		// otherwise download
		else
			return download (file, dest, false, additionalDownloadHeaders);
	}
}
