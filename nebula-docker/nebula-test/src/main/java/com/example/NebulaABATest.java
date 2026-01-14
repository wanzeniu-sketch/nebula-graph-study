package com.example;

import com.vesoft.nebula.client.graph.data.ResultSet;
import java.util.concurrent.TimeUnit;

public class NebulaABATest {
    public static void main(String[] args) throws InterruptedException {
        // 1. 初始化连接池
        if (!NebulaUtils.initPool()) return;

        System.out.println("========== 🚀 开始 ABA 问题验证测试 ==========");

        // ---------------------------------------------------------
        // 1. 准备数据 (初始化 User A，版本号置为 1)
        // ---------------------------------------------------------
        String initSql = "INSERT VERTEX player(name, ver) VALUES \"user_aba\":(\"UserA\", 1)";
        NebulaUtils.execute(initSql);
        System.out.println("✅ [初始化] 数据已插入: UserA, ver=1");

        // ---------------------------------------------------------
        // 2. 模拟：你的线程读取数据
        // ---------------------------------------------------------
        String querySql = "FETCH PROP ON player \"user_aba\" YIELD properties(vertex).ver AS v, properties(vertex).name AS n";
        ResultSet rs = NebulaUtils.execute(querySql);
        
        // 拿到当前的版本号 (oldVersion = 1)
        long oldVersion = rs.getRows().get(0).getValues().get(0).getIVal(); 
        String oldName = new String(rs.getRows().get(0).getValues().get(1).getSVal());
        
        System.out.println("👀 [你的视角] 读取数据成功: name=" + oldName + ", ver=" + oldVersion);
        System.out.println("⏸️ [你的视角] 正在处理业务逻辑 (模拟耗时)...");

        // ---------------------------------------------------------
        // 3. 模拟：干扰线程偷偷修改数据 (ABA 攻击)
        // ---------------------------------------------------------
        System.out.println("\n😈 [捣乱者] 趁你不注意，开始搞破坏...");
        
        // 第一次修改：把名字改成 "Hacker"，版本号 +1 (变为 2)
        NebulaUtils.execute("UPDATE VERTEX ON player \"user_aba\" SET name = \"Hacker\", ver = 2");
        System.out.println("😈 [捣乱者] 把数据改成了 Hacker (ver=2)");
        
        // 第二次修改：把名字改回 "UserA"，版本号 +1 (变为 3)
        // 【注意】此时名字虽然还是 UserA，但数据其实已经“脏”了
        NebulaUtils.execute("UPDATE VERTEX ON player \"user_aba\" SET name = \"UserA\", ver = 3");
        System.out.println("😈 [捣乱者] 又把数据改回了 UserA (ver=3) -> ABA 场景形成！");

        // ---------------------------------------------------------
        // 4. 模拟：你尝试更新 (使用乐观锁 CAS)
        // ---------------------------------------------------------
        System.out.println("\n🔄 [你的视角] 业务处理完毕，准备提交更新...");
        
        // 目标：把名字改成 "UserB"，且版本号升为 2
        // 【核心防御逻辑】：WHERE ver == oldVersion (即 WHERE ver == 1)
        String casSql = String.format(
            "UPDATE VERTEX ON player \"user_aba\" " +
            "SET name = \"UserB\", ver = %d " +
            "WHERE ver == %d " +  // <--- 这就是防线！
            "YIELD ver", 
            oldVersion + 1, // 期望写入的新版本 (2)
            oldVersion      // 检查旧版本 (1)
        );

        System.out.println("🛡️ [系统] 执行 CAS 更新语句: " + casSql);
        ResultSet updateResp = NebulaUtils.execute(casSql);

        // ---------------------------------------------------------
        // 5. 验证结果
        // ---------------------------------------------------------
        if (updateResp.isEmpty()) {
            System.out.println("\n🎉 [验证通过] 更新被阻止了！");
            System.out.println("   原因: 数据库里的 ver 是 3，而你提供的是 1。");
            System.out.println("   结论: 成功防御了 ABA 问题，数据没有被错误覆盖。");
        } else {
            System.out.println("\n❌ [验证失败] 更新居然成功了？ABA 防御失效！");
        }
        
        System.out.println("==============================================");
        NebulaUtils.closePool();
    }
}