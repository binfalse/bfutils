/**
 * 
 */
package de.binfalse.bftools;

import java.io.IOException;
import java.io.OutputStream;


/**
 * @author Martin Scharm
 *
 */
public class SimpleOutputStream extends OutputStream
{

	  /** The string. */
  	private StringBuilder string = new StringBuilder();
	  
	  /* (non-Javadoc)
  	 * @see java.io.OutputStream#write(int)
  	 */
  	@Override
	  public void write(int b) throws IOException
	  {
	      this.string.append((char) b );
	  }

	  //Netbeans IDE automatically overrides this toString()
	  /* (non-Javadoc)
  	 * @see java.lang.Object#toString()
  	 */
  	public String toString()
	  {
	      return this.string.toString();
	  }
	  
	  /**
  	 * Reset the output stream.
  	 */
  	public void reset ()
	  {
	  	this.string = new StringBuilder();
	  }
}
