package com.example;

import com.example.entity.Substation;
import com.example.repository.SubstationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GridSimulationApp {

    // 👇 刚才可能不小心把这段 main 方法弄丢了，它是程序的总开关
    public static void main(String[] args) {
        SpringApplication.run(GridSimulationApp.class, args);
    }

    // N-1 仿真逻辑
    @Bean
    CommandLineRunner demo(SubstationRepository repo) {
        return args -> {
            System.out.println("========== ⚡️ 国家电网 N-1 冗余性分析系统启动 ⚡️ ==========");

            // 1. 清空旧数据
            repo.deleteAll();

            // 2. 构建【双电源】电网拓扑
            // 主电源
            Substation bjWest = new Substation("北京西站", "500kV");
            // 备用电源
            Substation tjNorth = new Substation("天津北站", "500kV");

            // 枢纽站 & 用户
            Substation haidian = new Substation("海淀站", "220kV");
            Substation zgc = new Substation("中关村配电室", "10kV");

            // 建立连接：海淀站现在是“双路供电”！
            bjWest.supplies(haidian);  // 路径 A
            tjNorth.supplies(haidian); // 路径 B (备用)
            
            haidian.supplies(zgc);

            // 保存 (保存两个源头，下面的都会自动保存)
            repo.save(bjWest);
            repo.save(tjNorth);
            System.out.println("✅ 电网拓扑构建完成：已建立 [北京西] 与 [天津北] 双路供电格局。");

            // ---------------------------------------------------------
            // 场景一：故障模拟 (和之前一样)
            // ---------------------------------------------------------
            String faultNode = "北京西站";
            System.out.println("\n🚨 突发事件：主电源 [" + faultNode + "] 发生爆炸故障！");

            // ---------------------------------------------------------
            // 场景二：N-1 冗余性分析 (核心新功能)
            // ---------------------------------------------------------
            String target = "中关村配电室";
            String backupSource = "天津北站";

            System.out.println("🤖 系统正在进行 N-1 分析：尝试切换至备用线路...");
            
            // 提问：如果没有了北京西站，天津北站能不能送到中关村？
            // 注意：这里需要你确认 Repository 里的 checkRedundancy 方法已经写好了
            boolean isSafe = repo.checkRedundancy(backupSource, target, faultNode);

            if (isSafe) {
                System.out.println("✅ [N-1 通过] 冗余切换成功！");
                System.out.println("   -> [" + target + "] 目前由 [" + backupSource + "] 供电。");
                System.out.println("   -> 电网供电可靠，未发生大规模停电。");
            } else {
                System.out.println("❌ [N-1 失败] 无备用线路！");
                System.out.println("   -> [" + target + "] 将发生全黑停电 (Blackout)。");
            }

            System.out.println("==========================================================");
        };
    }
}