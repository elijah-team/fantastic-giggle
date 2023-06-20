/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.i.ErrSink;
import tripleo.elijah.comp.IO;
import tripleo.elijah.comp.StdErrSink;
import tripleo.elijah.comp.internal.CompilationImpl;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tripleo.elijah.util.Helpers.List_of;

/**
 * @author Tripleo(envy)
 *
 */
public class TestBasic {

	@Ignore
	@Test
	public final void testBasicParse() throws Exception {
		final List<String> ez_files = Files.readLines(new File("test/basic/ez_files.txt"), Charsets.UTF_8);
		final List<String> args = new ArrayList<String>();
		args.addAll(ez_files);
		args.add("-sE");
		final ErrSink eee = new StdErrSink();
		final Compilation c = new CompilationImpl(eee, new IO());

		c.feedCmdLine(args);

		Assert.assertEquals(0, c.errorCount());
	}

//	@Test
	@SuppressWarnings("JUnit3StyleTestMethodInJUnit4Class")
	public final void testBasic() throws Exception {
		final List<String> ez_files = Files.readLines(new File("test/basic/ez_files.txt"), Charsets.UTF_8);
		final Map<Integer, Integer> errorCount = new HashMap<Integer, Integer>();
		int index = 0;

		for (String s : ez_files) {
//			List<String> args = List_of("test/basic", "-sO"/*, "-out"*/);
			final ErrSink eee = new StdErrSink();
			final Compilation c = new CompilationImpl(eee, new IO());

			c.feedCmdLine(List_of(s, "-sO"));

			if (c.errorCount() != 0)
				System.err.printf("Error count should be 0 but is %d for %s%n", c.errorCount(), s);
			errorCount.put(index, c.errorCount());
			index++;
		}

		// README this needs changing when running make
		Assert.assertEquals(7, (int)errorCount.get(0)); // TODO Error count obviously should be 0
		Assert.assertEquals(20, (int)errorCount.get(1)); // TODO Error count obviously should be 0
		Assert.assertEquals(9, (int)errorCount.get(2)); // TODO Error count obviously should be 0
	}

	//@Ignore
	@Test
	@SuppressWarnings("JUnit3StyleTestMethodInJUnit4Class")
	public final void testBasic_listfolders3() throws Exception {
		String s = "test/basic/listfolders3/listfolders3.ez";

		final ErrSink eee = new StdErrSink();
		final Compilation c = new CompilationImpl(eee, new IO());

		c.feedCmdLine(List_of(s, "-sO"));

		if (c.errorCount() != 0)
			System.err.printf("Error count should be 0 but is %d for %s%n", c.errorCount(), s);

		Assert.assertEquals(4, c.errorCount()); // TODO Error count obviously should be 0
	}

	@Ignore
	@Test
	public final void testBasic_listfolders4() throws Exception {
		String s = "test/basic/listfolders4/listfolders4.ez";

		final ErrSink eee = new StdErrSink();
		final Compilation c = new CompilationImpl(eee, new IO());

		c.feedCmdLine(List_of(s, "-sO"));

		if (c.errorCount() != 0)
			System.err.printf("Error count should be 0 but is %d for %s%n", c.errorCount(), s);

		Assert.assertEquals(4, c.errorCount()); // TODO Error count obviously should be 0
	}

	@Test
	public final void testBasic_fact1() throws Exception {
		String s = "test/basic/fact1/main2";

		final ErrSink eee = new StdErrSink();
		final Compilation c = new CompilationImpl(eee, new IO());

		c.feedCmdLine(List_of(s, "-sO"));

		if (c.errorCount() != 0)
			System.err.printf("Error count should be 0 but is %d for %s%n", c.errorCount(), s);

		Assert.assertEquals(6, c.errorCount()); // TODO Error count obviously should be 0
	}

}
	
//
//
//
