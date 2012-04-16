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
import net.sf.dynamicreports.report.builder.column.ColumnBuilder;
import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import net.sf.dynamicreports.report.definition.datatype.DRIDataType;
import net.sf.dynamicreports.report.exception.DRException;

/**
 * Filter for pages:export processor.
 */
@WebFilter(filterName = "exportFilter", urlPatterns = {"/*"})
public class ExportFilter implements Filter {
    
    public static final String PDF_FORMAT = "pdf";
    public static final String EXCEL_FORMAT = "excel";
    
    public static final String EXPORT_LIST = "org.thymeleaf.pagesdialect.exportListAttr"; // Cannot be overriden at the moment
    public static final String EXPORT_LIST_FORMAT = "org.thymeleaf.pagesdialect.exportListFormat"; // Cannot be overriden at the moment
    public static final String EXPORT_FIELDS = "org.thymeleaf.pagesdialect.exportFields"; // Cannot be overriden at the moment
    public static final String EXPORT_HEADERS = "org.thymeleaf.pagesdialect.exportHeaders"; // Cannot be overriden at the moment
    public static final String EXPORT_TITLE = "org.thymeleaf.pagesdialect.exportTitle"; // Cannot be overriden at the moment
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
    
    @Override
    public void doFilter(ServletRequest sRequest, ServletResponse sResponse, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(sRequest, sResponse); // FIXME: figure out some way to avoid the whole template processing
        HttpServletRequest request = (HttpServletRequest) sRequest;
        HttpServletResponse response = (HttpServletResponse) sResponse;
        if (request.getAttribute(EXPORT_LIST) != null && !response.isCommitted()) {
            String format = (String) request.getAttribute(EXPORT_LIST_FORMAT);
            List<String> fields = ((List<String>) request.getAttribute(EXPORT_FIELDS));
            List<String> headers = ((List<String>) request.getAttribute(EXPORT_HEADERS));
            List list = (List) request.getAttribute(EXPORT_LIST);
            String title = (String) request.getAttribute(EXPORT_TITLE);
            response.reset(); // Remove previous response
            // FIXME: fill in filename
            DynamicReport report = new DynamicReport(format, title, "export", response);
            ColumnBuilder[] columns = new ColumnBuilder[fields.size()];
            // FIXME: list could be empty
            Object sampleObject = list.get(0);
            for (int i = 0; i < fields.size(); i++) {
                String fieldPath = fields.get(i).trim();
                DRIDataType fieldType;
                try {
                    fieldType = type.detectType(PagesDialectUtil.getProperty(sampleObject, fieldPath).getClass());
                } catch (DRException ex) {
                    throw new IllegalArgumentException("Type of field -" + fieldPath + "- unknown", ex);
                }
                if (headers != null) {
                    columns[i] = col.column(headers.get(i), fieldPath, fieldType);
                } else {
                    columns[i] = col.column(fieldPath, fieldType);
                }
            }
            report.export(list, columns);
        }
    }

    @Override
    public void destroy() {
    }
}
