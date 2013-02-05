package net.sourceforge.pagesdialect;

import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.ProcessorResult;
import org.thymeleaf.processor.attr.AbstractAttrProcessor;

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

    public SortAttrProcessor(String attributeName, PagesDialect dialect) {
        super(attributeName);
        this.dialect = dialect;
    }

    @Override
    public int getPrecedence() {
        return PagesDialect.SORT_ATTR_PRECEDENCE;
    }

    @Override
    protected ProcessorResult processAttribute(Arguments arguments, Element element, String attributeName) {
        new SortCommand(arguments, element, attributeName, dialect).execute();
        return ProcessorResult.OK;
    }
}
