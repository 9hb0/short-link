package com.nageoffer.shortlink.admin.test;

public class UserTableShardingTest {

    public static final String SQL = "CREATE TABLE `t_link_%d` (\n" +
            "  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
            "  `domain` varchar(128) DEFAULT NULL COMMENT '域名',\n" +
            "  `short_uri` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '短链接',\n" +
            "  `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',\n" +
            "  `origin_url` varchar(1024) DEFAULT NULL COMMENT '原始链接',\n" +
            "  `click_num` int DEFAULT '0' COMMENT '点击量',\n" +
            "  `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',\n" +
            "  `enable_status` tinyint(1) DEFAULT NULL COMMENT '启用标识 0：未启用 1：已启用',\n" +
            "  `created_type` tinyint(1) DEFAULT NULL COMMENT '创建类型 0：控制台 1：接口',\n" +
            "  `valid_date_type` tinyint(1) DEFAULT NULL COMMENT '有效期类型 0：永久有效 1：用户自定义',\n" +
            "  `valid_date` datetime DEFAULT NULL COMMENT '有效期',\n" +
            "  `describe` varchar(1024) DEFAULT NULL COMMENT '描述',\n" +
            "  `totalPv` int  DEFAULT '0' COMMENT '历史Pv',\n"+
            "  `totalUv` int  DEFAULT '0' COMMENT '历史Uv',\n"+
            "  `totalUip` int  DEFAULT '0' COMMENT '历史UIp',\n"+
            "  `create_time` datetime DEFAULT NULL COMMENT '创建时间',\n" +
            "  `update_time` datetime DEFAULT NULL COMMENT '修改时间',\n" +
            "  `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',\n" +
            "  `favicon` varchar(256) DEFAULT NULL COMMENT '网站图标',\n" +
            "  PRIMARY KEY (`id`),\n" +
            "  UNIQUE KEY `idx_unique_full_short_url` (`full_short_url`) USING BTREE\n" +
            ") ENGINE=InnoDB AUTO_INCREMENT=1762385842304753667 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;"
            ;

    public static void main(String[] args) {
        for (int i=0 ;  i<16 ; i++){
            System.out.printf((SQL) + "%n" , i);
        }
    }

//    public static void main(String[] args) {
//        try {
//            // 加载并注册JDBC驱动
//            Class.forName("com.mysql.cj.jdbc.Driver");
//
//            String url = "jdbc:mysql://127.0.0.1:3306/shortlink?useUnicode=true&characterEncoding=UTF-8&rewriteBatchedStatements=true&allowMultiQueries=true&serverTimezone=Asia/Shanghai";
//            // 建立数据库连接
//            String username = "root";
//            String password = "20010304";
//            Connection connection = DriverManager.getConnection(url, username, password);
//
//            // 如果连接成功，打印一条消息
//            if (connection != null && !connection.isClosed()) {
//                System.out.println("成功连接到MySQL数据库！");
//            }
//
//            // 关闭连接（在实际应用中，通常会在finally块中关闭连接）
//            connection.close();
//        } catch (ClassNotFoundException e) {
//            System.err.println("未找到JDBC驱动：");
//            e.printStackTrace();
//        } catch (SQLException e) {
//            System.err.println("连接数据库时发生错误：");
//            e.printStackTrace();
//        }
//    }

}
