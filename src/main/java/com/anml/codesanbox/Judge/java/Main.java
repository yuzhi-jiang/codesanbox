package com.anml.codesanbox.Judge.java;

public class Main {
    public static void main(String[] args) {
        if(args.length<2){
            System.out.println("hello world");
        }
        Integer a=Integer.parseInt(args[0]);
        Integer b=Integer.parseInt(args[1]);

        System.out.println(a+b);
    }
}
