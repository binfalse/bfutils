/**
 * 
 */
package de.binfalse.bfutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;



/**
 * @author Martin Scharm
 * 
 */
public class Downloader
{
	
	/**
	 * Download a file from the web.
	 * 
	 * @param remoteUrl
	 *          the URL to the file
	 * @param target
	 *          the target to write to
	 * @param downloadAnyway
	 *          download the file even if the HTTP status is not 200
	 * @return a suggested name &mdash; might be null if the website doesn't
	 *         suggest a name
	 * @throws IOException
	 */
	public static final String download (String remoteUrl, File target,
		boolean downloadAnyway) throws IOException
	{
		String name = null;
		HttpClient client = HttpClientBuilder.create ().build ();
		HttpResponse getResponse = client.execute (new HttpGet (remoteUrl));
		
		// check if file exists
		if (!downloadAnyway && getResponse.getStatusLine ().getStatusCode () != 200)
			throw new IOException (getResponse.getStatusLine ().getStatusCode ()
				+ " " + getResponse.getStatusLine ().getReasonPhrase ()
				+ " while download " + remoteUrl);
		
		HttpEntity entity = getResponse.getEntity ();
		if (entity == null)
			throw new IOException (
				"No content returned while donwloading remote file " + remoteUrl);
		
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
				name = matcher.group (1);
		}
		
		// download it
		OutputStream output = new FileOutputStream (target);
		IOUtils.copy (entity.getContent (), output);
		return name;
	}
}
