package net.sourceforge.pagesdialect;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.support.PagedListHolder;
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
 * Thymeleaf processor that modifies a th:each to allow pagination.
 * Attribute value is page size.
 * It generates page navigation links (previous, next...) and number of results.
 * 
 * Example usage:
 * <pre>
 * {@code
 *    <div th:each="product : ${products}" pages:paginate="10">...</div>
 * }
 * </pre>
 **/
public class PaginateAttrProcessor extends AbstractAttrProcessor {

    private PagesDialect dialect;

    private String pageParam = "page"; // Default value. Can be overriden by config.

    public PaginateAttrProcessor(IAttributeNameProcessorMatcher matcher) {
        super(matcher);
    }

    public PaginateAttrProcessor(String attributeName) {
        super(attributeName);
    }

    public void setDialect(PagesDialect dialect) {
        this.dialect = dialect;
        if (dialect.getProperties().containsKey(PagesDialect.PAGE_PARAMETER)) {
            this.pageParam = dialect.getProperties().get(PagesDialect.PAGE_PARAMETER);
        }
    }

    @Override
    public int getPrecedence() {
        return PagesDialect.PAGINATE_ATTR_PRECEDENCE;
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
    private void addResultCount(Arguments arguments, Element container, PagedListHolder pagedList) {
        Element resultCount = new Element("span");
        resultCount.setAttribute("class", "paginate-count");
        String text;
        if (pagedList.getNrOfElements() == 1) {
            if (dialect.getProperties().containsKey(PagesDialect.I18N_ONE_RESULT)) {
                text = getMessage(arguments, dialect.getProperties().get(PagesDialect.I18N_ONE_RESULT), null);
            } else {
                text = "1 result";
            }
        } else {
            if (dialect.getProperties().containsKey(PagesDialect.I18N_RESULTS)) {
                String[] params = {(pagedList.getFirstElementOnPage() + 1) + "",
                    (pagedList.getLastElementOnPage() + 1) + "",
                    pagedList.getNrOfElements() + ""};
                text = getMessage(arguments, dialect.getProperties().get(PagesDialect.I18N_RESULTS), params);
            } else {
                text = pagedList.getNrOfElements() + " results";
            }
        }
        resultCount.addChild(new Text(text));
        container.getParent().insertAfter(container, resultCount);
    }
    
    /**
     * Adds a "No result found" text.
     */
    private void addNoResult(Arguments arguments, Element container) {
        Element noResultElement = new Element("span");
        noResultElement.setAttribute("class", "paginate-no-result");
        String text;
        if (dialect.getProperties().containsKey(PagesDialect.I18N_NONE)) {
            text = getMessage(arguments, dialect.getProperties().get(PagesDialect.I18N_NONE), null);
        } else {
            text = "No result found";
        }
        noResultElement.addChild(new Text(text));
        container.getParent().insertAfter(container, noResultElement);
    }
    
    /**
     * Returns the page URL with the "page" parameter added or modified.
     */
    private String getPageUrl(Arguments arguments, int pageNumber) {
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
    private void addNavigationLinks(Arguments arguments, Element container, PagedListHolder pagedList) {
        Element div = new Element("div");
        div.setAttribute("class", "paginate");
        String text;
        // "First" link
        if (!pagedList.isFirstPage()) {
            Element first = new Element("a");
            first.setAttribute("class", "paginate-first");
            first.setAttribute("href", getPageUrl(arguments, 0));
            if (dialect.getProperties().containsKey(PagesDialect.I18N_FIRST)) {
                text = getMessage(arguments, dialect.getProperties().get(PagesDialect.I18N_FIRST), null);
            } else {
                text = "First";
            }
            first.addChild(new Text(text));
            div.addChild(first);
        }
        // "Previous" link
        if (!pagedList.isFirstPage()) {
            Element previous = new Element("a");
            previous.setAttribute("class", "paginate-previous");
            previous.setAttribute("href", getPageUrl(arguments, pagedList.getPage() - 1));
            if (dialect.getProperties().containsKey(PagesDialect.I18N_PREVIOUS)) {
                text = getMessage(arguments, dialect.getProperties().get(PagesDialect.I18N_PREVIOUS), null);
            } else {
                text = "Previous";
            }
            previous.addChild(new Text(text));
            div.addChild(previous);
        }
        // "Page N of M" text
        Element currentPage = new Element("span");
        currentPage.setAttribute("class", "paginate-page");
        String[] params = {(pagedList.getPage() + 1) + "", pagedList.getPageCount() + ""};
        if (dialect.getProperties().containsKey(PagesDialect.I18N_PAGE)) {
            text = getMessage(arguments, dialect.getProperties().get(PagesDialect.I18N_PAGE), params);
        } else {
            text = "Page " + params[0] + " of " + params[1];
        }
        currentPage.addChild(new Text(text));
        div.addChild(currentPage);
        // "Next" link
        if (!pagedList.isLastPage()) {
            Element next = new Element("a");
            next.setAttribute("class", "paginate-next");
            next.setAttribute("href", getPageUrl(arguments, pagedList.getPage() + 1));
            if (dialect.getProperties().containsKey(PagesDialect.I18N_NEXT)) {
                text = getMessage(arguments, dialect.getProperties().get(PagesDialect.I18N_NEXT), null);
            } else {
                text = "Next";
            }
            next.addChild(new Text(text));
            div.addChild(next);
        }
        // "Last" link
        if (!pagedList.isLastPage()) {
            Element last = new Element("a");
            last.setAttribute("class", "paginate-last");
            last.setAttribute("href", getPageUrl(arguments, pagedList.getPageCount() - 1));
            if (dialect.getProperties().containsKey(PagesDialect.I18N_LAST)) {
                text = getMessage(arguments, dialect.getProperties().get(PagesDialect.I18N_LAST), null);
            } else {
                text = "Last";
            }
            last.addChild(new Text(text));
            div.addChild(last);
        }
        container.getParent().insertAfter(container, div);
    }
    
    @Override
    protected ProcessorResult processAttribute(Arguments arguments, Element element, String attributeName) {
        // Parse parameters
        String attributeValue = element.getAttributeValue(attributeName);
        int pageSize = Integer.parseInt(StandardExpressionProcessor.processExpression(arguments, attributeValue).toString());
        // Set current page
        IterationListPreparer iterationListPreparer = new IterationListPreparer(arguments, element);
        PagedListHolder pagedList = iterationListPreparer.findOrCreateIterationList();
        pagedList.setPageSize(pageSize);
        IWebContext context = (IWebContext) arguments.getContext();
        if (context.getRequestParameters().containsKey(pageParam)) {
            pagedList.setPage(Integer.parseInt(context.getRequestParameters().get(pageParam)[0]));
        }
        // Add navigation links
        Element container = PagesDialectUtil.getContainerElement(element);
        if (pagedList.getPageCount() > 1) {
            addNavigationLinks(arguments, container, pagedList);
        }
        // Add number of results text
        if (pagedList.getNrOfElements() > 0) {
            addResultCount(arguments, container, pagedList);
        } else {
            addNoResult(arguments, container);
        }
        // Housekeeping
        element.removeAttribute(attributeName);
        return ProcessorResult.OK;
    }
}
