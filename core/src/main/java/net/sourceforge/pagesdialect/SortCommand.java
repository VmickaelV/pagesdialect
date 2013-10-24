package net.sourceforge.pagesdialect;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import net.sf.dynamicreports.report.definition.expression.DRIValueFormatter;
import org.thymeleaf.Arguments;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.dom.Element;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.standard.processor.attr.StandardEachAttrProcessor;

/**
 * Adds a sort link and sort the iteration list.
 */
public class SortCommand extends SortLinkCommand {

    public SortCommand(Arguments arguments, Element element, String attributeName, PagesDialect dialect) {
        super(arguments, element, attributeName, dialect);
    }

    @Override
    public void execute() {
        // Parse parameters
        String sortField = element.getAttributeValue(attributeName).trim();
        Boolean desc = getSortType(sortField);
        // Sort original list, if requested
        if (desc != null) {
            List iterable = getIterableList();
            Collections.sort(iterable, getFieldComparator(sortField, desc));
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
    private Comparator getFieldComparator(final String field, final Boolean desc) {
        return new Comparator() {
            @Override
            public int compare(Object objA, Object objB) {
                Object propertyA = PagesDialectUtil.getProperty(objA, field);
                Object propertyB = PagesDialectUtil.getProperty(objB, field);
                int sign = desc != null && desc ? -1 : 1;
                if (propertyA == null && propertyB == null) {
                    return 0; // null == null required by Comparator contract
                } if (propertyA == null) {
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
}
