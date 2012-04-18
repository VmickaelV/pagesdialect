package org.thymeleaf.pagesdialect;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.thymeleaf.Arguments;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.dom.DOMSelector;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
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

    private PagesDialect dialect;

    private String exportParam = "export"; // Default value. Can be overriden by config.
    private String exportDivId = "exportlinkcontainer"; // Default value. Can be overriden by config.

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
        if (dialect.getProperties().containsKey(PagesDialect.EXPORT_DIV_ID)) {
            this.exportDivId = dialect.getProperties().get(PagesDialect.EXPORT_DIV_ID);
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
        return PagesDialect.EXPORT_ATTR_PRECEDENCE;
    }
    
    /**
     * Adds an export link within a div element
     */
    private void addExportLink(Arguments arguments, Element container) {
        // Build URL
        HttpServletRequest request = ((IWebContext) arguments.getContext()).getHttpServletRequest();
        String uri = request.getRequestURL().toString().split("\\?")[0];
        String query = request.getQueryString();
        String href = uri + "?" + (query != null ? query + "&": "") + exportParam + "=" + this.format;
        // Build link element
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
        // Add or retrieve <div> container
        System.out.println(exportDivId);
        DOMSelector selector = new DOMSelector("//div[@id=\"" + exportDivId + "\"]");
        List<Node> divs = selector.select((List) container.getParent().getElementChildren());
        Element div;
        if (divs.isEmpty()) {
            div = new Element("div");
            div.setAttribute("id", exportDivId);
            container.getParent().insertAfter(container, div);
        } else {
            div = (Element) divs.iterator().next();
        }
        div.addChild(anchor);
    }

    @Override
    protected ProcessorResult processAttribute(Arguments arguments, Element element, String attributeName) {
        HttpServletRequest request = ((IWebContext) arguments.getContext()).getHttpServletRequest();
        // Get iteration collection
        String iterationAttr = PagesDialectUtil.getStandardDialectPrefix(arguments) + ":" + StandardEachAttrProcessor.ATTR_NAME; // th:each
        if (!element.hasAttribute(iterationAttr)) {
            throw new TemplateProcessingException("Standard iteration attribute not found");
        }
        String iterationAttributeParams[] = element.getAttributeValue(iterationAttr).split(":");
        String listObject = iterationAttributeParams[1].trim();
        Object iterable = StandardExpressionProcessor.processExpression(arguments, listObject);
        List list = PagesDialectUtil.convertToList(iterable);
        if (!list.isEmpty()) {
            if (this.format.equals(request.getParameter(exportParam))) {
                // Store list information for filter
                request.setAttribute(ExportFilter.EXPORT_LIST, list);
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
                            // Feature requested: https://sourceforge.net/tracker/?func=detail&aid=3519058&group_id=509826&atid=2072662
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
                // Caption
                Element container = PagesDialectUtil.getContainerElement(element);
                if ("table".equals(container.getOriginalName())) {
                    Element firstChild = container.getElementChildren().get(0);
                    if ("caption".equals(firstChild.getOriginalName())) {
                        Text text = (Text) firstChild.getFirstChild();
                        request.setAttribute(ExportFilter.EXPORT_TITLE, text.getContent());
                    }
                }
            } else {
                // Add export link
                Element container = PagesDialectUtil.getContainerElement(element);
                addExportLink(arguments, container);
            }
        }
        // Housekeeping
        element.removeAttribute(attributeName);
        return ProcessorResult.OK;
    }
}
