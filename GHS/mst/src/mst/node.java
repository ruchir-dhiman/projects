package mst;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class node implements Runnable{
	private int id;
	private int[] peerList;
	private int[] weightList;
	private eStatus[] status;
	private nState state;
	private String name;
	BlockingQueue<M_Message> msg = new ArrayBlockingQueue<>(50);
	int level;
	int parent;
	int rec;
	int bestNode;
	int bestWt;
	int testNode;
	boolean stop;
	private HashMap<Integer,Integer> peerIndex = new HashMap<Integer,Integer>();
	
	
 public node(int id,int[] peerList,int[] weightList)//constructor
 {
	 this.id = id;
	 this.peerList = peerList;
	 this.weightList = weightList;
	 this.status = new eStatus[this.peerList.length];
	 this.stop = false;
	 for(int i=0;i<this.status.length;i++)
	 {
		 this.status[i] = eStatus.BASIC;
	 }
	 this.level = -1;
	 this.parent = -1;
	 this.name = Integer.toString(this.id);
	 this.state = nState.FIND;
	 
	 for(int i=0;i< this.peerList.length;i++)
	 {
		 this.peerIndex.put(this.peerList[i],i);
	 }
	 
	 this.bestNode = -1;
	 this.testNode = -1;
	 this.bestWt = Integer.MAX_VALUE;
 }
 
 public void printNode()//print node details
 {
	 System.out.println("\n\nNode: "+this.id +"\n");
	 System.out.println("id:"+this.id+" level:"+this.level+" state:"+this.state+" name:"+this.name+" parent:"+this.parent+" bn:"+this.bestNode +" bw:"+this.bestWt +" rec:"+this.rec);
	 System.out.println("PeerList:\n" + this.id);
	 for (int i=0;i < this.peerList.length;i++)
	 {
		 System.out.println("Id: "+this.id+" peer: "+this.peerList[i]+" status: "+this.status[i]);
	 }
 }
 
 
 private int getMinWt(node p)//get minimum weight edge still in BASIC state
 {
	 System.out.println("getminWt "+p.id);
	 int min  = Integer.MAX_VALUE;
	 int min_index = -1;
	 for(int i=0;i<p.peerList.length;i++)
	 {
		 if (min > p.weightList[i] && p.status[i] == eStatus.BASIC)
		 {
			 min_index = p.peerList[i];
			 min  = p.weightList[i];
		 }
	 }
	 System.out.println("getminWt "+p.id + "min_index" + min_index);
	 return min_index;
 }

 private void test(node p,int q,int l,String n) throws InterruptedException //to check if edge is eligible for BRANCH or REJECT
 {
	 System.out.println("test "+p.id +"from :"+q+"level :"+l+"name: "+n);
	 if(l>p.level)
	 {
		 //wait
		 System.out.println("Test Wait Node: "+ p.id);
		 M_Message m = new M_Message(q,p.id,Operation.TEST,l,n,null);
		 p.msg.add(m);
		 //Thread.sleep(1000);
		 System.out.println("adding Test on Node" + p.id);
		 //test(p,q,l,n);
		 
	 }
	 else if(n.equals(p.name))
	 {
		 System.out.println("test else if "+p.id);
		 if(p.status[p.peerIndex.get(q)] == eStatus.BASIC)
		 {
			 System.out.println("rejecting "+q +"on  node "+p.id);
			 p.status[p.peerIndex.get(q)] = eStatus.REJECT;
		 }
		 if(p.testNode != q)
		 {
			 System.out.println("Sending reject to "+q+" from "+p.id);
			 M_Message m = new M_Message(p.id,q,Operation.REJECT,0,null,null);
			 sub_main.nodeList.get(q).msg.add(m);
			 
			 //reject(sub_main.nodeList.get(q),p.id);
		 }
		 else
		 {
			 findMin(p);
		 }
	 }
	 else
	 {
		 System.out.println("tsending accept to"+q+" from "+p.id);
		 M_Message m = new M_Message(p.id,q,Operation.ACCEPT,0,null,null);
		 sub_main.nodeList.get(q).msg.add(m);
		 //accept(sub_main.nodeList.get(q),p.id);
	 }
	 
	 System.out.println("test exit "+p.id);
 }
 
 private void accept(node p,int q)//node is eligible for BRANCH
 {
	 System.out.println("accept "+p.id +" from"+q );
	 p.testNode = -1;
	 if(p.weightList[p.peerIndex.get(q)] < p.bestWt )
	 {
		 p.bestNode = q;
		 p.bestWt = p.weightList[p.peerIndex.get(q)];
	 }
	 reportMethod(p);
 }
 private void reject(node p,int q)//node has to be set to REJECT
 {
	 System.out.println("reject"+p.id +" from "+q);
	 if(p.status[p.peerIndex.get(q)] == eStatus.BASIC)
	 {
		 p.status[p.peerIndex.get(q)] = eStatus.REJECT;
	 }
	 findMin(p);
	 
 }
 
 private void reportMethod(node p)//manage data from childs
 {
	 System.out.println("report Method "+p.id);
	 int n = 0;
	 for(int i=0;i<p.peerList.length;i++)
	 {
		 if(p.status[i] == eStatus.BRANCH && p.parent != p.peerList[i])
		 {
			 n++;
		 }
	 }
	 if( p.rec == n && p.testNode == -1)
	 {
		 p.state = nState.FOUND;
		 System.out.println("Sending report to"+p.parent +" from "+p.id);
		 M_Message m = new M_Message(p.id,p.parent,Operation.REPORT,p.bestWt,null,null);
		 sub_main.nodeList.get(p.parent).msg.add(m);
		 //report(sub_main.nodeList.get(p.parent),p.id,p.bestWt);
	 }
	 System.out.println("report method exit "+p.id);
 }
 
 private void changeRoot(node p)//change root towards core node
 {
	 System.out.println("changeroot "+p.id);
	 if( p.status[p.peerIndex.get(p.bestNode)] == eStatus.BRANCH)
	 {
		 System.out.println("Sending changeRoot to "+p.bestNode+" from "+p.id);
		 M_Message m = new M_Message(p.id,p.bestNode,Operation.CHANGEROOT,0,null,null);
		 sub_main.nodeList.get(p.bestNode).msg.add(m);
		 //changeRoot(sub_main.nodeList.get(p.bestNode));
	 }
	 else
	 {
		 p.status[p.peerIndex.get(p.bestNode)] = eStatus.BRANCH;
		 sub_main.eSet.add(p.weightList[p.peerIndex.get(p.bestNode)]);
		 System.out.println("Sending connect to "+p.bestNode+" from "+p.id);
		 M_Message m = new M_Message(p.id,p.bestNode,Operation.CONNECT,p.level,null,null);
		 sub_main.nodeList.get(p.bestNode).msg.add(m);
		 //connect(sub_main.nodeList.get(p.bestNode),p.id,p.level);
	 }
	 System.out.println("changeroot exit "+p.id);
 }
 private void report(node p,int q, int wt) throws InterruptedException, IOException//handle report message
 {
	 System.out.println("report "+p.id);
	 if(q != p.parent)
	 {
		 if( wt < p.bestWt)
		 {
			 p.bestWt = wt;
			 p.bestNode = q;
		 }
		 p.rec = p.rec + 1;
		 reportMethod(p);
	 }
	 else
	 {
		 if(p.state == nState.FIND)
		 {
			 //wait
			 System.out.println("Report Wait Node: "+ p.id);
			 //Thread.sleep(1000);
			 M_Message m = new M_Message(q,p.id,Operation.REPORT,wt,null,null);
			 p.msg.add(m);
			 System.out.println("adding Report on node" + p.id);
			 //report(p,q,wt);
			 
		 }
		 else if(wt > p.bestWt)
		 {
			 changeRoot(p);
		 }
		 else if(wt == p.bestWt && p.bestWt == Integer.MAX_VALUE)
		 {
			 //stop
			 System.out.println("Stop Node:"+p.id);
			 this.stop = true;
			 if(sub_main.print == false)
			 {
				 sub_main.print = true;
				 sub_main.print_data();
			 }
		 }
	 }
	 System.out.println("report exit "+p.id);
 }
 private void findMin(node p)//get next min node
 {
	 System.out.println("find_min "+p.id);
	 int min_index = getMinWt(p);
	 if(min_index != -1)
	 {
		 p.testNode = min_index;
		 M_Message m = new M_Message(p.id,min_index,Operation.TEST,p.level,p.name,null);
		 sub_main.nodeList.get(min_index).msg.add(m);
		 //test(sub_main.nodeList.get(min_index),p.id,p.level,p.name);
	 }
	 else
	 {
		 p.testNode = -1;
		 reportMethod(p);
	 }
	 System.out.println("find_min exit "+p.id);
 }
 
 private void initiate(node p,int q,int l,String n,nState s) throws InterruptedException//reply of connect
 {
	 System.out.println("initiate "+p.id + " level:"+l+" name:"+n);
	 p.level = l;
	 p.name = n;
	 p.state = s;
	 
	 p.parent = q;
	 
	 p.bestNode = -1;
	 p.bestWt = Integer.MAX_VALUE;
	 p.testNode = -1;
	 
	 for(int i =0;i<p.peerList.length;i++)
	 {
		 if(p.status[i] == eStatus.BRANCH && p.peerList[i] != q)
		 {
			 M_Message m = new M_Message(p.id,p.peerList[i],Operation.INITIATE,l,n,s);
			 sub_main.nodeList.get(p.peerList[i]).msg.add(m);
			 //initiate(sub_main.nodeList.get(p.peerList[i]),p.id,l,n,s);
			 
			 
		 }
	 }
	 
	 if(p.state == nState.FIND)
	 {
		 p.rec = 0;
		 findMin(p);
	 }
	 System.out.println("initiate exit "+p.id);
 }
 
 private void connect(node p,int q,int l) throws InterruptedException//connect two different fragments
 {
	 System.out.println("connect "+p.id);
	 if(l < p.level)
	 {
		 p.status[p.peerIndex.get(q)] = eStatus.BRANCH;
		 sub_main.eSet.add(p.weightList[p.peerIndex.get(q)]);
		 M_Message m = new M_Message(p.id,q,Operation.INITIATE,p.level,p.name,p.state);
		 sub_main.nodeList.get(q).msg.add(m);
		 //initiate(sub_main.nodeList.get(q),p.id,p.level,p.name,p.state);
	 }
	 else if (p.status[p.peerIndex.get(q)] == eStatus.BASIC)
	 {
		 //wait
		 System.out.println("Connect Wait Node: "+ p.id);
		 //Thread.sleep(1000);
		 M_Message m = new M_Message(q,p.id,Operation.CONNECT,l,null,null);
		 p.msg.add(m);
		 System.out.println("Adding connect on "+p.id);
		 //connect(p,q,l);
		 
	 }
	 else
	 {
		 //sub_main.nh++;
		 String tempName = sub_main.edgeList.get(p.weightList[p.peerIndex.get(q)]).toString();
		 M_Message m = new M_Message(p.id,q,Operation.INITIATE,(p.level+1),(tempName),nState.FIND);
		 sub_main.nodeList.get(q).msg.add(m);
		 //initiate(sub_main.nodeList.get(q),p.id,(p.level+1),(p.name+tempName),nState.FIND);
	 }
	 System.out.println("connect exit "+p.id);
 }
 
 private void initialization(node p)//first function called by all nodes
 {
	 System.out.println("initialization "+p.id);
	int min_index = getMinWt(p);	 
	 if(min_index == -1)
	 {
		 System.out.println(" ***** No min edge *****");
	 }
	 else
	 {
		 p.level = 0;
		 p.state = nState.FOUND;
		 p.rec = 0;
		 p.status[p.peerIndex.get(min_index)] = eStatus.BRANCH;
		 sub_main.eSet.add(p.weightList[p.peerIndex.get(min_index)]);
		 node t = sub_main.nodeList.get(min_index);
		 //connect(t,p.id,0);
		 M_Message m = new M_Message(p.id,t.id,Operation.CONNECT,0,null,null);
		 t.msg.add(m);
	 }
	 System.out.println("initialization exit "+p.id);
 }

 private void processMessage() throws InterruptedException, IOException
 {
	 M_Message m = msg.poll(60L,TimeUnit.SECONDS);
	 if(m==null)
 		return;
	 switch(m.op)
	 {
	 	case CONNECT:
	 		System.out.println("connect from"+m.src);
	 		connect(this,m.src,m.i);
	 		System.out.println("After connect"+this.id);
	 		//printNode();
	 		break;
	 	case TEST:
	 		System.out.println("test from"+m.src);
	 		test(this,m.src,m.i,m.s);
	 		System.out.println("After test"+this.id);
	 		//printNode();
	 		break;
	 	case REPORT:
	 		System.out.println("report from"+m.src);
	 		report(this,m.src,m.i);
	 		System.out.println("After report"+this.id);
	 		//printNode();
	 		break;
	 	case INITIATE:
	 		System.out.println("initiate from"+m.src);
	 		initiate(this,m.src,m.i,m.s,m.n_s);
	 		System.out.println("After initiate"+this.id);
	 		//printNode();
	 		break;
	 	case ACCEPT:
	 		System.out.println("accept from"+m.src);
	 		accept(this,m.src);
	 		System.out.println("After accept"+this.id);
	 		//printNode();
	 		break;
	 	case REJECT:
	 		System.out.println("reject from"+m.src);
	 		reject(this,m.src);
	 		System.out.println("After reject"+this.id);
	 		//printNode();
	 		break;
	 	case CHANGEROOT:
	 		System.out.println("changeroot from"+m.src);
	 		changeRoot(this);
	 		System.out.println("After changeroot"+this.id);
	 		//printNode();
	 		break;
	 
	 }
	 
 }
 @Override
 public void run() 
 {
	// TODO Auto-generated method stub
	//printNode();
	try {
		Thread.sleep(2000);
	} catch (InterruptedException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	initialization(this);
	
	while(!(sub_main.print || this.stop))
	{
		try {
			processMessage();
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(sub_main.print);
	}
	
	System.out.println("node" + this.id + "stopped");
 }


}
