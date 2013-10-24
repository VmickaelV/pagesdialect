package net.sourceforge.pagesdialect;

import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.ProcessorResult;
import org.thymeleaf.processor.attr.AbstractAttrProcessor;

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

    public PaginateAttrProcessor(String attributeName, PagesDialect dialect) {
        super(attributeName);
        this.dialect = dialect;
    }

    @Override
    public int getPrecedence() {
        return PagesDialect.PAGINATE_ATTR_PRECEDENCE;
    }

    @Override
    protected ProcessorResult processAttribute(Arguments arguments, Element element, String attributeName) {
        new PaginateCommand(arguments, element, attributeName, dialect).execute();
        return ProcessorResult.OK;
    }
}
