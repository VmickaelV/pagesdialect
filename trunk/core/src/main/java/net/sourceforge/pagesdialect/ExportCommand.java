package net.sourceforge.pagesdialect;

import org.thymeleaf.Arguments;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.dom.DOMSelector;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.dom.Text;
import org.thymeleaf.util.MessageResolutionUtils;

import javax.servlet.FilterRegistration;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExportCommand {

    private Arguments arguments;
    private Element element;
    private String attributeName;
    private PagesDialect dialect;

    private String exportParam = "export"; // Default value. Can be overriden by config.
    private String exportDivId = "exportlinkcontainer"; // Default value. Can be overriden by config.

    private String format;
    private String exportLinkClass;
    private String i18Export;

    public ExportCommand(Arguments arguments, Element element, String attributeName, PagesDialect dialect, String format) {
        this.arguments = arguments;
        this.element = element;
        this.attributeName = attributeName;
        this.dialect = dialect;
        if (dialect.getProperties().containsKey(PagesDialect.EXPORT_DIV_ID)) {
            this.exportDivId = dialect.getProperties().get(PagesDialect.EXPORT_DIV_ID);
        }
        this.format = format;
        if (ExportPerformer.PDF_FORMAT.equals(format)) {
            this.exportLinkClass = "export-pdf";
            this.i18Export = PagesDialect.I18N_EXPORT_PDF;
        } else if (ExportPerformer.EXCEL_FORMAT.equals(format)) {
            this.exportLinkClass = "export-excel";
            this.i18Export = PagesDialect.I18N_EXPORT_EXCEL;
        } else {
            throw new IllegalArgumentException("Export format not recognized");
        }
        HttpServletRequest request = ((IWebContext) arguments.getContext()).getHttpServletRequest();
        readExportParam(request);
    }

    public void execute() {
        HttpServletRequest request = ((IWebContext) arguments.getContext()).getHttpServletRequest();
        // Get iteration list
        Collection list = findOriginalList(request);
        if (!list.isEmpty()) {
            if (this.format.equals(request.getParameter(exportParam))) {
                // Store list information for filter
                request.setAttribute(ExportPerformer.EXPORT_TYPE_FORMATTERS, this.dialect.getTypeFormatters());
                request.setAttribute(ExportPerformer.EXPORT_LIST, list);
                request.setAttribute(ExportPerformer.EXPORT_LIST_FORMAT, this.format);
                List<String> fields = new ArrayList<String>();
                List<String> headers = new ArrayList<String>();
                boolean someHeader = false;
                for (String part : element.getAttributeValue(attributeName).split(",")) {
                    if (part.contains(":")) {
                        fields.add(part.split(":")[0].trim());
                        String keyExpression = part.split(":")[1].trim();
                        if (!keyExpression.startsWith("$")) {
                            keyExpression = "'" + keyExpression + "'"; // Simplify constant expressions
                        }
                        String key = PagesDialectUtil.expressionValue(arguments, keyExpression).toString();
                        String header = MessageResolutionUtils.resolveMessageForTemplate(arguments, key, null, false);
                        if (header == null) {
                            header = key;
                        }
                        headers.add(header);
                        someHeader = true;
                    } else {
                        fields.add(part.trim());
                        headers.add("");
                    }
                }
                request.setAttribute(ExportPerformer.EXPORT_FIELDS, fields);
                if (someHeader) {
                    request.setAttribute(ExportPerformer.EXPORT_HEADERS, headers);
                }
                // Caption
                Element container = PagesDialectUtil.getContainerElement(element);
                if ("table".equals(container.getOriginalName())) {
                    Element firstChild = container.getElementChildren().get(0);
                    if ("caption".equals(firstChild.getOriginalName())) {
                        Text text = (Text) firstChild.getFirstChild();
                        request.setAttribute(ExportPerformer.EXPORT_TITLE, text.getContent());
                    }
                }
            } else {
                // Add export link
                Element container = PagesDialectUtil.getContainerElement(element);
                addExportLink(container);
            }
        }
        // Housekeeping
        element.removeAttribute(attributeName);
    }

    /**
     * Return export list. It can be the original iteration object or the sorted object set by SortAttrProcessor.
     */
    private Collection findOriginalList(HttpServletRequest request) {
        Collection list;
        if (request.getAttribute(IterationListPreparer.PAGED_LIST_HOLDER_ATTR) != null) {
            RecoverablePagedListHolder recoverablePagedListHolder = (RecoverablePagedListHolder) request.getAttribute(IterationListPreparer.PAGED_LIST_HOLDER_ATTR);
            list = recoverablePagedListHolder.getOriginalList();
        } else {
            IterationListFinder iterationListFinder = new IterationListFinder(arguments, element);
            list = (Collection) iterationListFinder.getIterationObject();
        }
        return list;
    }

    /**
     * Adds an export link within a div element
     */
    private void addExportLink(Element container) {
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
            text = PagesDialectUtil.templateMessage(arguments, dialect.getProperties().get(i18Export));
        } else {
            text = this.format;
        }
        anchor.addChild(new Text(text));
        // Add or retrieve <div> container
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

    /** Get export parameter name from ExportFilter configuration. */
    private void readExportParam(HttpServletRequest request) {
        for (FilterRegistration filterRegistration : request.getServletContext().getFilterRegistrations().values()) {
            if (filterRegistration.getClassName().equals(ExportFilter.class.getName())) {
                exportParam = filterRegistration.getInitParameter(ExportFilter.EXPORT_INIT_PARAMETER);
            }
        }
    }
}
