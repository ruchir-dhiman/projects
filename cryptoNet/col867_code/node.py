from random import randint,expovariate
from math import ceil
import pdb
import Transaction
import block

class node:
	def __init__(self,nid,state,iatime,genesis_blk,mean_cpu_power):
		self.nid = nid
		self.state = state
		self.iatime = iatime
		self.mean_cpu_power = mean_cpu_power
		self.bal = 50
		self.txns_heard = []
		self.blockChain = {}
		self.blockChain.update({0:genesis_blk})
		self.pblk_longest_chain = []
		self.pblk_longest_chain.append(0)
		self.length_longest_chain = (genesis_blk.blkid)
		self.txn_recv_q = {}
		self.levelOrder = {}
		self.levelOrder.update({0:[]})
		self.levelOrder[0].append(genesis_blk.blkid)
		#self.txn_send_q = {}
		self.blk_recv_q = {}
		#self.blk_send_q = {}
		self.peerlist_txn = {}
		self.peerlist_blk = {}
		self.prev_time_txn = 0
		self.prev_time_blk = 0
		self.probTxn = {}
		self.seenTxn = []
		self.discarded = []
		self.confTxn = []

	def setpeerlists(self,peerlist_blk,peerlist_txn):
		self.peerlist_blk = peerlist_blk
		self.peerlist_txn = peerlist_txn

	def generate_txn(self,ts,nodelist):
		if(ts == (self.prev_time_txn + self.iatime)):
			self.prev_time_txn = ts
			to = randint(0,len(nodelist)-1)
			if self.bal >0:
				btc = randint(0,self.bal)
				self.bal = self.bal - btc
				txn = Transaction.Transaction(btc,self.nid,to)
				for key in self.peerlist_txn:
					t = ts + self.peerlist_txn[key]
					if(t in nodelist[key].txn_recv_q):
						nodelist[key].txn_recv_q[t].append((self.nid,txn))
					else:
						nodelist[key].txn_recv_q.update({t:list()})
						nodelist[key].txn_recv_q[t].append((self.nid,txn))
		
		
	def process_txn(self,ts,nodelist):
		if(ts in self.txn_recv_q):
			#print "node: " + str(self.nid) + " Processing Txn"
			for txn in  self.txn_recv_q[ts]:
				if(txn[1].tid in self.seenTxn):
					continue
				else:
					
					self.txns_heard.append(txn[1])
					self.seenTxn.append(txn[1].tid)
					#if(txn[1].to_node == self.nid):
						#self.bal = self.bal + txn[1].cval
					for p in self.peerlist_txn:
						if(p == txn[0]):
							continue
						else:
							t = ts + self.peerlist_txn[p]
							if(t in nodelist[p].txn_recv_q):
								nodelist[p].txn_recv_q[t].append((self.nid,txn[1]))
							else:
								nodelist[p].txn_recv_q.update({t:list()})
								nodelist[p].txn_recv_q[t].append((self.nid,txn[1]))
			del self.txn_recv_q[ts]

	def generate_blk(self,ts,nodelist):
		ts_aux = ceil(expovariate(1/float(self.mean_cpu_power))) 
		#print "node: ts_aux :" + str(ts_aux) 
		if(ts == (self.prev_time_blk + ts_aux)):
			if self.txns_heard == []:
				#print "Can't"
				self.prev_time_blk = ts
				return
			else:				
				b = block.Block(self.length_longest_chain,self.nid,ts,(self.blockChain[self.length_longest_chain].level + 1))
				#print "node: " + str(self.nid) + " Generating Block" + str(b.blkid)
				b.txns_in_blk = self.txns_heard
				#print "Block txn list: " + str(self.txns_heard)
				#print "level Order before:" + str(self.levelOrder)
				for key in self.peerlist_blk:
					t = ts + self.peerlist_blk[key]
					if(t in nodelist[key].blk_recv_q):
						nodelist[key].blk_recv_q[t].append((self.nid,b))
					else:
						nodelist[key].blk_recv_q.update({t:list()})
						nodelist[key].blk_recv_q[t].append((self.nid,b))
				
				self.txns_heard = []
				self.length_longest_chain = (b.blkid)
				f=0
				for key in self.levelOrder:
					#pdb.set_trace()
					f=0
					if b.prevblkid in self.levelOrder[key]:
						
						if (key+1) in self.levelOrder:
							self.levelOrder[key+1].append(b.blkid)
						else:
							self.levelOrder.update({(key+1):[]})
							self.levelOrder[key+1].append(b.blkid)
						break
					else:
						f=1
				if f==0:
					if b.prevblkid in self.pblk_longest_chain:
						self.pblk_longest_chain.remove(b.prevblkid)
						#print "del parent"
					self.pblk_longest_chain.append(b.blkid)
					#print "Appending"
				self.prev_time_blk = ts # to be checked
				self.blockChain.update({b.blkid:b})
				#print "longest chain :" + str(self.pblk_longest_chain)

				#print "level Order after:" + str(self.levelOrder)
				#self.bal +=50

	def process_blk(self,ts,nodelist):
		if(ts in self.blk_recv_q):
			for blk in  self.blk_recv_q[ts]:
				if blk[1].blkid in self.blockChain:
					continue
				else:
					 #print "node: " + str(self.nid) + " Processing Block" + str(blk[1].blkid)
					 self.prev_time_blk = ts
					 self.blockChain.update({blk[1].blkid:blk[1]})
					 #print "level Order before:" + str(self.levelOrder)
					 if blk[1].level in self.levelOrder:
					 	if len(self.levelOrder[blk[1].level]) == 1 and self.blockChain[self.levelOrder[blk[1].level][0]].res == 1:
					 		for t in blk[1].txns_in_blk:
					 			if t.tid not in self.probTxn:
					 				self.probTxn.update({t.tid:blk[1].level})
					 			else:
					 				l = self.probTxn[t.tid]
					 				if l < blk[1].level:
					 					self.probTxn[t.tid] = blk[1].level
					 					l = blk[1].level

					 				if len(self.levelOrder[l]) == 1 and self.blockChain[self.levelOrder[l][0]].res == 1:
					 					self.discarded.append(t.tid)
					 					if self.nid == t.from_node:
					 						self.bal = self.bal + t.cval

					 		continue

					 f = 0
					 for key in self.levelOrder:
					 	f=0
						if blk[1].prevblkid in self.levelOrder[key]:
							#pdb.set_trace()
							if (key+1) in self.levelOrder:
								self.levelOrder[key+1].append(blk[1].blkid)
							else:
								self.levelOrder.update({(key+1):[]})
								self.levelOrder[key+1].append(blk[1].blkid)
							break
						else:
							f = 1
					 if blk[1].prevblkid in self.pblk_longest_chain:
					 	self.pblk_longest_chain.remove(blk[1].prevblkid)
					 	#print "procssing del parent"
					 	self.pblk_longest_chain.append(blk[1].blkid)
					 	self.length_longest_chain = (blk[1].blkid)
					 else:
					 	if f==0:
					 		self.pblk_longest_chain.append(blk[1].blkid) #not adding as parent not
					 	#else:
					 		#print "Not Adding"
					 #print "block chain: " + str(self.blockChain)
					 #print "longest chain: " + str(self.pblk_longest_chain)
					 #print "level Order after:" + str(self.levelOrder)
					 for txn in blk[1].txns_in_blk:
					 	if txn in self.txns_heard:
					 		self.txns_heard.remove(txn)
			#print "templist: " + str(tempList)
			#print "longest chain: " + str(self.pblk_longest_chain)
			
					
					 for key in self.peerlist_blk:
						t = ts + self.peerlist_blk[key]
						if(t in nodelist[key].blk_recv_q):
							nodelist[key].blk_recv_q[t].append((self.nid,blk[1]))
						else:
							nodelist[key].blk_recv_q.update({t:list()})
							nodelist[key].blk_recv_q[t].append((self.nid,blk[1])) 
			del self.blk_recv_q[ts]
			#print "Here"
			tempList = []
			for bid in self.pblk_longest_chain:
				#pdb.set_trace()
				if bid not in (self.levelOrder[max(self.levelOrder)]):
					tempList.append((bid,self.blockChain[bid].level))
			#print "templist: " + str(tempList)
			#print "longest chain: " + str(self.pblk_longest_chain)
			
			for b in tempList:
				#print "1"
				if b[1] <= (max(self.levelOrder) - 3) and 0 <= (max(self.levelOrder) - 3):
					#pdb.set_trace()
					self.pblk_longest_chain.remove(b[0])
					#print "discaarding " + str(b[0])
					self.levelOrder[self.blockChain[b[0]].level].remove(b[0])
					#tempList.remove(b)
					for t in self.blockChain[b[0]].txns_in_blk:
			 			if t.tid not in self.probTxn:
			 				self.probTxn.update({t.tid:self.blockChain[b[0]].level})
			 			else:
			 				l = self.probTxn[t.tid]
			 				if l < self.blockChain[b[0]].level:
			 					self.probTxn[t.tid] =self.blockChain[b[0]].level
			 					l = self.blockChain[b[0]].level

			 				if len(self.levelOrder[l]) == 1 and self.blockChain[self.levelOrder[l][0]].res == 1:
			 					self.discarded.append(t.tid)
			 					if self.nid == t.from_node:
			 						self.bal = self.bal + t.cval


					if len(self.levelOrder[self.blockChain[b[0]].level]) == 1:
						throw = 0
						for t in self.blockChain[b[0]].txns_in_blk:
							if t.tid in self.discarded:
								throw = 1
								break

						if self.blockChain[b[0]].creator_nid == self.nid and throw == 0:
							self.bal += 50
							self.blockChain[b[0]].res = 1
							
							for t in self.blockChain[b[0]].txns_in_blk:
								if t.tid not in self.confTxn:
									if t.tid not in self.probTxn:
										if t.to_node == self.nid:
											self.bal = self.bal + t.cval
											self.confTxn.append(t.tid)
										else:
											self.confTxn.append(t.tid)
									else:
										del self.probTxn[t.tid]
										self.confTxn.append(t.tid)
										if self.nid == t.to_node:
											self.bal = self.bal + t.cval
								else:
									continue
							if self.bal < 0:
								pdb.set_trace()
			
			i = max(self.levelOrder) - 3
			if i > 0:
				j = i + 1 
				parlist = []
				for l in self.levelOrder[j]:
					par = self.blockChain[l].prevblkid
					if par not in parlist:
						parlist.append(par)
				j = j-1
				for t in range (0,i+1):
					tempPar = []
					disdlist = list(set(self.levelOrder[j]) - set(parlist))
					self.levelOrder[j] = []
					self.levelOrder[j] = parlist
					if len(self.levelOrder[j]) == 1:
						throw = 0
						for t in self.blockChain[self.levelOrder[j][0]].txns_in_blk:
							if t.tid in self.discarded:
								throw = 1
								break
						if self.blockChain[self.levelOrder[j][0]].res == 0 and throw == 0:
							
							for t in self.blockChain[self.levelOrder[j][0]].txns_in_blk:
								if t.tid not in self.confTxn:
									if t.tid not in self.probTxn:
										if t.to_node == self.nid:
											self.bal = self.bal + t.cval
											self.confTxn.append(t.tid)
										else:
											self.confTxn.append(t.tid)
									else:
										del self.probTxn[t.tid]
										self.confTxn.append(t.tid)
										if self.nid == t.to_node:
											self.bal = self.bal + t.cval
								else:
									continue
							
							if self.bal < 0:
								pdb.set_trace()
							self.blockChain[self.levelOrder[j][0]].res = 1
					for d in disdlist:
						for t in self.blockChain[d].txns_in_blk:
							if t.tid not in self.probTxn:
				 				self.probTxn.update({t.tid:self.blockChain[d].level})
				 			else:
				 				l = self.probTxn[t.tid]
				 				if l < self.blockChain[d].level:
				 					self.probTxn[t.tid] = self.blockChain[d].level
				 					l = self.blockChain[d].level

				 				if len(self.levelOrder[l]) == 1 and self.blockChain[self.levelOrder[l][0]].res == 1:
				 					self.discarded.append(t.tid)
				 					if self.nid == t.from_node:
				 						self.bal = self.bal + t.cval
					for l in self.levelOrder[j]:
						par = self.blockChain[l].prevblkid
						if par not in tempPar:
							tempPar.append(par)
					parlist = tempPar
					j = j - 1





				lastLevel = -1
				for k in range (0,max(self.levelOrder)):
					#print "2"
					if len(self.levelOrder[i]) == 1:
						lastLevel = i
						break
					else:
						i = i-1
					
				k = lastLevel
				
				prev_par = -1
				while(k>=0):

					if len(self.levelOrder[k]) != 1:
						disdlist = list(set(self.levelOrder[k]) - set(prevpar))
						self.levelOrder[k] = []
						self.levelOrder[k].append(prev_par)
						throw = 0
						for t in self.blockChain[prev_par].txns_in_blk:
							if t.tid in self.discarded:
								throw = 1
								break
						if self.blockChain[prev_par].creator_nid == self.nid and self.blockChain[prev_par].res == 0 and throw == 0:
							self.bal += 50
							self.blockChain[prev_par].res = 1
							
							for t in self.blockChain[prev_par].txns_in_blk:
								if t.tid not in self.confTxn:
									if t.tid not in self.probTxn:
										if t.to_node == self.nid:
											self.bal = self.bal + t.cval
											self.confTxn.append(t.tid)
										else:
											self.confTxn.append(t.tid)
									else:
										del self.probTxn[t.tid]
										self.confTxn.append(t.tid)
										if self.nid == t.to_node:
											self.bal = self.bal + t.cval
								else:
									continue
							
							if self.bal < 0:
								pdb.set_trace()
						elif self.blockChain[prev_par].res == 0:
							for t in self.blockChain[prev_par].txns_in_blk:
								if t.tid in self.discarded:
									throw = 1
									break
							if throw == 0:
								self.blockChain[prev_par].res = 1
								
								for t in self.blockChain[prev_par].txns_in_blk:
									if t.tid not in self.confTxn:
										if t.tid not in self.probTxn:
											if t.to_node == self.nid:
												self.bal = self.bal + t.cval
												self.confTxn.append(t.tid)
											else:
												self.confTxn.append(t.tid)
										else:
											del self.probTxn[t.tid]
											self.confTxn.append(t.tid)
											if self.nid == t.to_node:
												self.bal = self.bal + t.cval
									else:
										continue

						for d in disdlist:
							for t in self.blockChain[d].txns_in_blk:
								if t.tid not in self.probTxn:
				 					self.probTxn.update({t.tid:self.blockChain[d].level})
				 				else:
				 					l = self.probTxn[t.tid]
				 					if l < self.blockChain[d].level:
				 						self.probTxn[t.tid] = self.blockChain[d].level
				 						l = self.blockChain[d].level

				 					if len(self.levelOrder[l]) == 1 and self.blockChain[self.levelOrder[l][0]].res == 1:
				 						self.discarded.append(t.tid)
				 						if self.nid == t.from_node:
				 							self.bal = self.bal + t.cval	
						prev_par = self.blockChain[prev_par].prevblkid
					else:
						prev_par = self.blockChain[self.levelOrder[k][0]].prevblkid
					k = k - 1

			#print "level Order:" + str(self.levelOrder)


		


