import javax.swing.*;
import java.util.ArrayList;

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
			insertInTreeList(node);
		}
		size++;

		return node;
	}

	private void insertInTreeList(HeapNode node){
		HeapNode tempNode = min.prev;
		tempNode.next = node;
		node.prev= tempNode;
		min.prev = node;
		node.next = min;
		if (min.key > node.key){
			min = node;
		}
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
		int changes = totalLinks;
		splitMin();
		consolidate();
		printTreeList();
		return totalLinks - changes;

	}


	private void splitMin(){ //split min tree to its sub trees and add to Tree list
		numOfTrees--;
		size--;

		HeapNode x = min;
		min = x.next;
		removeFromList(x);
		int rank = x.rank;
		x = x.child;
		HeapNode y;
		for ( int i = 0 ; i < rank ; i++){
			numOfTrees++;
			y = x.next;
			cutNode(x);
			x = y;
		}

	}

	private void consolidate(){
		min = fromBuckets(toBuckets());
	}

	private HeapNode fromBuckets(ArrayList<HeapNode> buckets){
		HeapNode x = null;
		numOfTrees = 0;
		for (int i = 0; i < buckets.size(); i++){
			if(buckets.get(i) != null){
				if (x == null){
					x = buckets.get(i);
				}
				else{
					insertToList(x,buckets.get(i));
					numOfTrees++;
					if (buckets.get(i).key < x.key) {x = buckets.get(i);}
				}
			}
		}
		return x;
	}

	private ArrayList<HeapNode> toBuckets() {
		int initialCapacity = (int)(Math.log(size) / Math.log((1 + Math.sqrt(5)) / 2));
		ArrayList<HeapNode> buckets = new ArrayList<>(initialCapacity);

		// Fill with nulls up to initialCapacity to avoid IndexOutOfBounds
		for (int i = 0; i < initialCapacity; i++) {
			buckets.add(null);
		}
		HeapNode x = min;
		while (x != null && x.next != x) {
			HeapNode y = x;
			x = x.next;
			removeFromList(y);

			// Ensure capacity for y.rank, and double if needed
			while (y.rank >= buckets.size()) {
				int newSize = buckets.size() * 2;
				while (buckets.size() < newSize) {
					buckets.add(null);
				}
			}

			while (buckets.get(y.rank) != null) {
				y = link(y, buckets.get(y.rank));
				buckets.set(y.rank - 1, null);

				// Ensure capacity again after linking (rank increased)
				while (y.rank >= buckets.size()) {
					int newSize = buckets.size() * 2;
					while (buckets.size() < newSize) {
						buckets.add(null);
					}
				}
			}

			buckets.set(y.rank, y);
		}
		return buckets;
	}

	private HeapNode link(HeapNode node1, HeapNode node2){
		totalLinks++;
		HeapNode tempMin = node1;
		HeapNode tempMax = node2;
		if (node1.key > node2.key){
			tempMin = node2;
			tempMax = node1;
		}
		addToChildList(tempMin,tempMax);
		return tempMin;
	}

	private void addToChildList(HeapNode parent, HeapNode child){
		parent.rank += 1;
		if (parent.child == null){
			parent.child = child;
			child.parent = parent;
		}
		else{
			HeapNode tempChild = parent.child;
			insertToList(tempChild,child);
		}
	}

	private void insertToList(HeapNode head , HeapNode node)
	{
		node.next = head.next;
		node.prev = head;
		head.next = node;
		node.next.prev = node;

		node.parent = head.parent;
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
		if (isHeapOrderViolated(x)) {
			return cutNode(x);
		}
		else{
			if (x.parent == null){
				if (min.key > x.key){
					min = x;
				}
			}
		}
		return 0;
	}


	private int cutNode(HeapNode node){
		HeapNode parent = node.parent;
		node.sonsCut = 0;
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
		insertInTreeList(node);

		parent.sonsCut++;
		parent.rank -= 1;
		totalCuts++;
		if (parent.sonsCut == c){
			parent.sonsCut = 0;
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
		decreaseKey(x,x.key+1);
		return deleteMin();
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
	public void printTreeList(){
		HeapNode temp = this.min;
		HeapNode end = this.min.prev;
		String str = "" + temp.key + " --> ";
		temp = temp.next;
		while (temp != end){
			str = str + temp.key + " --> ";
			if (temp.next == end){
				str = str + temp.next.key;
			}
			temp = temp.next;
		}
		System.out.println(str);
	}
	public void visualize() {
		SwingUtilities.invokeLater(() -> {
			new FibonacciHeapVisualizer(this).setVisible(true);
		});
	}

}
