#import pdb
#pdb.set_trace()
txn_id = 0

class Transaction:
	# def __init__():
	# 	self.tid = -1
	# 	self.cval = -1
	# 	self.from_node = -1
	# 	self.to_node = -1
	# 	self.listed = False

	def __init__(self,cval,from_node,to_node):
		global txn_id
		txn_id += 1
		self.tid = txn_id
		self.cval = cval
		self.from_node = from_node
		self.to_node = to_node
		self.listed = 0

	def getListed():
		return self.listed

	def setListed(flag):
		self.listed = flag