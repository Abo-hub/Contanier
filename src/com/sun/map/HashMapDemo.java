package com.sun.map;


public class HashMapDemo {

    public static long BKDHash(String str) {

        long seed = 131;
        long hash = 0;
        for (int i = 0; i < str.length(); i++) 
            hash = (hash * seed) + str.charAt(i);
        return hash;
    }

    public static void main(String[] args) {
        String str = "jack";
        String str2 = "jacl";
        System.out.println(BKDHash(str));
        System.out.println(BKDHash(str2));
      
        
    }
}