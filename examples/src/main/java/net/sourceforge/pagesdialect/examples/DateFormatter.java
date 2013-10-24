package net.sourceforge.pagesdialect.examples;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.definition.expression.DRIValueFormatter;
import net.sourceforge.pagesdialect.TypeFormatter;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Example of custom formatter. In this case, format a java.util.Date object.
 */
public class DateFormatter implements TypeFormatter<Date> {

    @Override
    public DRIValueFormatter<String, Date> getDRIValueFormatter(final HttpServletRequest request) {
        return new AbstractValueFormatter<String, Date>() {
            
            private DateFormat dateFormat;
            
            {
                Locale locale = RequestContextUtils.getLocale(request);
                this.dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
            }
            
            @Override
            public String format(Date date, ReportParameters params) {
                return dateFormat.format(date);
            }
        };
    }

    @Override
    public Class<Date> getValueClass() {
        return Date.class;
    }
}
