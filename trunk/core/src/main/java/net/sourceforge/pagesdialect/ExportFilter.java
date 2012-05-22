package net.sourceforge.pagesdialect;

import java.io.IOException;
import java.util.List;
import java.util.Set;
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
import net.sf.dynamicreports.report.builder.column.ColumnBuilder;
import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import net.sf.dynamicreports.report.definition.datatype.DRIDataType;
import net.sf.dynamicreports.report.exception.DRException;

/**
 * Servlet filter for pages:export processor.
 * Interrupt and clear the servlet response to serve the export file.
 */
@WebFilter(filterName = "exportFilter", urlPatterns = {"/*"}, dispatcherTypes = {DispatcherType.REQUEST},
        initParams = {@WebInitParam(name = "exportRequestParameterName", value = "export")})
public class ExportFilter implements Filter {
    
    public static final String PDF_FORMAT = "pdf";
    public static final String EXCEL_FORMAT = "excel";

    public static final String EXPORT_INIT_PARAMETER = "exportRequestParameterName";
    
    public static final String EXPORT_LIST = "org.thymeleaf.pagesdialect.exportListAttr"; // Cannot be overriden at the moment
    public static final String EXPORT_LIST_FORMAT = "org.thymeleaf.pagesdialect.exportListFormat"; // Cannot be overriden at the moment
    public static final String EXPORT_FIELDS = "org.thymeleaf.pagesdialect.exportFields"; // Cannot be overriden at the moment
    public static final String EXPORT_HEADERS = "org.thymeleaf.pagesdialect.exportHeaders"; // Cannot be overriden at the moment
    public static final String EXPORT_TITLE = "org.thymeleaf.pagesdialect.exportTitle"; // Cannot be overriden at the moment
    public static final String EXPORT_TYPE_FORMATTERS = "org.thymeleaf.pagesdialect.exportTypeFormatters"; // Cannot be overriden at the moment
    
    private String exportParam;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (filterConfig.getInitParameter(EXPORT_INIT_PARAMETER) == null) {
            throw new IllegalArgumentException(EXPORT_INIT_PARAMETER + " init parameter in web.xml is required for ExportFilter filter.");
        }
        this.exportParam = filterConfig.getInitParameter(EXPORT_INIT_PARAMETER);
    }
    
    @Override
    public void doFilter(ServletRequest sRequest, ServletResponse sResponse, FilterChain chain)
            throws IOException, ServletException {
        if (notExportingInProcess(sRequest)) {
            chain.doFilter(sRequest, sResponse);
        } else {
            HttpServletRequest request = (HttpServletRequest) sRequest;
            HttpServletResponse response = (HttpServletResponse) sResponse;
            chain.doFilter(sRequest, new IgnorableHttpServletResponse(response)); // FIXME: figure out some way to avoid the whole template processing
            response.reset(); // Remove previous response, if any
            Set<TypeFormatter> typeFormatters = (Set<TypeFormatter>) request.getAttribute(EXPORT_TYPE_FORMATTERS);
            String format = (String) request.getAttribute(EXPORT_LIST_FORMAT);
            List<String> fields = ((List<String>) request.getAttribute(EXPORT_FIELDS));
            List<String> headers = ((List<String>) request.getAttribute(EXPORT_HEADERS));
            List list = (List) request.getAttribute(EXPORT_LIST);
            String title = (String) request.getAttribute(EXPORT_TITLE);
            String filename = title != null ? PagesDialectUtil.simplifyString(title) : "export";
            DynamicReportsHelper report = new DynamicReportsHelper(format, title, filename, response);
            ColumnBuilder[] columns = new ColumnBuilder[fields.size()];
            if (list == null || list.isEmpty()) {
                throw new IllegalArgumentException("Export list is empty");
            }
            Object sampleObject = list.get(0);
            for (int i = 0; i < fields.size(); i++) {
                String fieldPath = fields.get(i).trim();
                DRIDataType fieldType = detectType(sampleObject, fieldPath, typeFormatters, request);
                if (headers != null) {
                    columns[i] = col.column(headers.get(i), fieldPath, fieldType);
                } else {
                    columns[i] = col.column(fieldPath, fieldType);
                }
            }
            report.export(list, columns);
        }
    }

    /**
     * Get the DRIDataType of a field, getting it from TypeFormatter set if found.
     */
    private DRIDataType detectType(Object object, String fieldPath, Set<TypeFormatter> typeFormatters, HttpServletRequest request) {
        Class objectClass = PagesDialectUtil.getPropertyClass(object, fieldPath);
        // search type in TypeFormatter set
        if (typeFormatters != null) {
            for (TypeFormatter typeFormatter : typeFormatters) {
                if (typeFormatter.getValueClass().equals(objectClass)) {
                    return new DRIDataTypeAdapter(typeFormatter, request);
                }
            }
        }
        // If not found, try automatic detection
        try {
            return type.detectType(objectClass);
        } catch (DRException ex) {
            throw new IllegalArgumentException("Type of field -" + fieldPath + "- unknown", ex);
        }
        
    }
    
    private boolean notExportingInProcess(ServletRequest sRequest) {
        return sRequest.getParameter(exportParam) == null;
    }

    @Override
    public void destroy() {
    }
}
