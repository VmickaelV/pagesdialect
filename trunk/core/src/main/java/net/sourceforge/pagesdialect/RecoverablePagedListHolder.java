package net.sourceforge.pagesdialect;

import java.util.List;
import org.springframework.beans.support.PagedListHolder;

/**
 * PagedListHolder that allows to recover original list.
 */
public class RecoverablePagedListHolder extends PagedListHolder {

    private List originalList;
    
    public RecoverablePagedListHolder(List originalList) {
        super(originalList);
        this.originalList = originalList;
    }

    public List getOriginalList() {
        return originalList;
    }
}
