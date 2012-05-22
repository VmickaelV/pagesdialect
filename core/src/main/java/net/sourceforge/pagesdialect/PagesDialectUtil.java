package net.sourceforge.pagesdialect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
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
    
    /**
     * Look up StandardDialect prefix, usually "th".
     */
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
     * Else, return the element itself.
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
     * @param propertyPath using dot notation, as in, "product.category.name".
     * @return null if any field in path is null.
     */
    // FIXME: use ONGL or SPeL to get properties
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

    /**
     * Return a property class from an object via reflection.
     * @param propertyPath using dot notation, as in, "product.category.name".
     * @return null if any field in path is null.
     */
    // FIXME: use ONGL or SPeL to get properties
    // FIXME: remove duplicated code with previous method
    public static Class getPropertyClass(Object obj, String propertyPath) {
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
            Method method = obj.getClass().getMethod(methodName);
            if (trail == null) {
                return method.getReturnType();
            } else {
                return getPropertyClass(method.invoke(obj), trail);
            }
        } catch (NoSuchMethodException ex) {
            throw new TemplateProcessingException("Sort field not found", ex);
        } catch (IllegalAccessException ex) {
            throw new TemplateProcessingException("Sort field not accesible", ex);
        } catch (InvocationTargetException ex) {
            throw new TemplateProcessingException("Error while getting sort field", ex);
        }
    }

    /** 
     * Return current date in format yyyy_MM_dd.
     */
    public static String now() {
        return new SimpleDateFormat("yyyy_MM_dd").format(new Date());
    }

    /** 
     * Simplifies a String removing all non alphanumeric data and replacing spaces by underscores.
     * @return empty String if the provided string is empty or null.
     */
    public static String simplifyString(String str) {
        StringBuilder sb = new StringBuilder();
        for (Character ch : str.toCharArray()) {
            if (Character.isLetterOrDigit(ch)) {
                sb.append(ch);
            } else if (ch == ' ') {
                sb.append("_");
            }
        }
        if (sb.length() == 0) {
            sb.append("_");
        }
        return sb.toString();
    }
}
