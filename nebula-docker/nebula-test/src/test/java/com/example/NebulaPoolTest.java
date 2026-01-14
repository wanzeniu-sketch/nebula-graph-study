package com.example;

import com.vesoft.nebula.client.graph.NebulaPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.net.NebulaPool;
import com.vesoft.nebula.client.graph.net.Session;

import java.util.Arrays;
import java.util.List;

public class NebulaPoolTest {
    public static void main(String[] args) {
        System.out.println("🚀 正在启动 NebulaGraph 连接测试...");
        
        NebulaPool pool = new NebulaPool();

        try {
            NebulaPoolConfig nebulaPoolConfig = new NebulaPoolConfig();
            nebulaPoolConfig.setMaxConnSize(100);
            nebulaPoolConfig.setMinConnSize(5);
            nebulaPoolConfig.setIdleTime(1000);
            nebulaPoolConfig.setTimeout(3000);

            List<HostAddress> addresses = Arrays.asList(new HostAddress("127.0.0.1", 9669));
            
            boolean initSuccess = pool.init(addresses, nebulaPoolConfig);
            if (!initSuccess) {
                System.out.println("❌ 连接池初始化失败！");
                return;
            }
            System.out.println("✅ 连接池初始化成功！");

            Session session = pool.getSession("root", "nebula", false);
            // 【修正1】这里是 getSessionID (注意 ID 都是大写)
            System.out.println("✅ Session 获取成功，ID: " + session.getSessionID());

            String nGql = "USE test_persist; FETCH PROP ON player \"p1\" YIELD properties(vertex);";
            
            System.out.println("📡 执行 nGQL: " + nGql);
            ResultSet resp = session.execute(nGql);

            if (resp.isSucceeded()) {
                System.out.println("--------------------------------------------------");
                System.out.println("🎉 恭喜！Java 代码成功读取到了 Docker 中的数据：");
                if (!resp.isEmpty()) {
                    // 【修正2】不能直接 getRow(0)，要先 getRows() 拿到列表，再 .get(0)
                    System.out.println("结果数据: " + resp.getRows().get(0)); 
                } else {
                    System.out.println("⚠️ 警告: 查询成功但没数据，请检查 p1 是否插入成功");
                }
                System.out.println("--------------------------------------------------");
            } else {
                System.out.println("❌ 查询失败: " + resp.getErrorMessage());
            }

            session.release();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.close();
        }
    }
}