1. Run the following command:
	python main.py

2. Inputs Required:
	Number of peers: 10 		(Number of nodes in the system)
	Enter z: 20 			(Percentage of slow nodes in system)
	Mean of inter-arrival time: 2 	(Mean of inter-arrival time between txns of nodes)
	Mean of CPU power: 10 		(Mean of CPU Power of nodes)
	Duration of Simulation: 30 	(Number of Simulations)
	(All inputs are assumed to be integers)

3. Output Interpretations:
	At every timestamp,for each node
	a. levelOrder: level order traversal of tree formed after resolving forks.
	b. Blochchain: list of block ids and their children block ids.
	c. Blockchain without resolving fork: list of block ids and their children block ids in tree without 		   resolving fork.


