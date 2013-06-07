package net.sourceforge.pagesdialect;

import org.springframework.data.domain.Page;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;

public class PaginatedCommand {

    private Arguments arguments;
    private Element element;
    private String attributeName;
    private PagesDialect dialect;

    private boolean executeProcessor;
    
    private Page pagedList;

    public PaginatedCommand(Arguments arguments, Element element, String attributeName, PagesDialect dialect) {
        this.arguments = arguments;
        this.element = element;
        this.attributeName = attributeName;
        this.dialect = dialect;
    }

    public void execute() {
        parseArguments();
        if (executeProcessor) {
            findIterationObject();
            addNavigationBar();
        }
        // Housekeeping
        element.removeAttribute(attributeName);
    }

    private void parseArguments() {
        String attributeValue = element.getAttributeValue(attributeName);
        String processedValue = PagesDialectUtil.expressionValue(arguments, attributeValue).toString();
        executeProcessor = "true".equals(processedValue);
    }

    private void findIterationObject() {
        IterationListFinder iterationListFinder = new IterationListFinder(arguments, element);
        pagedList = (Page) iterationListFinder.getIterationObject();
    }

    private void addNavigationBar() {
        NavigationBarRenderer renderer = new NavigationBarRenderer(arguments, element, dialect, pagedList);
        renderer.addNavigationBar();
    }
}
