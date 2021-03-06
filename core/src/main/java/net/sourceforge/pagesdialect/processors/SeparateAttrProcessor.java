package net.sourceforge.pagesdialect.processors;

import net.sourceforge.pagesdialect.PagesDialect;
import net.sourceforge.pagesdialect.commands.SeparateCommand;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.ProcessorResult;
import org.thymeleaf.processor.attr.AbstractAttrProcessor;

/**
 * Inside a th:each iteration, add a
 *    <div class="separator"></div>
 * elements after a fixed number of iterations.
 *
 * Example usage:
 * <pre>
 * {@code
 *    <div th:each="product : ${products}" ata:separate="product, 3">...</div>
 * }
 * </pre>
 **/
public class SeparateAttrProcessor extends AbstractAttrProcessor {

    public SeparateAttrProcessor(String attributeName) {
        super(attributeName);
    }

    @Override
    public int getPrecedence() {
        return PagesDialect.SEPARATE_ATTR_PRECEDENCE;
    }

    @Override
    protected ProcessorResult processAttribute(Arguments arguments, Element element, String attributeName) {
        new SeparateCommand(arguments, element, attributeName).execute();
        return ProcessorResult.OK;
    }
}
