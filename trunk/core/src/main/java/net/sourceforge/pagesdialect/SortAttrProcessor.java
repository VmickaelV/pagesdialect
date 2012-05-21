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
import org.thymeleaf.processor.IAttributeNameProcessorMatcher;
import org.thymeleaf.processor.ProcessorResult;
import org.thymeleaf.processor.attr.AbstractAttrProcessor;
import org.thymeleaf.standard.expression.StandardExpressionProcessor;
import org.thymeleaf.standard.processor.attr.StandardEachAttrProcessor;

/**
 * Thymeleaf processor that adds a link to sort an iteration collection.
 * Attribute value is sorting field.
 * 
 * Example usage:
 * <pre>
 * {@code
 *     <table th:each="product : ${products}">
 *        ...
 *        <th pages:sort="name">Sort by name</th>
 *        ...
 *     </table>
 * }
 * </pre>
 **/
public class SortAttrProcessor extends AbstractAttrProcessor {

    private PagesDialect dialect;
    private String sortParam = "sort"; // Default value. Can be overriden by config.
    private String sortTypeParam = "sortType"; // Default value. Can be overriden by config.

    public SortAttrProcessor(IAttributeNameProcessorMatcher matcher) {
        super(matcher);
    }

    public SortAttrProcessor(String attributeName) {
        super(attributeName);
    }

    public void setDialect(PagesDialect dialect) {
        this.dialect = dialect;
        if (dialect.getProperties().containsKey(PagesDialect.SORT_PARAMETER)) {
            this.sortParam = dialect.getProperties().get(PagesDialect.SORT_PARAMETER);
        }
        if (dialect.getProperties().containsKey(PagesDialect.SORT_TYPE_PARAMETER)) {
            this.sortTypeParam = dialect.getProperties().get(PagesDialect.SORT_TYPE_PARAMETER);
        }
    }

    @Override
    public int getPrecedence() {
        return PagesDialect.SORT_ATTR_PRECEDENCE;
    }
    
    /**
     * Returns the list iteration for the th:each
     */
    private List getIterableList(Arguments arguments, Element element) {
        Element table = PagesDialectUtil.getContainerElement(element);
        String iterationAttrName = PagesDialectUtil.getStandardDialectPrefix(arguments) + ":" + StandardEachAttrProcessor.ATTR_NAME; // th:each
        String iterationAttrValue = null;
        search : for (Element child : table.getElementChildren()) {
            if (child.hasAttribute(iterationAttrName)) {
                iterationAttrValue = child.getAttributeValue(iterationAttrName);
                break search;
            } 
            for (Element grandchild : child.getElementChildren()) {
                if (grandchild.hasAttribute(iterationAttrName)) {
                    iterationAttrValue = grandchild.getAttributeValue(iterationAttrName);
                    break search;
                } 
            }
        }
        if (iterationAttrValue == null) {
            throw new TemplateProcessingException("Iteration object not found");
        }
        String listExpression = iterationAttrValue.split(":")[1].trim();
        Object iterable = StandardExpressionProcessor.processExpression(arguments, listExpression);
        return PagesDialectUtil.convertToList(iterable);
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
                } else if (thereIsTypeFormatterForClass(propertyA.getClass())) {
                    // Try to sort after formatting
                    TypeFormatter typeFormatter = getTypeFormatterForClass(propertyA.getClass());
                    HttpServletRequest request = ((IWebContext) context).getHttpServletRequest();
                    DRIValueFormatter valueFormatter = typeFormatter.getDRIValueFormatter(request);
                    String valueA = valueFormatter.format(propertyA, null).toString();
                    String valueB = valueFormatter.format(propertyB, null).toString();
                    Collator collator = Collator.getInstance(context.getLocale());
                    return sign * collator.compare(valueA, valueB);
                } else {
                    throw new TemplateProcessingException("Sort field does not implement Comparable");
                }
            }
        };
    }
    
    private boolean thereIsTypeFormatterForClass(Class objectClass) {
        if (this.dialect.getTypeFormatters() != null) {
            for (TypeFormatter typeFormatter : this.dialect.getTypeFormatters()) {
                if (typeFormatter.getValueClass().equals(objectClass)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private TypeFormatter getTypeFormatterForClass(Class objectClass) {
        if (this.dialect.getTypeFormatters() != null) {
            for (TypeFormatter typeFormatter : this.dialect.getTypeFormatters()) {
                if (typeFormatter.getValueClass().equals(objectClass)) {
                    return typeFormatter;
                }
            }
        }
        throw new IllegalStateException("TypeFormatter not found for class " + objectClass.getName());
    }

    /**
     * Add a sort link to provided element.
     */
    private void addSortLink(Arguments arguments, Element element, String attributeName, String field, Boolean desc) {
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
    
    @Override
    protected ProcessorResult processAttribute(Arguments arguments, Element element, String attributeName) {
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
                List iterable = getIterableList(arguments, element);
                Collections.sort(iterable, getFieldComparator(context, sortField, desc));
            }
        }
        // Add sort link
        addSortLink(arguments, element, attributeName, sortField, desc);
        // Housekeeping
        return ProcessorResult.OK;
    }
}
