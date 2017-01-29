package net.sourceforge.pagesdialect.commands;

import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.ColumnBuilder;
import net.sf.dynamicreports.report.definition.datatype.DRIDataType;
import net.sf.dynamicreports.report.exception.DRException;
import net.sourceforge.pagesdialect.util.DRIDataTypeAdapter;
import net.sourceforge.pagesdialect.util.DynamicReportsHelper;
import net.sourceforge.pagesdialect.util.PagesDialectUtil;
import net.sourceforge.pagesdialect.util.TypeFormatter;

/**
 * Performs the export to PDF and Excel files.
 */
public class ExportPerformer {
    
    public static final String PDF_FORMAT = "pdf";
    public static final String EXCEL_FORMAT = "excel";
    
    public static final String EXPORT_LIST = "org.thymeleaf.pagesdialect.exportListAttr"; // Cannot be overriden at the moment
    public static final String EXPORT_LIST_FORMAT = "org.thymeleaf.pagesdialect.exportListFormat"; // Cannot be overriden at the moment
    public static final String EXPORT_FIELDS = "org.thymeleaf.pagesdialect.exportFields"; // Cannot be overriden at the moment
    public static final String EXPORT_HEADERS = "org.thymeleaf.pagesdialect.exportHeaders"; // Cannot be overriden at the moment
    public static final String EXPORT_TITLE = "org.thymeleaf.pagesdialect.exportTitle"; // Cannot be overriden at the moment
    public static final String EXPORT_TYPE_FORMATTERS = "org.thymeleaf.pagesdialect.exportTypeFormatters"; // Cannot be overriden at the moment
    
    private HttpServletRequest request;
    private HttpServletResponse response;
    private String exportParam;

    public ExportPerformer(HttpServletRequest request, HttpServletResponse response, String exportParam) {
        this.request = request;
        this.response = response;
        this.exportParam = exportParam;
    }

    public boolean notExportingInProcess() {
        return request.getParameter(exportParam) == null;
    }

    public void performExport() {
        Set<TypeFormatter> typeFormatters = (Set<TypeFormatter>) request.getAttribute(EXPORT_TYPE_FORMATTERS);
        String format = (String) request.getAttribute(EXPORT_LIST_FORMAT);
        List<String> fields = (List<String>) request.getAttribute(EXPORT_FIELDS);
        List<String> headers = (List<String>) request.getAttribute(EXPORT_HEADERS);
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
                columns[i] = DynamicReports.col.column(headers.get(i), fieldPath, fieldType);
            } else {
                columns[i] = DynamicReports.col.column(fieldPath, fieldType);
            }
        }
        report.export(list, columns);
    }

    /**
     * Get the DRIDataType of a field, getting it from TypeFormatter set if found.
     */
    private DRIDataType detectType(Object object, String fieldPath, Set<TypeFormatter> typeFormatters, HttpServletRequest request) {
        Class objectClass = PagesDialectUtil.getPropertyClass(object.getClass(), fieldPath);
        // search type in TypeFormatter set
        if (objectClass != null && typeFormatters != null) {
            for (TypeFormatter typeFormatter : typeFormatters) {
                if (typeFormatter.getValueClass().isAssignableFrom(objectClass)) {
                    return new DRIDataTypeAdapter(typeFormatter, request);
                }
            }
        }
        // If not found, try automatic detection
        try {
            return DynamicReports.type.detectType(objectClass);
        } catch (DRException ex) {
            throw new IllegalArgumentException("Type of field -" + fieldPath + "- unknown", ex);
        }
    }
}
