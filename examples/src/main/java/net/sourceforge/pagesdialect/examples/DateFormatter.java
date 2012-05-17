package net.sourceforge.pagesdialect.examples;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.definition.expression.DRIValueFormatter;
import net.sourceforge.pagesdialect.TypeFormatter;

/**
 * Example of custom formatter. In this case, format a java.util.Date object.
 */
public class DateFormatter implements TypeFormatter<Date> {

    @Override
    public DRIValueFormatter<String, Date> getDRIValueFormatter(final Locale locale) {
        return new AbstractValueFormatter<String, Date>() {
            
            private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
            
            @Override
            public String format(Date value, ReportParameters params) {
                return dateFormat.format(value);
            }
        };
    }

    @Override
    public Class<Date> getValueClass() {
        return Date.class;
    }
}
