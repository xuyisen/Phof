package visitors

import java.io.File
import java.nio.charset.CodingErrorAction
import java.sql.DriverManager

import utils.WriteToFile

import scala.io.{Codec, Source}


object MyApp {

  //1.数据库配置
  // 访问本地MySQL服务器，通过3306端口访问mysql数据库
  val url ="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false"
  //驱动名称
  val driver="com.mysql.jdbc.Driver"
  //用户名
  val username = "root"
  //密码
  val password = "123456"

  val writeToFile=new WriteToFile()
  //注册Driver
  Class.forName(driver)
  //2.初始化数据连接
  var connection= DriverManager.getConnection(url, username, password)

  def main(strings: Array[String]): Unit = {
    //    var target="protected def help(str: => String): Unit = addHelp(() => str)"
    //    val text="def\\s*.*\\(.*\\s*=>.*\\)".r
    //    val reg=text.findFirstIn(target)
    //    val c =String.valueOf(reg)
    //    var t=target
    //    if(c.length()>4){
    //      t=target.replace(reg.get,"")
    //    }
//        find_path_and_line("/Volumes/home/xuyisen/git/scala_project3/scala/","c_scala_s","scala_s")
//    update_call_code("D:/Git/scala_project/slick/", "slick_s", "source_code")
//    update_call_code("D:/Git/scala_project/slick/", "c_slick_s", "code")
//        update_call_type("/Users/xuyisen/文档/git/Binding.scala/","c_binding_s","binding_s")
    //  }
//    update_quan("D:/Git/scala_project/slick/","slick_s")
//    update_line_num("D:/Git/scala_project/slick/","slick_s")
//    val s=Some("aaaaaaaaa")
//    val command=s.filterNot(_.startsWith("!"))
//    val strs=Array("aaaaaaaaa","bbbbbbbbb","ccccccccc")
//    command.foreach(strs(strs.length-1)=_)
//    val c=command.toList
//    print(command)
//    print(strs)
//    for(i<-1 to 50){
//      update_full_code("/Users/xuyisen/文档/2018年实验/scala_project/akka/","akka_s")
      update_call_code("/Users/xuyisen/文档/2018年实验/scala_project/shapeless/","c_shapeless_s","code")
//      print(i+"轮\n")
//      Thread.sleep(3000)
//    }


//    update_call_code("/Users/xuyisen/文档/git/Binding.scala/","c_binding_s","code")
//    count_lambda()
  }



  def update_clone_code(foreStr:String,path:String,start:Int,end:Int): String ={
    var file_path=foreStr+path
    val decoder = Codec.UTF8.decoder.onMalformedInput(CodingErrorAction.IGNORE)
    var lines = Source.fromFile(file_path)(decoder).getLines().toList
    var method=""
    for(i <- start to end){
      method=method+lines(i-1)+"\n"
    }

    return method
  }


  def update_full_code(foreStr:String,tableName:String):Unit={
    val visitor=new MyVisitor()
    val psql=connection.prepareStatement("select * from " + tableName+" where type=1 and line_num like '%[%' and file_path not like '%/target/%' and file_path not like '%/src_managed/%' and  file_path not like '%/test/%' and full_code is null ")
    val resultSet=psql.executeQuery()
    while(resultSet.next()){
      val id=resultSet.getInt("id")
      var file_path=resultSet.getString("file_path")
      val line_num=resultSet.getString("line_num")
      file_path=foreStr+file_path
      visitor.line_id=id
      visitor.tableName1=tableName
      visitor.line_num=line_num
      visitor.file_path=file_path
      visitor.parse(file_path)
    }
  }

  def update_line_num(foreStr:String,tableName:String):Unit={
    val visitor=new MyVisitor()
    val psql=connection.prepareStatement("select * from " + tableName+" where type=1 and line_num like '%[%' and x is null ")
    val resultSet=psql.executeQuery()
    while(resultSet.next()){
      val id=resultSet.getInt("id")
      var file_path=resultSet.getString("file_path")
      val line_num=resultSet.getString("line_num")
      file_path=foreStr+file_path
      visitor.line_id=id
      visitor.tableName1=tableName
      visitor.line_num=line_num
      visitor.file_path=file_path
      visitor.parse(file_path)
    }
  }
  def update_quan(foreStr:String,tableName:String):Unit={
    val visitor=new MyVisitor()
    val psql=connection.prepareStatement("select * from " + tableName+" where type=1 and line_num like '%[%' and cyc is null ")
    val resultSet=psql.executeQuery()
    while(resultSet.next()){
      val id=resultSet.getInt("id")
      var file_path=resultSet.getString("file_path")
      val line_num=resultSet.getString("line_num")
      file_path=foreStr+file_path
      visitor.line_id=id
      visitor.tableName1=tableName
      visitor.line_num=line_num
      visitor.file_path=file_path
      visitor.parse(file_path)
    }
  }
  def find_path_and_line(foreStr:String,tableName1:String,tableName2:String):Unit={
    val visitor = new MyVisitor()
    val psql = connection.prepareStatement("select a.*,b.h_type as h_type from " + tableName1 + " a , "+ tableName2+" b where  a.dec_id=b.id and BINARY a.type = 1")
    val resultSet = psql.executeQuery()
    while (resultSet.next()) {
      var path_file = resultSet.getString("file_path")
      val line_num = resultSet.getString("line_num")
      val h_type=resultSet.getInt("h_type")
      val id=resultSet.getInt("id")
      path_file=foreStr+path_file
      visitor.id=id
      visitor.line_num=line_num
      visitor.h_type=h_type
      visitor.tableName1=tableName1
      visitor.file_path=path_file
      visitor.match_type=1
      visitor.parse(path_file)
    }
//    visitor.id=1804
//    visitor.line_num="[1008:11..1008:17)"
//    visitor.h_type=1
//    visitor.tableName1="c_fpinscala_s"
//    visitor.file_path="/Volumes/home/xuyisen/git/scala_project3/fpinscala/exercises/src/main/scala/fpinscala/streamingio/StreamingIO.scala"
//    visitor.match_type=1
//    visitor.parse("/Volumes/home/xuyisen/git/scala_project3/fpinscala/exercises/src/main/scala/fpinscala/streamingio/StreamingIO.scala")
  }

  def update_call_code(foreStr:String,tableName1:String,line_name:String):Unit={
    val psql=connection.prepareStatement("select * from "+tableName1+" where type = 0 and "+line_name+" is null and file_path not like '%target/%' and file_path not like '%/src_managed/%' and  file_path not like '%/test/%'");
    val resultSet=psql.executeQuery()
    val decoder = Codec.UTF8.decoder.onMalformedInput(CodingErrorAction.IGNORE)
    while(resultSet.next()) {
      var file_path = resultSet.getString("file_path")
      val line_num = resultSet.getString("line_num")
      val id = resultSet.getInt("id")
      file_path = foreStr + file_path
      if (line_num.length() >= 1) {
        val lines = Source.fromFile(file_path)(decoder).getLines().toList
        val temStr = line_num.split("\\.\\.")
        val temStr1 = temStr(0).split(":")
        val start_line = Integer.valueOf(temStr1(0).split("\\[")(1))
        var target = ""
        if (start_line < lines.length) {
          target = lines(start_line).trim()
        } else {
          println("-----------")
          println(file_path)
          println(line_num)
          println("-----------")
        }


        val psql1 = connection.prepareStatement("update " + tableName1 + " set " + line_name + " = ? where id = ?")
        psql1.setString(1, target)
        psql1.setInt(2, id)
        psql1.executeUpdate()

      }

    }

  }



  def update_call_type(foreStr:String,tableName1:String,tableName2:String):Unit= {
    val psql = connection.prepareStatement("select a.*,b.h_type as h_type,b.`code` as source_code from " + tableName1 + " a , "+ tableName2+" b where  a.dec_id=b.id and BINARY a.type = 1 and a.call_type is null");
    val resultSet = psql.executeQuery()
    val decoder = Codec.UTF8.decoder.onMalformedInput(CodingErrorAction.IGNORE)
    while (resultSet.next()) {
      var file_path = resultSet.getString("file_path")
      val line_num = resultSet.getString("line_num")
      val id = resultSet.getInt("id")
      val h_type=resultSet.getInt("h_type")
      var code=resultSet.getString("source_code")
      var call_type=2
      file_path = foreStr + file_path
      if (line_num.length() >= 1) {
        val lines = Source.fromFile(file_path)(decoder).getLines().toList
        val temStr = line_num.split("\\.\\.")
        val temStr1 = temStr(0).split(":")
        val start_line = Integer.valueOf(temStr1(0).split("\\[")(1))
        var target = ""
        code=code.split("method ")(1)
        code=code.split("\\[")(0)
        code=code.split("\\:")(0)
        code=code.split("\\(")(0)
        if (start_line <= lines.length) {
          target = lines(start_line).trim()
        } else {
          println("-----------")
          println(file_path)
          println(line_num)
          println("-----------")
        }
        val text="def\\s*.*\\(.*\\s*=>.*\\):?.*=[^>]".r
        val reg=text.findFirstIn(target)
        val c =String.valueOf(reg)

        val index=target.indexOf(code)
        var t=target
        if(c.length()>4){
          t=t.replace(reg.get,"")
        }
        if(index<0){
          println(id)
        }else{
          t=target.substring(index)
        }

        if(h_type==2||h_type==3){
          call_type=3
        }else if((!t.contains("=>"))&&(!t.contains("{"))){
          call_type=1
        }else if(t.contains("case ")){
          val p=t.split(" => ")
          if(p.length==2){
            call_type=1
          }
        }else if(t.matches(".*\\(.*\\)\\(.*\\s*=>\\s*\\{")){
          call_type=2
        }


        val psql1 = connection.prepareStatement("update " + tableName1 + " set code  = ? , call_type =? where id = ?")
        psql1.setString(1, target)
        psql1.setInt(2,call_type)
        psql1.setInt(3, id)
        psql1.executeUpdate()

      }

    }
  }

  def count_lambda():Unit={
    val project=Array(
//      "predictionio",
//      "finatra",
//      "algebird",
//      "circe",
//      "mmlspark",
//      "http4s",
      "scio",
      "sangria",
      "scalacheck",
      "zio",
      "quill",
      "doobie",
      "fs2",
      "giter8",
      "Binding.scala"
    )
    for(s<-project){
      val visitor=new MyVisitor()
      parse_file(new File("/Users/xuyisen/文档/2018年实验/scala_project/"+s+"/"),visitor)
      println(visitor.lambdaCount)
    }
  }
  def parse_file(file: File,p:MyVisitor): Unit = {
    if (file.isFile && file.getName.endsWith(".scala")&&(!file.getAbsolutePath().contains("\\test\\"))&&(!file.getAbsolutePath().contains("\\tests\\"))) {
      p.parse(file.getAbsolutePath)
    } else if (file.isDirectory) {
      for (subFile <- file.listFiles()) {
        parse_file(subFile,p)
      }
    }


  }

}

