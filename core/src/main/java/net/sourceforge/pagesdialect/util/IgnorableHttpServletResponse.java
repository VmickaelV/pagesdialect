package net.sourceforge.pagesdialect.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * HttpServletResponse implementation which ignores everything written to the it.
 */
public class IgnorableHttpServletResponse extends HttpServletResponseWrapper {

	public IgnorableHttpServletResponse(HttpServletResponse response) {
		super(response);
	}

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // do nothing
            }
        });
    }
}
