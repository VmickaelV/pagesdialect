package net.sourceforge.pagesdialect;

import java.util.Locale;
import net.sf.dynamicreports.report.definition.expression.DRIValueFormatter;

/**
 * Allow custom formatting for exporting using locale.
 * 
 * It also will be used in sorting if the type is not Comparable.
 */
public interface TypeFormatter<T> {
    
    DRIValueFormatter<String, T> getDRIValueFormatter(Locale locale);
    
    Class<T> getValueClass();
}
