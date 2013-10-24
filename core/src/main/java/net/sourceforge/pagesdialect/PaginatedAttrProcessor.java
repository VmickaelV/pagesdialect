package net.sourceforge.pagesdialect;

import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.ProcessorResult;
import org.thymeleaf.processor.attr.AbstractAttrProcessor;

/**
 * Thymeleaf processor which adds page navigation links (previous, next...) and number of results
 * to a th:each processor. The iteration object is expected to implement spring-data Page interface.
 * This processor do not paginate, it is intented to be used with server-side pagination.
 *
 * Example usage:
 * <pre>
 * {@code
 *    <div th:each="product : ${products}" pages:paginated="true">...</div>
 * }
 * </pre>
 **/
public class PaginatedAttrProcessor extends AbstractAttrProcessor {

    private PagesDialect dialect;

    public PaginatedAttrProcessor(String attributeName, PagesDialect dialect) {
        super(attributeName);
        this.dialect = dialect;
    }

    @Override
    public int getPrecedence() {
        return PagesDialect.PAGINATE_ATTR_PRECEDENCE;
    }

    @Override
    protected ProcessorResult processAttribute(Arguments arguments, Element element, String attributeName) {
        new PaginatedCommand(arguments, element, attributeName, dialect).execute();
        return ProcessorResult.OK;
    }
}
