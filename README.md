# 🚀 Graph Database Deep Dive: NebulaGraph vs. Neo4j

> **Project Author**: [Your Name/GitHub ID]
> **Environment**: macOS M4 (Apple Silicon) | Docker | Java 21

## 📖 项目背景 (Project Background)

本项目是一个深度对比研究项目，旨在探索主流图数据库 **NebulaGraph** (分布式架构) 和 **Neo4j** (原生单机架构) 在核心机制上的本质区别。

通过 Docker 容器化部署和 Java 客户端实战，本项目深入验证了两者在**数据一致性 (CAP)**、**事务机制 (ACID)**、**高并发控制**以及**图算法 (GDS)** 方面的不同表现与最佳实践。

---

## 🛠 技术栈 (Tech Stack)

* **Infrastructure**: Docker Desktop (Proxy Configured), Docker Compose
* **Databases**:
    * **NebulaGraph**: v3.6.0 (Distributed, Strong Partitioning)
    * **Neo4j**: v5.15.0 Community (Native Graph, ACID Supported)
* **Languages**: Java 21 (Preview features enabled)
* **Tools**: Maven, Neo4j Browser, Nebula Console
* **Algorithms**: Neo4j GDS (PageRank)

---

## ⚖️ 核心架构对比 (Architecture Comparison)

| 特性 Feature | NebulaGraph 🌌 | Neo4j 🟢 |
| :--- | :--- | :--- |
| **架构设计** | **分布式存储计算分离** (Shared-nothing) | **Native Graph** (单机/主从) |
| **适用场景** | 海量数据 (千亿点边)、高吞吐并发、风控/推荐 | 金融核心交易、复杂路径分析、中小型图谱 |
| **事务支持** | **无完整 ACID** (最终一致性) | **完全支持 ACID** (强一致性) |
| **并发控制** | **Last Write Wins** (需应用层实现乐观锁) | **悲观锁** (自动排队，串行化修改) |
| **查询语言** | **nGQL** (类 SQL) | **Cypher** (模式匹配，所见即所得) |
| **可视化** | 需单独部署 Studio / Console | 内置 Browser (非常强大，支持样式定制) |

---

## 📂 模块一：Neo4j 实战 (The ACID Power)

> 位于目录: `./Neo4j-docker`

### 1. 核心功能实现
* **连接池管理**: 封装 `Neo4jUtils`，实现 Driver 单例模式与资源自动释放。
* **ACID 事务模拟**: 在 `Neo4jTransactionTest.java` 中模拟银行转账场景。
    * ✅ **测试结果**: 验证了在扣款成功后手动抛出异常，Neo4j 能够自动 **回滚 (Rollback)**，保证资金不丢失。
* **参数化查询**: 使用 `Values.parameters()` 防止 Cypher 注入，提升执行效率。

### 2. 图算法实战 (Graph Data Science)
* **环境配置**: 解决了 Docker 镜像拉取失败问题，成功集成 GDS 插件。
* **算法应用**:
    * 构建了复杂的“职场信任关系网”。
    * 运行 **PageRank** 算法计算节点权重，发现“被 CEO 信任的实习生”拥有比“经理”更高的影响力。
    * **可视化**: 将算法评分写回图属性，通过 Neo4j Browser 实现了数据驱动的节点大小动态展示。

---

## 🌌 模块二：NebulaGraph 实战 (The Distributed Speed)

> 位于目录: `./nebula-docker`

### 1. 核心挑战与解决方案
* **分布式无锁架构**: 针对 Nebula 不支持事务的特性，在应用层实现了 **乐观锁 (Optimistic Locking)** 机制。
* **ABA 问题防御**:
    * 场景：高并发下同一数据被多次修改。
    * 方案：引入 `ver` (版本号) 字段，使用 CAS (Compare-And-Swap) 语法：
      ```sql
      UPDATE VERTEX ON player SET age = 30, ver = ver + 1 
      WHERE id == "101" AND ver == $old_ver;
      ```
* **Schema 管理**: 解决了容器内缺失 Console 工具的问题，通过 Java 代码直接维护图空间 (Space) 与 Tag/EdgeType。

---

## 🚀 快速开始 (How to Run)

### 前置要求
* Docker & Docker Compose
* Java JDK 21+
* Maven

### 1. 运行 Neo4j 演示
```bash
cd Neo4j-docker
# 启动容器 (包含 GDS 插件)
docker-compose up -d

# 运行事务测试代码
mvn clean compile
mvn exec:java -Dexec.mainClass="com.example.Neo4jTransactionTest"

访问可视化界面: http://localhost:7474 (User: neo4j / Pass: 12345678)

运行 NebulaGraph 演示
Bash

cd nebula-docker
# 启动分布式集群 (Meta + Storage + Graphd)
docker-compose up -d

# 运行并发测试代码
mvn clean compile
mvn exec:java -Dexec.mainClass="com.example.NebulaABATest"