package org.redcross.sar.util.mso;

import org.redcross.sar.mso.data.IMsoObjectIf;

/**
 *
 */
public interface Selector <T extends IMsoObjectIf>
{
    /**
     * Interface for selectors used for determining whether the object shall be selected (for reports etc) or not.
     *
     * @param anObject The object to test
     * @return <code>true</code> if the object shall be selected, <code>false</code> otherwise.
     */
    public boolean select(T anObject);
}
