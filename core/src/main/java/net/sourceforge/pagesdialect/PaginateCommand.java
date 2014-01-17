package net.sourceforge.pagesdialect;

import org.springframework.beans.support.PagedListHolder;
import org.thymeleaf.Arguments;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.dom.Element;

public class PaginateCommand {

    private final Arguments arguments;
    private final Element element;
    private final String attributeName;
    private final PagesDialect dialect;

    private int pageSize;
    private PagedListHolder pagedList;

    public PaginateCommand(Arguments arguments, Element element, String attributeName, PagesDialect dialect) {
        this.arguments = arguments;
        this.element = element;
        this.attributeName = attributeName;
        this.dialect = dialect;
    }

    public void execute() {
        parseArguments();
        setCurrentPage();
        addNavigationBar();
        // Housekeeping
        element.removeAttribute(attributeName);
    }

    private void parseArguments() {
        String attributeValue = element.getAttributeValue(attributeName);
        String processedValue = PagesDialectUtil.expressionValue(arguments, attributeValue).toString();
        pageSize = Integer.parseInt(processedValue);
    }

    private void setCurrentPage() {
        IterationListPreparer iterationListPreparer = new IterationListPreparer(arguments, element);
        pagedList = iterationListPreparer.findOrCreateIterationList();
        pagedList.setPageSize(pageSize);
        IWebContext context = (IWebContext) arguments.getContext();
        String pageParam = dialect.getPageParameter();
        if (context.getRequestParameters().containsKey(pageParam)) {
            pagedList.setPage(Integer.parseInt(context.getRequestParameters().get(pageParam)[0]));
        }
    }

    private void addNavigationBar() {
        NavigationBarRenderer renderer = new NavigationBarRenderer(arguments, element, dialect, pagedList);
        renderer.addNavigationBar();
    }
}
