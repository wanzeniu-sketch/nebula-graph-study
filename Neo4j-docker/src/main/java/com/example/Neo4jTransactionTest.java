package com.example;

import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Result;
import org.neo4j.driver.Values;

public class Neo4jTransactionTest {
    public static void main(String[] args) {
        // 1. 初始化
        Neo4jUtils.initDriver();

        try (Session session = Neo4jUtils.getDriver().session()) {
            System.out.println("========== 🏦 Neo4j ACID 事务转账测试 ==========");

            // ---------------------------------------------------------
            // 1. 数据准备：重置环境
            // ---------------------------------------------------------
            System.out.println("\n[1] 正在初始化账户...");
            session.executeWrite(tx -> {
                tx.run("MATCH (n) DETACH DELETE n"); // 清空库，慎用！
                tx.run("CREATE (:Account {name: 'Alice', balance: 1000})");
                tx.run("CREATE (:Account {name: 'Bob', balance: 0})");
                return null;
            });
            printBalances(session, "初始状态");

            // ---------------------------------------------------------
            // 2. 模拟失败的转账 (体现原子性)
            // ---------------------------------------------------------
            System.out.println("\n[2] 开始执行【故障转账】(Alice -100 -> 💥报错 -> Bob +100)...");
            
            try {
                // 手动控制事务
                session.executeWrite(tx -> {
                    // Step A: Alice 扣钱
                    tx.run("MATCH (a:Account {name: 'Alice'}) SET a.balance = a.balance - 100");
                    System.out.println("    ✅ Alice 扣款成功 (当前内存中余额: 900)");

                    // 模拟：突然发生严重错误 (比如断电、代码Bug)
                    if (true) { 
                        throw new RuntimeException("🔥 突发！机房爆炸了！转账中断！");
                    }

                    // Step B: Bob 加钱 (永远执行不到这里)
                    tx.run("MATCH (b:Account {name: 'Bob'}) SET b.balance = b.balance + 100");
                    return null;
                });
            } catch (Exception e) {
                System.out.println("    ⚠️ 捕获到异常: " + e.getMessage());
                System.out.println("    🛡️ 触发自动回滚机制！");
            }

            // ---------------------------------------------------------
            // 3. 验证回滚结果
            // ---------------------------------------------------------
            // 如果回滚成功，Alice 应该是 1000，而不是 900
            printBalances(session, "故障回滚后");

            // ---------------------------------------------------------
            // 4. 执行成功的转账 (对比组)
            // ---------------------------------------------------------
            System.out.println("\n[3] 开始执行【正常转账】...");
            session.executeWrite(tx -> {
                tx.run("MATCH (a:Account {name: 'Alice'}) SET a.balance = a.balance - 100");
                tx.run("MATCH (b:Account {name: 'Bob'}) SET b.balance = b.balance + 100");
                // 建立一条转账记录边
                tx.run("MATCH (a:Account {name: 'Alice'}), (b:Account {name: 'Bob'}) " +
                       "CREATE (a)-[:SENT {amount: 100}]->(b)");
                return null;
            });
            printBalances(session, "正常转账后");

        } finally {
            Neo4jUtils.closeDriver();
        }
    }

    // 辅助方法：打印当前余额
    private static void printBalances(Session session, String stage) {
        System.out.println("--- 📊 " + stage + " ---");
        session.executeRead(tx -> {
            Result res = tx.run("MATCH (n:Account) RETURN n.name, n.balance ORDER BY n.name");
            while (res.hasNext()) {
                var record = res.next();
                System.out.println("   👤 " + record.get("n.name").asString() + 
                                   ": $" + record.get("n.balance").asInt());
            }
            return null;
        });
        System.out.println("-------------------------");
    }
}