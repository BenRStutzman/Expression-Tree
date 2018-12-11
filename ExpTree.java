import java.util.Stack;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
* This class creates a binary expression tree from a mathematical expression given by the user.
* It accepts prefix, postfix, and infix notations, with certain limitations.
* It can create the tree, evaluate the expression, print a graphical representation of the tree,
* and give instructions for evaluating the expression in paragraph form.
* The main method combines all of these functions, but the functions can be used individually as well.
*/
public class ExpTree {
    
    //Node class for the tree, stores an operation and/or value and left and right children
    private class ExpTreeNode {
        public char op;
        public int val;
        public ExpTreeNode left;
        public ExpTreeNode right;
        
        //Constructors
        ExpTreeNode(int n) {
            val = n;
            op = ' ';
            left = null;
            right = null;
        }
        
        ExpTreeNode(char c, ExpTreeNode le, ExpTreeNode ri) {
            val = 0;
            op = c;
            left = le;
            right = ri;
        }
        
        boolean isLeaf() { return op == ' '; }
        
        public String toString() {
            if (isLeaf()) return Integer.toString(val);
            return Character.toString(op);
        }
    }
    
    //Used for iterating through prefix, just wraps an integer so it can keep increasing
    private class Position {
        public int val;
        
        //Constructors
        Position(int i) { val = i; }
        
        Position() { val = 0; }
    }
    
    private ExpTreeNode root;
    private boolean validExp = true; //turned false if some method realizes the input is invalid
    private boolean allDrawn = true; //turned false if not everything fits in the drawing
    private boolean evaluated = false; //turned true once it's evaluated
    private boolean overflow = false; //turned true if there's an integer overflow
    private String NotationDet; //initialized when the program decides what notation the input uses
    
    //Constructors
    ExpTree(String exp) { fill(exp); } //to fill directly upon creation
    
    ExpTree() { } //to make an empty tree
    
    //checks whether a character is one of the 6 valid operators
    boolean isOp(String c) {
        return (c.length() == 1 && "+-/*^%".contains(c));
    }
    
    boolean isOp(char c) {
        return isOp(Character.toString(c));
    }
    
    //determines whether a string is in prefix notation, provided it's made up of valid characters
    private boolean isPrefix(String input) { return isOp(input.charAt(0)) | input.matches("\\d+"); }
    
    //determines whether a string is in postfix notation, provided it's made up of valid characters
    private boolean isPostfix(String input) { return isOp(input.charAt(input.length() - 1)); }
    
    //determines whether a string is in infix notation, provided it's made up of valid characters
    private boolean isInfix(String input) { return (!input.contains(" ")); }
    
    //Note: the program might realize while it's converting to prefix or filling the tree that the input is
    //in fact invalid, even if one of the above methods said it was in their notation.
    
    //Converts postfix to prefix notation
    //Algorithm adapted from GeeksForGeeks
    private String[] postToPre(String postfixString) {
        String[] postfix = postfixString.split("\\s+");
        Stack stack = new Stack();
        for (String symbol : postfix) {
            if (isOp(symbol)) {
                if (stack.isEmpty()) {
                    validExp = false;
                    return null;
                }
                String num1 = (String) stack.pop();
                if (stack.isEmpty()) {
                    validExp = false;
                    return null;
                }
                String num2 = (String) stack.pop();
                stack.push(symbol + " " + num2 + " " + num1);
            } else stack.push(symbol);
        }
        if (stack.isEmpty()) {
            validExp = false;
            return null;
        }
        String[] prefix = ((String)stack.pop()).split("\\s+");
        if (stack.isEmpty()) return prefix;
        else {
            validExp = false;
            return null;
        }
    } 
    
    //Used for turning infix to postfix. Determines precedence of operations, or returns 0 if op is unknown.
    private int prec(String op) {
        switch (op) {
            case "+": return 1;
            case "-": return 1;
            case "*": return 2;
            case "/": return 2;
            case "%": return 2;
            case "^": return 3;
            default: return 0;
        }
    }
    
    //Algorithm adapted from GeeksForGeeks, used for eventually turning infix to prefix
    //It switches the order of same-precedence operations (like + and -) so they'll be in the correct
    //order when it's reversed to turn into infix.
    private String[] inToPost(String[] infix) {
        if (infix == null) return null;
        Stack stack = new Stack();
        ArrayList<String> postfix = new ArrayList<String>();
        for (String symbol : infix) {
            if (symbol.matches("\\d+")) postfix.add(symbol);
            else if (symbol.equals("(")) stack.push(symbol);
            else if (symbol.equals(")")) {
                // Output all operators since the last left paren:
                while (!stack.isEmpty()) {
                    if (stack.peek().equals("(")) break;
                    postfix.add((String) stack.pop());
                }
                if (!stack.isEmpty()) {
                    if (stack.pop().equals("(")) {
                        continue;
                    }
                }
                // No left paren in the stack:
                validExp = false;
                return null;
            } else if (isOp(symbol)) {
                int p = prec(symbol);
                // Pop and enqueue all ops in the stack
                // of equal or greater precedence:
                while (!stack.isEmpty()) {
                    if (p >= prec((String) stack.peek()) | stack.peek().equals("(")) break;
                    postfix.add((String) stack.pop());
                }
                stack.push(symbol); // Add the current op to the stack
            } else {
                validExp = false;
                return null;
            }
        }
        // Copy all the remaining operations to output:
        while (!stack.isEmpty()) postfix.add((String) stack.pop());
        return postfix.toArray(new String[0]);
    }
    
    //Used for turning infix to prefix; two reversals are required
    private String[] reverse(String[] forward) {
        if (forward == null) return null;
        int size = forward.length;
        String[] backward = new String[size];
        for (int i = size - 1; i >= 0; i--) {
            String symbol = forward[i];
            if (symbol.equals("(")) symbol = ")";
            else if (symbol.equals(")")) symbol = "(";
            backward[size - i - 1] = symbol;
        }
        return backward;
    }
    
    //Algorithm adapted from GeeksForGeeks
    private String[] inToPre(String input) {
        StringBuilder newInput = new StringBuilder();
        //puts spaces in so it's able to be split up into symbols
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isDigit(c)) newInput.append(c);
            else if (isOp(Character.toString(c))) newInput.append(" " + c + " ");
            else if (c == '(') newInput.append("( ");
            else if (c == ')') newInput.append(" )");
        }
        String[] infix = newInput.toString().split("\\s+");
        //the actual algorithm: reverse, change to postfix, and reverse again
        return reverse(inToPost(reverse(infix)));
    }
    
    //Detects notation of input and converts it to prefix
    private String[] toPrefix(String input) {
        if (input.length() == 0) {
            NotationDet = "No input detected.";
            return null;
        }
        if (isPrefix(input)) {
            NotationDet = "Input detected as prefix notation.";
            return input.split("\\s+");
        }
        if (isInfix(input)) {
            NotationDet = "Input detected as infix notation.";
            return inToPre(input);
        }
        if (isPostfix(input)) {
            NotationDet = "Input detected as postfix notation.";
            return postToPre(input);
        }
        validExp = false;
        return null;
    }
    
    //Recursively fills the tree with values and operators, using a prefix string and the Position wrapper
    private ExpTreeNode fill(String[] prefix, Position i){
        if (i.val >= prefix.length) {
            validExp = false;
            return null;
        }
        String symbol = prefix[i.val];
        i.val++;
        if (symbol.matches("\\d+") && !symbol.contains(".")) {
            try { return new ExpTreeNode(Integer.valueOf(symbol)); }
            catch (NumberFormatException e) { }
        } else if (isOp(symbol)) return new ExpTreeNode(symbol.charAt(0), fill(prefix, i), fill(prefix, i));
        validExp = false;
        return null;
    }
    
    //Checks the input to make sure it's valid, then converts to prefix and fills the tree
    public void fill (String input) {
        validExp = true;
        evaluated = false;
        overflow = false;
        for (char c : input.toCharArray()) {
            if ("0123456789+-*/^%() ".indexOf(c) < 0) {
                validExp = false;
                System.out.println("Input does not match prefix, infix, or postfix notation.");
                return;
            }
        }
        String[] prefix = toPrefix(input.trim());
        if (prefix == null) root = null;
        else {
            Position i = new Position();
            root = fill(prefix, i);
            if (i.val != prefix.length) {
                validExp = false;
                root = null;
            }          
        }
        if (validExp) System.out.println(NotationDet); //Says which notation it's in
        else System.out.println("Input does not match prefix, infix, or postfix notation.");
    }
    
    //Recursively gives the prefix notation of the expression by traversing the tree
    private String prefix(ExpTreeNode node) {
        if (node == null) return "";
        return node.toString() + " " + prefix(node.left) + prefix(node.right);
    }
    
    //Gives the prefix notation of the expression by traversing the tree
    public String prefix() {
        if (!validExp) return "Invalid expression; can't convert to prefix.";
        if (root == null) return "Tree is empty; nothing to see here.";
        return prefix(root).trim();
    }
    
    //Recursively gives the postfix notation of the expression by traversing the tree
    private String postfix(ExpTreeNode node) {
        if (node == null) return "";
        return postfix(node.left) + postfix(node.right) + " " + node.toString();
    }
    
    //Gives the postfix notation of the expression by traversing the tree
    public String postfix() { 
        if (!validExp) return "Invalid expression; can't convert to postfix.";
        if (root == null) return "Tree is empty; nothing to see here.";
        return postfix(root).trim();
    }
    
    //Recursively gives the infix notation of the expression by traversing the tree
    private String infix(ExpTreeNode node) {
        if (node == null) return "";
        if (node.op == ' ') return node.toString();
        return "(" + infix(node.left) + node.toString() + infix(node.right) + ")";
    }
    
    //Gives the prefix notation of the expression by traversing the tree
    public String infix() { 
        if (!validExp) return "Invalid expression; can't convert to infix.";
        if (root == null) return "Tree is empty; nothing to see here.";
        return infix(root).trim();
    }
    
    //Recursively evaluates the expression by traversing the tree and catches integer overflows
    private int eval(ExpTreeNode node, boolean printError) {
        if (node == null) return 0;
        int a = eval(node.left, printError);
        int b = eval(node.right, printError);
        if ((a == Integer.MAX_VALUE | b == Integer.MAX_VALUE) && overflow) {
            return node.val = Integer.MAX_VALUE;
        }
        switch (node.op) {
            case ' ': return node.val;
            case '+': 
                try { return node.val = Math.addExact(a,b); }
                catch (ArithmeticException e) {
                    if (printError) System.out.println("Integer overflow; can't evaluate.");
                    overflow = true;
                    return node.val = Integer.MAX_VALUE;
                }
            case '*':
                try { return node.val = Math.multiplyExact(a,b); }
                catch (ArithmeticException e) {
                    if (printError) System.out.println("Integer overflow; can't evaluate.");
                    overflow = true;
                    return node.val = Integer.MAX_VALUE;
                }
            case '-':
                try { return node.val = Math.addExact(a,-b); }
                catch (ArithmeticException e) {
                    if (printError) System.out.println("Integer overflow; can't evaluate.");
                    overflow = true;
                    return node.val = Integer.MAX_VALUE;
                }
            case '/':
                if (b == 0) {
                    System.out.println("Error: division by zero");
                    validExp = false;
                    return 0;
                }
                return node.val = a / b;
            case '%':
                if (b == 0) {
                    System.out.println("Error: division by zero");
                    validExp = false;
                    return 0;
                } else return node.val = a % b;
            case '^':
                node.val = (int) Math.pow(a, b);
                if (node.val == Integer.MAX_VALUE) {
                    if (printError) System.out.println("Integer overflow; can't evaluate.");
                    overflow = true;
                }
                return node.val;
            default:
                System.out.println("Unrecognized operator: " + node.op);
                return 0;
        }
    }
    
    //Evaluates the tree, printError is whether you want it to tell you if there's an error
    //(set to false, for example, if used inside of the instruct() method)
    private int eval(boolean printError) { 
        overflow = false;
        if (validExp) {
            evaluated = true;
            int result = eval(root, printError);
            if (result == Integer.MAX_VALUE) return 0;
            return result;
        }
        if (printError) System.out.println("Invalid expression; can't evaluate.");
        return 0;
    }
    
    //Evaluates the tree and tells you if there are errors
    public int eval() { return eval(true); }
    
    //Used for drawing the tree
    private String insertString(String original, String nodeString, int pos) {
        return original.substring(0,pos) + nodeString + original.substring(pos + nodeString.length());
    }
    
    //Recursively edits an array of strings (rows) to give a graphical description of the tree
    private String[] draw(ExpTreeNode node, String[] drawing, int level, int parPos, int dir) {
        if (node == null) return drawing;
        //Things that don't fit in the picture
        if (level > 4) {
            if (level == 5) {
                drawing[14] = insertString(drawing[14], "|", parPos - 1);
                drawing[15] = insertString(drawing[15], "X", parPos - 1);
            }
            allDrawn = false;
            return drawing;
        }
        //Some calculations for nice spacing
        int spread = 80 / ((int) Math.pow(2, level));
        int pos = (int) Math.floor(parPos + spread / 2.0 * dir) - 1;
        parPos--;
        //Inserts the actual value or operator
        if (pos + node.toString().length() > 80 | node.toString().length() > spread - 1) {
            drawing[level * 3 + 1] = insertString(drawing[level * 3 + 1], "X", pos);
            allDrawn = false;
        } else drawing[level * 3 + 1] = insertString(drawing[level * 3 + 1], node.toString(), pos);
        //Prints the value in parentheses next to operations
        if (evaluated && node.op != ' ') {
            String value = (node.val == Integer.MAX_VALUE && overflow) ? "(BIG)" : "(" + node.val + ")";
            if (pos + value.length() > 78 | value.length() > spread - 3) {
                drawing[level * 3 + 1] = insertString(drawing[level * 3 + 1], "X", pos + 2);
                allDrawn = false;
            } else drawing[level * 3 + 1] = insertString(drawing[level * 3 + 1], value, pos + 2);
        }
        //Builds the vertical and horizontal lines that give the tree structure
        if (level > 0) {
            if (dir == -1) {
                StringBuilder horizLine = new StringBuilder();
                for (int i = 0; i < spread - 1; i++) horizLine.append("_");
                drawing[level * 3 - 1] = insertString(drawing[level * 3 - 1], horizLine.toString(), pos + 1);
                drawing[level * 3 - 1] = insertString(drawing[level * 3 - 1], "|", parPos);
            }
            drawing[level * 3] = insertString(drawing[level * 3], "|", pos);
        }
        return draw(node.right, draw(node.left, drawing, level + 1, pos + 1, -1), level + 1, pos + 1, 1);
    }
    
    //Returns a string with the drawing of a tree, plus a message about whether it is completely drawn
    public String toString() {
        allDrawn = true;
        if (!validExp) return "Invalid expression; can't draw a tree.";
        if (root == null) return "Tree is empty; nothing to see here.";
        String[] drawing = new String[16];
        StringBuilder line = new StringBuilder();
        for (int j = 0; j < 80; j++) line.append(" ");
        String emptyLine = line.toString();
        for (int i = 0; i < drawing.length; i++) drawing[i] = emptyLine;
        String tree = String.join("\n", draw(root, drawing, 0, 0, 1));
        String message = allDrawn ? "Tree drawn successfully." :
            "Some parts of the tree don't fit in the drawing (indicated by Xs).";
        return (message + "\n" + tree).trim();
    }
    
    //Used for describing an operation in words
    private String opNoun(char c) {
        switch (c) {
            case ' ': return "value";
            case '+': return "addition result";
            case '-': return "subtraction result";
            case '*': return "multiplication result";
            case '/': return "division result";
            case '^': return "exponentiation result";
            case '%': return "modulus result";
            default: return "magical result";
        }
    }
    
    //Used for giving instructions
    private String operation(char c, ExpTreeNode a, ExpTreeNode b) {
        String aString = "the " + opNoun(a.op) + " " + a.val;
        String bString = "the " + opNoun(b.op) + " " + b.val;
        switch (c) {
            case '+': return "add " + aString + " and " + bString;
            case '-': return "subtract " + bString + " from " + aString;
            case '*': return "multiply " + aString + " and the " + bString;
            case '/': return "divide the " + aString + " by the " + bString;
            case '^': return "raise " + aString + " to the power of " + bString;
            case '%': return "take " + aString + " modulo " + bString;
            default: return "say abracadabra to " + aString + " and " + bString;
        }
    }
    
    //Used for giving ordered instructions
    private String ordinal(int i) {
        switch (i) {
            case 1: return "First";
            case 2: return "Second";
            case 3: return "Third";
            case 4: return "Fourth";
            case 5: return "Fifth";
            case 6: return "Sixth";
            case 7: return "Seventh";
            case 8: return "Eighth";
            case 9: return "Ninth";
            case 10: return "Tenth";
            default: return (new Random()).nextBoolean() ? "Next" : "Then";
        }
    }  
    
    //Recursively adds instructions to an array, using the evaluated tree
    private ArrayList<String> instruct(ExpTreeNode node, ArrayList<String> steps, Position i) {
        if (node == null) return steps;
        if (!node.isLeaf()) {
            steps = instruct(node.right, instruct(node.left, steps, i), i);
            StringBuilder step = new StringBuilder();
            if (node == root) {
                if (steps.size() == 0) step.append("All you have to do is ");
                else if (steps.size() == 1) step.append("Then, ");
                else step.append("Finally, ");
            }
            else step.append(ordinal(i.val) + ", ");
            step.append(operation(node.op, node.left, node.right));
            step.append(" to get " + Integer.toString(node.val) + ".");
            steps.add(step.toString());
            i.val++;
        }
        return steps;
    }
    
    //Returns a semi-normal-sounding paragraph describing how to evaluate the expression
    public String instruct() {
        if (!evaluated) this.eval(false);
        if (overflow) return "Integer overflow; I guess you've gotta do it by hand.";
        if (!validExp) return "Invalid expression; you're on your own, kid.";
        if (root == null) return "Take nothing and do nothing to it; you get nothing.";
        String[] steps = instruct(root, new ArrayList<String>(), new Position(1)).toArray(new String[0]);
        if (steps.length == 0) return "Take " + root.val + " and do nothing to it; you get " + root.val + ".";
        return String.join(" ", steps) + " The final result is " + Integer.toString(root.val) + ".";
    }
    
    //makes a tree and prints out a bunch of stuff all in a row, as required by the assignment
    public static void doItAll(String input) {
        ExpTree myExpTree = new ExpTree(input);
        System.out.println("\nPrefix:      " + myExpTree.prefix());
        System.out.println("Postfix:     " + myExpTree.postfix());
        System.out.println("Infix:       " + myExpTree.infix());
        System.out.print("\nFinal value: ");
        int result = myExpTree.eval();
        if (myExpTree.validExp && !myExpTree.overflow) System.out.println(result);
        System.out.println("\nDrawing:\n\n" + myExpTree);
        System.out.println("\nInstructions:\n\n" + myExpTree.instruct());
    }
    
    //Lets the user keep making trees in a loop until he/she gets bored
    public static void main(String[] args) {
        //Allows giving the tree an expression immediately from the command line
        if (args.length > 0) {
            System.out.println();
            doItAll(String.join(" ", args));
            return;
        }
        String details = "Enter a mathematical expression in prefix, postfix, or infix notation.\n" +
                         "The expression may only use integers from 0 to 2147483647\n" +
                         "and the operations +, -, *, /, ^, or %. The division operator is integer division.\n\n"+
                         "Prefix notation puts the operator before each pair of operands, as in\n" +
                         "'+ 1 1', which evaluates to 2. A longer example is\n" +
                         "+ * + * 8 7 4 5 * + 2 2 * 3 7         which evaluates to 384.\n" +
                         "Please USE SPACES between operands and operators for prefix notation.\n\n" +
                         "Postfix notation puts the operator after each pair of operands, as in\n" +
                         "'1 1 +', which evaluates to 2. A longer example is\n" +
                         "8 7 * 4 + 5 * 2 2 + 3 7 * * +         which evaluates to 384.\n" + 
                         "Please USE SPACES between operands and operators for postfix notation.\n\n" +
                         "Infix notation puts the operator in between each pair of operands, as in\n" +
                         "'1 + 1', which evaluates to 2. A longer example is\n" +
                         "((((8*7)+4)*5)+((2+2)*(3*7)))         which evaluates to 384.\n" +
                         "Parentheses may be used for infix notation, but aren't required.\n" +
                         "Please DO NOT USE SPACES for infix notation.";
        
        String prompt = "Enter an expression (d for details or q to quit): ";
        
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            while (input.equals("d")) {
                System.out.println("\n" + details + "\n");
                System.out.print(prompt);
                input = scanner.nextLine();
            }
            if (input.equals("q")) break;
            System.out.println();
            doItAll(input);
            System.out.println();
        }
    }
}
