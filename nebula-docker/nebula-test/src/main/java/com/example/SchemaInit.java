package com.example;

public class SchemaInit {
    public static void main(String[] args) {
        // 1. 连接数据库
        if (!NebulaUtils.initPool()) return;

        System.out.println("🔧 正在执行 Schema 修改...");

        try {
            // 2. 执行修改语句
            // 注意：ALTER 语句是修改元数据，执行后需要等待一会
            String alterSql = "USE test_persist; ALTER TAG player ADD (ver int64 DEFAULT 0);";
            NebulaUtils.execute(alterSql);
            
            System.out.println("✅ Schema 修改指令已发送！");
            System.out.println("⏳ 请等待 10 秒钟，让元数据同步到所有节点...");
            
            // 强制等待 10 秒，防止立刻运行测试找不到字段
            Thread.sleep(10000); 
            
            System.out.println("🚀 准备就绪！现在可以去运行 NebulaABATest 了。");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            NebulaUtils.closePool();
        }
    }
}