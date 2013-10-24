package net.sourceforge.pagesdialect;

import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.IAttributeNameProcessorMatcher;
import org.thymeleaf.processor.ProcessorResult;
import org.thymeleaf.processor.attr.AbstractAttrProcessor;

/**
 * Thymeleaf processor that adds a link to export a Collection.
 *
 * Example usage:
 * <pre>
 * {@code
 *      <tr th:each="product : ${products}" pages:excel="name, category.name, stock, price">
 * }
 * </pre>
 *
 * Example usage with i18n keys:
 * <pre>
 * {@code
 *      <tr th:each="product : ${products}" pages:pdf="name:name, category.name:category, formattedPrice:total">
 * }
 * </pre>
 **/
public class ExportAttrProcessor extends AbstractAttrProcessor {

    private PagesDialect dialect;

    private String format;

    public ExportAttrProcessor(IAttributeNameProcessorMatcher matcher) {
        super(matcher);
    }

    public ExportAttrProcessor(String attributeName, PagesDialect dialect, String format) {
        super(attributeName);
        this.dialect = dialect;
        this.format = format;
    }

    @Override
    public int getPrecedence() {
        return PagesDialect.EXPORT_ATTR_PRECEDENCE;
    }

    @Override
    protected ProcessorResult processAttribute(Arguments arguments, Element element, String attributeName) {
        new ExportCommand(arguments, element, attributeName, dialect, format).execute();
        return ProcessorResult.OK;
    }
}
