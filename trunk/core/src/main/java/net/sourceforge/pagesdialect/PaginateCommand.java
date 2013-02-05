package net.sourceforge.pagesdialect;

import java.text.MessageFormat;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.support.PagedListHolder;
import org.thymeleaf.Arguments;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Text;

public class PaginateCommand {

    private Arguments arguments;
    private Element element;
    private String attributeName;
    private PagesDialect dialect;

    private String pageParam = "page"; // Default value. Can be overriden by config.
    private int pageSize;
    private PagedListHolder pagedList;
    private Element container;

    public PaginateCommand(Arguments arguments, Element element, String attributeName, PagesDialect dialect) {
        this.arguments = arguments;
        this.element = element;
        this.attributeName = attributeName;
        this.dialect = dialect;
        if (dialect.getProperties().containsKey(PagesDialect.PAGE_PARAMETER)) {
            this.pageParam = dialect.getProperties().get(PagesDialect.PAGE_PARAMETER);
        }
    }

    public void execute() {
        parseArguments();
        setCurrentPage();
        addNavigationLinksIfNeeded();
        addNumberOfResultsText();
        // Housekeeping
        element.removeAttribute(attributeName);
    }

    private void parseArguments() {
        String attributeValue = element.getAttributeValue(attributeName);
        String processedValue = PagesDialectUtil.expressionValue(arguments, attributeValue).toString();
        pageSize = Integer.parseInt(processedValue);
    }

    private void setCurrentPage() {
        IterationListPreparer iterationListPreparer = new IterationListPreparer(arguments, element);
        pagedList = iterationListPreparer.findOrCreateIterationList();
        pagedList.setPageSize(pageSize);
        IWebContext context = (IWebContext) arguments.getContext();
        if (context.getRequestParameters().containsKey(pageParam)) {
            pagedList.setPage(Integer.parseInt(context.getRequestParameters().get(pageParam)[0]));
        }
    }

    private void addNavigationLinksIfNeeded() {
        container = PagesDialectUtil.getContainerElement(element);
        if (pagedList.getPageCount() > 1) {
            addNavigationLinks();
        }
    }

    private void addNumberOfResultsText() {
        if (pagedList.getNrOfElements() > 0) {
            addResultCount();
        } else {
            addNoResult();
        }
    }

    /**
     * Adds a text with the number of results after the container element, like
     *
     * <pre>
     * {@code
     *    <span class="paginate-count">Showing 10 - 20 of 58 Results</span>
     * }
     * </pre>
     */
    private void addResultCount() {
        Element resultCount = new Element("span");
        resultCount.setAttribute("class", "paginate-count");
        String text;
        if (pagedList.getNrOfElements() == 1) {
            text = getMessageOrDefault(arguments, "1 result", PagesDialect.I18N_ONE_RESULT);
        } else {
            String[] params = {(pagedList.getFirstElementOnPage() + 1) + "",
                (pagedList.getLastElementOnPage() + 1) + "",
                pagedList.getNrOfElements() + ""};
            text = getMessageOrDefault(arguments, "{0} results", PagesDialect.I18N_RESULTS, params);
        }
        resultCount.addChild(new Text(text));
        container.getParent().insertAfter(container, resultCount);
    }

    /**
     * Adds a "No result found" text.
     */
    private void addNoResult() {
        Element noResultElement = new Element("span");
        noResultElement.setAttribute("class", "paginate-no-result");
        String text = getMessageOrDefault(arguments, "No result found", PagesDialect.I18N_NONE);
        noResultElement.addChild(new Text(text));
        container.getParent().insertAfter(container, noResultElement);
    }

    /**
     * Returns the page URL with the "page" parameter added or modified.
     */
    private String getPageUrl(int pageNumber) {
        HttpServletRequest request = ((IWebContext) arguments.getContext()).getHttpServletRequest();
        String uri = request.getRequestURL().toString().split("\\?")[0];
        String query = request.getQueryString();
        if (query == null) {
            query = pageParam + "=" + pageNumber;
        } else if (query.contains(pageParam + "=")) {
            query = query.replaceAll(pageParam + "=[0-9]+", pageParam + "=" + pageNumber);
        } else {
            query += "&" + pageParam + "=" + pageNumber;
        }
        return uri + "?" + query;
    }

    /**
     * Adds navigation links after the container element, like:
     *
     * <pre>
     * {@code
     *    <div class="pagination">
     *        <a href="...">First</a>
     *        <a href="...">Previous</a>
     *        <span>Page 2 of 5</span>
     *        <a href="...">Next</a>
     *        <a href="...">Last</a>
     *    </div>
     * }
     * </pre>
     */
    private void addNavigationLinks() {
        Element div = new Element("div");
        div.setAttribute("class", "paginate");
        String text;
        // "First" link
        if (!pagedList.isFirstPage()) {
            addLinkToDiv(div, 0, "paginate-first", "First", PagesDialect.I18N_FIRST);
        }
        // "Previous" link
        if (!pagedList.isFirstPage()) {
            addLinkToDiv(div, pagedList.getPage() - 1, "paginate-previous", "Previous", PagesDialect.I18N_PREVIOUS);
        }
        // "Page N of M" text
        Element currentPage = new Element("span");
        currentPage.setAttribute("class", "paginate-page");
        String[] params = {(pagedList.getPage() + 1) + "", pagedList.getPageCount() + ""};
        text = getMessageOrDefault(arguments, "Page {0} of {1}", PagesDialect.I18N_PAGE, params);
        currentPage.addChild(new Text(text));
        div.addChild(currentPage);
        // "Next" link
        if (!pagedList.isLastPage()) {
            addLinkToDiv(div, pagedList.getPage() + 1, "paginate-next", "Next", PagesDialect.I18N_NEXT);
        }
        // "Last" link
        if (!pagedList.isLastPage()) {
            addLinkToDiv(div, pagedList.getPageCount() - 1, "paginate-last", "Last", PagesDialect.I18N_LAST);
        }
        container.getParent().insertAfter(container, div);
    }

    private void addLinkToDiv(Element div, int targetPage, String className, String defaultText, String i18nKey) {
        Element link = new Element("a");
        link.setAttribute("class", "paginate-last");
        link.setAttribute("href", getPageUrl(pagedList.getPageCount() - 1));
        String text = getMessageOrDefault(arguments, "Last", PagesDialect.I18N_LAST);
        link.addChild(new Text(text));
        div.addChild(link);
        Text separator = new Text(" ");
        div.addChild(separator);
    }

    private String getMessageOrDefault(Arguments arguments, String defaultMessage, String propertyKey, String ... params) {
        String messageKey;
        if (dialect.getProperties().containsKey(propertyKey)) {
            messageKey = dialect.getProperties().get(propertyKey);
        } else {
            messageKey = propertyKey;
        }
        String message = PagesDialectUtil.templateMessage(arguments, messageKey, params);
        if (message.startsWith("??" + messageKey)) {
            MessageFormat formatter = new MessageFormat(defaultMessage);
            return formatter.format(params);
        } else {
            return message;
        }
    }
}
