/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.util;

import tripleo.util.buffer.Buffer;
import tripleo.util.buffer.DefaultBuffer;
import tripleo.util.buffer.TextBuffer;

/**
 * Created 4/26/21 11:36 PM
 */
public class BufferTabbedOutputStream {

	int tabwidth = 0;
	TextBuffer b = new DefaultBuffer("");
	private boolean do_tabs = false;
	private boolean _closed = false;

	public void close() {
		if (!is_connected())
			throw new IllegalStateException("is_connected assertion failed; closing twice");

//		b = null;
		_closed = true;
	}

	public boolean is_connected() {
		return !_closed;
	}

	public void dec_tabs() {
		tabwidth--;
	}

	public void flush() {
//		b.flush();
	}

	public Buffer getBuffer() {
		return b;
	}

	public void incr_tabs() {
		tabwidth++;
	}

	public void put_newline() {
		doIndent();
	}

	void doIndent() {
		for (int i = 0; i < tabwidth; i++)
			 b.append("\t");
	}

	public void put_string(final String s) {
		if (!is_connected())
			throw new IllegalStateException("is_connected assertion failed");

//		if (do_tabs)
//			doIndent();
		b.append(s);
//		do_tabs = false;
	}

	public void put_string_ln(final String s) {
		if (!is_connected())
			throw new IllegalStateException("is_connected assertion failed");

		if (do_tabs)
			doIndent();
		b.append(s);
		b.append("\n");
//		doIndent();
		do_tabs = true;
	}

	public void put_string_ln_no_tabs(final String s) {
		if (!is_connected())
			throw new IllegalStateException("is_connected assertion failed");

		b.append(s);
		b.append("\n");
//		do_tabs = true;
	}

	public void quote_string(final String s) {
		if (!is_connected())
			throw new IllegalStateException("is_connected assertion failed");

		b.append("\"");
		b.append(s);
		b.append("\"");
	}

	public int t() {
		return tabwidth;
	}

}

//
//
//
