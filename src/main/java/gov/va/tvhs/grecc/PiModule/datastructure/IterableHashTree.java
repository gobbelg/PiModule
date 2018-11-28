package gov.va.tvhs.grecc.PiModule.datastructure;

import java.util.List;

/**
 * Extends the Iterable interface of the type Pair where the left side of the
 * pair is a list of String objects and the right side is of type V. Provides a
 * common interface for implementations of HashTress that can iterate over the
 * tree and iteratively return lists of strings within the tree.
 *
 * @author gobbelgt - Jul 25, 2017
 *
 * @param <V>
 *            - V is any type that extends HashTree of type V
 */
public interface IterableHashTree<V extends HashTree<V>> extends Iterable<Pair<List<String>, V>>
{

}
