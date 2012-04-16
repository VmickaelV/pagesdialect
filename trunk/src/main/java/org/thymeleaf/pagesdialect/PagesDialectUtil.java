package org.thymeleaf.pagesdialect;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.thymeleaf.Arguments;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.dom.Element;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.spring3.dialect.SpringStandardDialect;
import org.thymeleaf.standard.StandardDialect;

/**
 * Utility methods for PagesDialect.
 */
public class PagesDialectUtil {
    
    public static String getStandardDialectPrefix(Arguments arguments) {
        Map<String, IDialect> dialectMap = arguments.getConfiguration().getDialects();
        for (Entry<String, IDialect> dialectEntry : dialectMap.entrySet()) {
            IDialect dialect = dialectEntry.getValue();
            if (dialect instanceof SpringStandardDialect || dialect instanceof StandardDialect) {
                return dialectEntry.getKey();
            }
        }
        throw new TemplateProcessingException("Standard dialect not found");
    }

    /**
     * Return the container element for some elements.
     * If the iteration element is a tr, td or th, return the parent table.
     * If the iteration element is a list item, return the list element.
     * Else, use the element itself.
     */
    public static Element getContainerElement(Element element) {
        String name = element.getOriginalName();
        if ("tr".equals(name) || "th".equals(name) || "td".equals(name)) {
            Element aux = (Element) element.getParent();
            while (!"table".equals(aux.getOriginalName()) && aux.getParent() != null) {
                aux = (Element) aux.getParent();
            }
            return aux;
        } else if ("li".equals(name)) {
            return (Element) element.getParent();
        } else {
            return element;
        }
    }

    /**
     * Return a property from an object via reflection.
     * @param propertyPath using dot notation
     * @return null if any field in path is null
     */
    public static Object getProperty(Object obj, String propertyPath) {
        if (obj == null) {
            return null;
        }
        String field, trail = null;
        int dotPos = propertyPath.indexOf('.');
        if (dotPos >= 0) {
            field = propertyPath.substring(0, dotPos);
            trail = propertyPath.substring(dotPos + 1);
        } else {
            field = propertyPath;
        }
        String methodName = "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
        try {
            Object result = obj.getClass().getMethod(methodName).invoke(obj);
            if (trail == null) {
                return result;
            } else {
                return getProperty(result, trail);
            }
        } catch (NoSuchMethodException ex) {
            throw new TemplateProcessingException("Sort field not found", ex);
        } catch (IllegalAccessException ex) {
            throw new TemplateProcessingException("Sort field not accesible", ex);
        } catch (InvocationTargetException ex) {
            throw new TemplateProcessingException("Error while getting sort field", ex);
        }
    }

    public static List convertToList(Object iterable) {
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

