package net.sourceforge.pagesdialect;

import javax.servlet.http.HttpServletRequest;
import net.sf.dynamicreports.report.base.datatype.AbstractDataType;
import net.sf.dynamicreports.report.definition.expression.DRIValueFormatter;

public class DRIDataTypeAdapter<T> extends AbstractDataType<T, T> {

    private TypeFormatter<T> typeFormatter;
    private DRIValueFormatter<String, T> valueFormatter;
    
    public DRIDataTypeAdapter(TypeFormatter<T> typeFormatter, HttpServletRequest request) {
        this.typeFormatter = typeFormatter;
        this.valueFormatter = typeFormatter.getDRIValueFormatter(request);
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
