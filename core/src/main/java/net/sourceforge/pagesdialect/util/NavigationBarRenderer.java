package net.sourceforge.pagesdialect.util;

import java.text.MessageFormat;
import javax.servlet.http.HttpServletRequest;

import net.sourceforge.pagesdialect.PagesDialect;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.data.domain.Page;
import org.thymeleaf.Arguments;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Text;

/**
 * Utility class to add a page navigation bar and result count.
 */
public class NavigationBarRenderer {
    
    private Arguments arguments;
    private Element element;
    private PagesDialect dialect;
    private Element container;
    private int pageCount;
    private int page;
    private long nrOfElements;
    private int firstElementOnPage;
    private int lastElementOnPage;
    private boolean firstPage;
    private boolean lastPage;

    /** Constructor using spring PagedListHolder object. */
    public NavigationBarRenderer(Arguments arguments, Element element, PagesDialect dialect, PagedListHolder pagedList) {
        this.arguments = arguments;
        this.element = element;
        this.dialect = dialect;
        pageCount = pagedList.getPageCount();
        page = pagedList.getPage();
        nrOfElements = pagedList.getNrOfElements();
        firstElementOnPage = pagedList.getFirstElementOnPage();
        lastElementOnPage = pagedList.getLastElementOnPage();
        firstPage = pagedList.isFirstPage();
        lastPage = pagedList.isLastPage();
    }
    
    /** Constructor using spring-date Page object. */
    public NavigationBarRenderer(Arguments arguments, Element element, PagesDialect dialect, Page pagedList) {
        this.arguments = arguments;
        this.element = element;
        this.dialect = dialect;
        pageCount = pagedList.getTotalPages();
        page = pagedList.getNumber();
        nrOfElements = pagedList.getTotalElements();
        firstElementOnPage = pagedList.getNumber() * pagedList.getSize();
        lastElementOnPage = pagedList.getNumber() * pagedList.getSize() + pagedList.getNumberOfElements() - 1;
        firstPage = pagedList.isFirstPage();
        lastPage = pagedList.isLastPage();
    }

    void addNavigationBar() {
        addNavigationLinksIfNeeded();
        addNumberOfResultsText();
    }

    private void addNavigationLinksIfNeeded() {
        container = PagesDialectUtil.getContainerElement(element);
        if (pageCount > 1) {
            addNavigationLinks();
        }
    }

    private void addNumberOfResultsText() {
        if (nrOfElements > 0) {
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
        if (nrOfElements == 1) {
            text = getMessageOrDefault(arguments, "1 result", PagesDialect.I18N_ONE_RESULT);
        } else {
            String[] params = {(firstElementOnPage + 1) + "", (lastElementOnPage + 1) + "", nrOfElements + ""};
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
        String pageParam = dialect.getPageParameter();
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
        if (!firstPage) {
            // "First" link
            addLinkToDiv(div, 0, "paginate-first", "First", PagesDialect.I18N_FIRST);
            // "Previous" link
            addLinkToDiv(div, page - 1, "paginate-previous", "Previous", PagesDialect.I18N_PREVIOUS);
        }
        // "Page N of M" text
        Element currentPage = new Element("span");
        currentPage.setAttribute("class", "paginate-page");
        String[] params = {(page + 1) + "", pageCount + ""};
        text = getMessageOrDefault(arguments, "Page {0} of {1}", PagesDialect.I18N_PAGE, params);
        currentPage.addChild(new Text(text));
        div.addChild(currentPage);
        div.addChild(new Text(" "));
        if (!lastPage) {
            // "Next" link
            addLinkToDiv(div, page + 1, "paginate-next", "Next", PagesDialect.I18N_NEXT);
            // "Last" link
            addLinkToDiv(div, pageCount - 1, "paginate-last", "Last", PagesDialect.I18N_LAST);
        }
        container.getParent().insertAfter(container, div);
    }

    private void addLinkToDiv(Element div, int targetPage, String className, String defaultText, String i18nKey) {
        Element link = new Element("a");
        link.setAttribute("class", className);
        link.setAttribute("href", getPageUrl(targetPage));
        String text = getMessageOrDefault(arguments, defaultText, i18nKey);
        link.addChild(new Text(text));
        div.addChild(link);
        Text separator = new Text(" ");
        div.addChild(separator);
    }

    private String getMessageOrDefault(Arguments arguments, String defaultMessage, String propertyKey, String... params) {
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
