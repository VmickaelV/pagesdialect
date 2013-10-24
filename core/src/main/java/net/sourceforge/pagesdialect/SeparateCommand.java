package net.sourceforge.pagesdialect;

import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.standard.expression.StandardExpressionProcessor;

public class SeparateCommand {

    private Arguments arguments;
    private Element element;
    private String attributeName;

    public SeparateCommand(Arguments arguments, Element element, String attributeName) {
        this.arguments = arguments;
        this.element = element;
        this.attributeName = attributeName;
    }

    public void execute() {
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
    }
}
