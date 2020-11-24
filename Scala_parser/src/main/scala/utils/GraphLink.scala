package utils
class GraphLink {

  var first:Node=null
  var last:Node=null

  def isEmpty():Boolean={
    return first==null
  }

  def printNode():Int={
    var sum=0
    var current=first
    while(current!=null){
      sum=sum+1
      current=current.next
    }
    sum
  }

  def insert(x:Int):Unit={
    val newNode= new Node(x)
    if(this.isEmpty()){
      first=newNode
      last=newNode
    }else{
      last.next=newNode
      last=newNode
    }

  }

}
