package net.sourceforge.pagesdialect.commands;

import net.sourceforge.pagesdialect.util.PagesDialectUtil;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.exceptions.TemplateProcessingException;

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
        String intervalExpression = params[1].trim();
        String intervalStr = PagesDialectUtil.expressionValue(arguments, intervalExpression).toString();
        int interval = Integer.parseInt(intervalStr);
        String iterationCountExpression = "${" + params[0].trim() + "Stat.count}";
        String iterationCountStr = PagesDialectUtil.expressionValue(arguments, iterationCountExpression).toString();
        int iteration = Integer.parseInt(iterationCountStr);
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
