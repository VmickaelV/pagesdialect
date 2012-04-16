package org.thymeleaf.pagesdialect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.thymeleaf.dialect.AbstractXHTMLEnabledDialect;
import org.thymeleaf.processor.IProcessor;

/**
 * Custom Thymeleaf dialect with some pagination utilities.
 */
public class PagesDialect extends AbstractXHTMLEnabledDialect {

    // Default values. Can be overriden by configuration.
    public static final String PAGE_PARAMETER = "pageParameter";
    public static final String PAGED_LIST_ATTR = "pagedListAttribute";
    public static final String I18N_ONE_RESULT = "pagesdialect.oneResult";
    public static final String I18N_RESULTS = "pagesdialect.results";
    public static final String I18N_PREVIOUS = "pagesdialect.previous";
    public static final String I18N_NEXT = "pagesdialect.next";
    public static final String I18N_PAGE = "pagesdialect.page";
    public static final String I18N_FIRST = "pagesdialect.first";
    public static final String I18N_LAST = "pagesdialect.last";
    public static final String I18N_NONE = "pagesdialect.none";

    private Map<String, String> properties = new HashMap<String, String>();
    
    private final Set<IProcessor> attrProcessors = new HashSet<IProcessor>();
    
    public PagesDialect() {
        PaginateAttrProcessor paginateAttrProcessor = new PaginateAttrProcessor("paginate");
        paginateAttrProcessor.setDialect(this);
        attrProcessors.add(paginateAttrProcessor);
        attrProcessors.add(new SeparateAttrProcessor("separate"));
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String getPrefix() {
        return "pages";
    }

    @Override
    public boolean isLenient() {
        return false;
    }

    @Override
    public Set<IProcessor> getProcessors() {
        return attrProcessors;
    }
}
