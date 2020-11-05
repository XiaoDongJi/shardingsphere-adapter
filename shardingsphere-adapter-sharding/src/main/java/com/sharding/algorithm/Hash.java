package com.sharding.algorithm;

/**
 * 一致性hash
 * @Link https://my.oschina.net/u/4305951/blog/4136275
 * @author: jixd
 * @date: 2020/9/21 11:27 上午
 */
public class Hash {

    public static int fnv1_32_hash(String str) {
        final int p = 16777619;
        int hash = (int)2166136261L;
        for (int i = 0; i < str.length(); i++) {
            hash =( hash ^ str.charAt(i) ) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash;
    }

    public static void main(String[] args) {
        System.out.println(fnv1_32_hash("BJZYZCW81507260733-02") & 15);
    }

}
