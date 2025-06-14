import javax.swing.*;
import java.util.ArrayList;

/**
 * FibonacciHeap
 *
 * An implementation of Fibonacci heap over positive integers.
 *
 */
public class FibonacciHeap {
	public HeapNode min;
	private int size;
	private int numOfTrees;
	private final int c;           // cascading cut threshold
	private int totalCuts;
	private int totalLinks;

	/**
	 *
	 * Constructor to initialize an empty heap.
	 * pre: c >= 2.
	 *
	 */
	public FibonacciHeap(int c) {
		this.c = c;
		this.min = null;
		this.size = 0;
		this.numOfTrees = 0;
		this.totalCuts = 0;
		this.totalLinks = 0;
	}
	/**
	 *
	 * pre: key > 0
	 *
	 * Insert (key,info) into the heap and return the newly generated HeapNode.
	 *
	 */
	public HeapNode insert(int key, String info) {
		if (key <= 0) throw new IllegalArgumentException("key must be positive");
		HeapNode node = createNewNode(key, info);
		insertInTreeList(node);
		size++;
		return node;
	}

	private HeapNode createNewNode(int key, String info) {
		HeapNode node = new HeapNode();
		node.key = key;
		node.info = info;
		node.rank = 0;
		node.sonsCut = 0;
		node.parent = null;
		node.child = null;
		node.next = node;
		node.prev = node;
		return node;
	}

	/**
	 *
	 * Return the minimal HeapNode, null if empty.
	 *
	 */
	public HeapNode findMin() {
		return min;
	}

	/**
	 *
	 * Delete the minimal item.
	 * Return the number of links.
	 *
	 */
	public int deleteMin() {
		if (min == null) return 0;
		int oldLinks = totalLinks;
		HeapNode z = min;
		// Add children of min to root list
		if (z.child != null) {
			HeapNode child = z.child;
			HeapNode start = child;
			do {
				HeapNode nextChild = child.next;
				child.parent = null;
				child.sonsCut = 0;
				// Insert child into root list
				insertInTreeList(child);
				child = nextChild;
			} while (child != start);
		}
		// Remove z from root list
		if (z.next == z) {
			min = null;
		} else {
			HeapNode nextRoot = z.next;
			removeFromList(z);
			min = nextRoot;
		}
		size--;
		numOfTrees--;
		if (min != null) {
			consolidate();
		}
		return totalLinks - oldLinks;
	}

	private void consolidate() {
		if (min == null) return;
		int maxRank = (int)(Math.floor(Math.log(size) / Math.log((1 + Math.sqrt(5)) / 2))) + 1;
		ArrayList<HeapNode> buckets = new ArrayList<>(maxRank + 1);
		for (int i = 0; i <= maxRank; i++) buckets.add(null);

		// gather current roots
		ArrayList<HeapNode> roots = new ArrayList<>();
		HeapNode curr = min;
		do {
			roots.add(curr);
			curr = curr.next;
		} while (curr != min);
		numOfTrees = 0;

		// link trees of same rank
		for (HeapNode x : roots) {
			int r = x.rank;
			while (buckets.get(r) != null) {
				HeapNode y = buckets.get(r);
				if (x.key > y.key) { HeapNode tmp = x; x = y; y = tmp; }
				link(x, y);
				buckets.set(r, null);
				r = x.rank;
			}
			buckets.set(r, x);
		}

		// rebuild root list and find new min
		min = null;
		for (HeapNode node : buckets) {
			if (node != null) {
				node.next = node.prev = node;
				insertInTreeList(node);
			}
		}
	}

	private HeapNode link(HeapNode x, HeapNode y) {
		// assume x.key <= y.key
		totalLinks++;
		removeFromList(y);
		addToChildList(x, y);
		return x;
	}

	private void addToChildList(HeapNode parent, HeapNode child) {
		child.parent = parent;
		child.next = child.prev = child;
		if (parent.child == null) {
			parent.child = child;
		} else {
			// Insert child into the child list
			insertToList(parent.child, child);
			// Ensure parent.child always points to the child with the largest rank
			if (child.rank > parent.child.rank) {
				parent.child = child;
			}
		}
		parent.rank++;
	}

	private void insertInTreeList(HeapNode node) {
		if (min == null) {
			min = node;
		} else {
			node.next = min;
			node.prev = min.prev;
			min.prev.next = node;
			min.prev = node;
			if (node.key < min.key) min = node;
		}
		numOfTrees++;
	}

	private void insertToList(HeapNode head, HeapNode node) {
		node.next = head.next;
		head.next.prev = node;
		head.next = node;
		node.prev = head;
	}

	/**
	 *
	 * pre: 0<diff<x.key
	 *
	 * Decrease the key of x by diff and fix the heap.
	 * Return the number of cuts.
	 *
	 */
	public int decreaseKey(HeapNode x, int diff) {
		if (x == null) throw new IllegalArgumentException();
		// We rely on pre-condition: 0<diff<x.key
		int cutsBefore = totalCuts;
		x.key -= diff;
		if (isHeapOrderViolated(x)) {
			cutNode(x);
		} else if (x.key < min.key) {
			min = x;
		}
		return totalCuts - cutsBefore;
	}

	private void cutNode(HeapNode x) {
		HeapNode p = x.parent;
		if (p.child == x) {
			if (x.next != x) p.child = x.next;
			else p.child = null;
		}
		removeFromList(x);
		insertInTreeList(x);
		x.parent = null;
		x.sonsCut = 0;
		p.rank--;
		totalCuts++;
		if (++p.sonsCut == c) {
			p.sonsCut = 0;
			cutNode(p);
		}
	}

	private void removeFromList(HeapNode node) {
		if (node.next != node) {
			node.prev.next = node.next;
			node.next.prev = node.prev;
		}
		node.next = node.prev = node;
	}

	private boolean isHeapOrderViolated(HeapNode x) {
		return x.parent != null && x.key < x.parent.key;
	}

	/**
	 *
	 * Delete the x from the heap.
	 * Return the number of links.
	 *
	 */
	public int delete(HeapNode x) {
		decreaseKey(x, x.key);
		return deleteMin();
	}

	/**
	 *
	 * Return the total number of links.
	 *
	 */
	public int totalLinks() { return totalLinks; }
	/**
	 *
	 * Return the total number of cuts.
	 *
	 */
	public int totalCuts()  { return totalCuts; }
	/**
	 *
	 * Return the number of elements in the heap
	 *
	 */
	public int size()       { return size; }
	/**
	 *
	 * Return the number of trees in the heap.
	 *
	 */
	public int numTrees()   { return numOfTrees; }

	/**
	 *
	 * Meld the heap with heap2
	 *
	 */
	public void meld(FibonacciHeap heap2) {
		if (heap2 == null || heap2.min == null) return;
		if (min == null) {
			// adopt heap2 entirely
			min = heap2.min;
			size = heap2.size;
			numOfTrees = heap2.numOfTrees;
			totalLinks = heap2.totalLinks;
			totalCuts = heap2.totalCuts;
		}
		else {
			HeapNode r1 = min.next;
			HeapNode l2 = heap2.min.prev;
			min.next = heap2.min;
			heap2.min.prev = min;
			r1.prev = l2;
			l2.next = r1;
			if (heap2.min.key < min.key) min = heap2.min;
			size += heap2.size;
			numOfTrees += heap2.numOfTrees;
			totalLinks += heap2.totalLinks;
			totalCuts += heap2.totalCuts;
		}
	}


	public void visualize() {
		SwingUtilities.invokeLater(() -> new FibonacciHeapVisualizer(this).setVisible(true));
	}
	/**
	 * Class implementing a node in a Fibonacci Heap.
	 *
	 */
	public static class HeapNode {
		public int key;
		public String info;
		public HeapNode child, next, prev, parent;
		public int rank;
		public int sonsCut;
	}
}
