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

    // Configuration attributes to override default parameters.
    public static final String PAGE_PARAMETER = "pageParameter";
    public static final String PAGED_LIST_ATTR = "pagedListAttribute";
    public static final String SORT_PARAMETER = "sortParameter";
    public static final String SORT_TYPE_PARAMETER = "sortTypeParameter";
    
    // i18n keys. Can be overriden by configuration.
    public static final String I18N_ONE_RESULT = "pagesdialect.oneResult";
    public static final String I18N_RESULTS = "pagesdialect.results";
    public static final String I18N_PREVIOUS = "pagesdialect.previous";
    public static final String I18N_NEXT = "pagesdialect.next";
    public static final String I18N_PAGE = "pagesdialect.page";
    public static final String I18N_FIRST = "pagesdialect.first";
    public static final String I18N_LAST = "pagesdialect.last";
    public static final String I18N_NONE = "pagesdialect.none";

    private Map<String, String> properties = new HashMap<String, String>();
    
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
        Set<IProcessor> attrProcessors = new HashSet<IProcessor>();
        PaginateAttrProcessor paginateAttrProcessor = new PaginateAttrProcessor("paginate");
        paginateAttrProcessor.setDialect(this);
        attrProcessors.add(paginateAttrProcessor);
        SortAttrProcessor sortAttrProcessor = new SortAttrProcessor("sort");
        sortAttrProcessor.setDialect(this);
        attrProcessors.add(sortAttrProcessor);
        attrProcessors.add(new SeparateAttrProcessor("separate"));
        return attrProcessors;
    }
}
