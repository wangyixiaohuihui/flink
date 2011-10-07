package eu.stratosphere.sopremo.expressions;

import java.util.Iterator;

import eu.stratosphere.sopremo.EvaluationContext;
import eu.stratosphere.sopremo.jsondatamodel.ArrayNode;
import eu.stratosphere.sopremo.jsondatamodel.JsonNode;
import eu.stratosphere.sopremo.jsondatamodel.NullNode;

/**
 * Merges several arrays by taking the first non-null value for each respective array.
 * 
 * @author Arvid Heise
 */
@OptimizerHints(scope = Scope.ARRAY, transitive = true, minNodes = 1, maxNodes = OptimizerHints.UNBOUND, iterating = true)
public class ArrayMerger extends EvaluationExpression {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6884623565349727369L;

	@Override
	public JsonNode evaluate(final JsonNode node, final EvaluationContext context) {
		final Iterator<JsonNode> arrays = ((ArrayNode) node).iterator();
		final ArrayNode mergedArray = new ArrayNode();
		JsonNode nextNode;
		while (arrays.hasNext())
			if ((nextNode = arrays.next()) != NullNode.getInstance()) {

				final ArrayNode array = (ArrayNode) nextNode;
				for (int index = 0; index < array.size(); index++)
					if (mergedArray.size() <= index)
						mergedArray.add(array.get(index));
					else if (this.isNull(mergedArray.get(index)) && !this.isNull(array.get(index)))
						mergedArray.set(index, array.get(index));
			}
		return mergedArray;
	}

	private boolean isNull(final JsonNode value) {
		return value == null || value.isNull();
	}

	@Override
	protected void toString(final StringBuilder builder) {
		builder.append("[*]+...+[*]");
	}

}
