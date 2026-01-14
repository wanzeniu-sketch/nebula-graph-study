package com.example.entity;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Node("Substation") // 对应图数据库的 Label
public class Substation {

    @Id @GeneratedValue // 自动生成 ID
    private Long id;

    private String name;
    private String voltage; // 电压等级，例如 500kV, 220kV

    // 定义供电关系：指向下游
    @Relationship(type = "SUPPLIES_TO", direction = Relationship.Direction.OUTGOING)
    private List<Substation> downstream = new ArrayList<>();

    // 1. 无参构造函数 (SDN 需要)
    public Substation() {
    }

    // 2. 带参构造函数 (方便我们 new 对象)
    public Substation(String name, String voltage) {
        this.name = name;
        this.voltage = voltage;
    }

    // 3. 业务方法：添加下游变电站
    public void supplies(Substation otherStation) {
        this.downstream.add(otherStation);
    }

    // 4. 手动添加 Getter 方法 (为了解决编译报错)
    public String getName() {
        return name;
    }

    public String getVoltage() {
        return voltage;
    }

    public List<Substation> getDownstream() {
        return downstream;
    }
}