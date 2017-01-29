package net.sourceforge.pagesdialect.commands;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.pagesdialect.PagesDialect;
import org.thymeleaf.Arguments;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;

/**
 * Adds a sort link.
 */
public class SortLinkCommand {

    protected Arguments arguments;
    protected Element element;
    protected String attributeName;
    protected PagesDialect dialect;
    protected IWebContext context;

    private String sortParam = "sort"; // Default value. Can be overriden by config.
    private String sortTypeParam = "sortType"; // Default value. Can be overriden by config.

    public SortLinkCommand(Arguments arguments, Element element, String attributeName, PagesDialect dialect) {
        this.arguments = arguments;
        this.element = element;
        this.attributeName = attributeName;
        this.dialect = dialect;
        if (dialect.getProperties().containsKey(PagesDialect.SORT_PARAMETER)) {
            sortParam = dialect.getProperties().get(PagesDialect.SORT_PARAMETER);
        }
        if (dialect.getProperties().containsKey(PagesDialect.SORT_TYPE_PARAMETER)) {
            sortTypeParam = dialect.getProperties().get(PagesDialect.SORT_TYPE_PARAMETER);
        }
        this.context = (IWebContext) arguments.getContext();
    }

    public void execute() {
        // Parse parameters
        String sortField = element.getAttributeValue(attributeName).trim();
        Boolean desc = getSortType(sortField);
        // Add sort link
        addSortLink(sortField, desc);
        // Housekeeping
        element.removeAttribute(attributeName);
    }

    protected Boolean getSortType(String sortField) {
        if (context.getRequestParameters().containsKey(this.sortParam)) {
            String sortValue = context.getRequestParameters().get(this.sortParam)[0];
            if (sortValue != null && sortField.equals(sortValue)) {
                if (context.getRequestParameters().containsKey(this.sortTypeParam)) {
                    return "desc".equals(context.getRequestParameters().get(this.sortTypeParam)[0]);
                }
            }
        }
        return null;
    }
    

    /**
     * Add a sort link to provided element.
     */
    protected void addSortLink(String field, Boolean desc) {
        // Build URL
        HttpServletRequest request = ((IWebContext) arguments.getContext()).getHttpServletRequest();
        String uri = request.getRequestURL().toString().split("\\?")[0];
        String query = request.getQueryString();
        // Remove previous parameters
        if (query != null) {
            query = query.replaceAll("&?" + sortParam + "=[^&]*", "");
            query = query.replaceAll("&?" + sortTypeParam + "=[^&]*", "");
        }
        if (query != null && !"".equals(query)) {
            query += "&";
        } else {
            query = "";
        }
        query += sortParam + "=" + field + "&" + sortTypeParam + "=" + (desc == null || desc ? "asc" : "desc");
        String href = uri + "?" + query;
        // Insert new anchor between element and its content
        Element anchor = element.cloneElementNodeWithNewName(element, "a", false);
        for (String attr : anchor.getAttributeMap().keySet()) {
            anchor.removeAttribute(attr);
        }
        anchor.setAttribute("href", href);
        if (desc == null) {
            anchor.setAttribute("class", "sort-sortable");
        } else if (desc) {
            anchor.setAttribute("class", "sort-desc");
        } else {
            anchor.setAttribute("class", "sort-asc");
        }
        for (Node child : element.getChildren()) {
            element.removeChild(child);
        }
        element.addChild(anchor);
    }
}
