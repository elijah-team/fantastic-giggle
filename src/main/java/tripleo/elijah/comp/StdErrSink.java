/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
/**
 * Created Mar 25, 2019 at 3:00:39 PM
 */
package tripleo.elijah.comp;

import tripleo.elijah.comp.i.ErrSink;
import tripleo.elijah.diagnostic.Diagnostic;

/**
 * @author tripleo(sb)
 */
public class StdErrSink implements ErrSink {

	private int _errorCount;

	@Override
	public int errorCount() {
		return _errorCount;
	}

	@Override
	public void exception(final Exception e) {
		_errorCount++;
		System.err.println("exception: " + e);
		e.printStackTrace(System.err);
	}

	@Override
	public void info(final String message) {
		System.err.printf("INFO: %s%n", message);
	}

	@Override
	public void reportDiagnostic(Diagnostic diagnostic) {
		if (diagnostic.severity() == Diagnostic.Severity.ERROR)
			_errorCount++;
		diagnostic.report(System.err);
	}

	@Override
	public void reportError(final String s) {
		_errorCount++;
		System.err.printf("ERROR: %s%n", s);
	}

	@Override
	public void reportWarning(final String s) {
		System.err.printf("WARNING: %s%n", s);
	}
}

//
//
//
