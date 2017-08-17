from random import expovariate,randint,uniform
from math import ceil
#import pdb
import node
import block
import treeNode
import Transaction
#pdb.set_trace()

def printTree(nid,longestChain):
	parent  = longestChain
	tNode = {}
	while(len(parent) > 0 and parent[0] != -1):
		#print "parent: " +str(parent)
		tempParent = []
		for b in parent:
			if b != 0:
				b1 = nodelist[nid].blockChain[b]
				if b in tNode:
					if b1.prevblkid in tNode:
						if b1.blkid not in tNode[b1.prevblkid].childList:
							tNode[b1.prevblkid].childList.append(b1.blkid)
					else:
						t = treeNode.treeNode(b1.prevblkid)
						tNode.update({b1.prevblkid:t})
						if b1.blkid not in tNode[b1.prevblkid].childList:
							tNode[b1.prevblkid].childList.append(b1.blkid)
				else:
					t1 = treeNode.treeNode(b1.blkid)
					tNode.update({b1.blkid:t1})
					if b1.prevblkid in tNode:
						if b1.blkid not in tNode[b1.prevblkid].childList:
							tNode[b1.prevblkid].childList.append(b1.blkid)
					else:
						t = treeNode.treeNode(b1.prevblkid)
						tNode.update({b1.prevblkid:t})
						if b1.blkid not in tNode[b1.prevblkid].childList:
							tNode[b1.prevblkid].childList.append(b1.blkid)
				if b1.prevblkid not in tempParent:
					tempParent.append(b1.prevblkid)
		#pdb.set_trace()
		parent = tempParent

	for key in tNode:
		print str(key) + " " + " childList: " +str(tNode[key].childList)
		# tIds = []	
		# for t in nodelist[nid].blockChain[key].txns_in_blk:
		# 	tIds.append(t.tid)
		# print tIds

def printTreeNoFork(nid,blockChain):
	tNode = {}
	for b in blockChain:
		if blockChain[b].prevblkid in tNode:
			tNode[blockChain[b].prevblkid].childList.append(blockChain[b].blkid)
		else:
			tNode.update({blockChain[b].prevblkid:treeNode.treeNode(blockChain[b].prevblkid)})
			tNode[blockChain[b].prevblkid].childList.append(blockChain[b].blkid)
		if blockChain[b].blkid not in tNode:
			tNode.update({blockChain[b].blkid:treeNode.treeNode(blockChain[b].blkid)})
	for key in tNode:
		print str(key) + " " + " childList: " +str(tNode[key].childList)



n = raw_input('Number of peers: ')
z = raw_input('Enter z: ')
while(int(z)>100):
	z = raw_input('Enter z(<=100): ')

lambda1 = raw_input('Mean of inter-arrival time: ') # 0.5 to 0.7
lambda2 = raw_input('Mean of CPU power: ') # 0.5 to 0.7

#node1 = node.node(1)
#print Node.getNid(node1)
#print node1.nid
#node1.setBal(4);
#print node1.getBal();

nodelist = []

num_of_slow_nodes = int((float(z)/100)*int(n))
genesis_blk = block.Block(-1,-1,0,0)

for i in range(0,int(n)):
	if(i < num_of_slow_nodes):
		state = 0  # slow
	else:
		state = 1 # fast

	iatime = ceil(expovariate(1/float(lambda1)))
	mean_of_cpu_power = ceil(expovariate(1/float(lambda2)))
	#print str(i) + " " + str(iatime) + " " + str(mean_of_cpu_power) + "\n"
	
	tnode = node.node(i,state,iatime,genesis_blk,mean_of_cpu_power)
	nodelist.append(tnode)


for i in range(0,int(n)):
	num_of_peers = randint(1,int(n)-1)
	peerlist_blk = {}
	peerlist_txn = {}

	while(len(peerlist_blk) < num_of_peers):
		c = 0
		nextpeer = randint(0,int(n)-1)
		if(nextpeer == i):
			continue
		elif(nextpeer in peerlist_txn):
			continue
		else:
			prop = uniform(0.01,0.5)
			if(nodelist[i].state == 0):
				c = 5
			elif(nodelist[nextpeer].state == 0):
				c = 5
			else:
				c = 100
			par = 96/float(c*1000) # * 1000
			
			d = expovariate(1/par)
			lat_blk = ceil(prop + (8/float(c)) + d)
			lat_txn = ceil(prop + d)
			peerlist_blk.update({nextpeer:lat_blk})
			peerlist_txn.update({nextpeer:lat_txn})
			
	nodelist[i].setpeerlists(peerlist_blk,peerlist_txn)

# print nodelist[2].peerlist_txn
# nodelist[2].generate_txn(nodelist,2)
# print nodelist[3].peerlist_txn
# nodelist[3].generate_txn(nodelist,3)


n_s = raw_input('Duration of Simulation: ')
for k in range(0,int(n_s)):
	print "\n\n\nTimeStamp " + str(k)
	for peer in nodelist:
		print "\n\nnode: " + str(peer.nid)
		peer.generate_txn(k,nodelist)
		peer.process_txn(k,nodelist)
		peer.generate_blk(k,nodelist)
		peer.process_blk(k,nodelist)
		print "levelOrder"
		print str(peer.levelOrder)

		print "Blockchain "
		printTree(peer.nid,peer.pblk_longest_chain) 
		print "Blockchain without resolving fork"
		printTreeNoFork(peer.nid,peer.blockChain)
		# print "Txn list"
		# for k1 in peer.txn_recv_q:
		# 	for l in peer.txn_recv_q[k1]:
		# 		#pdb.set_trace()
		# 		print str(k1) + " " + str(l[1].tid)
		# print "Block list"
		# for k2 in peer.blk_recv_q:
		# 	for l in peer.blk_recv_q[k2]:
		# 		#pdb.set_trace()
		# 		print str(k2) + " " + str(l[1].blkid)
				

	#print "\n"
'''
loop of timestamp
loop of  nodes
 if generate txn
 receive q 
 if generate blk
 receive q
 '''

# print "Txn list"
# for k1 in peer.txn_recv_q:
# 	for l in peer.txn_recv_q[k1]:
# 		#pdb.set_trace()
# 		print str(k1) + " " + str(l[1].tid)
# print "Block list"
# for k2 in peer.blk_recv_q:
# 	for l in peer.blk_recv_q[k2]:
# 		#pdb.set_trace()
# 		print str(k2) + " " + str(l[1].blkid)


