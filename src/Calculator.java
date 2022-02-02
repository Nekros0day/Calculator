import java.util.*;

import javafx.util.converter.NumberStringConverter;

import static java.lang.Double.NaN;
import static java.lang.Math.pow;


/*
 *   A calculator for rather simple arithmetic expressions
 *
 *   This is not the program, it's a class declaration (with methods) in it's
 *   own file (which must be named Calculator.java)
 *
 *   NOTE:
 *   - No negative numbers implemented
 */
public class Calculator {

    // Here are the only allowed instance variables!
    // Error messages (more on static later)
    final static String MISSING_OPERAND = "Missing or bad operand";
    final static String DIV_BY_ZERO = "Division with 0";
    final static String MISSING_OPERATOR = "Missing operator or parenthesis";
    final static String OP_NOT_FOUND = "Operator not found";

    // Definition of operators
    final static String OPERATORS = "+-*/^";

    // Method used in REPL
    double eval(String expr) {
        if (expr.length() == 0) {
            return NaN;
        }
        List<String> tokens = tokenize(expr);
        List<String> postfix = infix2Postfix(tokens);
        return evalPostfix(postfix);
    }

    // ------  Evaluate RPN expression -------------------

    double evalPostfix(List<String> postfix) {
        //checks if missing parenthesis
        evalPare(postfix);
        if (postfix.isEmpty()) {
            return NaN;
        }
        Deque<String> stack = new ArrayDeque<>();
        double d1;
        double d2;
        double awnser = 0;
        double awns = 0;
        for (String sh : postfix) {
            if(OPERATORS.contains(sh)){
                //Check for missing operand (check if next value is an Operator)
                if(stack.isEmpty()){
                    throw new IllegalArgumentException(MISSING_OPERAND);
                }
                
                d1 =  Double.parseDouble(stack.pop());
                //Check for missing operand (check if next value is an Operator)
                if(stack.isEmpty()){
                    throw new IllegalArgumentException(MISSING_OPERAND);
                }
    
                d2 =  Double.parseDouble(stack.pop());
                //Applys operator to operands then pushes awsner onto stack
                awns = applyOperator(sh, d1, d2);
                stack.push(Double.toString(awns));
            }
            else{
            //push operand onto stack
            stack.push(sh);
            }
        }
        //Set final awnser
        awnser = Double.parseDouble(stack.pop());
        //checks if stack is not empty, thus theres a missing operator
        if(!stack.isEmpty()){
            throw new IllegalArgumentException(MISSING_OPERATOR);
        }
        return awnser;
    }
    //Check for parenthesis problem
    void evalPare(List<String> postfix){
        for(String str : postfix)
        {
            if (str.contains("(") | str.contains(")")){
                throw new IllegalArgumentException(MISSING_OPERATOR);
            }
        }
    }

    double applyOperator(String op, double d1, double d2) {
        switch (op) {
            case "+":
                return d1 + d2;
            case "-":
                return d2 - d1;
            case "*":
                return d1 * d2;
            case "/":
                if (d1 == 0) {
                    throw new IllegalArgumentException(DIV_BY_ZERO);
                }
                return d2 / d1;
            case "^":
                return pow(d2, d1);
        }
        throw new RuntimeException(OP_NOT_FOUND);
    }

    // ------- Infix 2 Postfix ------------------------

    List<String> infix2Postfix(List<String> infix) {
        List<String> postfix = new ArrayList<>();
        Deque<String> stack = new ArrayDeque<>();
        //Go thorugh each token
        for (String sh : infix) {
            //If current token is a Operator:
            if (OPERATORS.contains(sh)) {
                int newPrec = getPrecedence(sh);
                //Set standard Prec for stacks 
                int oldPrec = 0;
                
                //Get oldPrec Precedence from top of stack token
                if(!stack.isEmpty()){
                    if(OPERATORS.contains(stack.getFirst())){
                    oldPrec = getPrecedence(stack.peek());
                    }
                }
                //Special case for ^
                if(getAssociativity(sh) == Assoc.RIGHT){
                        stack.push(sh);
                }
                else{
                    //Actions to take based on newPrec and oldPrec
                    while ( !stack.isEmpty() && (newPrec <= oldPrec) ){
                        //Special case for ^
                        if(getAssociativity(sh) == Assoc.RIGHT){
                            stack.push(sh);
                        }
                        else if (!(stack.peek().equals("(") | stack.peek().equals(")"))){
                            postfix.add(stack.pop());
                        }
                        else{
                            break;
                        }
                    }
                    stack.push(sh);
                }
            } 
            //Left parenthesis
            else if (sh.equals("(")) {
                stack.push(sh);

            } 
            //Right parenthesis
            else if (sh.equals(")")) {
                while (!stack.isEmpty()){
                    if(stack.peek().equals("(")){
                        break;
                    }
                    postfix.add(stack.pop());
                }
                if(!stack.isEmpty()){
                    stack.pop();
                }
                else{
                    stack.push(")");
                }
            }
            //Add digit to output stream 
            else {
                postfix.add(sh);
            }
        }

        while ( ! stack.isEmpty()){
            postfix.add(stack.pop());
        }
        return postfix;
    }





    int getPrecedence(String op) {
        if ("+-".contains(op)) {
            return 2;
        } else if ("*/".contains(op)) {
            return 3;
        } else if ("^".contains(op)) {
            return 4;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    Assoc getAssociativity(String op) {
        if ("+-*/".contains(op)) {
            return Assoc.LEFT;
        } else if ("^".contains(op)) {
            return Assoc.RIGHT;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    enum Assoc {
        LEFT,
        RIGHT
    }

    // ---------- Tokenize -----------------------

    // List String (not char) because numbers (with many chars)
    List<String> tokenize(String expr) {
        List<String> tokenized = new ArrayList<>();
        //Builds operand from digits
        String currentInt = "";
        int count = 1;
        for (char ch : expr.toCharArray()) {
            //Skips empty spaces
            if(ch != ' '){
                //Add digit to Current operand if char is a digit
                if(Character.isDigit(ch)){
                currentInt +=  ch;
                }
                //Adds operand if char is a operator 
                else if(!Character.isDigit(ch) & currentInt != ""){
                    tokenized.add(currentInt);
                    currentInt = "";
                    tokenized.add(Character.toString(ch));
                }
                //Adds operator to tokenized
                else{
                    tokenized.add(Character.toString(ch));
                }
            }
            //Adds last operand if end of expr.toCharArray or char is space ("12 5"  adds 12 to tokenized)
            if ((count == expr.length() & currentInt != "") | (ch == ' ' & currentInt != "")){
                tokenized.add(currentInt);
                currentInt = "";
            }
            count++;
        }
        return tokenized;
    }
    /*
     List<String> tokenize(String expr) {
        List<String> tokenized = new ArrayList<>();
        expr = removeSpaces(expr);
        String currentInt = "";
        int count = 1;
        for (char ch : expr.toCharArray()) {
            if(Character.isDigit(ch)){
            currentInt +=  ch;
            }
            else if(!Character.isDigit(ch) & currentInt != ""){
                tokenized.add(currentInt);
                currentInt = "";
                tokenized.add(Character.toString(ch));
            }
            else{
                tokenized.add(Character.toString(ch));
            }
            if (count == expr.length() & currentInt != ""){
                tokenized.add(currentInt);
            }
            count++;
        }
        return tokenized;
    }
    String removeSpaces(String str) {
        StringBuilder sb = new StringBuilder();
        for (char ch : str.toCharArray()) {
            if (!Character.isSpaceChar(ch)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
    */

}
