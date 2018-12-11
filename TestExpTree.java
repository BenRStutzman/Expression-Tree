/**
* Run this to try a whole bunch of different inputs at once. Add to the array of strings if necessary.
*/
public class TestExpTree
{
    public static void main(String[] args) {
        
        //A whole bunch of test cases
        String [] inputs = {
        "", //empty tree
        "3", //just a digit
        "34235", //just a number
        "+ * + * 8 7 4 5 * + 2 2 * 3 7", //prefix
        "8 7 * 4 + 5 * 2 2 + 3 7 * * +", //postfix
        "((((8*7)+4)*5)+((2+2)*(3*7)))", //infix
        "((((1*2)+(3*4))*((5*6)+(177*10)))+(((9*8)+(7*6))*((5*4)+(1*22))))", //full infix
        "((((1*2)+(3*4))*((500*600)+(177*1000)))+(((9*(8+(12+17)))+(7*6))*((5*4)+(1*22222))))", //overly full infix
        "1000+1", //partially parenthesized infix
        "(8*7+4)*5+(2+2)*(3*7)", //another partially parenthesized infix
        "8*7+4*5+2+2*3*7", //unparenthesized infix
        "I lost the game.", //Austin's idea
        "++++", //Another of Austin's ideas
        "1+2*3-4%5^2/3",
        "+ * + * 8 7 4 5 * + 2 2 * 3 7 +", //prefix with extra operand
        "+ * + * 8 7 4 5 * + 2 2 * 3", //prefix missing a number
        "8 7 * 4 + 5 * 2 2 + 3 7 * * + 9", //postfix with extra number
        "7 * 4 + 5 * 2 2 + 3 7 * *", //postfix missing an operand
        "2^10000", //big number
        "(2^10)+(2^(3*20))", //big numbers combined
        "23f",
        "1.2",
        "-54",
        "54-",
        "2147483647",
        "2147483647-1",
        "2147483647+1",
        "2147483647+0+1",
    };
        
        //Does all the tree methods for every test case
        System.out.println();
        for (int i = 0; i < inputs.length; i++) {
            System.out.println("----- TEST " + i + ": input = \"" + inputs[i] + "\" ------");
            ExpTree.main(inputs[i].split(" "));
            if (i < inputs.length - 1) System.out.println();
        }
    }
}
