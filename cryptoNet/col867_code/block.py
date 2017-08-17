#import pdb
#pdb.set_trace()
blk_id = -1

class Block:
	def __init__(self,pbid,nid,ts,level):
		global blk_id
		blk_id  += 1
		self.blkid = blk_id
		self.prevblkid = pbid
		self.txns_in_blk = []
		self.creator_nid = nid
		self.ts = ts
		self.level = level
		self.res = 0 #0 - non permanent 1 - permanent