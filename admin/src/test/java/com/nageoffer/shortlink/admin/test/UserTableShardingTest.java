package com.nageoffer.shortlink.admin.test;

public class UserTableShardingTest {

    public static final String SQL = "CREATE TABLE `t_user_%d` (\n" +
            "  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
            "  `username` varchar(256) DEFAULT NULL COMMENT '用户名',\n" +
            "  `password` varchar(512) DEFAULT NULL COMMENT '密码',\n" +
            "  `real_name` varchar(256) DEFAULT NULL COMMENT '真实姓名',\n" +
            "  `phone` varchar(128) DEFAULT NULL COMMENT '手机号',\n" +
            "  `mail` varchar(512) DEFAULT NULL COMMENT '邮箱',\n" +
            "  `deletion_time` bigint(20) DEFAULT NULL COMMENT '注销时间戳',\n" +
            "  `create_time` datetime DEFAULT NULL COMMENT '创建时间',\n" +
            "  `update_time` datetime DEFAULT NULL COMMENT '修改时间',\n" +
            "  `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',\n" +
            "  PRIMARY KEY (`id`)\n" +
            ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4; ";

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
