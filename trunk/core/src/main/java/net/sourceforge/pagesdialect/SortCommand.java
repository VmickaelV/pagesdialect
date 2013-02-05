package net.sourceforge.pagesdialect;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import net.sf.dynamicreports.report.definition.expression.DRIValueFormatter;
import org.thymeleaf.Arguments;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.standard.processor.attr.StandardEachAttrProcessor;

public class SortCommand {

    private Arguments arguments;
    private Element element;
    private String attributeName;
    private PagesDialect dialect;

    private String sortParam = "sort"; // Default value. Can be overriden by config.
    private String sortTypeParam = "sortType"; // Default value. Can be overriden by config.

    public SortCommand(Arguments arguments, Element element, String attributeName, PagesDialect dialect) {
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
    }

    public void execute() {
        // Parse parameters
        String sortField = element.getAttributeValue(attributeName).trim();
        // Sort original list, if requested
        Boolean desc = null;
        IWebContext context = (IWebContext) arguments.getContext();
        if (context.getRequestParameters().containsKey(this.sortParam)) {
            String sortValue = context.getRequestParameters().get(this.sortParam)[0];
            if (sortValue != null && sortField.equals(sortValue)) {
                if (context.getRequestParameters().containsKey(this.sortTypeParam)) {
                    desc = "desc".equals(context.getRequestParameters().get(this.sortTypeParam)[0]);
                }
                List iterable = getIterableList();
                Collections.sort(iterable, getFieldComparator(context, sortField, desc));
            }
        }
        // Add sort link
        addSortLink(sortField, desc);
        // Housekeeping
        element.removeAttribute(attributeName);
    }

    /**
     * Returns the list iteration for the th:each
     */
    private List getIterableList() {
        Element elementContainingIteration = null;
        Element table = PagesDialectUtil.getContainerElement(element);
        String iterationAttrName = PagesDialectUtil.getStandardDialectPrefix(arguments) + ":" + StandardEachAttrProcessor.ATTR_NAME; // th:each
        search : for (Element child : table.getElementChildren()) {
            if (child.hasAttribute(iterationAttrName)) {
                elementContainingIteration = child;
                break search;
            }
            for (Element grandchild : child.getElementChildren()) {
                if (grandchild.hasAttribute(iterationAttrName)) {
                    elementContainingIteration = grandchild;
                    break search;
                }
            }
        }
        if (elementContainingIteration == null) {
            throw new TemplateProcessingException("Iteration object not found");
        }
        IterationListPreparer iterationListPreparer = new IterationListPreparer(arguments, elementContainingIteration);
        return iterationListPreparer.findOrCreateIterationList().getOriginalList();
    }

    /**
     * Builds a comparator for provided field.
     * @param field property which will be used by comparator.
     */
    private Comparator getFieldComparator(final IContext context, final String field, final Boolean desc) {
        return new Comparator() {
            @Override
            public int compare(Object objA, Object objB) {
                Object propertyA = PagesDialectUtil.getProperty(objA, field);
                Object propertyB = PagesDialectUtil.getProperty(objB, field);
                int sign = desc != null && desc ? -1 : 1;
                if (propertyA == null) {
                    return sign * -1; // nulls at beggining
                } else if (propertyB == null) {
                    return sign * 1; // nulls at beggining
                } if (propertyA instanceof Comparable && propertyB instanceof Comparable) {
                    return sign * ((Comparable) propertyA).compareTo(propertyB);
                } else if (thereIsTypeFormatterForObject(propertyA)) {
                    // Try to sort after formatting
                    TypeFormatter typeFormatter = getTypeFormatterForObject(propertyA);
                    HttpServletRequest request = ((IWebContext) context).getHttpServletRequest();
                    DRIValueFormatter valueFormatter = typeFormatter.getDRIValueFormatter(request);
                    String valueA = valueFormatter.format(propertyA, null).toString();
                    String valueB = valueFormatter.format(propertyB, null).toString();
                    Collator collator = Collator.getInstance(context.getLocale());
                    return sign * collator.compare(valueA, valueB);
                } else {
                    throw new TemplateProcessingException("Field does not implement Comparable");
                }
            }
        };
    }

    private boolean thereIsTypeFormatterForObject(Object object) {
        if (this.dialect.getTypeFormatters() != null) {
            for (TypeFormatter typeFormatter : this.dialect.getTypeFormatters()) {
                Class typeFormatterClass = typeFormatter.getValueClass();
                if (typeFormatterClass.isInstance(object)) {
                    return true;
                }
            }
        }
        return false;
    }

    private TypeFormatter getTypeFormatterForObject(Object object) {
        if (this.dialect.getTypeFormatters() != null) {
            for (TypeFormatter typeFormatter : this.dialect.getTypeFormatters()) {
                Class typeFormatterClass = typeFormatter.getValueClass();
                if (typeFormatterClass.isInstance(object)) {
                    return typeFormatter;
                }
            }
        }
        throw new IllegalStateException("TypeFormatter not found for class " + object.getClass().getName());
    }

    /**
     * Add a sort link to provided element.
     */
    private void addSortLink(String field, Boolean desc) {
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
        anchor.removeAttribute(attributeName);
        anchor.setAttribute("href", href);
        if (desc == null) {
            anchor.setAttribute("class", "sort-sortable");
        } else if (desc) {
            anchor.setAttribute("class", "sort-desc");
        } else {
            anchor.setAttribute("class", "sort-asc");
        }
        for (String attr : element.getAttributeMap().keySet()) {
            element.removeAttribute(attr);
        }
        for (Node child : element.getChildren()) {
            element.removeChild(child);
        }
        element.addChild(anchor);
    }
}
