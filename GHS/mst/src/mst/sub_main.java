package mst;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class sub_main {
	private static int count = 0;
	public static int nh = 0;
	private static int [][] adjacencyMat;
	public static HashMap<Integer,node> nodeList = new HashMap<Integer,node>();
	public static HashMap<Integer,Integer> edgeList = new HashMap<Integer,Integer>();
	public static Set<Integer> set = new HashSet<Integer>();
	public static Set<Integer> eSet = new HashSet<Integer>();
	public static boolean print = false;
	public static void print_data() throws IOException
	{
		System.out.println("Final output");
		TreeSet sortedSet = new TreeSet<Integer>(eSet);
		System.out.println(sortedSet);
		String line = null;
		File out = new File("/home/shrutig/git/mst/output.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(out));
        
        File inp = new File("/home/shrutig/git/mst/input.txt");
	    RandomAccessFile br = new RandomAccessFile(inp,"r");
	    
	    Iterator it = sortedSet.iterator();
	    
	    while(it.hasNext())
	    {
	    	int e = (int)(it.next());
	    	while ((line = br.readLine()) != null)
	        {
	            String[] fields = line.split(":");
	            
	            int weight = Integer.parseInt(fields[1]);
	            if(e == weight)
	            {
	            	bw.write(line+"\n");
	            }    
	            
	        }
	    	br.seek(0);
	    }
	    br.close();
	    bw.close();
		
		
	}
	private static void add_node(int id)
	{
		System.out.println("Adding node");
		//Random rand = new Random();
		int peer_count = 0;
		for(int i=0;i<count;i++)
		{
			if(adjacencyMat[id][i] != -1)
			{
				peer_count++;
			}
				
		}
		int[] peerList = new int[peer_count];
		int[] weightList = new int[peer_count];
		int j = 0;
		for(int i=0;i<count;i++)
		{
			if(adjacencyMat[id][i] != -1)
			{
				peerList[j] = i;
				weightList[j] = adjacencyMat[id][i];
				j++;
			}
				
		}
				
		node n = new node(id,peerList,weightList);
		nodeList.put(id,n);
		new Thread(n).start();
		
	}
	
	public static void main(String[] args) throws IOException
	{
		 File inp = new File("/home/shrutig/git/mst/input.txt");
	     RandomAccessFile br = new RandomAccessFile(inp,"r");

	    String line = null;
		int nId1,nId2,weight,n_node;
		
	    while ((line = br.readLine()) != null)
        {
            String[] fields = line.split(":");
            String[] nodes = fields[0].split(",");
            nId1 = Integer.parseInt(nodes[0]);
            nId2 = Integer.parseInt(nodes[1]);
            
            set.add(nId1);
            set.add(nId2);
            
        }
	    
	    n_node = set.size();
	    
	    
		
		adjacencyMat = new int[n_node][n_node];
		count = n_node;
		nh = count;
		System.out.println("Initializing Matrix");
		for(int i=0;i<n_node;i++)
		{
			for(int j=0;j<n_node;j++)
			{
				adjacencyMat[i][j] = -1;
			}
		}
		System.out.println("Getting Edges");
		nh++;
		br.seek(0);
		
		
		while ((line = br.readLine()) != null)
        {
            String[] fields = line.split(":");
            String[] nodes = fields[0].split(",");
            nId1 = Integer.parseInt(nodes[0]);
            nId2 = Integer.parseInt(nodes[1]);

            weight = Integer.parseInt(fields[1]);
            if (nId1 < 0 || nId2 < 0 || weight <=0 || weight > Integer.MAX_VALUE)
            {
            	System.out.println(" node Id or weight is out of range");
            	System.exit(0);
            }
            adjacencyMat[nId1][nId2] = weight; // to be verified check with input file
            adjacencyMat[nId2][nId1] = weight;
            System.out.println(line+ "edge added");
            edgeList.put(weight,nh);
			nh++;
        }
		br.close();

//		for(int i=0;i<n_edge;i++)
//		{
//			System.out.println("i th:"+i);
//			Random rand = new Random();
//			int nodeA = rand.nextInt(n_node) + 0;
//			int nodeB = rand.nextInt(n_node) + 0;
//			if(nodeA == nodeB)
//			{
//				i--;
//				continue;
//			}
//			else if (adjacencyMat[nodeA][nodeB] != -1)
//			{
//				i--;
//				continue;
//			}
//			else
//			{
//				int weight = -1;
//				int flag = 1;
//				
//				while(flag == 1)
//				{
//					weight = rand.nextInt(n_node*5) + 1;
//					flag = 0;
//					for(int t=0;t<count;t++)
//					{
//						for(int s=0;s<count;s++)
//						{
//							if(weight == adjacencyMat[t][s])
//							{
//								flag = 1;
//								break;
//							}
//						}
//						if(flag == 1)
//							break;
//					}
//					
//				}
//				
//				//code to remove any isolated node
//				
//				adjacencyMat[nodeA][nodeB] = weight;
//				adjacencyMat[nodeB][nodeA] = weight;
//				edgeList.put(weight,nh);
//				nh++;
//			}
//					
//		}
		
		//code to check isolated node
//		adjacencyMat[0][0] = -1;
//		adjacencyMat[1][1] = -1;
//		adjacencyMat[2][2] = -1;
//		adjacencyMat[3][3] = -1;
//		adjacencyMat[4][4] = -1;
//		adjacencyMat[5][5] = -1;
//		adjacencyMat[0][1] = 5;
//		adjacencyMat[0][2] = 8;
//		adjacencyMat[0][3] = 12;
//		adjacencyMat[0][4] = 18;
//		adjacencyMat[0][5] = -1;
//		adjacencyMat[1][0] = 5;
//		adjacencyMat[1][2] = 10;
//		adjacencyMat[1][3] = -1;
//		adjacencyMat[1][4] = 3;
//		adjacencyMat[1][5] = 24;
//		adjacencyMat[2][0] = 8;
//		adjacencyMat[2][1] = 10;
//		adjacencyMat[2][3] = 7;
//		adjacencyMat[2][4] = 30;
//		adjacencyMat[2][5] = 14;
//		adjacencyMat[3][0] = 12;
//		adjacencyMat[3][1] = -1;
//		adjacencyMat[3][2] = 7;
//		adjacencyMat[3][4] = 28;
//		adjacencyMat[3][5] = -1;
//		adjacencyMat[4][0] = 18;
//		adjacencyMat[4][1] = 3;
//		adjacencyMat[4][2] = 30;
//		adjacencyMat[4][3] = 28;
//		adjacencyMat[4][5] = 6;
//		adjacencyMat[5][0] = -1;
//		adjacencyMat[5][1] = 24;
//		adjacencyMat[5][2] = 14;
//		adjacencyMat[5][3] = -1;
//		adjacencyMat[5][4] = 6;
//		
//		edgeList.put(5,7);
//		edgeList.put(8,8);
//		edgeList.put(12,9);
//		edgeList.put(18,10);
//		edgeList.put(24,11);
//		edgeList.put(10,12);
//		edgeList.put(3,13);
//		edgeList.put(28,14);
//		edgeList.put(7,15);
//		edgeList.put(30,16);
//		edgeList.put(14,17);
//		edgeList.put(6,18);

		
		
		for(int i=0;i<n_node;i++)
		{
			for(int j=0;j<n_node;j++)
			{
				System.out.print(" "+adjacencyMat[i][j]);
			}
			System.out.print("\n");
		}
		
		
		
		for(int i=0;i<n_node;i++)
		{
			add_node(i);
		}
		

		
	}
	


}
