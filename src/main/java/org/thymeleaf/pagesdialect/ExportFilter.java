package org.thymeleaf.pagesdialect;

import java.io.IOException;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter for pages:export processor.
 */
@WebFilter(filterName = "exportFilter", urlPatterns = {"/*"})
public class ExportFilter implements Filter {
    
    public static final String PDF_FORMAT = "pdf";
    public static final String EXCEL_FORMAT = "excel";
    
    public static final String EXPORT_LIST_ATTR = "org.thymeleaf.pagesdialect.exportListAttr"; // Cannot be overriden at the moment
    public static final String EXPORT_LIST_FORMAT = "org.thymeleaf.pagesdialect.exportListFormat"; // Cannot be overriden at the moment
    public static final String EXPORT_FIELDS = "org.thymeleaf.pagesdialect.exportFields"; // Cannot be overriden at the moment
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
    
    @Override
    public void doFilter(ServletRequest sRequest, ServletResponse sResponse, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(sRequest, sResponse);
        HttpServletRequest request = (HttpServletRequest) sRequest;
        HttpServletResponse response = (HttpServletResponse) sResponse;
        if (request.getAttribute(EXPORT_LIST_ATTR) != null && !response.isCommitted()) {
            String format = (String) request.getAttribute(EXPORT_LIST_FORMAT);
            String fields = (String) request.getAttribute(EXPORT_FIELDS);
            response.reset();
            response.setContentType("text/plain");
            List list = (List) request.getAttribute(EXPORT_LIST_ATTR);
            response.getOutputStream().print("Formato " + format + "\n");
            response.getOutputStream().print("Campos " + fields + "\n");
            response.getOutputStream().print(list.toString());
            response.flushBuffer();
        }
    }

    @Override
    public void destroy() {
    }
}
