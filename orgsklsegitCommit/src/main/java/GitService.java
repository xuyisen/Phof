




import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

public class GitService {

    public static Map<String,String> month=new HashMap<String, String>(){{
        put("Jan","01");
        put("Feb","02");
        put("Mar","03");
        put("Apr","04");
        put("May","05");
        put("Jun","06");
        put("Jul","07");
        put("Aug","08");
        put("Sep","09");
        put("Oct","10");
        put("Nov","11");
        put("Dec","12");
    }};

    public static List<FileAndFunction> fileAndFunctions(String fileName){
        List<FileAndFunction> fileAndFunctions=new ArrayList<FileAndFunction>();
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                String str[]=tempString.split("%%%");
                FileAndFunction fileAndFunction=new FileAndFunction();
                fileAndFunction.file=str[0].split("D:/Git/scala_project/finagle/")[1];
                fileAndFunction.function=str[1];
                fileAndFunctions.add(fileAndFunction);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return fileAndFunctions;
    }

    public static String  readFileByLines(String fileName,String tabelName) {
        CommitTem commitTem=new CommitTem();
        String url="jdbc:mysql://localhost:3306/db_finagle?useSSL=false";
        String user="root";
        String password="123456";
        String StringResult = null;
        String tempFile="";
        PreparedStatement psql=null;
        String tempStrList[];
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection=DriverManager.getConnection(url,user,password);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(fileName)));
            BufferedReader in = new BufferedReader(new InputStreamReader(bis, "utf-8"), 10 * 1024 * 1024);// 10M缓存
            boolean start=true;
            while (in.ready()) {
                String line = in.readLine();
                if(line.startsWith("commit")){
                   tempStrList=line.split(" ");
                    if(start){
                        commitTem.commit=tempStrList[1];
                        start=false;
                        continue;
                    }
                    if(tempStrList[0].equals("commit")&&tempStrList[1].length()==40){
                        commitTem.file=tempFile.replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", "emoji");
                        StringResult = tempFile;
                         psql = connection.prepareStatement("insert into "+tabelName+" (commit,author,file,date) "
                                + "values(?,?,?,?)");
                         psql.setString(1,commitTem.commit);
                         psql.setString(2,commitTem.author);
                         psql.setString(3,commitTem.file);
                         psql.setObject(4,commitTem.date);
                         psql.executeUpdate();
                         commitTem.commit=tempStrList[1];
                         tempFile="";
                    }else{
                        tempFile+=line;
                    }
                }else if(line.startsWith("Author:")){
                    tempStrList=line.split(":");
                    commitTem.author=tempStrList[1];
                }else if(line.startsWith("Date:")){
                    commitTem.date=getDate(line);
                }else if(line.startsWith("- ")){
                    continue;
                }else{
                    tempFile+=line;
                }
            }
            psql = connection.prepareStatement("insert into "+tabelName+" (commit,author,file,date) "
                    + "values(?,?,?,?)");
            psql.setString(1,commitTem.commit);
            psql.setString(2,commitTem.author);
            psql.setString(3,commitTem.file);
            psql.setObject(4,commitTem.date);
            psql.executeUpdate();
            in.close();
        } catch (IOException ex) {
            System.out.println(StringResult);
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println(StringResult);
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println(StringResult);
            e.printStackTrace();
        }
        return StringResult;
    }

    public static void addAuthor(){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function";
        String url2="jdbc:mysql://localhost:3306/db_finagle";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        String tableName1="c_fpinscala_test";
        String tableName2="fpinscala";
        int line=0;
        String subString="D:/Git/scala_project/fpinscala";

        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url2,user,password);
            psql1=connection1.prepareStatement("select count(*) totalCount from "+tableName1);
            ResultSet rset=psql1.executeQuery();
            if(rset.next()){
                line=rset.getInt("totalCount");
            }
            for(int i=1;i<=line;i++){
                psql1=connection1.prepareStatement(" select * from "+tableName1+" where id ="+i);
                ResultSet resultSet=psql1.executeQuery();
                if (resultSet.next()){
                    String path=resultSet.getString("path");
                    path=path.replace("\\","/");
                    path=path.split(subString)[1];
                    String firstLine=resultSet.getString("firstLine").split("\n")[0];
                    psql2=connection2.prepareStatement("SELECT\n" +
                            tableName2+".file,\n" +
                            tableName2+".`commit`,\n" +
                            tableName2+".author,\n" +
                            tableName2+".date\n" +
                            "FROM `"+tableName2+"`\n" +
                            "WHERE\n" +
                            tableName2+".file LIKE ? AND\n" +
                            tableName2+".file LIKE ? \n" +
                            "ORDER BY\n" +
                            tableName2+".date ASC");
                    psql2.setString(1,"%"+path+"%");
                    psql2.setString(2,"%"+firstLine+"%");
                    ResultSet resultSet1=psql2.executeQuery();
                    if(resultSet1.next()){
                        String author=resultSet1.getString("author");
                        psql1=connection1.prepareStatement("update `"+tableName1+"` set author = ? where id = "+i);
                        psql1.setString(1,author);
                        psql1.executeUpdate();
                    }else{
                        String split[]=path.split("/");
                        path=split[split.length-1];
                        psql2.setString(1,"%"+path+"%");
                        psql2.setString(2,"%"+firstLine+"%");
                        ResultSet resultSet2=psql2.executeQuery();
                        if(resultSet2.next()){
                            String author=resultSet2.getString("author");
                            psql1=connection1.prepareStatement("update `"+tableName1+"` set author = ? where id = "+i);
                            psql1.setString(1,author);
                            psql1.executeUpdate();
                        }

                    }

                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static LocalDateTime getDate(String str) {
        String tempStr[]=str.split("\\s+");
        DateTimeFormatter dateFormatter=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateStr=tempStr[5]+"-"+month.get(tempStr[2])+"-"+(tempStr[3].length()==2? tempStr[3]:"0"+tempStr[3])+" "+tempStr[4];
        return LocalDateTime.parse(dateStr,dateFormatter);

    }
    public static int countAuthor(String tableName){
        String url2="jdbc:mysql://localhost:3306/db_dec_call_function";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        String tableName1="c_"+tableName+"_s";
        String tableName2=tableName+"_s";
        int sum=0;
        int sum2=0;

        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url2,user,password);

            psql1=connection1.prepareStatement(" select * from "+tableName1+" where type= 1 and first_author is not null");
            ResultSet resultSet=psql1.executeQuery();
            while (resultSet.next()) {
                int dec_id = resultSet.getInt("dec_id");
                String author = resultSet.getString("author").split("<")[0];
                psql1 = connection1.prepareStatement("select * from " + tableName2 + " where id =" + dec_id+ " and author is not null");
                ResultSet resultSet1 = psql1.executeQuery();
                if (resultSet1.next()) {
                    String author2=resultSet1.getString("author").split("<")[0];
                    if (author.equalsIgnoreCase(author2)) {
                        sum++;
                    }
                }
                sum2++;
            }


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sum;
    }

    public static void updateDate(){
        String url2="jdbc:mysql://localhost:3306/db_dec_call_function";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        String tableName1="c_spray";
        String tableName2="c_spray_test";
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url2,user,password);

            psql1=connection1.prepareStatement(" select * from "+tableName1+ " where type = 4");
            ResultSet resultSet=psql1.executeQuery();
            while (resultSet.next()) {
                int dec_id = resultSet.getInt("dec_id");
                String path = resultSet.getString("path");
                String firstLine=resultSet.getString("firstLine");
                int type=resultSet.getInt("type");
                String fullLine=resultSet.getString("fullLine");
                String author=resultSet.getString("author");
                psql1 = connection1.prepareStatement("select * from " + tableName2 + " where dec_id=? and path=? and  firstLine=? and type=?");
                psql1.setInt(1,dec_id);
                psql1.setString(2,path);
                psql1.setString(3,firstLine);
                psql1.setInt(4,type);
                ResultSet resultSet1 = psql1.executeQuery();
                if (!resultSet1.next()) {
                    psql1 = connection1.prepareStatement("INSERT INTO "+ tableName2+" (path,type,dec_id,firstLine,fullLine,author) VALUES (?,?,?,?,?,?) ");
                    psql1.setString(1, path);
                    psql1.setInt(2, type);
                    psql1.setInt(3, dec_id);
                    psql1.setString(4, firstLine);
                    psql1.setString(5,fullLine);
                    psql1.setString(6,author);
                    psql1.executeUpdate();
                }


            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(String file,String str){
        try {

            File file1 = new File(file);
            if (!file1.exists()) {
                file1.createNewFile();
            }
            FileWriter fw = new FileWriter(file1.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(str);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void handleSemantic_dec(String fileName,String tableName){
        String url="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql=null;
        String file_path="";//路径
        String code="";     // 源代码
        String name="";     //方法名字
        int type=0;         //普通函数 还是 高阶函数
        String line_num="";     // 代码行数
        int private_type=0; //私有函数还是非私有函数
        int lambda=0;       //lambda函数还是非lambda函数
        int nesting=0;      //嵌套函数还是非嵌套函数
        int h_type=0;
        boolean is_method=false;   //是否是函数声明
        boolean start=false;
        String pattern=".*\\sFunction[0-9].*";

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection=DriverManager.getConnection(url,user,password);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(fileName)));
            BufferedReader in = new BufferedReader(new InputStreamReader(bis, "utf-8"), 10 * 1024 * 1024);// 10M缓存
            while (in.ready()) {
                String line = in.readLine();
                if (line.startsWith("Uri => ")) {
                    file_path = line.split("Uri => ")[1];
                } else if (line.startsWith("Symbols:")) {
                    start = true;
                } else if (line.startsWith("Occurrences:")) {
                    start = false;
                } else {
                    if (start) {
                        if (line.contains(" => ")) {
                            String temStr[] = line.split(" => ");
                            name = temStr[0];
                            code = temStr[1];
                            type=0;
                            h_type=0;
                            if (temStr[1].contains("method")) {
                                if(temStr[1].startsWith("method")||
                                        temStr[1].startsWith("abstract method")||
                                        temStr[1].startsWith("final method")||
                                temStr[1].startsWith("protected method")||
                                temStr[1].startsWith("public method")){
                                    private_type=0;
                                    lambda=0;
                                    is_method=true;
                                    if(temStr[1].contains("):")){
                                        String temStr2[]=temStr[1].split("\\):");
                                        if(Pattern.matches(pattern,temStr2[0])&&Pattern.matches(pattern,temStr2[1])){
                                            h_type=3;
                                            type=1;
                                        }else if(Pattern.matches(pattern,temStr2[0])){
                                            type=1;
                                            h_type=1;
                                        }else if(Pattern.matches(pattern,temStr2[1])){
                                            type=1;
                                            h_type=2;
                                        }else{
                                            type=0;
                                            h_type=0;
                                        }
                                    }else if(Pattern.matches(pattern,temStr[1])){
                                        type=1;
                                        h_type=2;
                                    }else{
                                        type=0;
                                        h_type=0;
                                    }
                                }else if(temStr[1].startsWith("private method")){
                                    private_type=1;
                                    lambda=0;
                                    is_method=true;
                                    if(temStr[1].contains("):")){
                                        String temStr2[]=temStr[1].split("\\):");
                                        if(Pattern.matches(pattern,temStr2[0])&&Pattern.matches(pattern,temStr2[1])){
                                            h_type=3;
                                            type=1;
                                        }else if(Pattern.matches(pattern,temStr2[0])){
                                            type=1;
                                            h_type=1;
                                        }else if(Pattern.matches(pattern,temStr2[1])){
                                            type=1;
                                            h_type=2;
                                        }else{
                                            type=0;
                                            h_type=0;
                                        }
                                    }else if(Pattern.matches(pattern,temStr[1])){
                                        type=1;
                                        h_type=2;
                                    }else{
                                        type=0;
                                        h_type=0;
                                    }
                                }else if(temStr[1].startsWith("val method")||temStr[1].startsWith("lazy val method")){
                                    if(temStr[1].contains(":")){
                                        String temStr2[]=temStr[1].split(": ");
                                        if(Pattern.matches(pattern,temStr2[1])){
                                            is_method=true;
                                            lambda=1;
                                            private_type=0;
                                            String temStr3=temStr2[1].replace(" Function","");
                                            if(temStr3.contains("],")){
                                                String temStr4[]=temStr3.split("],");
                                                if(Pattern.matches(pattern,temStr4[0])&&Pattern.matches(pattern,temStr4[1])){
                                                    h_type=3;
                                                    type=1;
                                                }else if(Pattern.matches(pattern,temStr4[0])){
                                                    h_type=1;
                                                    type=1;
                                                }else if(Pattern.matches(pattern,temStr4[1])){
                                                    h_type=2;
                                                    type=1;
                                                }else{
                                                    h_type=0;
                                                    type=0;
                                                }
                                            }else if(temStr3.contains(",")){
                                                String temStr4[]=temStr3.split(",");
                                                if(Pattern.matches(pattern,temStr4[1])){
                                                    h_type=2;
                                                    type=1;
                                                }else{
                                                    h_type=0;
                                                    type=0;
                                                }
                                            }else {
                                                type=0;
                                                h_type=0;
                                            }

                                        }else {
                                            type=0;
                                            h_type=0;
                                        }
                                    }else {
                                        type=0;
                                        h_type=0;
                                    }
                                }else if(temStr[1].startsWith("private val method")){
                                    if(temStr[1].contains(":")){
                                        String temStr2[]=temStr[1].split(": ");
                                        if(Pattern.matches(pattern,temStr2[1])){
                                            is_method=true;
                                            lambda=1;
                                            private_type=1;
                                            String temStr3=temStr2[1].replace(" Function","");
                                            if(temStr3.contains("],")){
                                                String temStr4[]=temStr3.split("],");
                                                if(Pattern.matches(pattern,temStr4[0])&&Pattern.matches(pattern,temStr4[1])){
                                                    h_type=3;
                                                    type=1;
                                                }else if(Pattern.matches(pattern,temStr4[0])){
                                                    h_type=1;
                                                    type=1;
                                                }else if(Pattern.matches(pattern,temStr4[1])){
                                                    h_type=2;
                                                    type=1;
                                                }else{
                                                    h_type=0;
                                                    type=0;
                                                }
                                            }else if(temStr3.contains(",")){
                                                String temStr4[]=temStr3.split(",");
                                                if(Pattern.matches(pattern,temStr4[1])){
                                                    h_type=2;
                                                    type=1;
                                                }else{
                                                    h_type=0;
                                                    type=0;
                                                }
                                            }else {
                                                type=0;
                                                h_type=0;
                                            }

                                        }else {
                                            type=0;
                                            h_type=0;
                                        }
                                    }else {
                                        type=0;
                                        h_type=0;
                                    }
                                }else {
                                    type=0;
                                    h_type=0;
                                }
                            }else {
                                type=0;
                                h_type=0;
                            }
                            if(is_method){
                                is_method=false;
                                if(name.startsWith("local")){
                                    nesting=1;
                                }else{
                                    nesting=0;
                                }
                                psql= connection.prepareStatement("INSERT INTO "+ tableName+" (file_path,type,name,line_num,nesting,private,lambda,h_type,code) VALUES (?,?,?,?,?,?,?,?,?) ");
                                psql.setString(1,file_path);
                                psql.setInt(2,type);
                                psql.setString(3,name);
                                psql.setString(4,line_num);
                                psql.setInt(5,nesting);
                                psql.setInt(6,private_type);
                                psql.setInt(7,lambda);
                                psql.setInt(8,h_type);
                                psql.setString(9,code);
                                psql.executeUpdate();
                            }
                        }
                    }
                }

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void handleSemantic_call(String fileName,String tableName1,String tableName2){
        String url="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        String file_path="";//路径
        String line_num="";  //具体的位置
        int dec_id;         //声明函数id
        String name="";     //调用的方法的名字
        int type=0;         //是否是高阶函数的调用
        boolean start=false;  //开始遍历

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection=DriverManager.getConnection(url,user,password);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(fileName)));
            BufferedReader in = new BufferedReader(new InputStreamReader(bis, "utf-8"), 20 * 1024 * 1024);// 10M缓存
            while (in.ready()) {
                String line = in.readLine();
                if (line.startsWith("Uri => ")) {
                    file_path = line.split("Uri => ")[1];
                } else if (line.startsWith("Occurrences:")) {
                    start = true;
                } else if (line.startsWith("Summary:")) {
                    start = false;
                } else {
                    if (start) {
                        if (line.contains(" => ")) {
                            String temStr[] = line.split(" => ");
                            line_num = temStr[0];
                            name = temStr[1];
                            if (name.startsWith("local")) {
                                psql1 = connection.prepareStatement(" select * from " + tableName1 + " where name = ? and file_path = ?");
                                psql1.setString(1, name);
                                psql1.setString(2,file_path);
                            } else {
                                psql1 = connection.prepareStatement(" select * from " + tableName1 + " where name = ?");
                                psql1.setString(1, name);
                            }
                            ResultSet resultSet = psql1.executeQuery();
                            if (resultSet.next()) {
                                dec_id = resultSet.getInt("id");
                                type=resultSet.getInt("type");
                                psql2 = connection.prepareStatement("INSERT into " + tableName2 + " (dec_id,line_num,type,file_path) values (?,?,?,?)");
                                psql2.setInt(1, dec_id);
                                psql2.setString(2, line_num);
                                psql2.setInt(3,type);
                                psql2.setString(4,file_path);
                                psql2.executeUpdate();
                            }
                        }else if(line.contains(" <= ")){
                            String temStr[] = line.split(" <= ");
                            line_num = temStr[0];
                            name = temStr[1];
                            psql1 = connection.prepareStatement(" select * from " + tableName1 + " where name = ? and file_path = ?");
                            psql1.setString(1, name);
                            psql1.setString(2, file_path);
                            ResultSet resultSet=psql1.executeQuery();
                            if(resultSet.next()){
                                dec_id=resultSet.getInt("id");
                                psql2=connection.prepareStatement("update `" +tableName1 + "`set line_num=?  where id = ?");
                                psql2.setString(1,line_num);
                                psql2.setInt(2,dec_id);
                                psql2.executeUpdate();
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void handleSemantic_symbol(String fileName,String tableName){
        String url="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql=null;
        String file_path="";//路径
        String desc="";     // 描述
        String name="";     //方法名字
        boolean start=false;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection=DriverManager.getConnection(url,user,password);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(fileName)));
            BufferedReader in = new BufferedReader(new InputStreamReader(bis, "utf-8"), 10 * 1024 * 1024);// 10M缓存
            while (in.ready()) {
                String line = in.readLine();
                if (line.startsWith("Uri => ")) {
                    file_path = line.split("Uri => ")[1];
                } else if (line.startsWith("Symbols:")) {
                    start = true;
                } else if (line.startsWith("Occurrences:")) {
                    start = false;
                } else {
                    if (start&&!file_path.contains("src_managed")) {
                        if (line.contains(" => ")) {
                            String temStr[] = line.split(" => ");
                            name = temStr[0];
                            desc = temStr[1];
                            psql= connection.prepareStatement("INSERT INTO "+ tableName+" (file_path,`desc`,name) VALUES (?,?,?) ");
                            psql.setString(1,file_path);
                            psql.setString(2,desc);
                            psql.setString(3,name);
                            psql.executeUpdate();
                        }
                    }
                }

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void handleSemantic_occu(String fileName,String tableName1,String tableName2){
        String url="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        String file_path="";//路径
        String line_num="";  //具体的位置
        String symbol="";    //符号
        String name="";     //调用的方法的名字
        String desc="";     //描述
        boolean start=false;  //开始遍历

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection=DriverManager.getConnection(url,user,password);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(fileName)));
            BufferedReader in = new BufferedReader(new InputStreamReader(bis, "utf-8"), 10 * 1024 * 1024);// 10M缓存
            while (in.ready()) {
                String line = in.readLine();
                if (line.startsWith("Uri => ")) {
                    file_path = line.split("Uri => ")[1];
                } else if (line.startsWith("Occurrences:")) {
                    start = true;
                } else if (line.startsWith("Summary:")) {
                    start = false;
                } else {
                    if (start) {
                        if (line.contains(" => ")||line.contains(" <= ")) {
                            String temStr[];
                            if(line.contains(" => ")){
                                temStr= line.split(" => ");
                                symbol="=>";
                            }else{
                                temStr=line.split(" <= ");
                                symbol="<=";
                            }
                            line_num = temStr[0];
                            name = temStr[1];
                            psql2 = connection.prepareStatement("INSERT into " + tableName2 + " (line_num,symbol,name,file_path) values (?,?,?,?)");
                            psql2.setString(1, line_num);
                            psql2.setString(2,symbol);
                            psql2.setString(3,name);
                            psql2.setString(4,file_path);
                            psql2.executeUpdate();

                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void update_author_1(String tableName1,String tableName2,String subString){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url2="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        int line=0;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url2,user,password);
            psql1=connection1.prepareStatement("select * from "+tableName1+" where type=1 and author is null");
            ResultSet rset=psql1.executeQuery();
            while(rset.next()){
                String line_num=rset.getString("line_num");
                if(line_num.length()>=1){
                    int id=rset.getInt("id");
                    String source_code=rset.getString("source_code");
                    String file_path=rset.getString("file_path");
                    file_path = subString + file_path.replace("/", "\\");
                    psql2 = connection2.prepareStatement("select * from " + tableName2 + " where path =? and firstLine like ?");
                    psql2.setString(1, file_path);
                    psql2.setString(2,   source_code + "%");
                    ResultSet resultSet1 = psql2.executeQuery();
                    if (resultSet1.next()) {
                        String author = resultSet1.getString("author");
                        psql1 = connection1.prepareStatement("update `" + tableName1 + "` set author = ? where id = " + id);
                        psql1.setString(1, author);
                        psql1.executeUpdate();
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public static void update_author_2(String tableName1,String tableName2,String columnName){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url2="jdbc:mysql://localhost:3306/db_finagle?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url2,user,password);
            psql1=connection1.prepareStatement("select * from "+tableName1+ " where id> 51417  and `type` = 0 and `first_author` is null and line_num like '%[%' and `source_code` is not null and `full_code` is not null");
            ResultSet rset=psql1.executeQuery();
            while(rset.next()){
                int id=rset.getInt("id");
                String file_path=rset.getString("file_path");
                String source_code=rset.getString(columnName);
                psql2=connection2.prepareStatement("SELECT\n" +
                        tableName2+".file,\n" +
                        tableName2+".`commit`,\n" +
                        tableName2+".author,\n" +
                        tableName2+".date\n" +
                        "FROM `"+tableName2+"`\n" +
                        "WHERE\n" +
                        tableName2+".file LIKE ? AND\n" +
                        tableName2+".file LIKE ? \n" +
                        "ORDER BY\n" +
                        tableName2+".date ASC");
                psql2.setString(1,"%"+source_code+"%");
                psql2.setString(2,"%"+file_path+"%");
//                ResultSet resultSet1=psql2.executeQuery();
//                if(resultSet1.next()){
//                    String author=resultSet1.getString("author");
//                    psql1=connection1.prepareStatement("update `"+tableName1+"` set author = ? where id = "+id);
//                    psql1.setString(1,author);
//                    psql1.executeUpdate();
//                }else{
//                    System.out.println(id+" , "+file_path+" , "+source_code);
//                }
//                psql2=connection2.prepareStatement("SELECT\n" +
//                        tableName2+".file,\n" +
//                        tableName2+".`commit`,\n" +
//                        tableName2+".author,\n" +
//                        tableName2+".date\n" +
//                        "FROM `"+tableName2+"`\n" +
//                        "WHERE\n" +
//                        "MATCH(`file`) AGAINST(?) AND\n" +
//                        "MATCH(`file`) AGAINST(?)");
//                psql2.setString(1,source_code);
//                psql2.setString(2,file_path);
//
                ResultSet resultSet1=psql2.executeQuery();
                HashMap<String,Integer> authors = new HashMap<>();
                boolean has_find = false;
                int num_author = 0;
                int num_commit = 0;
                String first_author ="";
                while(resultSet1.next()){
                    String author=resultSet1.getString("author");
                    if(!has_find){
                        first_author =author;
                    }
                    has_find = true;
                    num_commit++;
                    if(authors.containsKey(author)){
                        int number = authors.get(author)+1;
                        authors.put(author,number);
                    }else {
                        authors.put(author, 1);
                    }
                }
                if(has_find){
                    int max = 0;
                    String true_author = "";
                    for (String key : authors.keySet()){
                        num_author++;
                        int temp = authors.get(key);
                        if(temp>max){
                            max = temp;
                            true_author = key;
                        }
                    }
                    authors.clear();
                    psql1=connection1.prepareStatement("update `"+tableName1+"` set author = ?, first_author = ?, num_author = ?, num_commit = ? where id = "+id);
                    psql1.setString(1,true_author);
                    psql1.setString(2,first_author);
                    psql1.setInt(3,num_author);
                    psql1.setInt(4,num_commit);
                    psql1.executeUpdate();
                }else{
                    System.out.println(id+" , "+file_path+" , "+source_code);
                }
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void update_author_3(String tableName1,String tableName2){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url2="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url2,user,password);
            psql1=connection1.prepareStatement("select * from "+tableName1+" where type=1 and author is null");
            ResultSet rset=psql1.executeQuery();
            while(rset.next()){
                int dec_id=rset.getInt("dec_id");
                int id=rset.getInt("id");
                System.out.println(id);
                psql2=connection2.prepareStatement("select * from "+tableName2+" where id = "+dec_id);
                ResultSet resultSet=psql2.executeQuery();
                if(resultSet.next()){
                    String author=resultSet.getString("author");
                    psql1 = connection1.prepareStatement("update `" + tableName1 + "` set author = ? where id = " + id);
                    psql1.setString(1, author);
                    psql1.executeUpdate();

                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void countType(String tableName1,String tableName2,int type){
        int count1=0;
        int count2=0;
        int count3=0;
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url2="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url2,user,password);
            psql1=connection1.prepareStatement("select * from "+tableName1+" where type = 1 and line_num like '%[%' and file_path not like '%/target/%' and h_type = "+type);
            ResultSet rset=psql1.executeQuery();
            while(rset.next()){
                int id=rset.getInt("id");
                psql2=connection2.prepareStatement("select count(*) as totalcount from "+tableName2+" where dec_id = "+id);
                ResultSet resultSet=psql2.executeQuery();
                if(resultSet.next()){
                    int line=resultSet.getInt("totalcount");
                    if(line==0){
                        count3++;
                    }else if(line==1){
                        count2++;
                    }else if(line>=2){
                        count1++;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.print(count1+"   ");
        System.out.print(count2+"   ");
        System.out.print(count3+"   ");
    }

    public static void countPrivate(String tableName1,String tableName2){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url2="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        int count=0;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url2,user,password);
            psql1=connection1.prepareStatement("select * from "+tableName1+" where author is not null and private = 1 and type = 1 and line_num like '%[%' ");
            ResultSet rset=psql1.executeQuery();
            while(rset.next()){
                int id=rset.getInt("id");
                psql2=connection2.prepareStatement("select count(*) as totalcount from "+tableName2+" where dec_id = "+id);
                ResultSet resultSet=psql2.executeQuery();
                if(resultSet.next()){
                    int line=resultSet.getInt("totalcount");
                    if(line==0){
                        System.out.println(id);
                        count++;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("总数："+count);
    }

    public static void updateUnFind(String tableName1,String tableName2,String tableName3){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url2="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url2,user,password);
            psql1=connection1.prepareStatement("select * from "+tableName1+" where author is not null");
            ResultSet rset=psql1.executeQuery();
            while(rset.next()){
                int id=rset.getInt("id");
                psql2=connection2.prepareStatement("select count(*) as totalcount from "+tableName2+" where dec_id = "+id);
                ResultSet resultSet=psql2.executeQuery();
                if(resultSet.next()){
                    int line=resultSet.getInt("totalcount");
                    if(line==0){
                        System.out.println(id);
                        String name=rset.getNString("name");
                        if(!name.startsWith("local")) {
                            String subString = name;
                            if (name.contains("#")) {
                                subString = name.split("#")[0] + "#";
                            } else if (name.contains("\\.")) {
                                subString = name.split("\\.")[0] + "\\.";
                            }
                            psql1 = connection1.prepareStatement("select * from " + tableName3 + " where `name` like ? and symbol = ?");
                            psql1.setString(1, subString + "%");
                            psql1.setString(2, "=>");
                            ResultSet resultSet1 = psql1.executeQuery();
                            boolean hasp = false;
                            while (resultSet1.next()) {
                                String name1 = resultSet1.getString("name");
                                if (name1.equalsIgnoreCase(subString)) {
                                    hasp = true;
                                } else if (name1.startsWith(name)) {
                                    if (hasp) {
                                        psql1 = connection1.prepareStatement("insert into " + tableName2 + "(dec_id,line_num,type,file_path) values (?,?,?,?)");
                                        psql1.setInt(1, id);
                                        psql1.setString(2, resultSet1.getString("line_num"));
                                        psql1.setInt(3, 1);
                                        psql1.setString(4, resultSet1.getString("file_path"));
                                        psql1.executeUpdate();
                                        hasp = false;
                                    }
                                } else {
                                    hasp = false;
                                }
                            }
                        }


                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void countauthor2(String tableName1,String tableName2,String columnName, String call_type){
        int count1=0;
        int count2=0;
        int count3=0;
        int count4=0;
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url2="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url3="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";

        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        PreparedStatement psql3;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url2,user,password);
            Connection connection3=DriverManager.getConnection(url3,user,password);
            psql1=connection1.prepareStatement("select * from "+tableName1+" where lineNum2 is not null and file_path not like '%src_managed%'");
            ResultSet rset=psql1.executeQuery();
            while(rset.next()){
                int id=rset.getInt("id");
                System.out.println(id);
                int type=0;
                String author=rset.getString(columnName).split("<")[0];
                int tempNum=0;
                int sum=0;
                psql2=connection2.prepareStatement("select * from "+tableName2+" where `code` is not null and dec_id = "+id);
                ResultSet resultSet=psql2.executeQuery();
                while(resultSet.next()){
                    sum++;
                    int id_2 =  resultSet.getInt("id");
                    System.out.println(id_2);
                    String author2=resultSet.getString(columnName).split("<")[0];
                    if(author.equalsIgnoreCase(author2)){
                        tempNum++;
                    }
                }
                if(sum==0){
                    count1++;
                    type=1;
                }else if(tempNum==0){
                    type=2;
                    count2++;
                }else if(tempNum==sum){
                    type=3;
                    count3++;
                }else{
                    type=4;
                    count4++;
                }
                psql3=connection3.prepareStatement("update "+ tableName1+ " set "+call_type+"= ? where id = ?");
                psql3.setInt(1,type);
                psql3.setInt(2,id);
                psql3.executeUpdate();

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.print(count3+"   ");
        System.out.print(count2+"   ");
        System.out.print(count4+"   ");
        System.out.println(count1);
    }

    public static void updateOverride(String tableName1,String tableName2,String tableName3,String tableName4){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;

        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            psql1=connection1.prepareStatement("SELECT * from "+tableName1+" where id not in(SELECT dec_id from "+tableName2+" where type=1) and type=1 and line_num LIKE '[%' and `name` not like 'local%' and `code` not like '%PartialFunction%' and source_code like '%override%' and override is null;");
            ResultSet resultSet=psql1.executeQuery();
            String className;
            String methodName;
            Stack<String> exts=new Stack<>();
            int j=0;
            while(resultSet.next()){
                j++;
                String name=resultSet.getString("name");
                className="";
                methodName="";
                exts.clear();
                int main_id=resultSet.getInt("id");
                System.out.println(j+":  "+main_id);
                if(main_id==16160){
                    continue;
                }
                if(name.contains("#")){
                    String tempStr[]=name.split("#");
                    for(int i=0;i<tempStr.length-1;i++){
                        className=className+tempStr[i]+"#";
                    }
                    methodName=tempStr[tempStr.length-1];
                    String tempStr1[]=methodName.split("\\.");
                    if(tempStr1.length>1){
                        for(int i=0;i<tempStr1.length-1;i++){
                            className=className+tempStr1[i]+".";
                        }
                    }
                    methodName=tempStr1[tempStr1.length-1]+".";
                }else if(name.contains(".")){
                    String tempStr[]=name.split("\\.");
                    for(int i=0;i<tempStr.length-1;i++){
                        className=className+tempStr[i]+".";
                    }
                    methodName=tempStr[tempStr.length-1]+".";
                }
                if(methodName.contains("(+")){
                    String tempStr5[]=methodName.split("\\(+");
                    methodName=tempStr5[0]+"().";
                }
                handSQL(className,connection1,tableName3,methodName,tableName1,tableName4,main_id,false);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static boolean handSQL(String className,Connection connection1,String tableName3,String methodName,String tableName1,String tableName4,int main_id,boolean hasFind) throws SQLException {

        if(!className.equalsIgnoreCase("")){
            Stack<String>exts=new Stack<>();
           PreparedStatement psql2=connection1.prepareStatement("select * from "+ tableName3+" where `name` = ?");
            psql2.setString(1,className);
            ResultSet resultSet1=psql2.executeQuery();
            if(resultSet1.next()){
                String desc=resultSet1.getString("desc");
                if(desc.contains("extends")){
                    String tempStr1[]=desc.split("extends");
                    if(tempStr1.length>1){
                        String tempStr2[]=tempStr1[1].split("with");
                        for(String s:tempStr2){
                            String tempStr3=s;
                            if(tempStr3.contains("(")){
                                tempStr3=tempStr3.split("\\(")[0];
                            }
                            if(tempStr3.contains("[")){
                                tempStr3=tempStr3.split("\\[")[0];
                            }

                            if(tempStr3.contains(".")){
                                String tempStr4[]=tempStr3.split("\\.");
                                tempStr3=tempStr4[tempStr4.length-1];
                            }

                            if(tempStr3.contains("{")){
                                tempStr3=tempStr3.split("\\{")[0];
                            }
                            tempStr3=tempStr3.trim();
                            if(!tempStr3.equals("AnyRef")&&
                                    !tempStr3.equals("Serializable")&&
                            !tempStr3.equals("Any")){
                                exts.push(tempStr3);
                            }
                        }

                    }
                }

            }
            while(!exts.isEmpty()&&!hasFind){
                String ext=exts.pop();
                PreparedStatement psql3=connection1.prepareStatement("select * from "+tableName4+" where `name` = ? and symbol = '<='" );
                psql3.setString(1,className);
                ResultSet resultSet2=psql3.executeQuery();
                if(resultSet2.next()) {
                    int id = resultSet2.getInt("id");
                    PreparedStatement psql4 = connection1.prepareStatement("select * from " + tableName4 + " where id < ? and id > ? and `name` regexp ?");
                    psql4.setInt(2,id);
                    psql4.setInt(1, id + 40);
                    psql4.setString(3, "(\\.|/|#)"+ext+"(#|\\.)$");
                    ResultSet resultSet3 = psql4.executeQuery();
                    if (resultSet3.next()) {
                        String className1 = resultSet3.getString("name");
                        String functionName=className1 + methodName;
                        PreparedStatement psql5 = connection1.prepareStatement("select * from " + tableName3 + " where `name` = ?");
                        psql5.setString(1, functionName);
                        ResultSet resultSet4 = psql5.executeQuery();
                        if (resultSet4.next()) {
                            String desc = resultSet4.getString("desc");
                            int override = 0;
                            if (desc.contains("abstract method")) {
                                override = 1;
                            } else if (desc.startsWith("method")||desc.startsWith("protected method")) {
                                override = 2;
                            } else {
                                int id1 = resultSet4.getInt("id");
                                System.out.println(id1 + "---" + desc);
                            }
                            PreparedStatement psql6 = connection1.prepareStatement("update " + tableName1 + " set override = ? where id =?");
                            psql6.setInt(1, override);
                            psql6.setInt(2, main_id);
                            psql6.executeUpdate();
                            return true;
                        } else {
                            hasFind=handSQL(className1,connection1,tableName3,methodName,tableName1,tableName4,main_id,hasFind);
                        }
                    }

                }
            }

        }
        return false;
    }

    public static void handAuthor(String tableName,String tableName2){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url2="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url3="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url4="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        PreparedStatement psql3;
        PreparedStatement psql4;
        int count=0;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url2,user,password);
            Connection connection3=DriverManager.getConnection(url3,user,password);
            Connection connection4=DriverManager.getConnection(url4,user,password);
            psql1=connection1.prepareStatement("select author , count(author) as count from "+tableName+" where type =1 and author is not null group by author");
            ResultSet rset=psql1.executeQuery();
            while(rset.next()){
                String author1=rset.getString("author");
                int dec_count=rset.getInt("count");
                String author=author1.split("<")[1].split(">")[0];
                psql3=connection3.prepareStatement("select count(*) as a from "+ tableName2+" where  author like ?");
                psql3.setString(1,"%"+author+"%");
                ResultSet resultSet2=psql3.executeQuery();
                int call_count=0;
                if(resultSet2.next()){
                    call_count=resultSet2.getInt("a");
                }
                psql2=connection2.prepareStatement("select * from authors where email = ?");
                psql2.setString(1,author);
                ResultSet resultSet=psql2.executeQuery();
                if(resultSet.next()){
                    int id=resultSet.getInt("id");
                    psql4=connection4.prepareStatement("update authors set dec_count =? ,call_count=? ,project = ? where id =?");
                    psql4.setInt(1,dec_count);
                    psql4.setInt(2,call_count);
                    psql4.setString(3,tableName);
                    psql4.setInt(4,id);
                    psql4.executeUpdate();

                }else{
                    psql2=connection2.prepareStatement("select * from authors where `name` = ?");
                    psql2.setString(1,author1.split("<")[0].trim());
                    ResultSet resultSet1=psql2.executeQuery();
                    if(resultSet1.next()){
                        int id=resultSet1.getInt("id");
                        psql4=connection4.prepareStatement("update authors set dec_count =? ,call_count=? ,project = ? where id =?");
                        psql4.setInt(1,dec_count);
                        psql4.setInt(2,call_count);
                        psql4.setString(3,tableName);
                        psql4.setInt(4,id);
                        psql4.executeUpdate();
                    }else{
                        System.out.println(author1+" "+dec_count+" "+call_count);
                    }

                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("总数："+count);
    }

    public static void updateAuthor(String tableName){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url2="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url3="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        PreparedStatement psql3;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url2,user,password);
            Connection connection3=DriverManager.getConnection(url3,user,password);
            psql1=connection1.prepareStatement("select author , count(author) as count from "+tableName+" where `type` =1 and lineNum is not null and file_path not like '%src_managed%' and author is not null group by author");
            ResultSet rset=psql1.executeQuery();
            while(rset.next()){
                String author1=rset.getString("author");
                String author=author1.split("<")[1].split(">")[0];
                psql2=connection2.prepareStatement("select * from authors where email like ? and project = ? and dec_count is not null");
                psql2.setString(1,"%"+author+"%");
                psql2.setString(2,tableName);
                ResultSet resultSet=psql2.executeQuery();
                if(resultSet.next()){
                    int public_repos=resultSet.getInt("public_repos");
                    int public_gists=resultSet.getInt("public_gists");
                    int followers=resultSet.getInt("followers");
                    int following=resultSet.getInt("following");
                    String created_at=resultSet.getString("created_at");
                    String updated_at=resultSet.getString("updated_at");
                    int dec_count=resultSet.getInt("dec_count");
                    int call_count=resultSet.getInt("call_count");
                    psql3=connection3.prepareStatement("update "+tableName+" set public_repos =? ,public_gists=? ,followers=?,following=?,created_at=?,updated_at=?,dec_count=?,call_count=? where author = ?");
                    psql3.setInt(1,public_repos);
                    psql3.setInt(2,public_gists);
                    psql3.setInt(3,followers);
                    psql3.setInt(4,following);
                    psql3.setString(5,created_at);
                    psql3.setString(6,updated_at);
                    psql3.setInt(7,dec_count);
                    psql3.setInt(8,call_count);
                    psql3.setString(9,author1);
                    psql3.executeUpdate();

                }else{
                    System.out.println(author1);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void countDefinitionType(String tableName,int type){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url2="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url3="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url4="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url5="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url6="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url7="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url8="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        PreparedStatement psql3;
        PreparedStatement psql4;
        PreparedStatement psql5;
        PreparedStatement psql6;
        PreparedStatement psql7;
        PreparedStatement psql8;

        int author=0;
        float followers=0;
        float dec_count=0;
        float call_count=0;
        float lineNums=0;
        float cyc=0;
        int definition=0;
        int call=0;

        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url2,user,password);
            Connection connection3=DriverManager.getConnection(url3,user,password);
            Connection connection4=DriverManager.getConnection(url4,user,password);
            Connection connection5=DriverManager.getConnection(url5,user,password);
            Connection connection6=DriverManager.getConnection(url6,user,password);
            Connection connection7=DriverManager.getConnection(url7,user,password);
            Connection connection8=DriverManager.getConnection(url8,user,password);
            psql1=connection1.prepareStatement("select author from "+tableName+" where type = 1 and h_type= "+type+" and line_num like \"%[%\" and public_repos is not null and file_path not like '%src_manage%' group by author;");
            psql2=connection2.prepareStatement("select AVG(followers) as avgg from "+tableName+" where `type` = 1 and h_type="+type+" and line_num like '%[%' and public_repos is not null and file_path not like '%src_manage%' group  by author;");
            psql3=connection3.prepareStatement("select AVG(dec_count) as avgg from "+tableName+" where `type` = 1 and h_type="+type+" and line_num like '%[%' and public_repos is not null and file_path not like '%src_manage%' group  by author;");
            psql4=connection4.prepareStatement("select AVG(call_count) as avgg from "+tableName+" where `type` = 1 and h_type="+type+" and line_num like '%[%' and public_repos is not null and file_path not like '%src_manage%' group  by author;");
            psql5=connection5.prepareStatement("select AVG(lineNum) as avgg from "+tableName+" where `type` = 1 and h_type="+type+" and line_num like '%[%' and public_repos is not null and file_path not like '%src_manage%';");
            psql6=connection6.prepareStatement("select AVG(cyc) as avgg from "+tableName+" where `type` = 1 and h_type="+type+" and line_num like '%[%' and public_repos is not null and file_path not like '%src_manage%';");
            psql7=connection7.prepareStatement("select * from "+tableName+" where `type` = 1 and h_type="+type+" and line_num like '%[%' and public_repos is not null and file_path not like '%src_manage%'");
            psql8=connection8.prepareStatement("select * from c_"+tableName+" where dec_id in (select id from "+tableName+" where `type` = 1 and h_type="+type+" and line_num like '%[%' and public_repos is not null and file_path not like '%src_manage%');");
            ResultSet resultSet1=psql1.executeQuery();
            while(resultSet1.next()){
                author++;
            }
            float count=0;
            float sum=0;
            ResultSet resultSet2=psql2.executeQuery();
            while(resultSet2.next()){
                count++;
                sum=sum+resultSet2.getFloat("avgg");
            }
            followers=sum/count;
            sum=0;
            ResultSet resultSet3=psql3.executeQuery();
            while (resultSet3.next()){
                sum=sum+resultSet3.getFloat("avgg");
            }
            dec_count=sum/count;
            sum=0;
            ResultSet resultSet4=psql4.executeQuery();
            while(resultSet4.next()){
                sum=sum+resultSet4.getFloat("avgg");
            }
            call_count=sum/count;
            ResultSet resultSet5=psql5.executeQuery();
            while(resultSet5.next()){
                lineNums=resultSet5.getFloat("avgg");
            }
            ResultSet resultSet6=psql6.executeQuery();
            while (resultSet6.next()){
                cyc=resultSet6.getFloat("avgg");
            }
            ResultSet resultSet7=psql7.executeQuery();
            while (resultSet7.next()){
                definition++;
            }
            ResultSet resultSet8=psql8.executeQuery();
            while (resultSet8.next()){
                call++;
            }
            System.out.println(author+" "+followers+" "+dec_count+" "+call_count+" "+lineNums+" "+cyc+" "+definition+" "+call);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void updateCallNum(String tableName1,String tableName2){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url2="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url3="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";

        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        PreparedStatement psql3;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url2,user,password);
            Connection connection3=DriverManager.getConnection(url3,user,password);
            psql1=connection1.prepareStatement("select * from "+tableName1+" where `type` =1 and lineNum is not null and file_path not like '%src_managed%' and author is not null");
            ResultSet rset=psql1.executeQuery();
            while(rset.next()){
                int id=rset.getInt("id");
                String author=rset.getString("author").split("<")[0];
                psql2=connection2.prepareStatement("select * from "+tableName2+" where dec_id = "+id);
                ResultSet resultSet=psql2.executeQuery();
                int call_num2=0;
                while(resultSet.next()){
                    String author2=resultSet.getString("author").split("<")[0];
                    if(!author.equalsIgnoreCase(author2)){
                        call_num2++;
                    }
                }
                psql3=connection3.prepareStatement("update "+ tableName1+ " set call_num2= ? where id = ?");
                psql3.setInt(1,call_num2);
                psql3.setInt(2,id);
                psql3.executeUpdate();

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void integrate(String tableName){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url2="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";

        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        PreparedStatement psql3;
        String pattern=".*\\sFunction[0-9].*";
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url2,user,password);
            Connection connection3=DriverManager.getConnection(url2,user,password);
            psql1=connection1.prepareStatement("select * from "+tableName+" where `type` = 1 and line_num like '%[%' and file_path not like '%src_manage%' and lineNum is not null");
            ResultSet rset=psql1.executeQuery();
            while(rset.next()){
                String code=rset.getString("code");
                int id =rset.getInt("id");
                if(Pattern.matches(pattern,code)){
                    continue;
                }else{
                    psql2=connection2.prepareStatement("update "+tableName+ " set `type`= 0 where id = ?");
                    psql2.setInt(1,id);
                    psql2.executeUpdate();
                    psql3=connection3.prepareStatement("update "+"c_"+tableName+" set `type` = 0 where dec_id = ?"  );
                    psql3.setInt(1,id);
                    psql3.executeUpdate();
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateT(String tableName){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url2="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";

        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        PreparedStatement psql3;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url2,user,password);
            psql1=connection1.prepareStatement("select * from "+tableName+" where file_path not like '%/target/%'  and first_author is not null");
            ResultSet rset=psql1.executeQuery();
            while(rset.next()){
                int type=rset.getInt("type");
                int h_type=rset.getInt("h_type");
                int cyc=rset.getInt("cyc");
                int lineNum=rset.getInt("lineNum");
                int call_type=rset.getInt("call_type");
                int call_num=rset.getInt("call_num");
                String file_path=rset.getString("file_path");
                String author=rset.getString("author");
                int lineNum2=rset.getInt("lineNum2");
                int cyc2=rset.getInt("cyc2");
                String code=rset.getString("code");
                String line_num=rset.getString("line_num");
                int nesting=rset.getInt("nesting");
                int privateT=rset.getInt("private");
                int lambda=rset.getInt("lambda");
                String name=rset.getString("name");
                String source_code=rset.getString("source_code");
                int override=rset.getInt("override");
                String full_code=rset.getString("full_code");
                int style=rset.getInt("style");
                String first_author= rset.getString("first_author");
                int modifier = rset.getInt("modifier");
                int num_author = rset.getInt("num_author");
                int num_commit=rset.getInt("num_commit");
                int call_type_1=rset.getInt("call_type_1");
                psql2=connection2.prepareStatement("INSERT into all_project (`type`,h_type,cyc,lineNum," +
                        "call_type,call_num,project,file_path" +
                        ",author,lineNum2,cyc2,`code`,line_num," +
                        "nesting,private,lambda,`name`,source_code," +
                        "override,full_code,style,first_author,modifier,num_author,num_commit,call_type_1) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                psql2.setInt(1,type);
                psql2.setInt(2,h_type);
                psql2.setInt(3,cyc);
                psql2.setInt(4,lineNum);
                psql2.setInt(5,call_type);
                psql2.setInt(6,call_num);
                psql2.setString(7,tableName);
                psql2.setString(8,file_path);
                psql2.setString(9,author);
                psql2.setInt(10,lineNum2);
                psql2.setInt(11,cyc2);
                psql2.setString(12,code);
                psql2.setString(13,line_num);
                psql2.setInt(14,nesting);
                psql2.setInt(15,privateT);
                psql2.setInt(16,lambda);
                psql2.setString(17,name);
                psql2.setString(18,source_code);
                psql2.setInt(19,override);
                psql2.setString(20,full_code);
                psql2.setInt(21,style);
                psql2.setString(22,first_author);
                psql2.setInt(23,modifier);
                psql2.setInt(24,num_author);
                psql2.setInt(25,num_commit);
                psql2.setInt(26,call_type_1);
                psql2.executeUpdate();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateOpen(String tableName){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url2="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";

        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        PreparedStatement psql3;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url2,user,password);
            psql1=connection1.prepareStatement("select * from "+tableName);
            ResultSet rset=psql1.executeQuery();
            while(rset.next()){
                int h_type=rset.getInt("type");
                int cyc=rset.getInt("cyc");
                int lineNum=rset.getInt("lineNum");
                int call_type=rset.getInt("call_type");
                int call_num=rset.getInt("call_num");
                String file_path=rset.getString("path");
                String author=rset.getString("author");
                int lineNum2=rset.getInt("lineNum2");
                String code=rset.getString("firstLine");
                String name=rset.getString("functionName");
                String full_code=rset.getString("fullLine");
                int style=rset.getInt("style");
                psql2=connection2.prepareStatement("INSERT into all_project (`type`,h_type,cyc,lineNum," +
                        "call_type,call_num,project,file_path" +
                        ",author,lineNum2,`code`," +
                        "`name`," +
                        "full_code,style) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                psql2.setInt(1,1);
                psql2.setInt(2,h_type);
                psql2.setInt(3,cyc);
                psql2.setInt(4,lineNum);
                psql2.setInt(5,call_type);
                psql2.setInt(6,call_num);
                psql2.setString(7,tableName);
                psql2.setString(8,file_path);
                psql2.setString(9,author);
                psql2.setInt(10,lineNum2);
                psql2.setString(11,code);
                psql2.setString(12,name);
                psql2.setString(13,full_code);
                psql2.setInt(14,style);
                psql2.executeUpdate();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void executeSql(String tableName){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url2="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url3="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url4="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        PreparedStatement psql3;
        PreparedStatement psql4;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url2,user,password);
            Connection connection3=DriverManager.getConnection(url3,user,password);
            Connection connection4=DriverManager.getConnection(url4,user,password);
            psql1=connection1.prepareStatement("select count(*) as count from "+tableName+" where file_path not like '%/target/%' and line_num like '%[%';");
            psql2=connection2.prepareStatement("select count(*) as count from c_"+tableName+"  where file_path not like '%/target/%' and line_num like '%[%'");
            psql3=connection3.prepareStatement("select count(*) as count from "+tableName+" where type = 1 and author is not null and file_path not like '%/target/%';");
            psql4=connection4.prepareStatement("select count(*) as count from c_"+tableName+" where type = 1 and author is not null and file_path not like '%/target/%';");
            ResultSet resultSet1=psql1.executeQuery();
            ResultSet resultSet2=psql2.executeQuery();
            ResultSet resultSet3=psql3.executeQuery();
            ResultSet resultSet4=psql4.executeQuery();
            if(resultSet1.next()){
                System.out.print(resultSet1.getInt("count")+" ");
            }
            if(resultSet2.next()){
                System.out.print(resultSet2.getInt("count")+" ");
            }
            if(resultSet3.next()){
                System.out.print(resultSet3.getInt("count")+" ");
            }
            if(resultSet4.next()){
                System.out.println(resultSet4.getInt("count")+" ");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void countLineNum(String tableName){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            psql1=connection1.prepareStatement("select SUM(lineNum2>0 and lineNum2<10) as a," +
                    "SUM(lineNum2>=10 and lineNum2<20) as b," +
                    "SUM(LineNum2>=20 and lineNum2<30) as c," +
                    "SUM(lineNum2>=30) as d from "+tableName+" where `type` = 1 and author is not null and file_path not like '%/target/%';");
            ResultSet resultSet1=psql1.executeQuery();
            if(resultSet1.next()){
                System.out.println(resultSet1.getInt("a")
                        +" "+resultSet1.getInt("b")
                        +" "+resultSet1.getInt("c")
                +" "+resultSet1.getInt("d"));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void countCyc(String tableName){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            psql1=connection1.prepareStatement("select SUM(cyc2=1) as a," +
                    "SUM(cyc2=2) as b," +
                    "SUM(cyc2>=3 and cyc2 <5) as c," +
                    "SUM(cyc2>=5 and cyc2 <10) as d," +
                    "SUM(cyc2>=10) as e from "+tableName+" where `type` = 1 and author is not null and file_path not like '%/target/%';");
            ResultSet resultSet1=psql1.executeQuery();
            if(resultSet1.next()){
                System.out.println(resultSet1.getInt("a")
                        +" "+resultSet1.getInt("b")
                        +" "+resultSet1.getInt("c")
                        +" "+resultSet1.getInt("d")
                +" "+resultSet1.getInt("e"));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void countCall(String tableName){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            psql1=connection1.prepareStatement("select SUM(call_type=1) as a," +
                    "SUM(call_type=2) as b," +
                    "SUM(call_type=3) as c  from "+tableName+" where `type` = 1 and author is not null and file_path not like '%/target/%';");
            ResultSet resultSet1=psql1.executeQuery();
            if(resultSet1.next()){
                System.out.println(resultSet1.getInt("a")
                        +" "+resultSet1.getInt("b")
                        +" "+resultSet1.getInt("c"));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void countTypeII(String tableName){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            psql1=connection1.prepareStatement("select count(*) as a from "+tableName+" where `type` = 1 and author is not null and call_type =2;");
            ResultSet resultSet1=psql1.executeQuery();
            if(resultSet1.next()){
                System.out.println(resultSet1.getInt("a"));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void countDefinitionTypeII(String tableName){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            psql1=connection1.prepareStatement("select SUM(call_type=3) as a," +
                    "SUM(call_type=2) as b," +
                    "SUM(call_type=4) as c," +
                    "SUm(call_type=1) as d from "+tableName+" where `type` = 1 and author is not null and file_path not like '%/target/%';");
            ResultSet resultSet1=psql1.executeQuery();
            if(resultSet1.next()){
                System.out.println(resultSet1.getInt("a")
                        +" "+resultSet1.getInt("b")
                        +" "+resultSet1.getInt("c")
                +" "+resultSet1.getInt("d"));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void countTypeIII(String tableName,int h_type,int call_type){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url1,user,password);
            psql1=connection1.prepareStatement("select * from "+tableName+" where `type` = 1 and first_author is not null and file_path not like '%/target/%' and call_type =? and h_type = ?;");
            psql1.setInt(1,call_type);
            psql1.setInt(2,h_type);
            ResultSet resultSet1=psql1.executeQuery();
            int sum=0;
            while(resultSet1.next()){
                int id=resultSet1.getInt("id");
                psql2=connection2.prepareStatement("select count(*) as a from c_"+tableName+" where dec_id = ? and first_author is not null");
                psql2.setInt(1,id);
                ResultSet resultSet=psql2.executeQuery();
                if(resultSet.next()){
                    sum+=resultSet.getInt("a");
                }
            }
            System.out.print(sum+" ");
            connection1.close();
            connection2.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void countCoefficient(String tableName,int h_type){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        PreparedStatement psql3;
        PreparedStatement psql4;
        int definitions=0;
        int calls=0;
        int authors_def=0;
        int authors_call=0;
        int cyc=0;
        int lineNum=0;
        Set<String> authors=new HashSet<>();
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url1,user,password);
            Connection connection3=DriverManager.getConnection(url1,user,password);
            Connection connection4=DriverManager.getConnection(url1,user,password);
            psql1=connection1.prepareStatement("select *  from "+tableName+" where `type` = ? and call_num >0;");
            psql1.setInt(1,h_type);
            ResultSet resultSet1=psql1.executeQuery();
            while(resultSet1.next()){
                definitions=definitions+1;
                cyc=cyc+resultSet1.getInt("cyc");
                lineNum=lineNum+resultSet1.getInt("lineNum2");
                psql2=connection2.prepareStatement("select count(*) as a from c_"+tableName+"_test where  dec_id = ?");
                int id=resultSet1.getInt("id");
                psql2.setInt(1,id);
                ResultSet resultSet=psql2.executeQuery();
                if(resultSet.next()){
                    calls=calls+resultSet.getInt("a");
                }
                psql4=connection4.prepareStatement("select * from c_"+tableName+"_test where dec_id = ?");
                psql4.setInt(1,id);
                ResultSet resultSet2=psql4.executeQuery();
                while (resultSet2.next()){
                    authors.add(resultSet2.getString("author"));
                }
            }
            psql3=connection3.prepareStatement("select author from "+tableName+" where  `type` = ? and call_num >0 GROUP BY author");
            psql3.setInt(1,h_type);
            ResultSet resultSet3=psql3.executeQuery();
            while(resultSet3.next()){
                authors_def=authors_def+1;
            }
            authors_call=authors.size();
            System.out.println(calls+" "+definitions+" "+authors_def+" "+authors_call+" "+cyc+" "+lineNum);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteByPath(String tableName1,String tableName2){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        PreparedStatement psql3;
        PreparedStatement psql4;
        PreparedStatement psql5;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url1,user,password);
            Connection connection3=DriverManager.getConnection(url1,user,password);
            Connection connection4=DriverManager.getConnection(url1,user,password);
            Connection connection5=DriverManager.getConnection(url1,user,password);
            psql1=connection1.prepareStatement("select * from "+tableName1+" where file_path like '%/test/%' or file_path like '%/tests/%' or file_path like '%/target/%' or file_path like '%slick-testkit/%'");
            ResultSet resultSet=psql1.executeQuery();
            while (resultSet.next()){
                int id=resultSet.getInt("id");
                psql2=connection2.prepareStatement("delete from "+ tableName1 +" where id = ?");
                psql2.setInt(1,id);
                psql2.executeUpdate();
                psql3=connection3.prepareStatement("delete from "+tableName2+ " where dec_id =?");
                psql3.setInt(1,id);
                psql3.executeUpdate();
            }
            psql4=connection4.prepareStatement("select * from "+tableName2+" where file_path like '%/test/%' or file_path like '%/tests/%' or file_path like '%/target/%' or file_path like '%slick-testkit/%'");

            ResultSet resultSet1=psql4.executeQuery();
            while (resultSet1.next()){
                int id=resultSet1.getInt("id");
                psql5=connection2.prepareStatement("delete from "+ tableName2 +" where id = ?");
                psql5.setInt(1,id);
                psql5.executeUpdate();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateLineNum(String tableName){
        String url2="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        Stack<String> codeStack = new Stack<>();
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url2,user,password);
            psql1=connection1.prepareStatement(" select * from "+tableName+" where `type` = 1 and line_num like '%[%' and file_path not like '%/target/%' and lineNum2 is null");
            ResultSet resultSet=psql1.executeQuery();
            while (resultSet.next()) {
                int result=0;
                int id=resultSet.getInt("id");
                String full_code=resultSet.getString("full_code");
                int lineNum=resultSet.getInt("lineNum");
                String str[]=full_code.split("\n");
                for(int i=0;i<str.length;i++){
                    if (!str[i].trim().startsWith("//")) {
                        if (!str[i].trim().startsWith("/*")) {
//ensure if current line is empty line...
                            if (str[i].trim().length()!=0) {
                                result++;
                            }
                            else {
                                System.out.println("one empty line appeared...");
                            }
                        }
                        else {
                            codeStack.push(str[i]);
                            System.out.println("after/* "+str[i]);
//if codeStack isn't empty, then there must be--
//at least one String ending with "/*" at the bottom of codeStack.
//so needs looping
                            while(!codeStack.isEmpty())
                            {
//current string doesn't endWith "*/" after trim operation
                                while(((++i<str.length)&&
                                        (!(str[i].trim()).endsWith("*/"))))
                                {
                                    codeStack.push(str[i]);
                                    System.out.println("after/* "+str[i]);
                                }
//pop the element of codeStack util current string startsWith "/*"
                                while((!codeStack.isEmpty())&&
                                        (!codeStack.pop().startsWith("/*")))
                                {
                                }
                            }
                        }
                    }
                    else {
                        System.out.println("after// "+str[i]);
                    }
                }
                if(full_code.equalsIgnoreCase("1")){
                    result=lineNum;
                }
                psql2=connection1.prepareStatement("update "+tableName+" set lineNum2 = ?  where id = ?");
                psql2.setInt(1,result);
                psql2.setInt(2,id);
                psql2.executeUpdate();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateDate(String tableName){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            psql1=connection1.prepareStatement("select *  from c_"+tableName+" where `type` = 1");
            ResultSet resultSet1=psql1.executeQuery();
            while(resultSet1.next()){
               int id=resultSet1.getInt("dec_id");
               psql1=connection1.prepareStatement("select * from "+tableName+" where id =?");
               psql1.setInt(1,id);
               ResultSet resultSet=psql1.executeQuery();
               if(resultSet.next()){
                   int type = resultSet.getInt("type");
                   if(type==0){
                       psql1=connection1.prepareStatement("update  c_"+tableName+" set `type`= ? where dec_id=?");
                       psql1.setInt(1,0);
                       psql1.setInt(2,id);
                       psql1.executeUpdate();
                   }
               }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void handleStyle(String fileName,String tableName){
        String url="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql=null;
        int type = 0;
        String message="";
        String file="";
        int line=0;
        try{
            Connection connection=DriverManager.getConnection(url,user,password);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(fileName)));
            BufferedReader in = new BufferedReader(new InputStreamReader(bis, "utf-8"), 10 * 1024 * 1024);// 10M缓存
            while (in.ready()){
                String strLine=in.readLine();
                if(strLine.contains("file=")){
                    String []strings=strLine.split("file=");
                    if(strings[0].trim().equalsIgnoreCase("warning")){
                        type=1;
                    }else if(strings[0].trim().equalsIgnoreCase("error")){
                        type=2;
                    }
                    if(strings.length>1&&strings[1].contains("message=")){
                        String[] strings1=strings[1].split("message=");
                        file=strings1[0].trim();
                        if(strings1.length>1&&strings1[1].contains("line=")){
                            String[] strings2=strings1[1].split("line=");
                            message=strings2[0];
                            line=Integer.valueOf(strings2[1].split(" ")[0]);
                        }
                    }
                    message=message.replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", "emoji");;
                    psql=connection.prepareStatement("insert into "+tableName+" (`type`,file,message,line) values (?,?,?,?)");
                    psql.setInt(1,type);
                    psql.setString(2,file);
                    psql.setString(3,message);
                    psql.setInt(4,line);
                    psql.executeUpdate();
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void update_style(String tableName){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            psql1=connection1.prepareStatement("select *  from "+tableName+" where `type` =1 and file_path not like '%src_managed%' and cyc2 is not null and full_code is not null");
            ResultSet resultSet1=psql1.executeQuery();
            while(resultSet1.next()){
                int id=resultSet1.getInt("id");
                String file_path=resultSet1.getString("file_path");
                String full_code[]=resultSet1.getString("full_code").split("\n");
                int lineNum = full_code.length;
                String line_num=resultSet1.getString("line_num");
                String []strs=line_num.split(":");
                int startLine=Integer.valueOf(strs[0].split("\\[")[1]);
                psql1=connection1.prepareStatement("update "+tableName+"tyle set dec_id =? where file like ? and line >= ? and line <= ?");
                psql1.setInt(1,id);
                psql1.setString(2,"%"+file_path+"%");
                psql1.setInt(3,startLine+1);
                psql1.setInt(4,startLine+lineNum);
                psql1.executeUpdate();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void update_style_project(String tableName){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            psql1=connection1.prepareStatement("select *  from "+tableName+" where `type` =1 and lineNum2 is not null and file_path not like '%src_managed%' and cyc2 is not null");
            ResultSet resultSet1=psql1.executeQuery();
            while(resultSet1.next()){
                int id=resultSet1.getInt("id");

                psql1=connection1.prepareStatement("select count(*) as a from "+tableName+"tyle where dec_id = ?");
                psql1.setInt(1,id);
                ResultSet resultSet=psql1.executeQuery();
                if(resultSet.next()){
                    int style=resultSet.getInt("a");
                    psql1=connection1.prepareStatement("update "+tableName+" set style = ? where id = ?");
                    psql1.setInt(1,style);
                    psql1.setInt(2,id);
                    psql1.executeUpdate();
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void count_call_num(String tableName){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            psql1=connection1.prepareStatement("select *  from "+tableName+"_s where lineNum2 is not null and file_path not like '%src_managed%' and cyc2 is not null");
            ResultSet resultSet1=psql1.executeQuery();
            while(resultSet1.next()){
                int id=resultSet1.getInt("id");

                psql1=connection1.prepareStatement("select count(*) as a from c_"+tableName+"_s where dec_id = ?");
                psql1.setInt(1,id);
                ResultSet resultSet=psql1.executeQuery();
                if(resultSet.next()){
                    int call_num=resultSet.getInt("a");
                    psql1=connection1.prepareStatement("update "+tableName+"_s set call_num = ? where id = ?");
                    psql1.setInt(1,call_num);
                    psql1.setInt(2,id);
                    psql1.executeUpdate();
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void update_dotty(){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url2="jdbc:mysql://localhost:3306/db_dec_call_function2?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url2,user,password);
            psql2=connection2.prepareStatement("select *  from dotty_s where `type` =1 and lineNum is not null and file_path not like '%src_managed%' ");
            ResultSet resultSet1=psql2.executeQuery();
            while(resultSet1.next()){
                int id=resultSet1.getInt("id");
                int lineNum=resultSet1.getInt("lineNum");
                psql1=connection1.prepareStatement("update dotty_s set lineNum = ? where id = ?");
                psql1.setInt(1,lineNum);
                psql1.setInt(2,id);
                psql1.executeUpdate();

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void count_definition_type(String tableName,int h_type){
        int count1=0;
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            psql1=connection1.prepareStatement("select * from "+tableName+" where h_type = "+h_type+" and type = 1 and first_author is not null");
            ResultSet rset=psql1.executeQuery();
            while(rset.next()){
                int id=rset.getInt("id");
                psql1=connection1.prepareStatement("select count(*) as totalcount from c_"+tableName+" where first_author is not null and  dec_id = "+id);
                ResultSet resultSet=psql1.executeQuery();
                if(resultSet.next()){
                    int line=resultSet.getInt("totalcount");
                    count1+=line;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.print(count1+" ");
    }

    public static void generateSQL(String[] projects){
        String result = "";
        for(int i=0;i<projects.length;i++){
            result+="DROP TABLE IF EXISTS `" +projects[i]+"`;\n"+
                    "CREATE TABLE `" +projects[i]+"` (\n"+
                    "  `commit` varchar(255) NOT NULL DEFAULT '',\n" +
                    "  `author` varchar(255) DEFAULT NULL,\n" +
                    "  `file` longtext,\n" +
                    "  `date` datetime DEFAULT NULL,\n" +
                    "  PRIMARY KEY (`commit`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n" +
                    "\n" +
                    "SET FOREIGN_KEY_CHECKS = 1;";
            result+="\n\n";
        }

        System.out.println(result);
    }

    public static void temp(String tableName){
        int count = 0;
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            psql1=connection1.prepareStatement("select * from c_"+tableName+"_s where  `type` = 1 and line_num like '%[%'");
            ResultSet rset=psql1.executeQuery();
            while(rset.next()){
                count++;
                int id=rset.getInt("id");
                int dec_id = rset.getInt("dec_id");
                psql1=connection1.prepareStatement("select *  from "+tableName+"_s where id = "+dec_id+" and author is null");
                ResultSet resultSet=psql1.executeQuery();
                if(resultSet.next()){
                    System.out.println(id);
                }else{

                }
            }
            System.out.println(count);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void update_author_4(String tableName1){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url2="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url1,user,password);
            psql1=connection1.prepareStatement("select * from "+tableName1+" where line_num like '%[%' and `code` is not null");
            ResultSet rset=psql1.executeQuery();
            String before_author = "";
            while(rset.next()){
                int id=rset.getInt("id");
                String author = rset.getString("author");
//                String code = rset.getString("code");
//                int modifier = 1;
//                if(code.contains("private method")){
//                    modifier = 3;
//                }else if(code.contains("protected method")){
//                    modifier = 2;
//                }
                if(author == null){
                    author = before_author;
                    psql2 = connection2.prepareStatement("update `"+tableName1+"` set author = ? where id = " +id);
                    psql2.setString(1,author);
//                    psql2.setInt(2,modifier);
                    psql2.executeUpdate();
                    before_author=author;
                }else{
//                    psql2 = connection2.prepareStatement("update `"+tableName1+"` set modifier = ? where id = " +id);
//                    psql2.setInt(1,modifier);
//                    psql2.executeUpdate();
                    before_author = author;
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void test(){
        String url1="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String url2="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false";
        String user="root";
        String password="123456";
        PreparedStatement psql1;
        PreparedStatement psql2;
        PreparedStatement psql3;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection1=DriverManager.getConnection(url1,user,password);
            Connection connection2=DriverManager.getConnection(url1,user,password);
            Connection connection4=DriverManager.getConnection(url1,user,password);
            psql1=connection1.prepareStatement("select * from c_akka_s where type = 1 and  first_author is not null");
            ResultSet rset=psql1.executeQuery();
            while(rset.next()){
                int id=rset.getInt("id");
                int dec_id = rset.getInt("dec_id");
                psql2 = connection2.prepareStatement(" select count(*) as a from akka_s where first_author is not null and id = "+dec_id);
                ResultSet resultSet = psql2.executeQuery();
                if(resultSet.next()){
                    int count= resultSet.getInt("a");
                    if(count==0){
                        psql3 = connection4.prepareStatement("delete from c_akka_s where id = "+id);
                        psql3.executeUpdate();
                        System.out.println(id);
                    }
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws InterruptedException {
        String project[]={
                "scala",
                "framework",
                "gitbucket",
                "finagle",
                "kafka",
                "lila",
                "bfg",
                "gatling",
                "scalaz",
                "sbt",
                "dotty",
                "scalding",
                "shapeless",
                "scalatra",
                "jobserver",
                "util",
                "slick",
                "lagom",
                "ammonite"
        };
        String project2[] = {
                "predictionio",
                "finatra",
                "algebird",
                "circe",
                "mmlspark",
                "http4s",
                "scio",
                "sangria",
                "scalacheck",
                "zio",
                "quill",
                "doobie",
                "fs2",
                "giter8",
                "binding",
                "akka"
        };
//        generateSQL(project2);
//        String path="D:\\Git\\scala_project\\finagle\\.git";
//        List<String> pattern=gitGetLog(path)
//        String str=readFileByLines("kafka.txt","kafka");
//        addAuthor();
//        countAuthor();
//        updateDate();
//        List<CommitTem> commits=cutCommits(str,pattern);
//        List<FileAndFunction> fileAndFunctions=fileAndFunctions("path.txt");
//        List<String> authors=compareStr(commits,fileAndFunctions);
//        String author="";
//        for(String s:authors){
//            author+=s;
//        }
//        writeFile("author.txt",author);

//        handleSemantic_dec("./semantic/slick.txt","slick_s");
//        handleSemantic_call("./semantic/lila.txt", "lila_s", "c_lila_s");
//        for(int i=0;i<project2.length;i++){
//            System.out.println(i+1);
//            readFileByLines("./commit/"+project2[i]+".txt",project2[i]);
////            Thread.sleep(60000);
//
//        }
//        readFileByLines("./commit/lagom.txt","lagom");
//        handleSemantic_call("./semantic/akka.txt","akka_s","c_akka_s");
//        handleSemantic_dec("./semantic/binding.txt","binding_s");
//        handleSemantic_symbol("./semantic/binding.txt","predictionio_symbol");
//        count_call_num("gatling");
//        handleSemantic_occu("./semantic/predictionio.txt","scala_symbol","predictionio_occu");
//         update_author_1("gatling_s","gatling","D:\\Git\\scala_project\\gatling\\");
//        long startTime=System.currentTimeMillis();
//        update_author_2("akka_s","akka","source_code");
//        long endTime=System.currentTimeMillis(); //获取结束时间
//        System.out.println("程序运行时间： "+(endTime-startTime)+"ms");
//        update_author_4("c_akka_s");
//        update_author_3("c_predictionio_s","predictionio_s");
//        countType("lagom_s","c_lagom_s",1);
//        countPrivate("lila_s","c_lila_s");
//        updateUnFind("ammonite_s","c__s","algebird_occu");
//        updateOverride("slick_s","c_slick_s","slick_symbol","slick_occu");
//        countauthor2("gatling_s","c_gatling_s","first_author","call_type_1");
//        handAuthor("slick_s","c_slick_s");
//        updateAuthor("slick_s");
//        countDefinitionType("lagom_s",3);
//        updateCallNum("slick_s","c_slick_s");
//        integrate("util_s");
//        integrate("util_s");
//        updateT("openwhisk");
//
        int sum = 0;
        for (int i=0;i<project.length;i++){
            sum += countAuthor(project[i]);
        }
        for (int i=0;i<project2.length;i++){
            sum += countAuthor(project2[i]);
        }
        System.out.println(sum);


//        updateOpen("openwhisk");
//        countCoefficient("openwhisk",1);
//        updateLineNum("ammonite_s");

//        updateCallNum("scala_s","c_scala_s");
//        deleteByPath("slick_s","c_slick_s");
//        updateDate("sbt_s");
//        handleStyle("./style/akka.txt","akka_style");
//        update_style("akka_s");
//        update_style_project("akka_s");
//        update_dotty();
//        for (int i = 0; i < project.length; i++) {
//            updateT( project[i] + "_s");
//        }
//        updateT("akka_s");
//        count_definition_type("akka_s",2);
//        countDefinitionTypeII("binding_s");
//        String tableName = "binding_s";
//        for(int i=1;i<=3;i++){
//            countTypeIII(tableName,i,3);
//            countTypeIII(tableName,i,2);
//            countTypeIII(tableName,i,4);
//        }
//        test();
//temp("http4s");
    }
}