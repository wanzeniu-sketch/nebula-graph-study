# 🚀 Graph Database Deep Dive: NebulaGraph vs. Neo4j

> **Project Author**: [你的名字/GitHub ID]
> **Environment**: macOS M4 | Docker | Java 21 | VS Code

## 📖 项目背景
本项目记录了对主流图数据库 **NebulaGraph** (分布式) 和 **Neo4j** (原生单机) 的深度对比研究。
通过 Docker 容器化部署、Java 客户端开发、以及高并发/事务场景的模拟测试，深入探索了两种架构在**数据一致性**、**事务机制**及**图算法**应用上的本质区别。

---

## 🛠 技术栈 (Tech Stack)

* **Infrastructure**: Docker, Docker Compose
* **Databases**:
    * **NebulaGraph**: v3.6.0 (Distributed, Strong Partitioning)
    * **Neo4j**: v5.15.0 Community (Native Graph, ACID)
* **Languages**: Java 21 (Preview features enabled)
* **Tools**: Maven, Neo4j Browser, Nebula Console
* **Algorithms**: Neo4j GDS (Graph Data Science) - PageRank

---

## ⚖️ 核心架构对比 (Architectural Insights)

这是本项目得出的最重要的架构选型结论：

| 特性 | NebulaGraph 🌌 | Neo4j 🟢 |
| :--- | :--- | :--- |
| **架构设计** | **分布式存储计算分离** (Shared-nothing) | **Native Graph** (单机/主从) |
| **适用场景** | 海量数据 (千亿点边)、高吞吐并发、风控/推荐 | 金融核心交易、复杂路径分析、中小型图谱 |
| **事务支持** | **不支持完整 ACID** (最终一致性) | **完全支持 ACID** (强一致性) |
| **并发控制** | **Last Write Wins** (需应用层实现乐观锁) | **悲观锁** (自动排队，串行化修改) |
| **查询语言** | **nGQL** (类 SQL) | **Cypher** (模式匹配，所见即所得) |
| **可视化** | 需单独部署 Studio / Console | 内置 Browser (非常强大，支持样式定制) |

---

## 🧩 模块一：Neo4j 实战 (The ACID Power)

### 1. 部署与配置
* 针对 **Apple Silicon (M4)** 芯片进行了 Docker 内存与 IO 优化。
* 解决了 Docker 镜像拉取时的 TLS Handshake 网络问题（配置 Docker Proxy）。
* 集成 **GDS (Graph Data Science)** 插件，用于运行图算法。

### 2. 核心功能实现
* **连接池管理**: 封装 `Neo4jUtils`，实现 Driver 单例模式与资源自动释放。
* **ACID 事务模拟**: 在 `Neo4jTransactionTest` 中模拟银行转账场景。
    * ✅ **测试结果**: 在扣款成功后手动抛出异常，数据库自动回滚 (Rollback)，验证了数据的原子性。
* **图算法 (PageRank)**:
    * 构建“职场信任关系网”。
    * 运行 PageRank 算法计算节点权重，识别出“被 CEO 信任的实习生”拥有比经理更高的影响力。
    * 将计算结果写回图属性，并通过 Neo4j Browser 进行**数据驱动的大小可视化 (Data-driven Styling)**。

### 3. 代码结构
```text
src/main/java/com/example/
├── Neo4jUtils.java           // 连接池与通用 CRUD 封装
├── Neo4jCRUDTest.java        // 增删改查全流程测试 (防注入参数化查询)
└── Neo4jTransactionTest.java // 转账异常回滚测试 (ACID 验证)

模块二：NebulaGraph 实战 (The Distributed Speed)

1. 核心挑战
ABA 问题防御: 在分布式无锁架构下，实现了基于 CAS (Compare-And-Swap) 和 version 版本号的应用层乐观锁。

Schema 管理: 解决了容器内缺失 Console 工具的问题，通过 Java 代码直接维护图空间与 Schema。

2. 关键代码逻辑 (Java)
Java

// 乐观锁核心逻辑：防止更新丢失
String cypher = "MATCH (n) WHERE n.id = $id AND n.ver = $oldVer " +
                "SET n.prop = $newVal, n.ver = n.ver + 1";
// 如果影响行数为 0，说明数据已被他人修改，抛出异常提示重试。

快速开始 (How to Run)
1. 启动 Neo4j 容器
Bash

cd Neo4j-docker
docker-compose up -d
2. 运行测试代码
确保已安装 Maven：

Bash

# 运行 CRUD 测试
mvn exec:java -Dexec.mainClass="com.example.Neo4jCRUDTest"

# 运行事务回滚测试
mvn exec:java -Dexec.mainClass="com.example.Neo4jTransactionTest"
3. 查看可视化
访问浏览器 http://localhost:7474 (User: neo4j / Pass: 12345678)。