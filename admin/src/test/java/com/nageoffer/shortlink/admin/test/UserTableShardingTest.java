package com.nageoffer.shortlink.admin.test;

public class UserTableShardingTest {

    public static final String SQL = "CREATE TABLE `t_link_goto_%d`(\n" +
            "      `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
            "      `gid` varchar(32) DEFAULT 'default' COMMENT  '分组标识',\n" +
            "      `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',\n" +
            "      PRIMARY KEY (`id`))\n" +
            "      ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

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
