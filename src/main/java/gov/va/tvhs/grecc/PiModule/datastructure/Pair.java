package gov.va.tvhs.grecc.PiModule.datastructure;

import java.io.Serializable;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author gobbelgt - Jul 25, 2017
 *
 * @param <L>
 *            - Type of the left side of the pair
 * @param <R>
 *            - Type of the right side of the pair
 */
public class Pair<L, R> implements Serializable
{
	private static final long serialVersionUID = 326876310603208098L;
	private int pairHashCode = 0;
	public final L left;
	public final R right;

	/**
	 * Constructor. Accepts two objects and constructs a pair. Neither can be null.
	 *
	 * @param left
	 *            The left object
	 * @param right
	 *            The right object
	 */
	public Pair(L left, R right)
	{
		String errString = "Instance members of the Pair class must be non-null";
		Validate.notNull(left, errString);
		this.left = left;
		Validate.notNull(right, errString);
		this.right = right;
	}

	/**
	 * Returns true iff the the two pairs are equal.
	 *
	 * @param other
	 *            some other pair
	 *
	 * @return true if equal
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == this)
		{
			return true;
		}
		if (!(other instanceof Pair))
		{
			return false;
		}
		Pair<?, ?> that = (Pair<?, ?>) other;
		return new EqualsBuilder().append(this.left, that.left).append(this.right, that.right).isEquals();
	}

	/**
	 * Returns a hash code for this pair
	 *
	 * @return the hash code
	 */
	@Override
	public int hashCode()
	{
		if (this.pairHashCode != 0)
		{
			return this.pairHashCode;
		}
		this.pairHashCode = new HashCodeBuilder().append(this.left).append(this.right).toHashCode();
		return this.pairHashCode;
	}

	/**
	 * Returns the string form of this pair: "(left,right)"
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("(");
		sb.append(this.left.toString());
		sb.append(",");
		sb.append(this.right.toString());
		sb.append(")");
		return sb.toString();
	}
}
