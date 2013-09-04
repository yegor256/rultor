package com.rultor.shell;

import static org.junit.Assert.*;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link Terminal}.
 * 
 * @author anouar.badri
 * @version $Id$
 */
public final class TerminalTest {

	/**
	 * terminal.exec must throw an instance of IOException when the method
	 * shell.exec() terminate with non-zero exit code.
	 * 
	 * @throws Exception
	 *             If some problem inside
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void returnsLatestBranch() throws Exception {
		final Shell shell = Mockito.mock(Shell.class);

		Mockito.doReturn(1)
				.when(shell)
				.exec(Mockito.anyString(), Mockito.any(InputStream.class),
						Mockito.any(TeeOutputStream.class),
						Mockito.any(TeeOutputStream.class));

		final Terminal terminal = new Terminal(shell);

		try {
			terminal.exec("", "");
			fail("should throw IOException"); // fail if no exception is thrown

		}
		// must throw IOException
		catch (IOException ex) {
             assertTrue(true);
		}
		// should not throw any other exception
		catch (Exception ex) {
			fail("other exception was thrown"); // fail if no exception is
												// thrown

		}

	}
}

