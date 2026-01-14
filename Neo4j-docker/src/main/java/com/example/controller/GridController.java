package com.example.controller;

import com.example.entity.Substation;
import com.example.repository.SubstationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController // 1. 告诉 Spring 这是一个 Web 接口
@RequestMapping("/api/grid") // 2. 所有接口的统一前缀
public class GridController {

    private final SubstationRepository repo;

    // Spring 自动注入 Repository
    public GridController(SubstationRepository repo) {
        this.repo = repo;
    }

    /**
     * API 1: 查询受影响的站点
     * 用法: GET http://localhost:8080/api/grid/impact?faultNode=北京西站
     */
    @GetMapping("/impact")
    public List<Substation> analyzeImpact(@RequestParam String faultNode) {
        System.out.println("📥 收到 API 请求：分析 [" + faultNode + "] 故障影响...");
        return repo.findAllImpactedStations(faultNode);
    }

    /**
     * API 2: 执行 N-1 冗余性分析
     * 用法: GET http://localhost:8080/api/grid/n-1?target=中关村&backup=天津北&fault=北京西
     */
    @GetMapping("/n-1")
    public Map<String, Object> analyzeN1(
            @RequestParam("target") String target,
            @RequestParam("backup") String backup,
            @RequestParam("fault") String fault) {
        
        System.out.println("📥 收到 API 请求：N-1 分析 (" + target + " via " + backup + ")");
        
        boolean isSafe = repo.checkRedundancy(backup, target, fault);

        // 构建一个漂亮的 JSON 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("target_station", target);
        result.put("backup_source", backup);
        result.put("fault_simulation", fault);
        result.put("n_1_pass", isSafe); // true 或 false
        
        if (isSafe) {
            result.put("message", "✅ 安全！备用线路工作正常，供电未中断。");
        } else {
            result.put("message", "❌ 危险！无可用备用路径，将发生停电。");
        }

        return result;
    }
}