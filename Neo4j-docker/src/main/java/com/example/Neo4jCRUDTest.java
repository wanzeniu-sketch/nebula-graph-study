package com.example;

import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values; // 用于构建参数
import java.util.Collections;

public class Neo4jCRUDTest {
    public static void main(String[] args) {
        // 1. 初始化
        Neo4jUtils.initDriver();

        try {
            System.out.println("========== 🚀 Neo4j CRUD 完整测试 ==========");

            // -------------------------------------------------
            // 1️⃣ [Create] 新增节点
            // Cypher: CREATE (变量名:标签名 {属性...})
            // -------------------------------------------------
            String insertSql = "CREATE (n:Player {name: $name, age: $age})";
            Neo4jUtils.executeWrite(insertSql, Values.parameters("name", "Curry", "age", 30).asMap());
            System.out.println("1️⃣ [新增] 插入 Player Curry 成功");

            // -------------------------------------------------
            // 2️⃣ [Read] 查询节点
            // Cypher: MATCH (n:标签) WHERE ... RETURN n
            // -------------------------------------------------
            try (Session session = Neo4jUtils.getDriver().session()) {
                String readSql = "MATCH (n:Player) WHERE n.name = $name RETURN n.name, n.age";
                String name = session.executeRead(tx -> {
                    Result result = tx.run(readSql, Values.parameters("name", "Curry").asMap());
                    if (result.hasNext()) {
                        Record record = result.next();
                        return record.get("n.name").asString() + " (Age: " + record.get("n.age").asInt() + ")";
                    }
                    return null;
                });
                System.out.println("2️⃣ [查询] 查到了: " + name);
            }

            // -------------------------------------------------
            // 3️⃣ [Update] 更新数据
            // Cypher: MATCH ... SET n.prop = value
            // -------------------------------------------------
            String updateSql = "MATCH (n:Player {name: $oldName}) SET n.name = $newName";
            Neo4jUtils.executeWrite(updateSql, Values.parameters("oldName", "Curry", "newName", "Stephen").asMap());
            System.out.println("3️⃣ [更新] 修改成功，Curry -> Stephen");

            // 再次查询验证更新
            try (Session session = Neo4jUtils.getDriver().session()) {
                String checkSql = "MATCH (n:Player {name: $name}) RETURN n.name";
                String resultName = session.executeRead(tx -> {
                    Result res = tx.run(checkSql, Values.parameters("name", "Stephen").asMap());
                    return res.hasNext() ? res.next().get(0).asString() : null;
                });
                System.out.println("   [验证] 更新后的名字是: " + resultName);
            }

            // -------------------------------------------------
            // 4️⃣ [Delete] 删除节点
            // Cypher: MATCH ... DELETE n
            // -------------------------------------------------
            //String deleteSql = "MATCH (n:Player {name: $name}) DELETE n";
            //Neo4jUtils.executeWrite(deleteSql, Values.parameters("name", "Stephen").asMap());
            //System.out.println("4️⃣ [删除] 删除 Stephen 成功");

            // -------------------------------------------------
            // 5️⃣ [Verify] 最终验证
            // -------------------------------------------------
            try (Session session = Neo4jUtils.getDriver().session()) {
                long count = session.executeRead(tx -> {
                    Result res = tx.run("MATCH (n:Player {name: $name}) RETURN count(n)", 
                                        Values.parameters("name", "Stephen").asMap());
                    return res.single().get(0).asLong();
                });
                
                if (count == 0) {
                    System.out.println("5️⃣ [验证] 再次查询，结果为空 (验证通过) ✅");
                } else {
                    System.out.println("❌ [验证失败] 数据没删掉！");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 6. 关闭连接池
            Neo4jUtils.closeDriver();
        }
    }
}