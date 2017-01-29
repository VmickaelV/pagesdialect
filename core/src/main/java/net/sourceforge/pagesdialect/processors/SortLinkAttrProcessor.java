package net.sourceforge.pagesdialect.processors;

import net.sourceforge.pagesdialect.PagesDialect;
import net.sourceforge.pagesdialect.commands.SortLinkCommand;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.ProcessorResult;
import org.thymeleaf.processor.attr.AbstractAttrProcessor;

/**
 * Thymeleaf processor that adds a link to sort an iteration collection.
 * This processor do not perform sorting. It is intended to be used with server-side sorting.
 * Attribute value is sorting field.
 *
 * Example usage:
 * <pre>
 * {@code
 *     <table th:each="product : ${products}">
 *        ...
 *        <th pages:sortLink="name">Sort by name</th>
 *        ...
 *     </table>
 * }
 * </pre>
 **/
public class SortLinkAttrProcessor extends AbstractAttrProcessor {

    private PagesDialect dialect;

    public SortLinkAttrProcessor(String attributeName, PagesDialect dialect) {
        super(attributeName);
        this.dialect = dialect;
    }

    @Override
    public int getPrecedence() {
        return PagesDialect.SORT_ATTR_PRECEDENCE;
    }

    @Override
    protected ProcessorResult processAttribute(Arguments arguments, Element element, String attributeName) {
        new SortLinkCommand(arguments, element, attributeName, dialect).execute();
        return ProcessorResult.OK;
    }
}
