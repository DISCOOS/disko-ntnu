package org.redcross.sar.data;

/**
 * This interface is used to translate.
 *
 * @author kennetgu
 *
 */

public interface ITranslator<S,T extends IData> {

	public S[] translate(T[] data);

}
