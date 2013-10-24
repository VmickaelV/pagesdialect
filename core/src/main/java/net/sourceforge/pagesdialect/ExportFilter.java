package net.sourceforge.pagesdialect;

import java.io.IOException;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet filter for pages:export processor.
 * Interrupt and clear the servlet response to serve the export file.
 */
@WebFilter(
    filterName = "exportFilter",
    urlPatterns = {"/*"},
    dispatcherTypes = {DispatcherType.REQUEST},
    initParams = {@WebInitParam(name = "exportRequestParameterName", value = "export")})
public class ExportFilter implements Filter {
    
    public static final String EXPORT_INIT_PARAMETER = "exportRequestParameterName";
    
    private String exportParam;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (filterConfig.getInitParameter(EXPORT_INIT_PARAMETER) == null) {
            throw new IllegalArgumentException(EXPORT_INIT_PARAMETER + " init parameter in web.xml is required for ExportFilter filter.");
        }
        exportParam = filterConfig.getInitParameter(EXPORT_INIT_PARAMETER);
    }
    
    @Override
    public void doFilter(ServletRequest sRequest, ServletResponse sResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) sRequest;
        HttpServletResponse response = (HttpServletResponse) sResponse;
        ExportPerformer exportPerformer = new ExportPerformer(request, response, exportParam);
        if (exportPerformer.notExportingInProcess()) {
            chain.doFilter(sRequest, sResponse);
        } else {
            chain.doFilter(sRequest, new IgnorableHttpServletResponse(response)); // FIXME: figure out some way to avoid the whole template processing
            sResponse.reset(); // Remove previous response, if any
            exportPerformer.performExport();
        }
    }
    
    @Override
    public void destroy() {
    }
}
