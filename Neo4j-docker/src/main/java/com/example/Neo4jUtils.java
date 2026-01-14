package com.example;

import org.neo4j.driver.*;
import java.util.Map;

public class Neo4jUtils {
    // Driver 本身就是线程安全的连接池，必须保持单例
    private static Driver driver;

    // 1. 初始化连接池 (应用启动时调用一次)
    public static void initDriver() {
        if (driver == null) {
            // Bolt 协议端口通常是 7687
            String uri = "bolt://localhost:7687";
            // 填入你在 Docker 里设置的账号密码
            AuthToken token = AuthTokens.basic("neo4j", "12345678");
            
            // 配置连接池参数 (可选，Neo4j 默认配置已经很好了)
            Config config = Config.builder()
                    .withMaxConnectionPoolSize(100) // 最大连接数
                    .withConnectionTimeout(3000, java.util.concurrent.TimeUnit.MILLISECONDS)
                    .build();

            driver = GraphDatabase.driver(uri, token, config);
            System.out.println("✅ [Neo4j] 连接池初始化成功");
        }
    }

    // 2. 获取 Driver 实例 (供业务层使用)
    public static Driver getDriver() {
        return driver;
    }

    // 3. 关闭连接池 (应用退出时调用)
    public static void closeDriver() {
        if (driver != null) {
            driver.close();
            System.out.println("🛑 [Neo4j] 连接池已关闭");
        }
    }

    // 4. 通用写操作 (自动管理 Session 和 事务)
    // cypher: 查询语句 (e.g. "CREATE (n:Person {name: $name})")
    // parameters: 参数 Map
    public static void executeWrite(String cypher, Map<String, Object> parameters) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run(cypher, parameters);
                return null;
            });
        }
    }
}