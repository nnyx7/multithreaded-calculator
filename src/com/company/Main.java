package com.company;

import expression.ExpressionTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) throws IOException {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in));

        String input = reader.readLine();

        ExpressionTree tree = new ExpressionTree(input);
        tree.printTree();

        System.out.println("Result:");
        System.out.println(tree.evaluate());
        System.out.print("Tree depth is: " + tree.treeDepth() + '\n');
    }
}