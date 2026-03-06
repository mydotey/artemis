# Java Artemis SQLite 配置指南

Java Artemis 默认使用 **MySQL** 数据库，但可以通过配置切换到 **SQLite**。

## 配置方式

### 方式1：使用配置文件（推荐）

创建 `data-source-sqlite.properties` 文件：

```properties
driverClassName=org.sqlite.JDBC
url=jdbc:sqlite:./artemis.db
username=
password=
initialSize=5
maxActive=20
maxIdle=10
minIdle=2
maxWait=30000
```

然后设置环境变量指向该配置文件：

```bash
export ARTEMIS_DB_CONFIG=data-source-sqlite.properties
java -jar artemis-server.jar
```

### 方式2：使用环境变量

直接通过环境变量设置数据库连接：

```bash
export ARTEMIS_DB_URL="jdbc:sqlite:./artemis.db"
export ARTEMIS_DB_DRIVER="org.sqlite.JDBC"
java -jar artemis-server.jar
```

### 方式3：使用系统属性

通过 JVM 系统属性设置：

```bash
java -Dartemis.db.url=jdbc:sqlite:./artemis.db \
     -Dartemis.db.driver=org.sqlite.JDBC \
     -jar artemis-server.jar
```

## 混合集群配置示例

在 artemis-test 混合集群中，Java 和 Rust 节点共享同一个 SQLite 数据库：

```bash
# 1. 创建共享数据目录
mkdir -p ./data

# 2. 启动 Java 节点（使用 SQLite）
export ARTEMIS_DB_URL="jdbc:sqlite:./data/artemis.db"
export ARTEMIS_DB_DRIVER="org.sqlite.JDBC"
java -jar artemis-server.jar --server.port=8081

# 3. 启动 Rust 节点（自动使用同一 SQLite 文件）
./artemis server --config rust-node1.toml
# rust-node1.toml 中配置: url = "sqlite://./data/artemis.db"
```

## 注意事项

1. **SQLite 并发写入**：SQLite 的并发写入性能有限，建议使用 WAL 模式：
   ```bash
   # 在连接URL中启用WAL模式
   jdbc:sqlite:./artemis.db?_journal_mode=WAL
   ```

2. **路径问题**：确保 Java 和 Rust 使用**相同的数据库文件路径**，可以是绝对路径或相对于各自工作目录的相同相对路径。

3. **表结构**：首次启动时会自动创建表结构，但需要确保 Java 和 Rust 的表结构兼容。

## 默认行为

- **默认数据库**：MySQL
- **默认 URL**：`jdbc:mysql://localhost:3306/artemis`
- **默认驱动**：`com.mysql.jdbc.Driver`

只有当明确配置上述 SQLite 参数时，才会切换到 SQLite 模式。
