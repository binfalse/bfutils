/**
 * 
 */
package de.binfalse.bfutils;

import java.util.Iterator;


/**
 * The Class AlphabetIterator to generate dummy strings iterating over the alphabet.
 * <code>.next ()</code> will produce strings in a sequence <code>a,b,c,...,x,y,z,aa,ab,...az,ba,bb,..</code>
 *
 * @author Martin Scharm
 */
public class AlphabetIterator implements Iterator<String>
{
  /** The now. */
  private int now;
  
  /** The vs. */
  private final char[] vs = new char['z' - 'a' + 1];
  
  
	/**
	 * Instantiates a new alphabet iterator.
	 */
	private AlphabetIterator ()
	{
		now = 0;
	}
	
	/**
	 * Gets an lower case iterator to iterate over a-z.
	 *
	 * @return the lower case iterator
	 */
	public static final AlphabetIterator getLowerCaseIterator ()
	{
		AlphabetIterator ai = new AlphabetIterator ();
    for (char i = 'a'; i <= 'z'; i++)
    	ai.vs[i - 'a'] = i;
    return ai;
	}
	
	/**
	 * Gets an upper case iterator to iterate over A-Z.
	 *
	 * @return the upper case iterator
	 */
	public static final AlphabetIterator getUpperCaseIterator ()
	{
		AlphabetIterator ai = new AlphabetIterator ();
    for (char i = 'A'; i <= 'Z'; i++)
    	ai.vs[i - 'A'] = i;
    return ai;
	}

  /**
   * Get the string number i.
   *
   * @param i the number
   * @return the string builder
   */
  private StringBuilder alpha(int i)
  {
      char r = vs[--i % vs.length];
      int n = i / vs.length;
      return n == 0 ? new StringBuilder().append(r) : alpha(n).append(r);
  }

  /* (non-Javadoc)
   * @see java.util.Iterator#next()
   */
  @Override
  public String next()
  {
      return alpha (++now).toString ();
  }

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext ()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove ()
	{
		throw new RuntimeException ("illegal operation");
	}
	
}
