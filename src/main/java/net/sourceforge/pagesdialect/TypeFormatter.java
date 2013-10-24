package net.sourceforge.pagesdialect;

import javax.servlet.http.HttpServletRequest;
import net.sf.dynamicreports.report.definition.expression.DRIValueFormatter;

/**
 * Allow custom formatting for exporting using locale.
 * 
 * It also will be used in sorting if the type is not Comparable.
 */
public interface TypeFormatter<T> {
    
    DRIValueFormatter<String, T> getDRIValueFormatter(HttpServletRequest request);
    
    Class<T> getValueClass();
}
