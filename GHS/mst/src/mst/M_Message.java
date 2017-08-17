package mst;

public class M_Message {
	int src;
	int dest;
	int i;
	Operation op;
	String s;
	nState n_s;
	
	public M_Message(int src,int dest,Operation op,int i,String s,nState n)
	{
		this.src = src;
		this.dest = dest;
		this.i = i;
		this.op = op;
		this.s = s;
		this.n_s = n;
	}
	
	public void printMsg()
	{
		System.out.println("Source"+this.src+"Dest"+this.dest+"op"+op);
	}

}
