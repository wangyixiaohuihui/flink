package eu.stratosphere.sopremo.expressions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.EnumMap;
import java.util.Map;

import eu.stratosphere.sopremo.EvaluationContext;
import eu.stratosphere.sopremo.NumberCoercer;
import eu.stratosphere.sopremo.jsondatamodel.BigIntegerNode;
import eu.stratosphere.sopremo.jsondatamodel.DecimalNode;
import eu.stratosphere.sopremo.jsondatamodel.DoubleNode;
import eu.stratosphere.sopremo.jsondatamodel.IntNode;
import eu.stratosphere.sopremo.jsondatamodel.JsonNode;
import eu.stratosphere.sopremo.jsondatamodel.JsonNode.TYPES;
import eu.stratosphere.sopremo.jsondatamodel.LongNode;
import eu.stratosphere.sopremo.jsondatamodel.NumericNode;

/**
 * Represents all basic arithmetic expressions covering the addition, subtraction, division, and multiplication for
 * various types of numbers.
 * 
 * @author Arvid Heise
 */
@OptimizerHints(scope = Scope.NUMBER, minNodes = 2, maxNodes = 2, transitive = true)
public class ArithmeticExpression extends EvaluationExpression {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9103414139002479181L;

	private final ArithmeticExpression.ArithmeticOperator operator;

	private final EvaluationExpression op1, op2;

	/**
	 * Initializes Arithmetic with two {@link EvaluationExpression}s and an {@link ArithmeticOperator} in infix
	 * notation.
	 * 
	 * @param op1
	 *        the first operand
	 * @param operator
	 *        the operator
	 * @param op2
	 *        the second operand
	 */
	public ArithmeticExpression(final EvaluationExpression op1, final ArithmeticOperator operator,
			final EvaluationExpression op2) {
		this.operator = operator;
		this.op1 = op1;
		this.op2 = op2;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || this.getClass() != obj.getClass())
			return false;
		return this.op1.equals(((ArithmeticExpression) obj).op1)
			&& this.operator.equals(((ArithmeticExpression) obj).operator)
			&& this.op2.equals(((ArithmeticExpression) obj).op2);
	}

	@Override
	public JsonNode evaluate(final JsonNode node, final EvaluationContext context) {
		return this.operator.evaluate((NumericNode) this.op1.evaluate(node, context),
			(NumericNode) this.op2.evaluate(node, context));
	}

	@Override
	public int hashCode() {
		return ((59 + this.op1.hashCode()) * 59 + this.operator.hashCode()) * 59 + this.op2.hashCode();
	}

	@Override
	protected void toString(final StringBuilder builder) {
		builder.append(this.op1);
		builder.append(' ');
		builder.append(this.operator);
		builder.append(' ');
		builder.append(this.op2);
	}

	/**
	 * Closed set of basic arithmetic operators.
	 * 
	 * @author Arvid Heise
	 */
	public static enum ArithmeticOperator {
		/**
		 * Addition
		 */
		ADDITION("+", new IntegerEvaluator() {
			@Override
			protected int evaluate(final int left, final int right) {
				return left + right;
			}
		}, new LongEvaluator() {
			@Override
			protected long evaluate(final long left, final long right) {
				return left + right;
			}
		}, new DoubleEvaluator() {
			@Override
			protected double evaluate(final double left, final double right) {
				return left + right;
			}
		}, new BigIntegerEvaluator() {
			@Override
			protected BigInteger evaluate(final BigInteger left, final BigInteger right) {
				return left.add(right);
			}
		}, new BigDecimalEvaluator() {
			@Override
			protected BigDecimal evaluate(final BigDecimal left, final BigDecimal right) {
				return left.add(right);
			}
		}),
		/**
		 * Subtraction
		 */
		SUBTRACTION("-", new IntegerEvaluator() {
			@Override
			protected int evaluate(final int left, final int right) {
				return left - right;
			}
		}, new LongEvaluator() {
			@Override
			protected long evaluate(final long left, final long right) {
				return left - right;
			}
		}, new DoubleEvaluator() {
			@Override
			protected double evaluate(final double left, final double right) {
				return left - right;
			}
		}, new BigIntegerEvaluator() {
			@Override
			protected BigInteger evaluate(final BigInteger left, final BigInteger right) {
				return left.subtract(right);
			}
		}, new BigDecimalEvaluator() {
			@Override
			protected BigDecimal evaluate(final BigDecimal left, final BigDecimal right) {
				return left.subtract(right);
			}
		}),
		/**
		 * Multiplication
		 */
		MULTIPLICATION("*", new IntegerEvaluator() {
			@Override
			protected int evaluate(final int left, final int right) {
				return left * right;
			}
		}, new LongEvaluator() {
			@Override
			protected long evaluate(final long left, final long right) {
				return left * right;
			}
		}, new DoubleEvaluator() {
			@Override
			protected double evaluate(final double left, final double right) {
				return left * right;
			}
		}, new BigIntegerEvaluator() {
			@Override
			protected BigInteger evaluate(final BigInteger left, final BigInteger right) {
				return left.multiply(right);
			}
		}, new BigDecimalEvaluator() {
			@Override
			protected BigDecimal evaluate(final BigDecimal left, final BigDecimal right) {
				return left.multiply(right);
			}
		}),
		/**
		 * Division
		 */
		DIVISION("/", DivisionEvaluator.INSTANCE, DivisionEvaluator.INSTANCE, new DoubleEvaluator() {
			@Override
			protected double evaluate(final double left, final double right) {
				return left / right;
			}
		}, DivisionEvaluator.INSTANCE, DivisionEvaluator.INSTANCE);

		private final String sign;

		private final Map<JsonNode.TYPES, NumberEvaluator> typeEvaluators = new EnumMap<JsonNode.TYPES, NumberEvaluator>(
			JsonNode.TYPES.class);

		private ArithmeticOperator(final String sign, final NumberEvaluator integerEvaluator,
				final NumberEvaluator longEvaluator,
				final NumberEvaluator doubleEvaluator, final NumberEvaluator bigIntegerEvaluator,
				final NumberEvaluator bigDecimalEvaluator) {
			this.sign = sign;
			this.typeEvaluators.put(JsonNode.TYPES.IntNode, integerEvaluator);
			this.typeEvaluators.put(JsonNode.TYPES.LongNode, longEvaluator);
			this.typeEvaluators.put(JsonNode.TYPES.DoubleNode, doubleEvaluator);
			this.typeEvaluators.put(JsonNode.TYPES.BigIntegerNode, bigIntegerEvaluator);
			this.typeEvaluators.put(JsonNode.TYPES.DecimalNode, bigDecimalEvaluator);
		}

		/**
		 * Performs the binary operation on the two operands after coercing both values to a common number type.
		 * 
		 * @param left
		 *        the left operand
		 * @param right
		 *        the right operand
		 * @return the result of the operation
		 */
		public NumericNode evaluate(final NumericNode left, final NumericNode right) {
			final TYPES widerType = NumberCoercer.INSTANCE.getWiderType(left,
				right);
			return this.typeEvaluators.get(widerType).evaluate(left, right);
		}

		@Override
		public String toString() {
			return this.sign;
		}
	}

	private abstract static class BigDecimalEvaluator implements NumberEvaluator {
		protected abstract BigDecimal evaluate(BigDecimal left, BigDecimal right);

		@Override
		public NumericNode evaluate(final NumericNode left, final NumericNode right) {
			return DecimalNode.valueOf(this.evaluate(left.getDecimalValue(),
				right.getDecimalValue()));
		}
	}

	private abstract static class BigIntegerEvaluator implements NumberEvaluator {
		protected abstract BigInteger evaluate(BigInteger left, BigInteger right);

		@Override
		public NumericNode evaluate(final NumericNode left, final NumericNode right) {
			return BigIntegerNode.valueOf(this.evaluate(left.getBigIntegerValue(),
				right.getBigIntegerValue()));
		}
	}

	/**
	 * Taken from Groovy's org.codehaus.groovy.runtime.typehandling.BigDecimalMath
	 * 
	 * @author Arvid Heise
	 */
	static class DivisionEvaluator implements NumberEvaluator {
		private static final DivisionEvaluator INSTANCE = new DivisionEvaluator();

		// This is an arbitrary value, picked as a reasonable choice for a precision
		// for typical user math when a non-terminating result would otherwise occur.
		public static final int DIVISION_EXTRA_PRECISION = 10;

		// This is an arbitrary value, picked as a reasonable choice for a rounding point
		// for typical user math.
		public static final int DIVISION_MIN_SCALE = 10;

		@Override
		public NumericNode evaluate(final NumericNode left, final NumericNode right) {
			return DecimalNode.valueOf(divideImpl(left.getDecimalValue(),
				right.getDecimalValue()));
		}

		public static BigDecimal divideImpl(final BigDecimal bigLeft, final BigDecimal bigRight) {
			try {
				return bigLeft.divide(bigRight);
			} catch (final ArithmeticException e) {
				// set a DEFAULT precision if otherwise non-terminating
				final int precision = Math.max(bigLeft.precision(), bigRight.precision()) + DIVISION_EXTRA_PRECISION;
				BigDecimal result = bigLeft.divide(bigRight, new MathContext(precision));
				final int scale = Math.max(Math.max(bigLeft.scale(), bigRight.scale()), DIVISION_MIN_SCALE);
				if (result.scale() > scale)
					result = result.setScale(scale, BigDecimal.ROUND_HALF_UP);
				return result;
			}
		}
	}

	private abstract static class DoubleEvaluator implements NumberEvaluator {
		protected abstract double evaluate(double left, double right);

		@Override
		public NumericNode evaluate(final NumericNode left, final NumericNode right) {
			return DoubleNode.valueOf(this.evaluate(left.getDoubleValue(),
				right.getDoubleValue()));
		}
	}

	private abstract static class IntegerEvaluator implements NumberEvaluator {
		protected abstract int evaluate(int left, int right);

		@Override
		public NumericNode evaluate(final NumericNode left, final NumericNode right) {
			return IntNode.valueOf(this.evaluate(left.getIntValue(), right.getIntValue()));
		}
	}

	private abstract static class LongEvaluator implements NumberEvaluator {
		protected abstract long evaluate(long left, long right);

		@Override
		public NumericNode evaluate(final NumericNode left, final NumericNode right) {
			return LongNode.valueOf(this.evaluate(left.getLongValue(), right.getLongValue()));
		}
	}

	private static interface NumberEvaluator {
		public NumericNode evaluate(NumericNode left, NumericNode right);
	}
}