package net.sourceforge.pagesdialect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.thymeleaf.Arguments;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.dom.Element;
import org.thymeleaf.exceptions.TemplateProcessingException;

/**
 * Utility class to build a RecoverablePagedListHolder list from the iteration object.
 */
public class IterationListPreparer {

    public static final String PAGED_LIST_HOLDER_ATTR = "net_sourceforge_pagesdialect_PagesDialect_pagedListHolder";

    private Arguments arguments;
    private Element elementContainingIteration;

    public IterationListPreparer(Arguments arguments, Element elementContainingIteration) {
        this.arguments = arguments;
        this.elementContainingIteration = elementContainingIteration;
    }

    /**
     * Search for iteration expression.
     * If iteration object is not RecoverablePagedListHolder, replace it.
     */
    public RecoverablePagedListHolder findOrCreateIterationList() {
        // Check if RecoverablePagedListHolder has been already created
        IWebContext context = (IWebContext) arguments.getContext();
        if (context.getRequestAttributes().containsKey(PAGED_LIST_HOLDER_ATTR)) {
            return (RecoverablePagedListHolder) context.getRequestAttributes().get(PAGED_LIST_HOLDER_ATTR);
        } else {
            // Create and store RecoverablePagedListHolder.
            IterationListFinder iterationListFinder = new IterationListFinder(arguments, elementContainingIteration);
            Object originalIterable = iterationListFinder.getIterationObject();
            String itemName = iterationListFinder.getItemName();
            String iterationExpression = iterationListFinder.getIterationExpression();
            // Replace original with paged list
            RecoverablePagedListHolder pagedListHolder = new RecoverablePagedListHolder(convertToList(originalIterable));
            context.getRequestAttributes().put(PAGED_LIST_HOLDER_ATTR, pagedListHolder);
            elementContainingIteration.setAttribute(iterationExpression, itemName + " : ${#ctx.requestAttributes." + PAGED_LIST_HOLDER_ATTR + ".pageList}");
            return pagedListHolder;
        }    
    }
    
    
    
    /**
     * Converts an iterable object to a List.
     * @param iterable object of type List, Collection or array.
     * @return a List with the same items as the iterable object.
     */
    private List convertToList(Object iterable) {
        List list;
        if (iterable instanceof List) {
            list = (List) iterable;
        } else if (iterable instanceof Collection) {
            list = new ArrayList((Collection) iterable);
        } else if (iterable.getClass().isArray()) {
            list = Arrays.asList(iterable);
        } else {
            throw new TemplateProcessingException("Iteration object not recognized");
        }
        return list;
    }
}
