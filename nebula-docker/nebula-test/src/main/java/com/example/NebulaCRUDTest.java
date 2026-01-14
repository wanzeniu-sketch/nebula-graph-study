package com.example;

import com.vesoft.nebula.client.graph.data.ResultSet;

public class NebulaCRUDTest {
    public static void main(String[] args) throws InterruptedException {
        // 1. 初始化框架
        if (!NebulaUtils.initPool()) return;

        System.out.println("=========================================");
        
        // --- 场景 A: 插入数据 (Create) ---
        // 插入一个新球员 James，ID 为 p2
        String insertSql = "INSERT VERTEX player(name) VALUES \"p2\":(\"James\")";
        ResultSet insertResp = NebulaUtils.execute(insertSql);
        if (insertResp != null && insertResp.isSucceeded()) {
            System.out.println("1️⃣ [新增] 插入 James 成功");
        }

        // --- 场景 B: 查询数据 (Read) ---
        // 查刚才插入的 James
        String querySql = "FETCH PROP ON player \"p2\" YIELD properties(vertex)";
        ResultSet queryResp = NebulaUtils.execute(querySql);
        if (queryResp != null && !queryResp.isEmpty()) {
            System.out.println("2️⃣ [查询] 查到了: " + queryResp.getRows().get(0));
        }

        // --- 场景 C: 更新数据 (Update) ---
        // 把 James 改名为 LeBron
        // 注意：NebulaGraph 的 Update 语法比较特殊，通常直接覆盖插入或使用 UPDATE 语句
        String updateSql = "UPDATE VERTEX ON player \"p2\" SET name = \"LeBron\" YIELD name";
        ResultSet updateResp = NebulaUtils.execute(updateSql);
        if (updateResp != null && updateResp.isSucceeded()) {
            System.out.println("3️⃣ [更新] 修改成功，新名字: " + updateResp.getRows().get(0));
        }

        // --- 场景 D: 删除数据 (Delete) ---
        // 删掉 James
        String deleteSql = "DELETE VERTEX \"p2\"";
        ResultSet deleteResp = NebulaUtils.execute(deleteSql);
        if (deleteResp != null && deleteResp.isSucceeded()) {
            System.out.println("4️⃣ [删除] 删除 James 成功");
        }

        System.out.println("=========================================");
        
        // 5. 验证是否真的删掉了
        ResultSet checkResp = NebulaUtils.execute(querySql);
        if (checkResp != null && checkResp.getRows().isEmpty()) {
            System.out.println("5️⃣ [验证] 再次查询 p2，结果为空 (验证通过)");
        }

        // 6. 程序结束，关闭池
        NebulaUtils.closePool();
    }
}