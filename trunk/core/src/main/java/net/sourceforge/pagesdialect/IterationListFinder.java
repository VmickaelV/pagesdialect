package net.sourceforge.pagesdialect;

import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.standard.processor.attr.StandardEachAttrProcessor;

/**
 * Parses a th:each expression and return its components.
 */
public class IterationListFinder {
    
    private Arguments arguments;
    private Element element;
    
    private String iterationExpression;
    private String itemName;
    private String iterationObjectName;
    private Object iterationObject;

    public IterationListFinder(Arguments arguments, Element element) {
        this.arguments = arguments;
        this.element = element;
        findIterationExpression();
        parseComponents();
    }

    /**
     * Find th:each attribute value inside the iterating element.
     */
    private String findIterationExpression() {
        iterationExpression = PagesDialectUtil.getStandardDialectPrefix(arguments) + ":" + StandardEachAttrProcessor.ATTR_NAME; // th:each
        if (!element.hasAttribute(iterationExpression)) {
            throw new TemplateProcessingException("Standard iteration attribute not found");
        }
        return iterationExpression;
    }

    private void parseComponents() {
        String[] iterationAttributeParams = element.getAttributeValue(iterationExpression).split(":");
        itemName = iterationAttributeParams[0].trim();
        iterationObjectName = iterationAttributeParams[1].trim();
        iterationObject = PagesDialectUtil.expressionValue(arguments, iterationObjectName);
    }

    /** Given th:each="product : ${productList}" returns "th:each". */
    public String getIterationExpression() {
        return iterationExpression;
    }

    /** Given th:each="product : ${productList}" returns "product". */
    public String getItemName() {
        return itemName;
    }

    /** Given th:each="product : ${productList}" returns "productList". */
    public String getIterationObjectName() {
        return iterationObjectName;
    }

    /** Given th:each="product : ${productList}" returns productList object. */
    public Object getIterationObject() {
        return iterationObject;
    }
}
