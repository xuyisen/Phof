package visitors

import java.sql.DriverManager

import utils.{GraphLink, SortFile}

import scala.collection.mutable.ArrayBuffer

import scala.meta._

/** Reference URL:https://astexplorer.net/#/gist/22cf8a3fcb2155c087ae94b4d194c1b6/d10c646ecfae4c69c919408aa3aaefb2deda2df7
  * This class traverses the abstract syntax tree to extract the representation 1&2
  */
class MyVisitor {

  var line_num=""

  var start_line=0

  var start_column=0

  var end_line=0

  var end_column=0

  var call_type=1

  var code=""

  var tableName1=""

  var h_type=0

  var id=0

  var match_type=0

  var file_path=""

  var hasFind=false

  var hasFound=false

  //1.数据库配置
  // 访问本地MySQL服务器，通过3306端口访问mysql数据库
  val url ="jdbc:mysql://localhost:3306/db_dec_call_function?useSSL=false"
  //驱动名称
  val driver="com.mysql.jdbc.Driver"
  //用户名
  val username = "root"
  //密码
  val password = "123456"

  var head:Array[GraphLink]=new Array[GraphLink](300)

  var i=1


  var line_id=0

  var lambdaCount=0


  //注册Driver
  Class.forName(driver)
  //2.初始化数据连接
  var connection= DriverManager.getConnection(url, username, password)



  def visitCtor(ctor: Ctor):Unit={
    ctor match {
      case y:Ctor.Primary=>visitCtorPrimary(y)
      case y:Ctor.Secondary=>visitCtorSecondary(y)
      case _=>println("visit Ctor"+ctor.toString())
    }
  }
  def visitCtorPrimary(primary: Ctor.Primary):Unit={
    visitChild(primary)
  }
  def visitTerm(term: Term):Unit={
    term match {
      case y:Term.Select=>visitTermSelect(y)
      case y:Term.Block=>visitTermBlock(y)
      case y:Term.Annotate=>visitTermAnnotate(y)
      case y:Term.Apply=>visitTermApply(y)
      case y:Term.ApplyInfix=>visitTermApplyInfix(y)
      case y:Term.ApplyType=>visitTermApplyType(y)
      case y:Term.ApplyUnary=>visitTermApplyUnary(y)
      case y:Term.Ascribe=>visitTermAscribe(y)
      case y:Term.Assign=>visitTermAssign(y)
      case y:Term.Do=>visitTermDo(y)
      case y:Term.Eta=>visitTermEta(y)
      case y:Term.For=>visitTermFor(y)
      case y:Term.ForYield=>visitTermForYield(y)
      case y:Term.Function=>visitTermFunction(y)
      case y:Term.If=>visitTermIf(y)
      case y:Term.Interpolate=>visitTermInterpolate(y)
      case y:Term.Match=>visitTermMatch(y)
      case y:Term.Name=>visitTermName(y)
      case y:Term.New=>visitTermNew(y)
      case y:Term.NewAnonymous=>visitTermNewAnonymous(y)
      case y:Term.Param=>visitTermParam(y)
      case y:Term.PartialFunction=>visitTermPartialFunction(y)
      case y:Term.Placeholder=>visitTermPlaceholder(y)
      case y:Term.Ref=>visitTermRef(y)
      case y:Term.Repeated=>visitTermRepeated(y)
      case y:Term.Return=>visitTermReturn(y)
      case y:Term.Super=>visitTermSuper(y)
      case y:Term.This=>visitTermThis(y)
      case y:Term.Throw=>visitTermThrow(y)
      case y:Term.TryWithHandler=>visitTermTryWithHandler(y)
      case y:Term.Try=>visitTermTry(y)
      case y:Term.Tuple=>visitTermTuple(y)
      case y:Term.While=>visitTermWhile(y)
      case y:Term.Xml=>visitTermXml(y)
      case _=>println("visitTerm"+term.toString())
    }
  }
  def visitTermTry(value: Term.Try):Unit={
    visitChild(value)
  }
  def visitTermAnnotate(annotate: Term.Annotate):Unit={

    visitChild(annotate)
  }
  def visitTermApply(apply: Term.Apply):Unit={
    visitChild(apply)
  }
  def visitTermApplyInfix(infix: Term.ApplyInfix):Unit={
    visitChild(infix)
  }
  def visitTermApplyType(applyType: Term.ApplyType,re:Boolean=false):Unit={
      visitChild(applyType)
  }
  def visitTermApplyUnary(unary: Term.ApplyUnary):Unit={
    visitChild(unary)
  }
  def visitTermAscribe(ascribe: Term.Ascribe):Unit={
    visitChild(ascribe)
  }
  def visitTermAssign(assign: Term.Assign):Unit={
    visitChild(assign)
  }
  def visitTermBlock(block: Term.Block):Unit={
    visitChild(block)
  }
  def visitTermDo(value: Term.Do):Unit={
    visitChild(value)
  }
  def visitCtorSecondary(secondary: Ctor.Secondary):Unit={
    visitChild(secondary)
  }
  def visitTermEta(eta: Term.Eta):Unit={
    visitChild(eta)
  }
  def visitTermForYield(forYield: Term.ForYield):Unit={
    if(hasFind){
      i=i+1
      head(i)=new GraphLink()
      head(i-1).insert(i)
      val startId=i
      visitChild(forYield)
      i=i+1
      head(i)=new GraphLink()
      head(i-1).insert(i)
      head(i).insert(startId)
      i=i+1
      head(i)=new GraphLink()
      head(i-1).insert(i)
      head(startId).insert(i)
    }else{
      visitChild(forYield)
    }
  }
  def visitTermFunction(function: Term.Function):Unit={
    lambdaCount=lambdaCount+1
    visitChild(function)
  }
  def visitTermIf(value: Term.If):Unit={
    if(hasFind) {
        var endIds=new ArrayBuffer[Int]()
        i = i + 1
        val startid=i
        head(i) = new GraphLink()
        head(i - 1).insert(i)
        value.elsep match {
          case y: Lit.Unit => i = i + 1
            head(i) = new GraphLink()
            head(startid).insert(i)
            visitChild(value.thenp)
            i = i + 1
            head(i) = new GraphLink()
            head(i - 1).insert(i)
            head(startid).insert(i)
          case y: Term.Block => i = i + 1
            head(i) = new GraphLink()
            head(startid).insert(i)
            visitChild(value.thenp)
            val end1=i
            i = i + 1
            head(i) = new GraphLink()
            head(startid).insert(i)
            visitChild(y)
            val end2=i
            i = i + 1
            head(i) = new GraphLink()
            head(end1).insert(i)
            head(end2).insert(i)
          case y: Term.If =>
            i=i+1
            head(i)=new GraphLink()
            head(startid).insert(i)
            visitChild(value.thenp)
            endIds+=i
            visitTermIf2(y,startid,endIds)
          case y:Term.Name=>
            i = i + 1
            head(i) = new GraphLink()
            head(startid).insert(i)
            visitChild(value.thenp)
            val end1=i
            i = i + 1
            head(i) = new GraphLink()
            head(startid).insert(i)
            visitChild(y)
            val end2=i
            i = i + 1
            head(i) = new GraphLink()
            head(end1).insert(i)
            head(end2).insert(i)
          case y:Term.ApplyInfix=>
            i = i + 1
            head(i) = new GraphLink()
            head(startid).insert(i)
            visitChild(value.thenp)
            val end1=i
            i = i + 1
            head(i) = new GraphLink()
            head(startid).insert(i)
            visitTermApplyInfix(y)
            val end2=i
            i = i + 1
            head(i) = new GraphLink()
            head(end1).insert(i)
            head(end2).insert(i)
          case y:Term.Match=>
            i = i + 1
            head(i) = new GraphLink()
            head(startid).insert(i)
            visitChild(value.thenp)
            val end1=i
            i = i + 1
            head(i) = new GraphLink()
            head(startid).insert(i)
            visitTermMatch(y)
            val end2=i
            i = i + 1
            head(i) = new GraphLink()
            head(end1).insert(i)
            head(end2).insert(i)
          case y:Term.Function=>
            i = i + 1
            head(i) = new GraphLink()
            head(startid).insert(i)
            visitChild(value.thenp)
            val end1=i
            i = i + 1
            head(i) = new GraphLink()
            head(startid).insert(i)
            visitTermFunction(y)
            val end2=i
            i = i + 1
            head(i) = new GraphLink()
            head(end1).insert(i)
            head(end2).insert(i)
          case _=>
            i = i + 1
            head(i) = new GraphLink()
            head(startid).insert(i)
            visitChild(value.thenp)
            val end1=i
            i = i + 1
            head(i) = new GraphLink()
            head(startid).insert(i)
            visitChild(value.elsep)
            val end2=i
            i = i + 1
            head(i) = new GraphLink()
            head(end1).insert(i)
            head(end2).insert(i)
        }
      }else{
      visitChild(value)
    }

  }

  def visitTermIf2(value:Term.If,ifid:Int,endIds:ArrayBuffer[Int]):Unit={
    i=i+1
    head(i)=new GraphLink()
    head(ifid).insert(i)
    visitChild(value.thenp)
    endIds+=i
    value.elsep match{
      case y: Lit.Unit=>i=i+1
        head(i)=new GraphLink()
        head(ifid).insert(i)
        visitChild(y)
        endIds+=i
        i=i+1
        head(i)=new GraphLink()
        endIds+=ifid
        for(a<-endIds){
          head(a).insert(i)
        }
      case y:Term.Block=>i=i+1
        head(i)=new GraphLink()
        head(ifid).insert(i)
        visitTermBlock(y)
        endIds+=i
        i=i+1
        head(i)=new GraphLink()
        for(a<- endIds){
          head(a).insert(i)
        }
      case y:Term.Name=>i=i+1
        head(i)=new GraphLink()
        head(ifid).insert(i)
        endIds+=i
        i=i+1
        head(i)=new GraphLink()
        for(a<- endIds){
          head(a).insert(i)
        }
      case y:Term.ApplyInfix=>i=i+1
        head(i)=new GraphLink()
        head(ifid).insert(i)
        visitTermApplyInfix(y)
        endIds+=i
        i=i+1
        head(i)=new GraphLink()
        for(a<- endIds){
          head(a).insert(i)
        }
      case y:Term.Function=>
        i=i+1
        head(i)=new GraphLink()
        head(ifid).insert(i)
        visitTermFunction(y)
        endIds+=i
        i=i+1
        head(i)=new GraphLink()
        for(a<- endIds){
          head(a).insert(i)
        }
      case y:Term.If=>i=i+1
        head(i)=new GraphLink()
        head(ifid).insert(i)
        visitTermIf2(y,ifid,endIds)
      case y:Term.Match=>
        i=i+1
        head(i)=new GraphLink()
        head(ifid).insert(i)
        visitTermMatch(y)
        endIds+=i
        i=i+1
        head(i)=new GraphLink()
        for(a<- endIds){
          head(a).insert(i)
        }
      case _=>
        i=i+1
        head(i)=new GraphLink()
        head(ifid).insert(i)
        visitChild(value.elsep)
        endIds+=i
        i=i+1
        head(i)=new GraphLink()
        for(a<- endIds){
          head(a).insert(i)
        }
    }
  }
  def visitLit(lit: Lit):Unit={
    lit match {
      case y:Lit.Boolean=>visitLitBoolean(y)
      case y:Lit.Byte=>visitLitByte(y)
      case y:Lit.Char=>visitLitChar(y)
      case y:Lit.Double=>visitLitDouble(y)
      case y:Lit.Float=>visitLitFloat(y)
      case y:Lit.Int=>visitLitInt(y)
      case y:Lit.Long=>visitLitLong(y)
      case y:Lit.Null=>visitLitNull(y)
      case y:Lit.Short=>visitLitShort(y)
      case y:Lit.String=>visitLitString(y)
      case y:Lit.Symbol=>visitLitSymbol(y)
      case y:Lit.Unit=>visitLitUnit(y)
      case _=>println("visitLit"+lit.toString())
    }
  }
  def visitLitBoolean(boolean: Lit.Boolean):Unit={

  }
  def visitLitByte(byte: Lit.Byte):Unit={

  }
  def visitLitChar(char: Lit.Char):Unit={

  }
  def visitLitDouble(double: Lit.Double):Unit={

  }
  def visitLitFloat(float: Lit.Float):Unit={

  }
  def visitLitInt(int: Lit.Int):Unit={
  }
  def visitLitLong(long: Lit.Long):Unit={

  }
  def visitLitNull(value: Lit.Null):Unit={

  }
  def visitLitShort(short: Lit.Short):Unit={
  }
  def visitLitString(str: Lit.String):Unit={
  }
  def visitLitSymbol(symbol: Lit.Symbol):Unit={
  }
  def visitLitUnit(unit: Lit.Unit):Unit={
  }
  def visitTermInterpolate(interpolate: Term.Interpolate):Unit={
    visitChild(interpolate)
  }
  def visitTermMatch(value: Term.Match):Unit={
    if(hasFind){
      i=i+1
      val startid=i
      var endid=new ArrayBuffer[Int]()
      head(i)=new GraphLink()
      head(i-1).insert(i)
      for(a<-value.cases){
        i=i+1
        head(i)=new GraphLink()
        head(startid).insert(i)
        visitCase(a)
        endid+=i
      }
      i=i+1
      head(i)=new GraphLink()
      for(a<-endid){
        head(a).insert(i)
      }
    }else{
      visitChild(value)
    }
  }
  def visitTermName(name: Term.Name):Unit={
    val a=name.pos.startLine
    if(match_type==1) {
      if (start_line == name.pos.startLine &&
        start_column-5 <= name.pos.startColumn&&name.pos.startColumn<=start_column+5) {
        if (h_type == 2) {
          call_type = 3
          hasFind = true
//          val psql = connection.prepareStatement("update " + tableName1 + " set call_type = ?  where id = ?")
//          psql.setInt(1, call_type)
//          psql.setInt(2, id)
//          psql.executeUpdate()
        }else{
          val parent=name.parent
          parent match {
            case Some(pp)=>pp match {
              case y: Term.Select =>
                val grandparent = y.parent
                grandparent match {
                  case Some(ppp) => ppp match {
                    case z: Term.Apply =>
                      var function = false
                      for (arg <- z.args) {
                        arg match {
                          case o: Term.Function => function = true
                          case o: Term.PartialFunction => function = true
                          case o: Term.Block => function = true
                          case o: Term.Assign => o.rhs match {
                            case q: Term.Function => function = true
                            case _ =>
                          }
                          case _ =>
                        }
                      }
                      if (function) {
                        call_type = 2
                      } else if (h_type == 2) {
                        call_type = 3
                      } else {
                        call_type = 1
                      }
                      hasFind = true

//                      val psql = connection.prepareStatement("update " + tableName1 + " set call_type = ?  where id = ?")
//                      psql.setInt(1, call_type)
//                      psql.setInt(2, id)
//                      psql.executeUpdate()
                    case _ =>
                  }
                }
              case _ =>
            }
          }
        }
      }
    }
  }
  def visitTermNew(value: Term.New):Unit={
    visitChild(value)
  }
  def visitTermNewAnonymous(anonymous: Term.NewAnonymous):Unit={
    visitChild(anonymous)
  }
  def visitTermParam(param:Term.Param):Unit={
    visitChild(param)
  }
  def visitTermPartialFunction(function: Term.PartialFunction):Unit={
    lambdaCount=lambdaCount+1
    visitChild(function)
  }
  def visitTermPlaceholder(placeholder: Term.Placeholder):Unit={
    visitChild(placeholder)
  }
  def visitTermRef(ref: Term.Ref):Unit={
    visitChild(ref)
  }
  def visitTermRepeated(repeated: Term.Repeated):Unit={
    visitChild(repeated)
  }
  def visitTermReturn(value: Term.Return):Unit={
    visitChild(value)
  }
  def visitTermSelect(select: Term.Select):Unit={
    visitChild(select)
  }
  def visitTermSuper(value: Term.Super):Unit={
    visitChild(value)
  }
  def visitTermThis(value: Term.This):Unit={
    visitChild(value)
  }
  def visitTermThrow(value: Term.Throw):Unit={
    visitChild(value)
  }
  def visitTermTryWithHandler(handler: Term.TryWithHandler):Unit={
    visitChild(handler)
  }
  def visitTermTuple(tuple: Term.Tuple):Unit={
    visitChild(tuple)
  }
  def visitTermWhile(value: Term.While):Unit={
    if(hasFind){
      i=i+1
      head(i)=new GraphLink()
      head(i-1).insert(i)
      val startId=i
      visitChild(value)
      i=i+1
      head(i)=new GraphLink()
      head(i-1).insert(i)
      head(i).insert(startId)
      i=i+1
      head(i)=new GraphLink()
      head(startId).insert(i)
    }else{
      visitChild(value)
    }
  }
  def visitTermXml(xml: Term.Xml):Unit={
    visitChild(xml)
  }
  def visitTermFor(value: Term.For):Unit={
    if(hasFind){
      i=i+1
      head(i)=new GraphLink()
      head(i-1).insert(i)
      val startId=i
      visitChild(value)
      i=i+1
      head(i)=new GraphLink()
      head(i-1).insert(i)
      head(i).insert(startId)
      i=i+1
      head(i)=new GraphLink()
      head(startId).insert(i)
    }else{
      visitChild(value)
    }

  }
  def visitEnumerator(enumerator: Enumerator):Unit={
    enumerator match {
      case y:Enumerator.Generator=>visitEnumeratorGenerator(y)
      case y:Enumerator.Guard=>visitEnumeratorGuard(y)
      case y:Enumerator.Val=>visitEnumeratorVal(y)
      case _=>println("visitEnumerator"+enumerator.toString())
    }
  }
  def visitEnumeratorGenerator(generator: Enumerator.Generator):Unit={
    visitChild(generator)
  }
  def visitEnumeratorGuard(guard: Enumerator.Guard):Unit={
    visitChild(guard)
  }
  def visitEnumeratorVal(value: Enumerator.Val):Unit={
    visitChild(value)
  }
  def visitMod(mod: Mod):Unit={
    mod match {
      case y:Mod.Private=>visitModPrivate(y)
      case y:Mod.Abstract=>visitModAbstract(y)
      case y:Mod.Annot=>visitModAnnot(y)
      case y:Mod.Case=>visitModCase(y)
      case y:Mod.Contravariant=>visitModContravariant(y)
      case y:Mod.Covariant=>visitModCovariant(y)
      case y:Mod.Final=>visitModFinal(y)
      case y:Mod.Implicit=>visitModImplicit(y)
      case y:Mod.Inline=>visitModInline(y)
      case y:Mod.Lazy=>visitModLazy(y)
      case y:Mod.Override=>visitModOverride(y)
      case y:Mod.Protected=>visitModProtect(y)
      case y:Mod.Sealed=>visitModSealed(y)
      case y:Mod.ValParam=>visitModValParam(y)
      case y:Mod.VarParam=>visitModVarParam(y)
      case _=>println("visit mod 1"+mod.toString())
    }
  }
  def visitDefnType(value: Defn.Type):Unit={
    visitChild(value)
  }

  /**
    * Start traversing the statement inside the method body,
    * storing the value of the leaf node and the type of the intermediate statement
    * @param value
    */
  def visitDefnDef(value: Defn.Def):Unit= {
      if(start_line>=value.pos.startLine-0&& value.pos.startLine+ 0>= start_line){
        val full_code = value.toString()
        val source_code_list = full_code.split("\n")
        val source_code = source_code_list(0)
        var line_num2 = 0
        var whiteLines = 0
        var commentLines = 0
        var comment = false
        for( line<-source_code_list){
          var line1 = line.trim()
          if (line1.matches("^[\\s&&[^\\n]]*$")) {

            whiteLines += 1

          } else if (line.startsWith("/*") && !line.endsWith("*/")) {

            commentLines += 1

            comment = true;

          } else if (true == comment) {

            commentLines += 1

            if (line.endsWith("*/")) {

              comment = false;

            }

          } else if (line.startsWith("//")) {

          commentLines+=1

         } else {

          line_num2+=1

         }
      }
      hasFound=true

      hasFind =true
      head=new Array[GraphLink](300)
      i=1
      head(i)=new GraphLink()
      visitChild(value)
      i=i+1
      head(i)=new GraphLink()
      head(i-1).insert(i)

      var e = 0
      for(a<-1 to i){
        e = e+head(a).printNode()
      }
      val result = e-i+2
      hasFound=true
      hasFind=false

        val psql=connection.prepareStatement("update "+ tableName1+" set source_code= ?, lineNum2 = ?, cyc2 = ? , full_code = ? where id =?")
        psql.setString(1,source_code)
        psql.setInt(2,line_num2)
        psql.setInt(3,result)
        psql.setString(4,full_code)
        psql.setInt(5,line_id)
        psql.executeUpdate()


    }else{
      visitChild(value)
    }
  }

  def visitModCase(value: Mod.Case):Unit={
    visitChild(value)
  }
  def visitDefnVar(value: Defn.Var):Unit={
      visitChild(value)
  }
  def visitType(value: Type):Unit={
    value match {
      case y:Type.And=>visitTypeAnd(y)
      case y:Type.Annotate=>visitTypeAnnotate(y)
      case y:Type.Apply=>visitTypeApply(y)
      case y:Type.ApplyInfix=>visitTypeApplyInfix(y)
      case y:Type.Bounds=>visitTypeBounds(y)
      case y:Type.ByName=>visitTypeByName(y)
      case y:Type.Existential=>visitTypeExistential(y)
      case y:Type.Function=>visitTypeFunction(y)
      case y: Type.ImplicitFunction=>visitTypeImplicitFunction(y)
      case y:Type.Lambda=>visitTypeLambda(y)
      case y:Type.Method=>visitTypeMethod(y)
      case y:Type.Name=>visitTypeName(y)
      case y:Type.Or=>visitTypeOr(y)
      case y:Type.Param=>visitTypeParam(y)
      case y:Type.Placeholder=>visitTypePlaceholder(y)
      case y:Type.Project=>visitTypeProject(y)
      case y:Type.Ref=>visitTypeRef(y)
      case y:Type.Refine=>visitTypeRefine(y)
      case y:Type.Repeated=>visitTypeRepeated(y)
      case y:Type.Select=>visitTypeSelect(y)
      case y:Type.Singleton=>visitTypeSingleton(y)
      case y:Type.Tuple=>visitTypeTuple(y)
      case y:Type.Var=>visitTypeVar(y)
      case y:Type.With=>visitTypeWith(y)
      case _=>println("visit Type"+value.toString())
    }
  }
  def visitTypeAnnotate(annotate: Type.Annotate):Unit={
    visitChild(annotate)
  }
  def visitTypeApplyInfix(infix: Type.ApplyInfix):Unit={
    visitChild(infix)
  }
  def visitTypeAnd(and: Type.And):Unit={
    visitChild(and)
  }
  def visitTypeApply(apply: Type.Apply):Unit={
    visitChild(apply)
  }
  def visitTypeBounds(value: Type.Bounds):Unit={
    visitChild(value)
  }
  def visitTypeByName(name: Type.ByName):Unit={
    visitChild(name)
  }
  def visitTypeExistential(existential: Type.Existential):Unit={
    visitChild(existential)
  }
  def visitTypeFunction(function: Type.Function):Unit={
    lambdaCount=lambdaCount + 1
    visitChild(function)
  }
  def visitTypeImplicitFunction(function: Type.ImplicitFunction):Unit={
    lambdaCount=lambdaCount + 1
    visitChild(function)
  }
  def visitTypeLambda(lambda: Type.Lambda):Unit={
    lambdaCount = lambdaCount + 1
    visitChild(lambda)
  }
  def visitTypeMethod(method: Type.Method):Unit={
    visitChild(method)
  }
  def visitTypeName(name: Type.Name):Unit={
  }
  def visitTypeOr(or: Type.Or):Unit={
    visitChild(or)
  }
  def visitTypeParam(value: Type.Param):Unit={
    visitChild(value)
  }
  def visitTypePlaceholder(placeholder: Type.Placeholder):Unit={
    visitChild(placeholder)
  }
  def visitTypeProject(project: Type.Project):Unit={
    visitChild(project)
  }
  def visitTypeRef(ref: Type.Ref):Unit={
    visitChild(ref)
  }
  def visitTypeRefine(refine: Type.Refine):Unit={
    visitChild(refine)
  }
  def visitTypeRepeated(repeated: Type.Repeated):Unit={
    visitChild(repeated)
  }
  def visitTypeSelect(select: Type.Select):Unit={
    visitChild(select)
  }
  def visitTypeSingleton(singleton: Type.Singleton):Unit={
    visitChild(singleton)
  }
  def visitTypeTuple(tuple: Type.Tuple):Unit={
    visitChild(tuple)
  }
  def visitTypeVar(value: Type.Var):Unit={
    visitChild(value)
  }
  def visitTypeWith(value: Type.With):Unit={
    visitChild(value)
  }
  def visitTemplate(template: Template):Unit={
    visitChild(template)
  }
  def visitInit(init: Init):Unit={
    visitChild(init)
  }
  def visitSelf(self: Self):Unit={
    visitChild(self)
  }
  def visitDefnMacro(value: Defn.Macro):Unit={
    visitChild(value)
  }
  def visitDefnTrait(value: Defn.Trait):Unit={
    visitChild(value)
  }
  def visitDefnVal(value: Defn.Val):Unit={
      visitChild(value)
  }
  def visitPat(pat: Pat):Unit={
    pat match {
      case y:Pat.Alternative=>visitPatAlternative(y)
      case y:Pat.Bind=>visitPatBind(y)
      case y:Pat.Extract=>visitPatExtract(y)
      case y:Pat.ExtractInfix=>visitPatExtractInfix(y)
      case y:Pat.Interpolate=>visitPatInterpolate(y)
      case y:Pat.SeqWildcard=>visitPatSeqWildcard(y)
      case y:Pat.Tuple=>visitPatTuple(y)
      case y:Pat.Typed=>visitPatTyped(y)
      case y:Pat.Var=>visitPatVar(y)
      case y:Pat.Wildcard=>visitPatWildcard(y)
      case y:Pat.Xml=>visitPatXml(y)
      case _=>println("visitPat "+pat.toString())
    }
  }
  def visitPatAlternative(alternative: Pat.Alternative):Unit={
    visitChild(alternative)
  }
  def visitPatBind(bind: Pat.Bind):Unit={
    visitChild(bind)
  }
  def visitPatExtract(extract: Pat.Extract):Unit={
    visitChild(extract)
  }
  def visitPatExtractInfix(infix: Pat.ExtractInfix):Unit={
    visitChild(infix)
  }
  def visitPatInterpolate(interpolate: Pat.Interpolate):Unit={
    visitChild(interpolate)
  }
  def visitPatSeqWildcard(wildcard: Pat.SeqWildcard):Unit={
    visitChild(wildcard)
  }
  def visitPatTuple(tuple: Pat.Tuple):Unit={
    visitChild(tuple)
  }
  def visitPatTyped(typed: Pat.Typed):Unit={
    visitChild(typed)
  }
  def visitPatVar(value: Pat.Var):Unit={
    visitChild(value)
  }
  def visitPatWildcard(wildcard: Pat.Wildcard):Unit={
    visitChild(wildcard)
  }
  def visitPatXml(xml: Pat.Xml):Unit={
    visitChild(xml)
  }
  def visitModAbstract(value: Mod.Abstract):Unit={
    visitChild(value)
  }
  def visitModAnnot(annot: Mod.Annot):Unit={
    visitChild(annot)
  }
  def visitModContravariant(contravariant: Mod.Contravariant):Unit={
    visitChild(contravariant)
  }
  def visitModCovariant(covariant: Mod.Covariant):Unit={
    visitChild(covariant)
  }
  def visitModFinal(value: Mod.Final):Unit={
    visitChild(value)
  }
  def visitModImplicit(value: Mod.Implicit):Unit={
    visitChild(value)
  }
  def visitModInline(inline: Mod.Inline):Unit={
    visitChild(inline)
  }
  def visitModLazy(value: Mod.Lazy):Unit={
    visitChild(value)
  }
  def visitModOverride(value: Mod.Override):Unit={
    visitChild(value)
  }
  def visitModPrivate(value: Mod.Private):Unit={
    visitChild(value)
  }
  def visitName(name: Name):Unit={
    name match {
      case y:Name.Anonymous=>visitNameAnonymous(y)
      case y:Name.Indeterminate=>visitNameIndeterminate(y)
      case _=>println("visitName"+name.toString())
    }
  }
  def visitNameAnonymous(anonymous: Name.Anonymous):Unit={
  }
  def visitNameIndeterminate(indeterminate: Name.Indeterminate):Unit={
  }
  def visitModProtect(value: Mod.Protected):Unit={
    visitChild(value)
  }
  def visitModSealed(value: Mod.Sealed):Unit={
    visitChild(value)
  }
  def visitModValParam(param: Mod.ValParam):Unit={
    visitChild(param)
  }
  def visitModVarParam(param: Mod.VarParam):Unit={
    visitChild(param)
  }
  def visitDefnClass(clazz: Defn.Class):Unit={
    visitChild(clazz)
  }
  def visitDefnObject(value: Defn.Object):Unit= {
    visitChild(value)
  }
  def visitImport(value: Import):Unit={

  }
  def visitSource(tree:Source):Unit={
    visitChild(tree)
  }
  def visitDef(defn: Defn):Unit={
    defn match {
      case y: Defn.Object=>visitDefnObject(y)
      case y:Defn.Val=>visitDefnVal(y)
      case y:Defn.Def=>if(!hasFind){visitDefnDef(y)}
      case y:Defn.Class=>visitDefnClass(y)
      case y:Defn.Macro=>visitDefnMacro(y)
      case y:Defn.Trait=>visitDefnTrait(y)
      case y:Defn.Type=>visitDefnType(y)
      case y:Defn.Var=>visitDefnVar(y)
    }
  }
  def visitCase(value: Case):Unit={
    visitChild(value)
  }
  def visitDecl(decl: Decl):Unit={
    decl match {
      case y:Decl.Def=>visitDeclDef(y)
      case y:Decl.Type=>visitDeclType(y)
      case y:Decl.Val=>visitDeclVal(y)
      case y:Decl.Var=>visitDeclVar(y)
      case _=>println("visitDecl")
    }
  }
  def visitDeclDef(value: Decl.Def):Unit={
    if(value.pos.startLine==start_line){
      hasFind=true
      var source_code = value.toString().split("\n")(0)
      val psql=connection.prepareStatement("update "+ tableName1+" set source_code= ?, lineNum2 = ?, cyc2 = ? , full_code = ? where id =?")
      psql.setString(1,source_code)
      psql.setInt(2,1)
      psql.setInt(3,1)
      psql.setString(4,value.toString())
      psql.setInt(5,line_id)
      psql.executeUpdate()
      hasFound=true
      hasFind=false
    }else{
      visitChild(value)
    }
  }
  def visitDeclType(value: Decl.Type):Unit={
    visitChild(value)
  }
  def visitDeclVar(value: Decl.Var):Unit={
    visitChild(value)
  }
  def visitDeclVal(value: Decl.Val):Unit={
    visitChild(value)
  }
  def visitPkg(pkg: Pkg):Unit={
    visitChild(pkg)
  }
  def visitPkgObject(pkg: Pkg.Object):Unit={
    visitChild(pkg)
  }

  /**
    * The total recursive function,
    * and the order can not be changed
    * @param tree
    */
  def visitChild(tree:Tree):Unit={
    var nodes=tree.children
    for(a <- 0 to nodes.size-1){
      nodes(a) match {
        case y:Source=>visitSource(y)
        case y:Import=>visitImport(y)
        case y:Ctor=>visitCtor(y)
        case y:Template=>visitTemplate(y)
        case y:Init=>visitInit(y)
        case y:Self=>visitSelf(y)
        case y:Defn=>visitDef(y)
        case y:Mod=>visitMod(y)
        case y:Lit=>visitLit(y)
        case y:Enumerator=>visitEnumerator(y)
        case y:Type=>visitType(y)
        case y:Term=>visitTerm(y)
        case y:Pat=>visitPat(y)
        case y:Case=>visitCase(y)
        case y:Type.Param=>visitTypeParam(y)
        case y:Term.Param=>visitTermParam(y)
        case y:Type.Bounds=>visitTypeBounds(y)
        case y:Name=>visitName(y)
        case y:Decl=>visitDecl(y)
        case y:Pkg.Object=>visitPkgObject(y)
        case y:Pkg=>visitPkg(y)
        case _=>println("wrong of children  "+nodes(a).toString())
      }
    }
  }

  /**
    * the entrance of the AST tree
    * @param tree
    */
  def visitParent(tree:Tree):Unit={
    tree match {
      case y:Source=>visitSource(y)
      case y:Import=>visitImport(y)
      case y:Ctor=>visitCtor(y)
      case y:Template=>visitTemplate(y)
      case y:Init=>visitInit(y)
      case y:Self=>visitSelf(y)
      case y:Defn=>visitDef(y)
      case y:Mod=>visitMod(y)
      case y:Lit=>visitLit(y)
      case y:Enumerator=>visitEnumerator(y)
      case y:Type=>visitType(y)
      case y:Term=>visitTerm(y)
      case y:Pat=>visitPat(y)
      case y:Case=>visitCase(y)
      case y:Type.Param=>visitTypeParam(y)
      case y:Term.Param=>visitTermParam(y)
      case y:Type.Bounds=>visitTypeBounds(y)
      case y:Name=>visitName(y)
      case y:Decl=>visitDecl(y)
      case y:Pkg.Object=>visitPkgObject(y)
      case y:Pkg=>visitPkg(y)
      case _=>println("wrong of parent  "+tree.toString())
    }
  }

  def parse(filename: String):Unit= {
    if (line_num.length() >= 1) {
      val sort = new SortFile()
      sort.sortFile(filename)
      val t = new java.io.File(filename).parse[Source].get
      val temStr = line_num.split("\\.\\.")
      val temStr1 = temStr(0).split(":")
      val temStr2 = temStr(1).split(":")
      start_line = Integer.valueOf(temStr1(0).split("\\[")(1))
      start_column = Integer.valueOf(temStr1(1))
      end_line = Integer.valueOf(temStr2(0))
      end_column = Integer.valueOf(temStr2(1).split("\\)")(0))
      hasFind = false
      hasFound=false
      visitParent(t)
      if(!hasFound){
        println("----------")
        println(file_path)
        println(line_num)
        println("----------")
      }
    }
  }
}
