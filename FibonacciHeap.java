/**
 * FibonacciHeap
 *
 * An implementation of Fibonacci heap over positive integers.
 *
 */
public class FibonacciHeap
{
	public HeapNode min;
	private int size;
	private int numOfTrees;
	private int c;
	private int totalCuts;
	private int totalLinks;
	
	/**
	 *
	 * Constructor to initialize an empty heap.
	 * pre: c >= 2.
	 *
	 */
	public FibonacciHeap(int c)
	{
		this.c = c;
		this.min = null;
		this.totalLinks = 0;
		this.totalCuts = 0;
		this.size = 0;
		this.numOfTrees = 0;
	}

	/**
	 * 
	 * pre: key > 0
	 *
	 * Insert (key,info) into the heap and return the newly generated HeapNode.
	 *
	 */
	public HeapNode insert(int key, String info) 
	{
		HeapNode node = createNewNode(key,info);

		if (size == 0){ //initialize new heap
			min = node;
		}

		else { //add into existing
			insertInNodeList(node);
			if (min.key > key){
				min = node;
			}
		}
		size++;

		return node;
	}

	private void insertInNodeList(HeapNode node){
		HeapNode tempNode = min.prev;
		tempNode.next = node;
		node.prev= tempNode;
		min.prev = node;
		node.next = min;

		numOfTrees++;
	}

	private HeapNode createNewNode(int key, String info){
		HeapNode node = new HeapNode();
		node.key = key;
		node.info = info;
		node.rank = 0;
		node.next = node;
		node.prev = node;
		node.sonsCut = 0;
		return node;
	}

	/**
	 * 
	 * Return the minimal HeapNode, null if empty.
	 *
	 */
	public HeapNode findMin()
	{
		return min;
	}

	/**
	 * 
	 * Delete the minimal item.
	 * Return the number of links.
	 *
	 */
	public int deleteMin()
	{
		return 46; // should be replaced by student code

	}

	/**
	 * 
	 * pre: 0<diff<x.key
	 * 
	 * Decrease the key of x by diff and fix the heap.
	 * Return the number of cuts.
	 * 
	 */
	public int decreaseKey(HeapNode x, int diff) 
	{    
		x.key -= diff;
		if (isHeapOrderViolated(x)){
			return cutNode(x);
		}
		return 0;
	}


	private int cutNode(HeapNode node){
		HeapNode parent = node.parent;
		if (parent.child == node){
			if (node.next != node){
				parent.child = node.next;
			}
			else{
				parent.child = null;
			}
		}
		node.parent = null;
		removeFromList(node);
		insertInNodeList(node);
		parent.sonsCut++;
		totalCuts++;

		if (parent.sonsCut == c){
			return 1 + cutNode(parent);
		}
		return 1;

	}

	private void removeFromList(HeapNode node)
	{
		HeapNode next = node.next;
		HeapNode prev = node.prev;

		next.prev = node.prev; //change next and prev
		prev.next = node.next;

		node.prev = node; //node points to itself now
		node.next = node;

	}

	private boolean isHeapOrderViolated(HeapNode node)
	{
		if (node.parent == null){return false;}
		HeapNode parent = node.parent;
		return parent.key > node.key;
	}
	/**
	 * 
	 * Delete the x from the heap.
	 * Return the number of links.
	 *
	 */
	public int delete(HeapNode x) 
	{    
		return 46; // should be replaced by student code
	}


	/**
	 * 
	 * Return the total number of links.
	 * 
	 */
	public int totalLinks()
	{
		return totalLinks;
	}


	/**
	 * 
	 * Return the total number of cuts.
	 * 
	 */
	public int totalCuts()
	{
		return totalCuts;
	}


	/**
	 * 
	 * Meld the heap with heap2
	 *
	 */
	public void meld(FibonacciHeap heap2) {
		HeapNode tempMin = this.min;
		if (heap2.min.key<this.min.key){
			tempMin = heap2.min;
		}
		HeapNode temp1 = this.min.next;
		HeapNode temp2 = heap2.min.prev;

		this.min.next = temp2;
		temp2.prev = this.min;

		heap2.min.next = temp1;
		temp1.prev = heap2.min.next;

		this.min = tempMin;
	}

	/**
	 * 
	 * Return the number of elements in the heap
	 *   
	 */
	public int size()
	{
		return size;
	}


	/**
	 * 
	 * Return the number of trees in the heap.
	 * 
	 */
	public int numTrees()
	{
		return numOfTrees;
	}

	/**
	 * Class implementing a node in a Fibonacci Heap.
	 *  
	 */
	public static class HeapNode{
		public int key;
		public String info;
		public HeapNode child;
		public HeapNode next;
		public HeapNode prev;
		public HeapNode parent;
		public int rank;
		public int sonsCut;
	}
}
