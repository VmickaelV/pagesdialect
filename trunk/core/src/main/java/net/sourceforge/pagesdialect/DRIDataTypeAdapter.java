package net.sourceforge.pagesdialect;

import java.util.Locale;
import net.sf.dynamicreports.report.base.datatype.AbstractDataType;
import net.sf.dynamicreports.report.definition.expression.DRIValueFormatter;

public class DRIDataTypeAdapter<T> extends AbstractDataType<T, T> {

    private TypeFormatter<T> typeFormatter;
    private DRIValueFormatter<String, T> valueFormatter;
    
    public DRIDataTypeAdapter(TypeFormatter<T> typeFormatter, Locale locale) {
        this.typeFormatter = typeFormatter;
        this.valueFormatter = typeFormatter.getDRIValueFormatter(locale);
    }

    @Override
    public DRIValueFormatter<String, T> getValueFormatter() {
        return this.valueFormatter;
    }
    
    @Override
    public Class<T> getValueClass() {
        return this.typeFormatter.getValueClass();
    }
}
