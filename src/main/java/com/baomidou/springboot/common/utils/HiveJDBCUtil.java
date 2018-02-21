package com.baomidou.springboot.common.utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: CuiCan
 * @Date: 2017/6/27
 * @Time: 17:40
 * @Description:
 */
public class HiveJDBCUtil {

    public static String driverName = "org.apache.hive.jdbc.HiveDriver";
    public static String connectURL = "jdbc:hive2://172.16.52.110:10000/default";
    public static String connectUSER = "";
    public static String connectPWD = "";

//    static{
//        driverName = ProfileManager.getValue("hive.jdbc.driver","");
//        connectURL = ProfileManager.getValue("hive.jdbc.connectURL", "");
//        connectUSER = ProfileManager.getValue("hive.jdbc.connectUSER", "");
//        connectPWD = ProfileManager.getValue("hive.jdbc.connectPWD", "");
//    }

    /**
     * 获取Hive连接
     *
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static Connection getConn() throws SQLException, ClassNotFoundException {
        Class.forName(driverName);
        Connection conn = DriverManager.getConnection(connectURL, connectUSER, connectPWD);
        return conn;
    }

    /**
     * 查询List集合
     *
     * @param sql
     * @param params
     * @return
     * @throws Exception
     */
    public static List<Map<String, Object>> queryForValueList(String[] commands, String sql, Object... params) throws Exception {
        ResultSet rs = null;
        PreparedStatement ps = null;
        List<Map<String, Object>> rlist = new ArrayList<Map<String, Object>>();
        Connection con = null;
        try {
            con = getConn();
            if (commands != null) {//执行调用命令，比如说设置一些前提条件，比如创建临时函数
                for (String command : commands) {
                    con.createStatement().execute(command);
                }
            }
            ps = con.prepareStatement(sql);
            if (params != null && params.length > 0) {
                int i = 1;
                for (Object o : params) {
                    ps.setObject(i, o);
                    i++;
                }
            }
            rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int size = rsmd.getColumnCount();
            Map<String, Object> map = null;
            while (rs.next()) {
                map = new HashMap<>();
                for (int i = 1; i <= size; i++) {
                    map.put(rsmd.getColumnName(i), rs.getObject(i));
                }
                rlist.add(map);
            }
        } catch (Exception e) {
            throw new RuntimeException("jdbc查询出错");
        } finally {
            if (con != null)
                con.close();
        }
        return rlist;
    }

    public static void main(String[] args) throws Exception {
        String[] commands = new String[]{"add jar /data0/custom/lib/hiveUDF.jar",
                "create temporary function group_concat as 'org.apache.hadoop.hive.udf.GroupConcatInSetUdf'"};
        String sql = "select group_concat(event_id) as EVENT_id,group_concat(activity_name) as activity_name " +
                "from pad_log where log_date = ? ";
        List<Map<String, Object>> list = queryForValueList(commands, sql, "20170127");
        System.out.println(list);
    }
}
