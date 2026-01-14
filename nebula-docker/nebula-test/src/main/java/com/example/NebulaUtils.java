package com.example;

import com.vesoft.nebula.client.graph.NebulaPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.net.NebulaPool;
import com.vesoft.nebula.client.graph.net.Session;
import java.util.Arrays;
import java.util.List;

public class NebulaUtils {
    private static NebulaPool pool;
    private static final String SPACE_NAME = "test_persist"; // 默认操作的空间

    // 1. 初始化连接池 (单例模式，只初始化一次)
    public static boolean initPool() {
        if (pool != null) return true;
        
        pool = new NebulaPool();
        try {
            // === 这里就是你文档要求的连接池配置 ===
            NebulaPoolConfig config = new NebulaPoolConfig();
            config.setMaxConnSize(100); // 最大连接数
            config.setMinConnSize(5);   // 最小连接数
            config.setIdleTime(1000);   // 空闲回收时间
            config.setTimeout(3000);    // 连接超时时间

            List<HostAddress> addresses = Arrays.asList(new HostAddress("127.0.0.1", 9669));
            boolean success = pool.init(addresses, config);
            if(success) {
                System.out.println("✅ [框架] 连接池初始化成功");
            }
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. 通用执行方法 (自动获取Session -> 执行 -> 释放Session)
    // 这是一个万能方法，增删改查都调它
    public static ResultSet execute(String nGql) {
        Session session = null;
        try {
            // 获取 Session
            session = pool.getSession("root", "nebula", false);
            // 自动切空间 + 执行语句
            String fullSql = "USE " + SPACE_NAME + "; " + nGql;
            ResultSet resp = session.execute(fullSql);
            
            if (!resp.isSucceeded()) {
                System.err.println("❌ [执行失败] " + resp.getErrorMessage());
            }
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            // 确保 Session 永远被释放回连接池
            if (session != null) session.release();
        }
    }

    // 3. 关闭连接池 (应用退出时调用)
    public static void closePool() {
        if (pool != null) {
            pool.close();
            System.out.println("🛑 [框架] 连接池已关闭");
        }
    }
}