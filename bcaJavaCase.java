public class bcaJavaCase {
    private static final Integer mCurDetectCount = Integer.valueOf(0);

    protected void sampleSynchronizationOnStringOrBoxedCheck() {
        /**更新检测的次数1*/
        synchronized (mCurDetectCount) {  //高危SynchronizationOnStringOrBoxedCheck
            mCurDetectCount++;
            System.out.println("detectError检测失败 更新检测次数：" + mCurDetectCount);
        }
    }

    public static final String sampleStringBufferAndBuilderWithCharCheck(String str) {
        StringBuffer strBuf = new StringBuffer();
        StringBuffer tmpBuf = new StringBuffer('\\'); //高危：StringBufferAndBuilderWithCharCheck
        strBuf.append(StringEscapeUtils.unescapeJava(tmpBuf.toString()));
        return strBuf.toString();
    }

    private void example() {
        Thread myThread = new Thread(new RunnableJob());
        myThread.wait(2000);  //高危  WaitInSynchronizeCheck 和 ThreadWaitCallCheck
    }

    private void sampleTwoLocksWaitCheck() throws InterruptedException {
        // "wait" should not be called when multiple locks are held
        Object firstObj = new Object();
        Object secondObj = new Object();
        // threadB can't enter this block to request this.mon2 lock & release threadA
        synchronized (firstObj) {
            synchronized (secondObj) {
                // Noncompliant; threadA is stuck here holding lock on this.mon1
                secondObj.wait();   //高危  TwoLocksWaitCheck
            }
        }
    }

    private void sampleSynchronizationOnGetClassCheck() {
        // "getClass" should not be used for synchronization
        Object obj = new Object();
        synchronized (obj.getClass()) {  //高危 SynchronizationOnGetClassCheck
            System.out.println("SynchronizationOnGetClassCheck");
        }
    }

    private void sampleDoublePrefixOperatorCheck() {
        // Unary prefix operators should not be repeated
        int firstNum = 1;

        // Noncompliant; just use -i
        int secondNum = - - -firstNum;  //高危：DoublePrefixOperatorCheck
    }

    private void sampleWrongAssignmentOperatorCheck() {
        // "=+" should not be used instead of "+="
        int origin = 5;
        int num = 3;
        // Noncompliant; origin = 3
        origin =+ num;  //高危：WrongAssignmentOperatorCheck
    }

    private void sampleThreadRunCheck() {
        // Thread.run() should not be called directly
        Thread myThread = new Thread(() -> System.out.println("ThreadRunCheck"));
        myThread.run();  //高危：ThreadRunCheck
    }

    private void sampleKeywordAsIdentifierCheck() {
        // Future keywords should not be used as names
        String _ = " ";  //高危：KeywordAsIdentifierCheck
    }

    private void sampleBadComparison(Double y) {
        boolean x = (y == Double.NaN);
    }

     public static void main(String[] args,String pwd) throws ClassNotFoundException, SQLException {
        // TODO Auto-generated method stub
        //1.注册启动，使用反射技术，固定格式
                Class.forName("com.mysql.jdbc.Driver");
                 
                //2.获取数据库连接，DriverManager类中的静态方法
                //static Connection getConnection(String url,String user,String password)
                //返回值是Connection接口的实现类，在mysql驱动程序中
                //url的格式：数据库地址 jdbc:mysql://连接主机IP：端口号//数据库名字
                String url = "jdbc:mysql://localhost:3306/mybase";
                
                //高危：BCA-JAVA ExposedDBPasswd
                Connection con = DriverManager.getConnection(url,"root","123456");
//              System.out.println(con);
                 
                //3.获取语句执行平台，通过数据库连接对象，获取到SQL语句的执行者对象
                //con对象调用方法，Statement createStatement() 获取Statement对象，将SQL语句发送到数据库
                //返回值是Statement接口的实现类对象，在mysql驱动程序中
                Statement stat = (Statement) con.createStatement();
                 
                //4.执行sql语句
                //通过执行者对象调用方法执行SQL语句，获取结果
                //int executeUpdate(String sql)   执行数据库中的SQL语句，inset,delete,update
                //返回值是int,操作成功数据表多少行
//              String sql = "INSERT INTO sort(sname,sprice,sdesc) VALUES('手机11','5999','iphone11')";
//              int row = stat.executeUpdate(sql);
//              System.out.println(row);
                 
                 
                //查询SQL语句
                String sql = "SELECT * FROM sort";
                //ReaultSet executeQuery(String sql) 执行SQL语句中的select查询
                //返回值ResultSet接口的实现类对象，实现类在mysql驱动中
                ResultSet rs = stat.executeQuery(sql);
                System.out.println(rs);
                 
                while(rs.next()){
                    //获取每列的数据，使用的是ResultSet接口的getXXX方法,写String列名
                    System.out.println(rs.getInt("sid")+"   "+rs.getString("sname")+"   "+rs.getDouble("sprice")+"   "+rs.getString("sdesc"));
                }
                 
                //6.释放资源
                rs.close();
                stat.close();
                con.close();
    }

     @Nonnull
    public static String legacyColors( String message) {
        if (message == "N/A"){
            return null
        }
        StringBuilder result = new StringBuilder();
        String[] tokens = message.split("\\{|}");
        outer:
        for (String token : tokens) {
            for (TextColor color : TextColor.values()) {
                if (color.name().equalsIgnoreCase(token)) {
                    result.append(color);
                    continue outer;
                }
            }

            result.append(token);
        }

        return result.toString();
    }
}

    public class ObjectFinalizeOverridenCallsSuperFinalizeCheck extends T2{
    private void releaseSomeResources() {
        T2 t2 = new T2();
        Integer temp = t2.i;
        System.out.println("releaseSomeResources：……"+temp);
    }

    @Override
    protected void finalize() throws Throwable{
        super.finalize();  // 高危：ObjectFinalizeOverridenCallsSuperFinalizeCheck
        releaseSomeResources();
    }
}

public class T2 {
    // 假设是需要清理的资源
    public Integer i = new Integer(0);

    @Override
    protected void finalize() throws Throwable {
        // 在此处清理外层类的资源
        i = null;
        System.out.println("T2资源被清理");
    }
}
}

public class ObjectFinalizeOverloadedCheck{
    public int finalize(int someParameter) { 
        System.out.println("finalize is called by the Garbage Collector");
        return 0;
    }
}
