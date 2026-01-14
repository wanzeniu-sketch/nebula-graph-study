package com.example.repository;

import com.example.entity.Substation;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import java.util.List;

public interface SubstationRepository extends Neo4jRepository<Substation, Long> {

    // 1. 基础查询：根据名字找
    Substation findByName(String name);

    // 2. 高级查询：故障模拟 (找出某个站点的所有下游节点)
    // 使用 Cypher 变长路径查询 (-[:SUPPLIES_TO*]->)
    @Query("MATCH (start:Substation {name: $name})-[:SUPPLIES_TO*]->(end:Substation) RETURN end")
    List<Substation> findAllImpactedStations(String name);

    // 🌟 新增：N-1 冗余性分析
    // 逻辑：寻找从 startNode 到 endNode 的路径，且路径中不能包含 faultNode
    @Query("MATCH path = (start:Substation {name: $startNode})-[:SUPPLIES_TO*]->(end:Substation {name: $endNode}) " +
           "WHERE none(n IN nodes(path) WHERE n.name = $faultNode) " +
           "RETURN count(path) > 0")
    boolean checkRedundancy(String startNode, String endNode, String faultNode);

}