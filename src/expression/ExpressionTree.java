package expression;

import java.math.BigInteger;
import java.security.InvalidParameterException;

/**
 * A placeholder for a tree node.
 */
class Node{
    BigInteger value;
    /**
     * Are there parentheses around the arithmetical expression of the tree having this Node as a root.
     */
    boolean isInParentheses;
    Node left;
    Node right;

    /**
     * this.value == null, inParentheses = false, this.left == null, this.right == null.
     */
    Node(){
        this(null);
    }

    /**
     * inParentheses == false, this.left == null, this. right == null.
     * @param value It is directly assigned to this.value.
     */
    Node(BigInteger value) {
        this.value = value;
        this.isInParentheses = false;
        this.left = null;
        this.right = null;
    }
}

//TODO fix if needed when you see what the grammar is producing.
//TODO CHECK WHICH SIDE OF / IS USED FOR DIVIDING IN THE WHOLE CODE.
//TODO check tree terminology.
/**
 * Binary tree representing arithmetical expression with +, -, *, /, (,), produced by the grammar G = <N, T, Expr, P>
 * with P (grammar's rules):
 *      Num -> 0 | ... | 9 | 0Num | 1Num | ... | 9Num,
 *      Expr -> Expr + Term | Expr - Term | Term,
 *      Term -> Term * Factor | Term / Factor | Factor,
 *      Factor -> Num | (Expr).
 *
 * We are going to call that grammar the tree's grammar.
 * Every inner node (not leaf) is operator and every leaf is natural number which is not limited.
 */
public class ExpressionTree {
    private Node root;
    /**
     * treeDepth contains the depth of the expression tree.
     * treeDepth == 1, when the root of the tree is a leaf.
     */
    // private long treeDepth; // It is currently not in use.

    // Operators' constants.
    private static final int PLUS = -1;
    private static final int MINUS = -2;
    private static final int MULT = -3;
    // private static final int DIV = -4; // It is currently not in use.

    /**
     * @param expr String representation of an arithmetical expression from the tree's grammar.
     * @throws InvalidParameterException if expr is null or if expr is not part of the tree grammar.
     */
    public ExpressionTree(String expr){
        if(isExprValid(expr)) {
            this.root = new Node();
            // Removing all white characters.
            expr = expr.replaceAll("\\s","");
            StringBuilder builder = new StringBuilder(expr);
            this.parseExpression(builder, this.root);
            // this.treeDepth = findTreeDepth(root);
        }
    }

    /**
     * @param expr String representation of arithmetical value.
     * @return Always true if exception is not thrown before reaching return statement (i.e. when expr is not valid).
     * @throws InvalidParameterException with a respective message if expr is null or if expr is not part of the tree's
     *                                   grammar.
     */
    private boolean isExprValid(String expr){
        if(expr == null) {
            throw new InvalidParameterException("String expr cannot be null.");
        }
        // Removing all white characters.
        expr = expr.replaceAll("\\s","");
        if(expr.length() == 0){
            throw new InvalidParameterException("Empty string is not part of the grammar.");
        }
        //TODO check if expr is part of the grammar
        return true;
    }

    // TODO Check if 1.2. is actually possible in the actual grammar.
    /**
     * The expression tree is built using the following algorithm:
     * 1. Finding the last operator in the expression (not in parentheses) with the smallest priority because it should
     * be applied the last. That's because we are evaluating the tree expression from the bottom to the top of the tree.
     *      1.1. The expression can be in parentheses, so for ease we remove them at the beginning.
     *      1.2. The expression can end with operand in parentheses (like 1+2*(2+3)). We start to search the needed
     *      operand from right to left starting from the index before '(' (in the example that index is 3).
     *      (The priority of the operators in ascending order is:
     *             1. +, -,
     *             2. *,/,
     *             3. ().
     *       )
     * 2. After finding the operator with smallest priority, we split the expression into three parts:
     *      1. - left operand,
     *      2. - operator.
     *      3. - right operand.
     *    In the root node is saved information about the operator. We are applying the algorithm recursively for the
     *    left operand and for the right operand and we save that information respectively in the root.left Node and in
     *    the root.right Node.
     *
     * @param builder Holds the information about the current expression. Used as an alternative of the class String,
     *                because there's no need of many substring of the initial expression to be saved in the string
     *                pool.
     * @param root Node which is the root of the current expression.
     */
    private void parseExpression(StringBuilder builder, Node root){
        if(isExprInParentheses(builder)){
            // Removing the parentheses.
            builder = new StringBuilder(builder.subSequence(1, builder.length() - 1));
            root.isInParentheses = true;
        }

        int startIndex = builder.length() - 1;
        if(builder.charAt(builder.length() - 1) == ')'){
            startIndex = skipParenthesesAtTheEnd(builder);
        }
        int lastOperatorIndex = findOperatorAfter(builder, startIndex);

        if(lastOperatorIndex == -1){
            // In the grammar а NUM with more that one digit can start with '0', but BigInteger still evaluates it
            // correctly so no additional checks are added here.
            root.value = new BigInteger(builder.toString());
        }
        else{
            root.value = getOperatorsIntValue(builder.charAt(lastOperatorIndex));

            root.left = new Node();
            root.right = new Node();

            StringBuilder left = new StringBuilder(builder.subSequence(0, lastOperatorIndex));
            StringBuilder right = new StringBuilder(builder.subSequence(lastOperatorIndex + 1, builder.length()));

            parseExpression(left, root.left);
            parseExpression(right, root.right);
        }
    }

    /**
     * Checks if the expression in builder is surrounded by parentheses (like "(expr)").
     * @param builder Contains the current arithmetical expression.
     * @return If the expression in builder is surrounded by parentheses.
     */
    private boolean isExprInParentheses(StringBuilder builder){
        int len = builder.length();
        // len cannot be 0 in the current implementation, but the check is here for safety reasons.
        if(len == 0){
            return false;
        }

        if(builder.charAt(0) != '(' ||  builder.charAt(len - 1) != ')'){
            return false;
        }

        int parenthesesCounter = 0;
        int i = 0;
        for(; i < len; i++)
        {
            if(builder.charAt(i) == '('){
                parenthesesCounter++;
            }
            else if(builder.charAt(i) == ')'){
                parenthesesCounter--;
            }

            if(parenthesesCounter == 0) {
                break;
            }
        }
        return i == len - 1;
    }

    /**
     * Finds the index of the character before the last parentheses in the expression in builder.
     * @param builder Contains the current arithmetical expression.
     * @return Index of the character before the last parentheses. Returns builder.length() - 1 if there are no
     *         parentheses at the end of the expression.
     */
    private int skipParenthesesAtTheEnd(StringBuilder builder){
        int indexBeforeParentheses = builder.length() - 1;

        if(builder.charAt(builder.length() - 1) == ')'){
            int openedParentheses = 0;

            for(int i = builder.length() - 1; i >= 0; i--) {
                if (builder.charAt(i) == ')') {
                    openedParentheses++;
                } else if (builder.charAt(i) == '(') {
                    openedParentheses--;
                }

                if (openedParentheses == 0) {
                    indexBeforeParentheses = i - 1;
                    break;
                }
            }
        }

        return indexBeforeParentheses;
    }

    /**
     * Finds the index of the last operator in the expression with the lowest priority.
     * @param builder Contains the current arithmetical expression.
     * @param startIndex The last char index in builder that outside of parentheses.
     * @return Index of the last operator with lowest priority from the actual ones in the expression in builder.
     * If there are no operators, returns -1.
     */
    private int findOperatorAfter(StringBuilder builder, int startIndex){
        // If + and - are not found, we want the last * or / operator.
        int firstMultOrDivIndex = -1;
        // Shows if the char of the current index is inside of parentheses.
        boolean inParentheses = false;

        for(int i = startIndex; i >= 0; i--)
        {
            int ch = builder.charAt(i);

            if((ch == '+' || ch == '-') && !inParentheses){
                return i;
            }
            else if((ch == '*' || ch == '/') && firstMultOrDivIndex == -1 && !inParentheses){
                firstMultOrDivIndex = i;
            }
            else if(ch == ')'){
                inParentheses = true;
            }
            else if(ch == '('){
                inParentheses = false;
            }
        }

        // At this point it means that no + or - signs outside of parentheses are found.
        return firstMultOrDivIndex;
    }

    /**
     * Finds the pre mapped negative integer value for the specified operator.
     * @param operator A operator character, one of in the following: '+', '-, '*', '\'.
     * @return Pre mapped for the operator negative integer.
     * @throws InvalidParameterException If operator is not one of the characters listed above.
     */
    private BigInteger getOperatorsIntValue(char operator){
        switch (operator) {
            case '+' -> {
                // Equals to the int constant PLUS.
                BigInteger posVal = BigInteger.valueOf(1);
                return posVal.negate();
            }
            case '-' -> {
                // Equals to the int constant MINUS.
                BigInteger posVal = BigInteger.valueOf(2);
                return posVal.negate();
            }
            case '*' -> {
                // Equals to the int constant MULT.
                BigInteger posVal = BigInteger.valueOf(3);
                return posVal.negate();
            }
            case '/' -> { // case '/'
                // Equals to the int constant DIV.
                BigInteger posVal = BigInteger.valueOf(4);
                return posVal.negate();
            }
            default -> throw new InvalidParameterException("Operator must be one of the following: +, -, *, /. " +
                    "Given: " + operator + '.');
        }
    }

    /**
     * Finds the depth of the expression tree.
     * @return The depth of the expression tree. The tree's depth is 1, when the root of the tree is a leaf.
     */
    public long treeDepth(){
        return recursiveTreeDepth(this.root);
    }

    /**
     * Finds the depth of the expression tree with the specified root.
     * @param root Node representing current expression tree's root.
     * @return Depth of the tree having the specified root.
     */
    private long recursiveTreeDepth(Node root){
        if(root == null){
            return 0;
        }
        if(root.left == null && root.right == null){
            return 1;
        }
        return 1 + Math.max(recursiveTreeDepth(root.left), recursiveTreeDepth(root.right));
    }

    /**
     * Evaluates the tree's arithmetical expression.
     * @return The value of the tree's arithmetical expression.
     */
    public BigInteger evaluate(){
        return recursiveEvaluate(this.root);
    }

    /**
     * Evaluates the arithmetic value of the expression tree with the specified root.
     * @param root Node representing current tree's root.
     * @return The arithmetical expression value of the tree having the specified root.
     */
    private BigInteger recursiveEvaluate(Node root) {
        if(root.value.signum() != -1){
            return root.value;
        }
        else{
            BigInteger left =  recursiveEvaluate(root.left);
            BigInteger right = recursiveEvaluate(root.right);

            int intOperator = root.value.intValue();
            switch (intOperator){
                case PLUS ->{
                    return left.add(right);
                }
                case MINUS ->{
                    return left.subtract(right);
                }
                case MULT ->{
                    return left.multiply(right);
                }
                default ->{ // case DIV
                    return left.divide(right);
                }
            }
        }
    }

    /**
     * Prints the expression tree inorder.
     */
    public void printTree(){
        System.out.println("Tree's expression look:");
        recursivePrintTree(this.root);
        System.out.println();
    }

    /**
     * Prints the tree that is having the specified root inorder.
     * @param root Node representing current tree's root.
     */
    private void recursivePrintTree(Node root){
        if (root.value.signum() != -1) {
            System.out.print(root.value);
        } else {
            if(root.isInParentheses){
                System.out.print('(');
            }
            recursivePrintTree(root.left);

            int operator = root.value.intValue();
            switch (operator) {
                case PLUS -> System.out.print('+');
                case MINUS -> System.out.print('-');
                case MULT -> System.out.print('*');
                // case DIV
                default -> System.out.print('/');
            }

            recursivePrintTree(root.right);
            if(root.isInParentheses){
                System.out.print(')');
            }
        }
    }
}