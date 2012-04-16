package org.thymeleaf.pagesdialect;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.thymeleaf.Arguments;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Text;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.processor.IAttributeNameProcessorMatcher;
import org.thymeleaf.processor.ProcessorResult;
import org.thymeleaf.processor.attr.AbstractAttrProcessor;
import org.thymeleaf.standard.expression.StandardExpressionProcessor;
import org.thymeleaf.standard.processor.attr.StandardEachAttrProcessor;

/**
 * Thymeleaf processor that adds a link to export a Collection.
 * 
 * Example usage:
 * <pre>
 * {@code
 *      <tr th:each="product : ${products}" pages:excel="name, category.name, stock, price">
 * }
 * </pre>
 * 
 * Example usage with i18n keys:
 * <pre>
 * {@code
 *      <tr th:each="product : ${products}" pages:pdf="name:name, category.name:category, formattedPrice:total">
 * }
 * </pre>
 **/
public class ExportAttrProcessor extends AbstractAttrProcessor {

    public static final int ATTR_PRECEDENCE = PaginateAttrProcessor.ATTR_PRECEDENCE - 1; // Run before pages:paginate processor and after pages:sort processor

    private PagesDialect dialect;

    private String exportParam = "export"; // Default value. Can be overriden by config.

    private String format;
    private String exportLinkClass;
    private String i18Export;
    
    public ExportAttrProcessor(IAttributeNameProcessorMatcher matcher) {
        super(matcher);
    }

    public ExportAttrProcessor(String attributeName) {
        super(attributeName);
    }

    public void setDialect(PagesDialect dialect) {
        this.dialect = dialect;
        if (dialect.getProperties().containsKey(PagesDialect.EXPORT_PARAMETER)) {
            this.exportParam = dialect.getProperties().get(PagesDialect.EXPORT_PARAMETER);
        }
    }
    
    public void setFormat(String format) {
        this.format = format;
        if (ExportFilter.PDF_FORMAT.equals(format)) {
            this.exportLinkClass = "export-pdf";
            this.i18Export = PagesDialect.I18N_EXPORT_PDF;
        } else if (ExportFilter.EXCEL_FORMAT.equals(format)) {
            this.exportLinkClass = "export-excel";
            this.i18Export = PagesDialect.I18N_EXPORT_EXCEL;
        } else {
            throw new IllegalArgumentException("Export format not recognized");
        }
    }

    @Override
    public int getPrecedence() {
        return ATTR_PRECEDENCE;
    }
    
    /**
     * Adds an export link
     */
    // FIXME: place links within a <div class="export"> container
    private void addExportLink(Arguments arguments, Element container) {
        // Build URL
        HttpServletRequest request = ((IWebContext) arguments.getContext()).getHttpServletRequest();
        String uri = request.getRequestURL().toString().split("\\?")[0];
        String query = request.getQueryString();
        String href = uri + "?" + (query != null ? query + "&": "") + exportParam + "=" + this.format;
        // Add link element
        Element anchor = new Element("a");
        anchor.setAttribute("class", exportLinkClass);
        anchor.setAttribute("href", href);
        String text;
        if (dialect.getProperties().containsKey(i18Export)) {
            text = getMessage(arguments, dialect.getProperties().get(i18Export), null);
        } else {
            text = this.format;
        }
        anchor.addChild(new Text(text));
        container.getParent().insertAfter(container, anchor);
    }

    @Override
    protected ProcessorResult processAttribute(Arguments arguments, Element element, String attributeName) {
        HttpServletRequest request = ((IWebContext) arguments.getContext()).getHttpServletRequest();
        if (this.format.equals(request.getParameter(exportParam))) {
            // Get iteration collection
            String iterationAttr = PagesDialectUtil.getStandardDialectPrefix(arguments) + ":" + StandardEachAttrProcessor.ATTR_NAME; // th:each
            if (!element.hasAttribute(iterationAttr)) {
                throw new TemplateProcessingException("Standard iteration attribute not found");
            }
            String iterationAttributeParams[] = element.getAttributeValue(iterationAttr).split(":");
            String listObject = iterationAttributeParams[1].trim();
            Object iterable = StandardExpressionProcessor.processExpression(arguments, listObject);
            // Store list information for filter
            request.setAttribute(ExportFilter.EXPORT_LIST_ATTR, PagesDialectUtil.convertToList(iterable));
            request.setAttribute(ExportFilter.EXPORT_LIST_FORMAT, this.format);
            List<String> fields = new ArrayList<String>();
            List<String> headers = new ArrayList<String>();
            boolean someHeader = false;
            for (String part : element.getAttributeValue(attributeName).split(",")) {
                if (part.contains(":")) {
                    fields.add(part.split(":")[0].trim());
                    String key = part.split(":")[1].trim();
                    String header = getMessage(arguments, key, null);
                    if (header.startsWith("??") && header.endsWith("??")) {
                        // FIXME: do a stronger check for i18n existence
                        header = key;
                    }
                    headers.add(header);
                    someHeader = true;
                } else {
                    fields.add(part.trim());
                    headers.add("");
                }
            }
            request.setAttribute(ExportFilter.EXPORT_FIELDS, fields);
            if (someHeader) {
                request.setAttribute(ExportFilter.EXPORT_HEADERS, headers);
            }
        } else {
            // Add export link
            Element container = PagesDialectUtil.getContainerElement(element);
            addExportLink(arguments, container);
        }
        // Housekeeping
        element.removeAttribute(attributeName);
        return ProcessorResult.OK;
    }
}
