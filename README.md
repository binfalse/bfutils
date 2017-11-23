# BFUtils

Some useful utilities for everyday work with Java tools.

## Integrate BFUtils


BFUtils is available through [Maven's Central Repository](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.binfalse%22%20AND%20a%3A%22BFUtils%22).
If you are using Maven you just need to add the following dependency:

	<dependency>
		<groupId>de.binfalse</groupId>
		<artifactId>BFUtils</artifactId>
		<version>0.5.2</version>
	</dependency>

That's it. Maven will care about the rest. You can start using it!


## Available Utilities

### AlphabetIterator

The AlphabetIterator at `de.binfalse.bfutils.AlphabetIterator` can be used to generate unique dummy strings over the alphabet.
Iteratively calling `next ()` will produce a sequence of strings just as `a`, `b`, `c`, ..., `x`, `y`, `z`, `aa`, `ab`, ... `az`, `ba`, `bb`, ..

By default, you'll get lower case letters, but using `AlphabetIterator.getUpperCaseIterator ()` you'll get an instance that returns upper case letters.

Example:

	// create file with unique name
	AlphabetIterator alph = AlphabetIterator.getLowerCaseIterator ();
	
	String basename = "filename";
	String uniq = "";
	
	while (new File (basename + uniq).exists ())
		uniq = alph.next ();

### Simple Output Stream

Sometimes you need to supply an output stream, but you're actually just interested in the streams content.
In That case you may use `de.binfalse.bfutils.SimpleOutputStream`.
It is able to collect the stream and you can then obtain it using `toString ()`.

### Quickly download a file from the internet

The following snippet

	String url = "https://binfalse.de/";
	File target = new File ("/tmp/binfalse.web.page");
	boolean anyway = true;
	de.binfalse.bfutils.Downloader.download (url, target, anyway);

downloads the web resource `https://binfalse.de/` and stores it as `/tmp/binfalse.web.page`.
The HTTP response may indicate a failure.
With `anyway = true` we will still download the resulting web page.


### Retrieve files from web or disk

The `de.binfalse.bfutils.FileRetriever` helps you retrieving files.
You can configure a cache using `FileRetriever.setUpCache (new File ("/path/to/cache/dir"))` to not download the same thing over and over again.

To retrieve a file just call

	FileRetriever.getFile (URI, File);

The FileRetriever will check if it's local or not and get you the desired file.
By setting `FileRetriever.FIND_LOCAL = false` and `FileRetriever.FIND_REMOTE = false` you may prevent loading data from disk or the internet.


### General Tools

Some of the functions that I need every once in a while are collected in General Tools:

#### Calculate the minimum of 3 integers

	int min = GeneralTools.minimum (27, 13, 4392);

Will result in `min == 13`.

#### Compute the Levenshtein distance between two strings

	int distance = GeneralTools.computeLevenshteinDistance ("Martin", "Moritz");

Will result in a distance of `4`.

#### Repeat a string multiple times

	String repetition = GeneralTools.repeat ("abc", 3);

Will result in `abcabcabc`.

#### Improved displaying of double values

Doubles sometimes look ugly when communicated to humans. For example, in case of `13.0` the `.0` is not necessary information. Mathematical equations or chemical formulas may evaluate to `1x + 3y`. Here we could omit the `1`, as `x + 3y` is prettier and easier to read:

	String myDouble = GeneralTools.prettyDouble (13.0, 1);

See also https://github.com/binfalse/bfutils/blob/cb26b6287f43e8d0cb2d5007881b08457e4e2c1f/src/main/java/de/binfalse/bfutils/GeneralTools.java#L86

#### Convert a byte into a hexadecimal string

	String hex = GeneralTools.byteToHex ("Rostock".getBytes ());

Will result in `526f73746f636b` (`R` is `52`, `o` is `6f`, etc), see also [man ascii](http://man7.org/linux/man-pages/man7/ascii.7.html).

#### Calculate hash sums of strings

A few functions are available:

	GeneralTools.hashSha512 ("SBI");
	GeneralTools.hashSha256 ("SBI");
	GeneralTools.hashSha1 ("SBI");
	GeneralTools.hashMd5 ("SBI");
	GeneralTools.hash ("SBI");


Availability of the hashing method also depends on the availability of the corresponding algorithm on your system.
The function `GeneralTools.hash (String)` will return one of the above, depending on their availability.

#### Encode and decode Base64

	String base64 = GeneralTools.encodeBase64 ("Meer".getBytes ());
	String unbased = new String (GeneralTools.decodeBase64 (base64));

Will result in `TWVlcg==` (Base64 encoded) and `Meer` (Base64 decoded).


#### Read a whole file into a string

	String filecontents = GeneralTools.fileToString (new File ("/path/to/file"));

#### Write a string into a file

	GeneralTools.stringToFile ("some string", new File ("/path/to/file"));

Will overwrite the file if it exists.

#### Recursively delete a file or directory

	GeneralTools.delete (new File ("/path/to/dir"));

This will make sure that `/path/to/dir` gets deleted.
Even if it's a directory and still contains files.
