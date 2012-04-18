package org.thymeleaf.pagesdialect;

import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.processor.IAttributeNameProcessorMatcher;
import org.thymeleaf.processor.ProcessorResult;
import org.thymeleaf.processor.attr.AbstractAttrProcessor;
import org.thymeleaf.standard.expression.StandardExpressionProcessor;

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

    public SeparateAttrProcessor(IAttributeNameProcessorMatcher matcher) {
        super(matcher);
    }

    public SeparateAttrProcessor(String attributeName) {
        super(attributeName);
    }

    @Override
    public int getPrecedence() {
        return PagesDialect.SEPARATE_ATTR_PRECEDENCE;
    }
    
    @Override
    protected ProcessorResult processAttribute(Arguments arguments, Element element, String attributeName) {
        // Parse parameters
        String attributeValue = element.getAttributeValue(attributeName);
        String[] params = attributeValue.split(",");
        if (params.length != 2) {
            throw new TemplateProcessingException("Iteration object and interval required");
        }
        String intervalStr = params[1].trim();
        int interval = Integer.parseInt(StandardExpressionProcessor.processExpression(arguments, intervalStr).toString());
        String iterationCountExpression = "${" + params[0].trim() + "Stat.count}";
        int iteration = Integer.parseInt(StandardExpressionProcessor.processExpression(arguments, iterationCountExpression).toString());
        // Generate code
        if (iteration > 1 && iteration % interval == 1) {
            Element div = new Element("div");
            div.setAttribute("class", "separator");
            element.getParent().insertBefore(element, div);
        }
        // Housekeeping
        element.removeAttribute(attributeName);
        return ProcessorResult.OK;
    }
}
