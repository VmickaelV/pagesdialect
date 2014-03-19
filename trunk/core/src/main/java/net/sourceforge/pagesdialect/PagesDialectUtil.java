package net.sourceforge.pagesdialect;

import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.dom.Element;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.util.MessageResolutionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility methods for PagesDialect.
 */
public class PagesDialectUtil {

    private static final String SPRING_STANDARD_DIALECT_CLASSNAME = "SpringStandardDialect";
    private static final String STANDARD_DIALECT_CLASSNAME = "StandardDialect";

    /**
     * Look up StandardDialect prefix, usually "th".
     */
    public static String getStandardDialectPrefix(Arguments arguments) {
        Map<String, IDialect> dialectMap = arguments.getConfiguration().getDialects();
        for (Entry<String, IDialect> dialectEntry : dialectMap.entrySet()) {
            IDialect dialect = dialectEntry.getValue();
            String dialectName = dialect.getClass().getSimpleName();
            if (dialectName.equals(SPRING_STANDARD_DIALECT_CLASSNAME) || 
                dialectName.equals(STANDARD_DIALECT_CLASSNAME)) {
                return dialectEntry.getKey();
            }
        }
        throw new TemplateProcessingException("Standard dialect not found");
    }

    /**
     * Return the container element for some elements. If the iteration element is a tr, td or th, return the parent
     * table. If the iteration element is a list item, return the list element. Else, return the element itself.
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
     *
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
        Object result = invokeMethod(obj, field);
        if (trail == null) {
            return result;
        } else {
            return getProperty(result, trail);
        }
    }

    private static Object invokeMethod(Object obj, String fieldName) {
        try {
            // First try getXXX
            String methodName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            if (hasMethod(obj.getClass(), methodName)) {
                return obj.getClass().getMethod(methodName).invoke(obj);
            }
            // Then try isXXX
            methodName = "is" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            if (hasMethod(obj.getClass(), methodName)) {
                return obj.getClass().getMethod(methodName).invoke(obj);
            }
            throw new TemplateProcessingException("Field not found for field " + fieldName);
        } catch (NoSuchMethodException ex) {
            throw new TemplateProcessingException("Field not found", ex);
        } catch (IllegalAccessException ex) {
            throw new TemplateProcessingException("Field not accesible", ex);
        } catch (InvocationTargetException ex) {
            throw new TemplateProcessingException("Error while getting field", ex);
        }
    }

    private static boolean hasMethod(Class clazz, String methodName) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterTypes().length == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return a property class from an object via reflection.
     *
     * @param propertyPath using dot notation, as in, "product.category.name".
     * @return null if any field in path is null.
     */
    // FIXME: use ONGL or SPeL to get properties
    // FIXME: remove duplicated code with previous method
    public static Class getPropertyClass(Class parentClass, String propertyPath) {
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
            Method method = parentClass.getMethod(methodName);
            if (trail == null) {
                return method.getReturnType();
            } else {
                return getPropertyClass(method.getReturnType(), trail);
            }
        } catch (NoSuchMethodException ex) {
            throw new TemplateProcessingException("Field not found", ex);
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
     *
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

    /**
     * Process a Thymeleaf expression if it follows the pattern ${...}. If not, return the same expression.
     *
     * This is useful to avoid that attribute expressions need quotes, i.e., th:toggle="myID" instead of
     * th:toggle="'myID'"
     */
    public static Object expressionValue(Arguments arguments, String expression) {
        if (isExpression(expression)) {
            Configuration configuration = arguments.getConfiguration();
            IStandardExpressionParser expressionParser = StandardExpressions.getExpressionParser(configuration);
            IStandardExpression standardExpression = expressionParser.parseExpression(configuration, arguments, expression);
            return standardExpression.execute(configuration, arguments);
        } else {
            return expression;
        }
    }

    public static boolean isExpression(String expression) {
        return expression.startsWith("${") && expression.endsWith("}")
                || expression.startsWith("@{") && expression.endsWith("}")
                || expression.startsWith("#{") && expression.endsWith("}");
    }

    public static String templateMessage(Arguments arguments, String messageKey, String... params) {
        String templateMessage = MessageResolutionUtils.resolveMessageForTemplate(arguments, messageKey, params, false);
        if (templateMessage != null) {
            return templateMessage;
        }
        String processorMessage = MessageResolutionUtils.resolveMessageForClass(
                arguments.getConfiguration(), PagesDialect.class,
                arguments.getContext().getLocale(), messageKey, params, false);
        if (processorMessage != null) {
            return processorMessage;
        }
        return MessageResolutionUtils.getAbsentMessageRepresentation(
                messageKey, arguments.getContext().getLocale());
    }
}
